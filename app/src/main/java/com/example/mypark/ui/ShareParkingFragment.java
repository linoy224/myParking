package com.example.mypark.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mypark.R;
import com.example.mypark.db.DBHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class ShareParkingFragment extends DialogFragment {

    private GoogleMap popupMap;
    private LatLng selectedLocation;
    private TextView tvSelectedLocation;
    private TextView tvNameError;
    private EditText etParkingName;
    private ProgressBar progressBar;
    private Button btnSave, btnCancel;
    private final DBHandler dbHandler = new DBHandler();

    public interface OnParkingSavedListener {
        void onParkingSaved();
    }

    private OnParkingSavedListener listener;

    public void setOnParkingSavedListener(OnParkingSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MyPark);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_parking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        tvNameError = view.findViewById(R.id.tvNameError);
        etParkingName = view.findViewById(R.id.etParkingName);
        progressBar = view.findViewById(R.id.progressBar);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveParking());

        // Clear error when user starts typing
        etParkingName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvNameError.setVisibility(View.GONE);
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.popupMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void onMapReady(GoogleMap googleMap) {
        popupMap = googleMap;
        popupMap.getUiSettings().setZoomControlsEnabled(true);
        popupMap.getUiSettings().setZoomGesturesEnabled(true);

        // Center on user location
        if (getActivity() != null &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            popupMap.setMyLocationEnabled(true);
            FusedLocationProviderClient fused =
                    LocationServices.getFusedLocationProviderClient(getActivity());

            fused.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && popupMap != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    popupMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 16f));
                }
            });
        }

        // Tap to pick a spot
        popupMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            popupMap.clear();
            popupMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected spot"));

            tvSelectedLocation.setText(
                    String.format(Locale.US, "%.5f, %.5f", latLng.latitude, latLng.longitude));
        });
    }

    private void saveParking() {
        // Validate location
        if (selectedLocation == null) {
            Toast.makeText(getContext(), "Tap the map to pick a spot first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name — required, cannot be empty
        String name = etParkingName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            tvNameError.setText("Please give this spot a name");
            tvNameError.setVisibility(View.VISIBLE);
            etParkingName.requestFocus();
            return;
        }

        tvNameError.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        dbHandler.insertParking(name, selectedLocation.latitude, selectedLocation.longitude,
                new DBHandler.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(getContext(), "Parking saved!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onParkingSaved();
                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(getContext(), "Could not save: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
