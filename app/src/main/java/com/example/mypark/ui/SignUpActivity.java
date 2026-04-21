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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Activity בסיסית
import androidx.appcompat.app.AppCompatActivity;

// ViewModel (MVVM)
import androidx.lifecycle.ViewModelProvider;

// קבצי פרויקט
import com.example.mypark.R;
import com.example.mypark.viewmodel.SignUpViewModel;

//מסך הרשמה (Sign Up)
// אחראי על:
// - יצירת משתמש חדש
// - בדיקות קלט
// - מעבר למסך ראשי
// - ניווט חזרה למסך התחברות

public class SignUpActivity extends AppCompatActivity {

    // שדות קלט
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirm;
    private EditText etUsername;

    // כפתור הרשמה
    private Button btnSignup;

    // מעבר למסך התחברות
    private TextView btnBackToSignIn;

    // ViewModel שמנהל לוגיקה
    private SignUpViewModel viewModel;

    // סרגל טעינה
    ProgressBar progressBar;

    //יצירת המסך
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //יצירת ViewModel (MVVM)
        //**MVVM (Model–View–ViewModel)** היא ארכיטקטורת פיתוח ב־ Android שמפרידה בין הנתונים
        // (Model), ממשק המשתמש (View) והלוגיקה שמתווכת ביניהם
        // (ViewModel), כדי להפוך את הקוד למסודר, ברור וקל לתחזוקה.
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // אם המשתמש כבר מחובר → מדלגים למסך ראשי

        if (viewModel.isUserLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        //קישור layout
        setContentView(R.layout.activity_signup);

        // חיבור UI
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirm = findViewById(R.id.password2);
        etUsername = findViewById(R.id.username);
        btnSignup = findViewById(R.id.signinBtn);
        btnBackToSignIn = findViewById(R.id.btnsignupP);

        progressBar = findViewById(R.id.progressBar);

        //מאזין לשגיאת הרשמה מהViewModel
        viewModel.getSignUpError().observe(this, msg -> {

            // הסתרת טעינה
            progressBar.setVisibility(View.GONE);

            // הודעת שגיאה
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // הפעלת כפתור מחדש
            btnSignup.setEnabled(true);
        });

        //מאזין להצלחה בהרשמה
        viewModel.getSignUpSuccess().observe(this, success -> {

            if (success) {

                // הסתרת טעינה
                progressBar.setVisibility(View.GONE);

                // הודעה למשתמש
                Toast.makeText(this,
                        "נרשמת בהצלחה! ברוך הבא",
                        Toast.LENGTH_SHORT).show();

                //מעבר למסך ראשי
                Intent intent = new Intent(this, MainActivity.class);

                // ניקוי היסטוריית מסכים
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                // סגירת המסך הנוכחי
                finish();
            }
        });

        //לחיצה על כפתור הרשמה
        btnSignup.setOnClickListener(v -> {

            // לוג לדיבוג
            Log.d("SignupActivty", "btnSignup clicked");

            // הצגת טעינה
            progressBar.setVisibility(View.VISIBLE);

            // מניעת לחיצות כפולות
            btnSignup.setEnabled(false);

            // שליחת נתונים ל-ViewModel
            viewModel.signUp(
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim(),
                    etConfirm.getText().toString().trim(),
                    etUsername.getText().toString().trim()
            );
        });

        //מעבר למסך התחברות

        btnBackToSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, SignInActivity.class))
        );
    }
}