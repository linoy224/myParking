package com.example.mypark.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.util.Log;

import com.example.mypark.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean userMovedMap = false;
    // האובייקט הזה מאפשר לאפליקציה לקבל את המיקום של המשתמש.
    private FusedLocationProviderClient fusedLocationClient;
    // ייבוא הספריות הנדרשות
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // רשימה
    private RecyclerView recyclerView;
    private ParkingAdapter adapter;
    private ArrayList<Parking> parkingItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);
        //  אתחול הרשימה
        // 1. אתחול ה-RecyclerView (במקום ה-ListView)
        recyclerView = findViewById(R.id.myLocationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// 2. הגדרת האדפטר עם הלוגיקה המדויקת של המפה מהקוד המקורי שלך
        adapter = new ParkingAdapter(parkingItems, parking -> {
            // הפעולות שקרו קודם ב-onItemClick:
            userMovedMap = true;

            // שליפת המיקום מתוך האובייקט שנבחר
            LatLng selectedLocation = new LatLng(parking.getLat(), parking.getLng());

            Log.d("CHECK", "Selected Parking: " + parking.getName());
            Log.d("CHECK", "Location: " + selectedLocation);

            if (selectedLocation != null && mMap != null) {
                // הוספת המרקר בדיוק כמו קודם
                mMap.addMarker(new MarkerOptions().position(selectedLocation).title("חניה נבחרת"));

                // הזזת המפה עם אנימציה וזום 16f (בדיוק כמו שהיה לך)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 16f));

                // הלוגים המקוריים שלך לצורך דיבאג
                Log.d("MapMove", "Moving to: " + selectedLocation.toString());
                Log.d("MapDebug", "Lat: " + selectedLocation.latitude + ", Lng: " + selectedLocation.longitude);
            }
        });

// 3. חיבור האדפטר לרשימה
        recyclerView.setAdapter(adapter);

        //מפעילים את שירות המיקום של Android
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // מאפשר להציג את כפתורי ה-+ ו- הסטנדרטיים של גוגל על המפה
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // מאפשר למשתמש להגדיל ולהרחיק באמצעות "צביטה" עם שתי אצבעות
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // מוסיף מצפן שמופיע כשמסובבים את המפה
        mMap.getUiSettings().setCompassEnabled(true);

        loadParkingLocations();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && !userMovedMap) { // ➕ לא לדרוס בחירה מהמשתמש
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
            }
        });

        // Default location (Example: Tel Aviv)
//        LatLng defaultLoc = new LatLng(32.0853, 34.7818);
//        mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Marker in Tel Aviv"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 15f));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                    }
                });
            }
        }
    }

    // הפונקציה מביאה את כל החניות ששמורות ב-Firebase ומציגה אותן על המפה
    private void loadParkingLocations() {
        db.collection("parkings") // מציין לאיזה אוסף נתונים ניגשת.
                .get() //פקודה המבקשת את הנתונים פעם אחת
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            parkingItems.clear(); // מנקה טעינות קודמות
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                Double lat = document.getDouble("lat");
                                Double lng = document.getDouble("lng");
                                // במקום getString, אנחנו מושכים את האובייקט והופכים אותו לטקסט בבטחה
                                Object timeObj = document.get("time");
                                String time = (timeObj != null) ? timeObj.toString() : "";

                                if (lat != null && lng != null) {
                                    // יצירת אובייקט חניה והוספה לרשימה
                                    Parking parking = new Parking(name != null ? name : "חניה ללא שם", lat, lng, time);
                                    parkingItems.add(parking);

                                    if (mMap != null) {
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(name));
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged(); // רענון הרשימה
                        }

                    }
                });
    }
}