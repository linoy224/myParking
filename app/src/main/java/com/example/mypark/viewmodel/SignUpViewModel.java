package com.example.mypark.viewmodel;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Greeting TextView
    private final MutableLiveData<String> text = new MutableLiveData<>();
    public LiveData<String> getText() { return text; }
    public void setText(String value) { text.setValue(value); }

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
                        loginSuccess.setValue(true);
                    } else {
                        loginError.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "שגיאה בהתחברות"
                        );
                    }
                });
    }

    // SignUp
    private final MutableLiveData<String> signUpError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();
    public LiveData<String> getSignUpError() { return signUpError; }
    public LiveData<Boolean> getSignUpSuccess() { return signUpSuccess; }

    public void signUp(String email, String password, String confirm, String username) {

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
                        signUpError.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "שגיאה בהרשמה");
                        return;
                    }

                    if (auth.getCurrentUser() != null) {
                        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        auth.getCurrentUser().updateProfile(req)
                                .addOnCompleteListener(upt -> signUpSuccess.setValue(true));
                    }
                });
    }

    // ✅ בדיקה אם המשתמש כבר מחובר
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null;
    }
}
