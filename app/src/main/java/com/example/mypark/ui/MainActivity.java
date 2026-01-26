package com.example.mypark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mypark.R;
import com.example.mypark.viewmodel.SignUpViewModel;
import android.widget.ProgressBar;//להוסיף משתנה ProgressBar ב־MainActivity

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;
    private SignUpViewModel viewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.signinBtn);
        tvSignupLink = findViewById(R.id.btnsignupP);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        viewModel.getLoginError().observe(this, msg -> {
            progressBar.setVisibility(View.GONE);//להסתיר Loader במקרה של שגיאה
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            btnLogin.setEnabled(true);
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                progressBar.setVisibility(View.GONE);//להסתיר Loader במקרה של הצלחה
                startActivity(new Intent(this, HomePage.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);//להציג Loader בלחיצה על Sign In
            btnLogin.setEnabled(false);
            viewModel.login(
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim()
            );
        });

        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );
    }
}
