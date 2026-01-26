package com.example.mypark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirm = findViewById(R.id.password2);
        etUsername = findViewById(R.id.username);
        btnSignup = findViewById(R.id.signinBtn);
        btnBackToSignIn = findViewById(R.id.btnsignupP);

        progressBar = findViewById(R.id.progressBar);



        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        viewModel.getSignUpError().observe(this, msg -> {
            progressBar.setVisibility(View.GONE);//להסתיר Loader במקרה של שגיאה
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            btnSignup.setEnabled(true);
        });

        viewModel.getSignUpSuccess().observe(this, success -> {
            if (success) {
                progressBar.setVisibility(View.GONE);//להסתיר Loader במקרה של הצלחה
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        btnSignup.setOnClickListener(v -> {
            Log.d("SignupActivty", "btnSignup clicked");

            progressBar.setVisibility(View.VISIBLE);//להציג Loader בלחיצה על Sign up
            btnSignup.setEnabled(false);
            viewModel.signUp(
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim(),
                    etConfirm.getText().toString().trim(),
                    etUsername.getText().toString().trim()
            );
        });

        btnBackToSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class))
        );
    }
}
