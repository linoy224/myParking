package com.example.mypark.viewmodel;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Login
    private final MutableLiveData<String> loginError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    public LiveData<String> getLoginError() { return loginError; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }

    public void login(String email, String password) {

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginError.setValue("נא להכניס אימייל תקין");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            loginError.setValue("נא להכניס סיסמה");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 1. תשלחי רק true. ה-Activity יטפל במעבר.
                        loginSuccess.setValue(true);

                        // 2. לוג לבדיקה - תראי ב-Logcat אם זה מודפס
                        Log.d("VM_DEBUG", "Login successful for: " + email);
                    } else {
                        // שליחת הודעת השגיאה למשתמש
                        loginError.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "שגיאה בהתחברות"
                        );
                        // Don't set loginSuccess here — the error observer handles the UI
                    }
                });
    }

    // SignUp
    private final MutableLiveData<String> signUpError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();

    public LiveData<String> getSignUpError() { return signUpError; }
    public LiveData<Boolean> getSignUpSuccess() { return signUpSuccess; }

    public void signUp(String email, String password, String confirm, String username) {
        Log.d("SignupVM", "viewmodel signUp()");

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpError.setValue("אימייל לא תקין");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            signUpError.setValue("סיסמה חייבת להיות 6 תווים ומעלה");
            return;
        }

        if (!password.equals(confirm)) {
            signUpError.setValue("הסיסמאות לא תואמות");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            signUpError.setValue("שם משתמש נדרש");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        signUpError.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "שגיאה בהרשמה"
                        );
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest req =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                        user.updateProfile(req).addOnCompleteListener(upt -> {
                            if (upt.isSuccessful()) {
                                signUpSuccess.setValue(true);
                            } else {
                                // Profile update failed, but user was created — still let them in
                                Log.d("SignupVM", "Profile update failed, proceeding anyway");
                                signUpSuccess.setValue(true);
                            }
                        });
                    }
                });
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
