package com.example.mypark.ui;

// ייבוא הרשאות מיקום
import android.Manifest;

// עבודה עם נתונים שמורים במכשיר
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

// בדיקת הרשאות
import android.content.pm.PackageManager;

// חישובי מרחקים
import android.location.Location;

// עבודה עם קישורים (URL)
import android.net.Uri;

// מחזור חיים של Activity/Fragment
import android.os.Bundle;

// הדפסות ללוג (debug)
import android.util.Log;

// בניית ה־UI
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// רכיבי UI
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

// אנוטציות
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// הרשאות
import androidx.core.app.ActivityCompat;

// Fragment
import androidx.fragment.app.Fragment;

// רשימה
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// קבצי הפרויקט
import com.example.mypark.R;
import com.example.mypark.db.DBHandler;

// שירות מיקום
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

// מפות
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// UI מתקדם
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// מבני נתונים
import java.util.ArrayList;
import java.util.Map;

// i ראשי(אחד מהטאבים): מציג מפה + רשימת חניות + סינון לפי רדיוסFragment

public class HomeFragment extends Fragment {

    private GoogleMap mMap; // אובייקט המפה
    private boolean userMovedMap = false; // האם המשתמש הזיז את המפה

    private FusedLocationProviderClient fusedLocationClient; // שירות מיקום
    private final DBHandler dbHandler = new DBHandler(); // גישה למסד

    private ParkingAdapter adapter; // אדפטר לרשימה

    private final ArrayList<Parking> parkingItems = new ArrayList<>(); // חניות שמוצגות
    private final ArrayList<Parking> allParkings = new ArrayList<>(); // כל החניות מהשרת

    private LatLng userLocation; // מיקום המשתמש

    private int selectedRadiusMeters = 50; // רדיוס התחלתי
    private boolean useRadius = false; // האם להפעיל סינון

    private SeekBar radiusSeekBar; // סרגל בחירת רדיוס
    private TextView radiusText; // טקסט שמראה רדיוס
    private TextView tvEmptyState; // טקסט כשאין חניות
    private ToggleButton tbUseRadius; // כפתור הפעלה/כיבוי
    private MaterialCardView sliderCard; // כרטיס שמכיל את הסליידר

    //יצירת ה-UI מה-XML

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // מחזיר את הקובץ fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    //אחרי שהמסך נוצר – פה עושים את כל החיבורים והלוגיקה
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // חיבור רכיבי UI מה-XML
        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        radiusText = view.findViewById(R.id.radiusText);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tbUseRadius = view.findViewById(R.id.tbUseRadius);
        sliderCard = view.findViewById(R.id.sliderCard);

        //כפתור הפעלה/כיבוי רדיוס
        tbUseRadius.setOnCheckedChangeListener((buttonView, isChecked) -> {

            useRadius = isChecked; // שמירת מצב

            // אם מופעל → מציגים את הסליידר, אחרת מסתירים
            sliderCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            updateRadiusLabel(); // עדכון טקסט
            applyRadiusFilter(); // סינון מחדש
        });

        //שינוי רדיוס דרך הסליידר
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // כל שלב = 50 מטר
                selectedRadiusMeters = (progress + 1) * 50;

