package com.example.mypark.ui;

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

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink, textView;
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.signinBtn);
        tvSignupLink = findViewById(R.id.btnsignupP);
        textView = findViewById(R.id.title);

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Greeting text
        viewModel.getText().observe(this, textView::setText);
        viewModel.setText("שלום! זה עובד 🎉");

        // Login observers
        viewModel.getLoginError().observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            btnLogin.setEnabled(true);
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) goToHomePage();
        });

        // Signup link
        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );

        // Login button
        btnLogin.setOnClickListener(v -> {
            btnLogin.setEnabled(false);
            viewModel.login(safe(etEmail), safe(etPassword));
        });
    }

    private void goToHomePage() {
        startActivity(new Intent(this, HomePage.class));
        finish();
    }

    private static String safe(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
