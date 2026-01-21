package com.example.mypark.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mypark.R;

public class ShareParkingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_parking);

        Toast.makeText(this, "כאן תתווסף הוספת מיקום חניה 📍", Toast.LENGTH_LONG).show();
    }
}
