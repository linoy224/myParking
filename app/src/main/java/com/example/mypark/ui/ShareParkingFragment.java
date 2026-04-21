package com.example.mypark.ui;

// הרשאות מיקום
import android.Manifest;

// חלון קופץ (Dialog)
import android.app.Dialog;

// בדיקת הרשאות
import android.content.pm.PackageManager;

// Bundle לניהול מצב Fragment
import android.os.Bundle;

// עבודה עם טקסט ו-UI
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

// רכיבי UI
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// אנוטציות
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

// קבצים פנימיים
import com.example.mypark.R;
import com.example.mypark.db.DBHandler;

// Google Maps + Location
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// פורמט מספרים
import java.util.Locale;

//חלון קופץ להוספת חניה חדשה
//המשתמש בוחר מיקום על מפה + שם ושומר ל-Firebase

public class ShareParkingFragment extends DialogFragment {

    // מפה בתוך החלון
    private GoogleMap popupMap;

    // המיקום שהמשתמש בחר
    private LatLng selectedLocation;

    // הצגת קואורדינטות שנבחרו
    private TextView tvSelectedLocation;

    // הודעת שגיאה לשם
    private TextView tvNameError;

    // שדה להזנת שם חניה
    private EditText etParkingName;

    // סרגל טעינה בזמן שמירה
    private ProgressBar progressBar;

    // כפתורי פעולה
    private Button btnSave, btnCancel;

    // גישה למסד הנתונים
    private final DBHandler dbHandler = new DBHandler();

    //ממשק שמודיע למסך הקודם שהוספה חניה חדשה

    public interface OnParkingSavedListener {
        void onParkingSaved();
    }

    private OnParkingSavedListener listener;

    // חיבור listener מבחוץ
    public void setOnParkingSavedListener(OnParkingSavedListener listener) {
        this.listener = listener;
    }

    //הגדרת עיצוב החלון
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // מסיר כותרת ומגדיר עיצוב מותאם
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MyPark);
    }

    //יצירת הדיאלוג עצמו
    // חלון קטן שקופץ מעל המסך
    // באפליקציה (ב־Android), ומשמש להצגת הודעה, בקשת אישור מהמשתמש או קלט (למשל כפתורי אישור/ביטול).
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // הסרת כותרת
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            // רקע שקוף לחלון
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    //יצירת ה-View של החלון
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // טוען את ה-XML של החלון
        return inflater.inflate(R.layout.fragment_share_parking, container, false);
    }

    //אחרי יצירת ה-View – כאן כל הלוגיקה

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // חיבור רכיבי UI
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        tvNameError = view.findViewById(R.id.tvNameError);
        etParkingName = view.findViewById(R.id.etParkingName);
        progressBar = view.findViewById(R.id.progressBar);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        // כפתור ביטול – סוגר את החלון
        btnCancel.setOnClickListener(v -> dismiss());

        // כפתור שמירה
        btnSave.setOnClickListener(v -> saveParking());

        //הסתרת שגיאה כאשר המשתמש מתחיל להקליד
        etParkingName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvNameError.setVisibility(View.GONE);
        });

        //טעינת המפה מתוך ה-Fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.popupMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    //כאשר המפה מוכנה
    private void onMapReady(GoogleMap googleMap) {

        popupMap = googleMap;

        // אפשרויות שליטה במפה
        popupMap.getUiSettings().setZoomControlsEnabled(true);
        popupMap.getUiSettings().setZoomGesturesEnabled(true);

        //מיקום המשתמש (אם יש הרשאה)
        if (getActivity() != null &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            popupMap.setMyLocationEnabled(true);

            // קבלת מיקום אחרון
            FusedLocationProviderClient fused =
                    LocationServices.getFusedLocationProviderClient(getActivity());

            fused.getLastLocation().addOnSuccessListener(location -> {

                if (location != null && popupMap != null) {

                    LatLng userLoc = new LatLng(
                            location.getLatitude(),
                            location.getLongitude());

                    // ממרכז את המפה על המשתמש
                    popupMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(userLoc, 16f));
                }
            });
        }

        //לחיצה על המפה – בחירת חניה
        popupMap.setOnMapClickListener(latLng -> {

            selectedLocation = latLng; // שמירת מיקום

            popupMap.clear(); // מחיקת סימונים קודמים

            // הוספת סמן חדש
            popupMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected spot"));

            // הצגת קואורדינטות על המסך
            tvSelectedLocation.setText(
                    String.format(Locale.US,
                            "%.5f, %.5f",
                            latLng.latitude,
                            latLng.longitude));
        });
    }

    //שמירת החניה ב-Firebase
    private void saveParking() {

        // בדיקה שנבחר מיקום
        if (selectedLocation == null) {
            Toast.makeText(getContext(),
                    "Tap the map to pick a spot first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // קבלת שם החניה
        String name = etParkingName.getText().toString().trim();

        // בדיקה שלא ריק
        if (TextUtils.isEmpty(name)) {
            tvNameError.setText("Please give this spot a name");
            tvNameError.setVisibility(View.VISIBLE);
            etParkingName.requestFocus();
            return;
        }

        // הסתרת שגיאה והפעלת טעינה
        tvNameError.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        //שמירה ב-Firebase דרך DBHandler
        dbHandler.insertParking(
                name,
                selectedLocation.latitude,
                selectedLocation.longitude,
                new DBHandler.OnOperationListener() {

                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);

                        Toast.makeText(getContext(),
                                "Parking saved!",
                                Toast.LENGTH_SHORT).show();

                        // הודעה למסך הקודם לרענון
                        if (listener != null)
                            listener.onParkingSaved();

                        // סגירת החלון
                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);

                        Toast.makeText(getContext(),
                                "Could not save: " + message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //התאמת גודל החלון למסך
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