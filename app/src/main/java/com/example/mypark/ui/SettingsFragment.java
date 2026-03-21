package com.example.mypark.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mypark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingsFragment extends Fragment {

    private EditText etDisplayName;
    private TextView tvEmail;
    private RadioGroup rgNavApp;
    private RadioButton rbGoogleMaps, rbWaze;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDisplayName = view.findViewById(R.id.etDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        rgNavApp = view.findViewById(R.id.rgNavApp);
        rbGoogleMaps = view.findViewById(R.id.rbGoogleMaps);
        rbWaze = view.findViewById(R.id.rbWaze);
        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Load current user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            etDisplayName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        }

        // Load saved nav preference
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("mypark_prefs", Context.MODE_PRIVATE);
        String navApp = prefs.getString("nav_app", "google_maps");
        if ("waze".equals(navApp)) {
            rbWaze.setChecked(true);
        } else {
            rbGoogleMaps.setChecked(true);
        }

        // Save nav preference on change
        rgNavApp.setOnCheckedChangeListener((group, checkedId) -> {
            String selected = (checkedId == R.id.rbWaze) ? "waze" : "google_maps";
            prefs.edit().putString("nav_app", selected).apply();
            Toast.makeText(getContext(), "Navigation set to " +
                    (checkedId == R.id.rbWaze ? "Waze" : "Google Maps"), Toast.LENGTH_SHORT).show();
        });

        // Save profile
        btnSaveProfile.setOnClickListener(v -> {
            String newName = etDisplayName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (user != null) {
                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();
                user.updateProfile(req).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Could not update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
