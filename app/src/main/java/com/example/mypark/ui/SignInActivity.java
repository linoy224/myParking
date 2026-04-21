package com.example.mypark.ui;

// מעבר בין מסכים
import android.content.Intent;

// מצב Activity
import android.os.Bundle;

// לוגים לדיבוג
import android.util.Log;

// רכיבי UI
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

// Activity בסיסית
import androidx.appcompat.app.AppCompatActivity;

// ViewModel (ארכיטקטורת MVVM)
import androidx.lifecycle.ViewModelProvider;

// קבצי פרויקט
import com.example.mypark.R;
import com.example.mypark.viewmodel.SignUpViewModel;

//מסך התחברות (Sign In)
// אחראי על:
// - התחברות משתמש
// - מעבר למסך ראשי
// - מעבר להרשמה
// - הצגת טעינה ושגיאות

public class SignInActivity extends AppCompatActivity {

    // שדה אימייל
    private EditText etEmail;

    // שדה סיסמה
    private EditText etPassword;

    // כפתור התחברות
    private Button btnLogin;

    // קישור להרשמה
    private TextView tvSignupLink;

    // ViewModel שמנהל לוגיקה (MVVM)
    private SignUpViewModel viewModel;

    // סרגל טעינה
    private ProgressBar progressBar;

    // יצירת המסך
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //יצירת ViewModel (שכבת לוגיקה נפרדת מה-UI)
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        //בדיקה אם המשתמש כבר מחובר אם כן → מדלגים למסך הראשי
        if (viewModel.isUserLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish(); // סוגר את מסך ההתחברות
            return;
        }

        //קישור layout למסך
        setContentView(R.layout.activity_signin);

        // חיבור רכיבי UI מה-XML
        etEmail = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.signinBtn);
        tvSignupLink = findViewById(R.id.btnsignupP);
        progressBar = findViewById(R.id.progressBar);

        //מאזין לשגיאת התחברות מה-ViewModel

        viewModel.getLoginError().observe(this, msg -> {

            // הסתרת טעינה
            progressBar.setVisibility(View.GONE);

            // הצגת הודעת שגיאה
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // הפעלת כפתור מחדש
            btnLogin.setEnabled(true);
        });

        //מאזין להצלחה בהתחברות

        viewModel.getLoginSuccess().observe(this, success -> {

            if (success) {

                // הסתרת טעינה
                progressBar.setVisibility(View.GONE);

                // לוג לדיבוג
                Log.d("ACTIVITY_DEBUG",
                        "Success received! Moving to MainActivity");

                // מעבר למסך ראשי
                startActivity(new Intent(this, MainActivity.class));

                // סגירת מסך התחברות
                finish();
            }
        });

        //לחיצה על כפתור התחברות
        btnLogin.setOnClickListener(v -> {

            // הצגת טעינה
            progressBar.setVisibility(View.VISIBLE);

            // מניעת לחיצות כפולות
            btnLogin.setEnabled(false);

            // שליחת נתונים ל-ViewModel
            viewModel.login(
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim()
            );
        });

        //מעבר למסך הרשמה
        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );
    }
}