package com.example.mypark.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypark.R;
import com.example.mypark.db.DBHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment {

    private GoogleMap mMap;
    private boolean userMovedMap = false;
    private FusedLocationProviderClient fusedLocationClient;
    private final DBHandler dbHandler = new DBHandler();

    private ParkingAdapter adapter;
    private final ArrayList<Parking> parkingItems = new ArrayList<>();
    private final ArrayList<Parking> allParkings = new ArrayList<>();

    private LatLng userLocation;
    private int selectedRadiusMeters = 50;
    private boolean useRadius = false;

    private SeekBar radiusSeekBar;
    private TextView radiusText;
    private TextView tvEmptyState;
    private ToggleButton tbUseRadius;
    private MaterialCardView sliderCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        radiusText = view.findViewById(R.id.radiusText);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tbUseRadius = view.findViewById(R.id.tbUseRadius);
        sliderCard = view.findViewById(R.id.sliderCard);

        tbUseRadius.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useRadius = isChecked;
            sliderCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateRadiusLabel();
            applyRadiusFilter();
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRadiusMeters = (progress + 1) * 50;
                updateRadiusLabel();
                applyRadiusFilter();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateRadiusLabel();

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.myLocationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ParkingAdapter(parkingItems,
                // Click: zoom to parking on map
                parking -> {
                    userMovedMap = true;
                    LatLng loc = new LatLng(parking.getLat(), parking.getLng());
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions().position(loc).title(parking.getName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16f));
                    }
                },
                // Reserve: mark as reserved then open navigation
                (parking, position) -> {
                    dbHandler.reserveParking(parking.getDocumentId(), new DBHandler.OnOperationListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Spot reserved! Navigating...", Toast.LENGTH_SHORT).show();
                            loadParkingLocations();
                            openNavigation(parking.getLat(), parking.getLng());
                        }
                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Could not reserve: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );
        recyclerView.setAdapter(adapter);

        // FAB
        FloatingActionButton fab = view.findViewById(R.id.fabAddParking);
        fab.setOnClickListener(v -> {
            ShareParkingFragment dialog = new ShareParkingFragment();
            dialog.setOnParkingSavedListener(this::loadParkingLocations);
            dialog.show(getChildFragmentManager(), "shareParkingPopup");
        });

        // Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void updateRadiusLabel() {
        if (!useRadius) {
            radiusText.setText("Showing all parkings");
        } else {
            radiusText.setText("Radius: " + selectedRadiusMeters + "m");
        }
    }

    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        loadParkingLocations();

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                adapter.setUserLocation(userLocation);
                if (!userMovedMap) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                }
                applyRadiusFilter();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    private void loadParkingLocations() {
        dbHandler.loadAvailableParkings(new DBHandler.OnParkingsLoadedListener() {
            @Override
            public void onLoaded(java.util.List<Map<String, Object>> parkings) {
                allParkings.clear();
                for (Map<String, Object> data : parkings) {
                    String docId = (String) data.get("documentId");
                    String name = (String) data.get("name");
                    Double lat = (Double) data.get("lat");
                    Double lng = (Double) data.get("lng");
                    Object timeObj = data.get("time");
                    String time = (timeObj != null) ? timeObj.toString() : "";
                    Boolean reserved = (Boolean) data.get("reserved");

                    if (lat != null && lng != null) {
                        allParkings.add(new Parking(
                                docId,
                                name != null ? name : "Unnamed spot",
                                lat, lng, time,
                                reserved != null && reserved));
                    }
                }
                applyRadiusFilter();
            }

            @Override
            public void onError(String message) {
                Log.e("HomeFragment", "Failed to load parkings: " + message);
            }
        });
    }

    private void applyRadiusFilter() {
        parkingItems.clear();
        if (mMap != null) mMap.clear();

        for (Parking p : allParkings) {
            if (isWithinRadius(p)) {
                parkingItems.add(p);
                if (mMap != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(p.getLat(), p.getLng()))
                            .title(p.getName()));
                }
            }
        }
        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(parkingItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isWithinRadius(Parking parking) {
        if (!useRadius || userLocation == null) return true;
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                parking.getLat(), parking.getLng(), results);
        return results[0] <= selectedRadiusMeters;
    }

    private void openNavigation(double lat, double lng) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("mypark_prefs", Context.MODE_PRIVATE);
        String navApp = prefs.getString("nav_app", "google_maps");

        Intent intent;
        if ("waze".equals(navApp)) {
            String wazeUrl = "https://waze.com/ul?ll=" + lat + "," + lng + "&navigate=yes";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(wazeUrl));
            intent.setPackage("com.waze");
        } else {
            String mapsUrl = "google.navigation:q=" + lat + "," + lng;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
            intent.setPackage("com.google.android.apps.maps");
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Fallback: open in browser
            String fallback = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback)));
        }
    }
}
