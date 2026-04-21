package com.example.mypark.ui;

// עבודה עם Context (גישה למערכת), Intent (מעבר מסכים), SharedPreferences (שמירת נתונים מקומית)
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

// בניית UI של Fragment
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// רכיבי UI
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

// אנוטציות
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// קבצי הפרויקט
import com.example.mypark.R;

// Firebase Authentication – ניהול משתמשים
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

//* מסך הגדרות משתמש:
 // - הצגת פרטי משתמש
 // - שינוי שם
 // - בחירת אפליקציית ניווט
 // - Logout

public class SettingsFragment extends Fragment {

    // שדה להזנת שם המשתמש
    private EditText etDisplayName;

    // הצגת אימייל (לא ניתן לעריכה)
    private TextView tvEmail;

    // קבוצת כפתורי רדיו לבחירת אפליקציית ניווט
    private RadioGroup rgNavApp;

    private RadioButton rbGoogleMaps, rbWaze;

    //יצירת ה-View של המסך
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // מחזיר את layout של המסך
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    //אחרי שהמסך נטען – כאן עושים את כל הלוגיקה
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // חיבור רכיבי UI מה-XML
        etDisplayName = view.findViewById(R.id.etDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        rgNavApp = view.findViewById(R.id.rgNavApp);
        rbGoogleMaps = view.findViewById(R.id.rbGoogleMaps);
        rbWaze = view.findViewById(R.id.rbWaze);

        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        //קבלת המשתמש המחובר מ-Firebase

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // הצגת שם המשתמש
            etDisplayName.setText(user.getDisplayName());

            // הצגת אימייל
            tvEmail.setText(user.getEmail());
        }

        //טעינת העדפת ניווט מהזיכרון המקומי

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("mypark_prefs", Context.MODE_PRIVATE);

        String navApp = prefs.getString("nav_app", "google_maps");

        // אם שמור Waze – מסמן אותו
        if ("waze".equals(navApp)) {
            rbWaze.setChecked(true);
        } else {
            rbGoogleMaps.setChecked(true);
        }

        //שמירת בחירת אפליקציית ניווט

        rgNavApp.setOnCheckedChangeListener((group, checkedId) -> {

            // קובע איזה ערך לשמור
            String selected = (checkedId == R.id.rbWaze)
                    ? "waze"
                    : "google_maps";

            // שמירה בזיכרון
            prefs.edit().putString("nav_app", selected).apply();

            Toast.makeText(getContext(),
                    "Navigation set to " +
                            (checkedId == R.id.rbWaze ? "Waze" : "Google Maps"),
                    Toast.LENGTH_SHORT).show();
        });

        //שמירת פרופיל (עדכון שם משתמש)
        btnSaveProfile.setOnClickListener(v -> {

            String newName = etDisplayName.getText().toString().trim();

            // בדיקה שלא ריק
            if (newName.isEmpty()) {
                Toast.makeText(getContext(),
                        "Name cannot be empty",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (user != null) {

                // יצירת בקשת עדכון פרופיל
                UserProfileChangeRequest req =
                        new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();

                // עדכון ב-Firebase
                user.updateProfile(req).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Profile updated!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Could not update profile",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //Logout מהאפליקציה
        btnLogout.setOnClickListener(v -> {

            // יציאה מ-Firebase
            FirebaseAuth.getInstance().signOut();

            // מעבר למסך התחברות
            Intent intent = new Intent(requireContext(), SignInActivity.class);

            // מוחק את כל המסכים הקודמים מהזיכרון
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });
    }
}