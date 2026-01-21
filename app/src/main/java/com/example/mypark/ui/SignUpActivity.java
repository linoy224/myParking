package com.example.mypark.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mypark.R;
import com.example.mypark.viewmodel.SignUpViewModel;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirm, etUsername;
    private Button btnSignup;
    private TextView btnBackToSignIn;
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // הפניה לרכיבים
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirm = findViewById(R.id.password2);
        etUsername = findViewById(R.id.username);
        btnSignup = findViewById(R.id.signinBtn);
        btnBackToSignIn = findViewById(R.id.btnsignupP);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Observers
        viewModel.getSignUpError().observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            btnSignup.setEnabled(true);
        });

        // ✅ אחרי הרשמה – המשתמש נשאר במסך ההתחברות
        viewModel.getSignUpSuccess().observe(this, success -> {
            if (success) goToMainActivity(); // מחזיר למסך התחברות
        });

        // כפתורי UI
        btnSignup.setOnClickListener(v -> {
            btnSignup.setEnabled(false);
            viewModel.signUp(
                    safe(etEmail),
                    safe(etPassword),
                    safe(etConfirm),
                    safe(etUsername)
            );
        });

        btnBackToSignIn.setOnClickListener(v -> goToMainActivity());
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // סוגר את SignUpActivity
    }

    private static String safe(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        goToMainActivity();
    }
}
