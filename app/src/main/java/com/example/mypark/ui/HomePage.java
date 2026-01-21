package com.example.mypark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mypark.R;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        LinearLayout btnFindParking = findViewById(R.id.btnFindParking);
        LinearLayout btnShareParking = findViewById(R.id.btnShareParking);

        // מעבר למסך Find Parking
        btnFindParking.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, FindParkingActivity.class);
            startActivity(intent);
        });

        // מעבר למסך Share Parking
        btnShareParking.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, ShareParkingActivity.class);
            startActivity(intent);
        });
    }
}
