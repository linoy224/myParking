package com.example.mypark.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mypark.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ShareParkingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private Button btnAddLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_parking);

        btnAddLocation = findViewById(R.id.btnAddLocation);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        btnAddLocation.setOnClickListener(v -> checkPermission());
    }

    // בדיקת הרשאה
    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );

        } else {
            getLocation();
        }
    }

    // קבלת מיקום
    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            // להוסיף חיווי למשתמש על זה שאין הרשאות
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveToFirebase(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    } else {
                        Toast.makeText(
                                this,
                                "לא ניתן לקבל מיקום",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // תוצאה של בקשת הרשאה
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation();
            }
        }
    }

    // שמירה ל־Firebase
    private void saveToFirebase(double lat, double lng) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("time", System.currentTimeMillis());

        db.collection("parkings")
                .add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ShareMyParking", "saved successfully");
                        Toast.makeText(
                                this,
                                "המיקום נשמר בהצלחה 📍",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Log.e("ShareMyParking", "error saving location: " + task.getException().getMessage());

                        Toast.makeText(
                                this,
                                "שגיאה בשמירה",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
