package com.example.mypark.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mypark.R;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Add tabs with icons and text
        tabLayout.addTab(tabLayout.newTab()
                .setText("Explore")
                .setIcon(android.R.drawable.ic_menu_compass));
        tabLayout.addTab(tabLayout.newTab()
                .setText("Settings")
                .setIcon(android.R.drawable.ic_menu_preferences));

        // Load home by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                if (tab.getPosition() == 1) {
                    fragment = new SettingsFragment();
                } else {
                    fragment = new HomeFragment();
                }
                loadFragment(fragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
