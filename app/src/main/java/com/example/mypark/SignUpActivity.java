package com.example.mypark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * SignUpActivity
 * רושם משתמש חדש ב-FirebaseAuth עם אימייל + סיסמה,
 * מעדכן תצוגת שם (displayName), ואז מפעיל את MainActivity.
 * כולל כפתור "back to Sign in" שמחזיר ל-MainActivity.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword, etConfirm, etUsername;
    private Button btnSignup;
    private TextView btnBackToSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1) הפניה לרכיבים
        etEmail    = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirm  = findViewById(R.id.password2);
        etUsername = findViewById(R.id.username);
        btnSignup  = findViewById(R.id.signinBtn);
        btnBackToSignIn = findViewById(R.id.btnsignupP);

        auth = FirebaseAuth.getInstance();

        // 2) לוגיקת הרשמה
        btnSignup.setOnClickListener(v -> trySignUp());

        // 3) כפתור חזרה ל-Sign In
        btnBackToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // סוגר את SignUpActivity
        });
    }

    private void trySignUp() {
        String email    = safe(etEmail);
        String pass     = safe(etPassword);
        String confirm  = safe(etConfirm);
        String username = safe(etUsername);

        // בדיקות בסיס
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין"); etEmail.requestFocus(); return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("סיסמה חייבת להיות 6 תווים ומעלה"); etPassword.requestFocus(); return;
        }
        if (!pass.equals(confirm)) {
            etConfirm.setError("הסיסמאות לא תואמות"); etConfirm.requestFocus(); return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("שם משתמש נדרש"); etUsername.requestFocus(); return;
        }

        btnSignup.setEnabled(false);

        // יצירת משתמש ב-Firebase
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            btnSignup.setEnabled(true);

            if (!task.isSuccessful()) {
                String msg = task.getException() != null ? task.getException().getMessage() : "Sign up failed";
                Toast.makeText(this, "שגיאה בהרשמה: " + msg, Toast.LENGTH_LONG).show();
                Log.e("sign up", msg);
                return;
            }

            // עדכון displayName
            if (auth.getCurrentUser() != null) {
                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();

                auth.getCurrentUser().updateProfile(req).addOnCompleteListener(upt -> {
                    Log.d("sign up", "success: " + username);

                    // --- מעבר ל-MainActivity אחרי הרשמה מוצלחת ---
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish(); // סוגר את SignUpActivity
                });
            }
        });
    }

    private static String safe(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    @Override
    public void onBackPressed() {
        // לחיצה על כפתור Back של המכשיר → חזרה ל-Sign In
        super.onBackPressed();
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