                updateRadiusLabel(); // עדכון טקסט
                applyRadiusFilter(); // עדכון הרשימה
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {} // לא בשימוש
            @Override public void onStopTrackingTouch(SeekBar seekBar) {} // לא בשימוש
        });

        updateRadiusLabel(); // הצגת טקסט התחלתי

        //**RecyclerView** הוא רכיב בAndroid שמאפשר להציג רשימה של נתונים בצורה יעילה,
        // בכך שהוא ממחזר תצוגות של פריטים קיימים במקום ליצור כל פעם חדשים, וכך משפר ביצועים.

        //RecyclerView - רשימת חניות
        RecyclerView recyclerView = view.findViewById(R.id.myLocationList);
        // קובע שהרשימה תהיה אנכית
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        //יוצרת אובייקט חדש של המחלקה **ParkingAdapter** (האדפטר של ה־RecyclerView)
        // ומעבירה לו את רשימת הנתונים `parkingItems` כדי שיוכל להציג אותם ברשימה.
        adapter = new ParkingAdapter(parkingItems,

                // לחיצה על חניה
                parking -> {
                    userMovedMap = true; // המשתמש הזיז מפה

                    LatLng loc = new LatLng(parking.getLat(), parking.getLng());

                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions().position(loc).title(parking.getName()));
                        // מוסיף סימון

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16f));
                        // זום למיקום
                    }
                },

                // לחיצה על Reserve
                (parking, position) -> {

                    dbHandler.reserveParking(parking.getDocumentId(),

                            new DBHandler.OnOperationListener() {

                                @Override
                                public void onSuccess() {

                                    Toast.makeText(getContext(),
                                            "Spot reserved! Navigating...",
                                            Toast.LENGTH_SHORT).show();

                                    loadParkingLocations(); // רענון נתונים

                                    openNavigation(parking.getLat(), parking.getLng());
                                    // פתיחת ניווט
                                }

                                @Override
                                public void onError(String message) {

                                    Toast.makeText(getContext(),
                                            "Could not reserve: " + message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
        );

        recyclerView.setAdapter(adapter); // חיבור האדפטר

        //כפתור הוספת חניה (FAB)
        FloatingActionButton fab = view.findViewById(R.id.fabAddParking);

        fab.setOnClickListener(v -> {

            ShareParkingFragment dialog = new ShareParkingFragment();

            // אחרי שמוסיפים → טוען מחדש
            dialog.setOnParkingSavedListener(this::loadParkingLocations);

            dialog.show(getChildFragmentManager(), "shareParkingPopup");
        });

        //חיבור לשירות מיקום
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        //טעינת המפה
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }


     // עדכון טקסט רדיוס
    private void updateRadiusLabel() {

        if (!useRadius) {
            radiusText.setText("Showing all parkings");
        } else {
            radiusText.setText("Radius: " + selectedRadiusMeters + "m");
        }
    }

    //כשהמפה מוכנה
    private void onMapReady(GoogleMap googleMap) {

        mMap = googleMap; // שמירת המפה

        // הפעלת אפשרויות UI
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        loadParkingLocations(); // טעינת חניות

        // בדיקת הרשאות
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        enableMyLocation(); // הפעלת מיקום
    }

    //הפעלת מיקום המשתמש
    private void enableMyLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true); // מציג נקודה כחולה

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {

                    if (location != null) {

                        userLocation = new LatLng(
                                location.getLatitude(),
                                location.getLongitude());

                        //מעדכנת את האדפטר במיקום הנוכחי של המשתמש (userLocation),
                        // כדי שיוכל להשתמש בו (למשל לחישוב מרחקים או הצגת מידע רלוונטי).
                        adapter.setUserLocation(userLocation);

                        if (!userMovedMap) {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(userLocation, 15f));
                        }

                        applyRadiusFilter(); // סינון לפי מיקום
                    }
                });
    }

    //תוצאה של הרשאות
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 1
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            enableMyLocation();
        }
    }

    //טעינת חניות מהשרת
    private void loadParkingLocations() {

        dbHandler.loadAvailableParkings(new DBHandler.OnParkingsLoadedListener() {

            @Override
            public void onLoaded(java.util.List<Map<String, Object>> parkings) {

                allParkings.clear(); // ניקוי

                for (Map<String, Object> data : parkings) {

                    // שליפת נתונים
                    String docId = (String) data.get("documentId");
                    String name = (String) data.get("name");
                    Double lat = (Double) data.get("lat");
                    Double lng = (Double) data.get("lng");

                    String time = data.get("time") != null
                            ? data.get("time").toString() : "";

                    Boolean reserved = (Boolean) data.get("reserved");

                    if (lat != null && lng != null) {

                        allParkings.add(new Parking(
                                docId,
                                name != null ? name : "Unnamed spot",
                                lat,
                                lng,
                                time,
                                reserved != null && reserved
                        ));
                    }
                }

                applyRadiusFilter(); // עדכון תצוגה
            }

            @Override
            public void onError(String message) {
                Log.e("HomeFragment", "Failed to load parkings: " + message);
            }
        });
    }

    //סינון חניות
    private void applyRadiusFilter() {

        parkingItems.clear(); // ניקוי

        if (mMap != null) mMap.clear(); // ניקוי מפה

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

        adapter.notifyDataSetChanged(); // רענון UI

        // בודקת אם הרשימה `parkingItems`
        // ריקה, ואם כן מציגה את הטקסט `tvEmptyState`, ואם לא — מסתירה אותו.
        tvEmptyState.setVisibility(
                parkingItems.isEmpty() ? View.VISIBLE : View.GONE);
    }


     //בדיקת רדיוס
    private boolean isWithinRadius(Parking parking) {

        if (!useRadius || userLocation == null) return true;

        //`float[]` הוא מערך של מספרים עשרוניים (מסוג float) ב־Java
        // , שמאפשר לשמור כמה ערכים מאותו סוג בתוך משתנה אחד.
        float[] results = new float[1];

        Location.distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                parking.getLat(),
                parking.getLng(),
                results);

        return results[0] <= selectedRadiusMeters;
    }

    //פתיחת ניווט

    private void openNavigation(double lat, double lng) {

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("mypark_prefs", Context.MODE_PRIVATE);

        String navApp = prefs.getString("nav_app", "google_maps");

        //**Intent** הוא אובייקט ב־Android שמאפשר להעביר מידע ולבצע מעבר בין
        // מסכים (Activities) או להפעיל פעולות שונות באפליקציה.
        Intent intent;

        if ("waze".equals(navApp)) {

            String url = "https://waze.com/ul?ll=" + lat + "," + lng + "&navigate=yes";

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.waze");

        } else {

            String url = "google.navigation:q=" + lat + "," + lng;

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.google.android.apps.maps");
        }

        try {
            startActivity(intent);
        } catch (Exception e) {

            // אם אין אפליקציה → פותח בדפדפן
            String fallback = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng;

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback)));
        }
    }
}