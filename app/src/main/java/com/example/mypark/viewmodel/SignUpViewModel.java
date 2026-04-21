package com.example.mypark.viewmodel;

// בדיקות טקסט
import android.text.TextUtils;

// לוגים לדיבוג
import android.util.Log;

// בדיקת אימייל תקין
import android.util.Patterns;

// LiveData (תקשורת עם UI)
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Firebase Authentication
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

//
// ViewModel מרכזי של התחברות והרשמה
// תפקיד:
//- לנהל לוגיקה של login + signup
// - לבצע בדיקות תקינות נתונים
// - לתקשר עם Firebase
// - לשלוח תוצאות ל-Activity באמצעות LiveData

public class SignUpViewModel extends ViewModel {

    //אובייקט Firebase Authentication אחראי על יצירת משתמשים והתחברות
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // =========================
    // LOGIN SECTION
    // =========================

    // הודעת שגיאה להתחברות
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    // הצלחת התחברות (true/false)
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    //מאפשר ל-Activity להאזין לשגיאות התחברות
    public LiveData<String> getLoginError() {
        return loginError;
    }

    //מאפשר ל-Activity לדעת אם ההתחברות הצליחה
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    //פונקציית התחברות משתמש

    public void login(String email, String password) {

        // בדיקת תקינות אימייל
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginError.setValue("נא להכניס אימייל תקין");
            return;
        }

        // בדיקת סיסמה ריקה
        if (TextUtils.isEmpty(password)) {
            loginError.setValue("נא להכניס סיסמה");
            return;
        }

        //שליחת בקשת התחברות ל-Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // התחברות הצליחה → שליחת הצלחה ל-UI
                        loginSuccess.setValue(true);

                        // לוג לבדיקה ב-Logcat
                        Log.d("VM_DEBUG",
                                "Login successful for: " + email);

                    } else {

                        // התחברות נכשלה → שליחת הודעת שגיאה
                        loginError.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "שגיאה בהתחברות"
                        );
                    }
                });
    }

    // =========================
    // SIGNUP SECTION
    // =========================

    // שגיאות בהרשמה
    private final MutableLiveData<String> signUpError = new MutableLiveData<>();

    // הצלחת הרשמה
    private final MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();

    //LiveData של שגיאות הרשמה
    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    //LiveData של הצלחת הרשמה
    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    //פונקציית הרשמה למשתמש חדש
    public void signUp(String email, String password,
                       String confirm, String username) {

        Log.d("SignupVM", "viewmodel signUp()");

        // בדיקת אימייל תקין
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpError.setValue("אימייל לא תקין");
            return;
        }

        // בדיקת סיסמה (אורך מינימלי)
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            signUpError.setValue("סיסמה חייבת להיות 6 תווים ומעלה");
            return;
        }

        // בדיקת התאמה בין סיסמאות
        if (!password.equals(confirm)) {
            signUpError.setValue("הסיסמאות לא תואמות");
            return;
        }

        // בדיקת שם משתמש
        if (TextUtils.isEmpty(username)) {
            signUpError.setValue("שם משתמש נדרש");
            return;
        }

        //יצירת משתמש חדש ב-Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        // שגיאה ביצירת משתמש
                        signUpError.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "שגיאה בהרשמה"
                        );
                        return;
                    }

                    // המשתמש נוצר בהצלחה
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {

                        // עדכון שם משתמש בפרופיל Firebase
                        UserProfileChangeRequest req =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                        user.updateProfile(req)
                                .addOnCompleteListener(upt -> {

                                    if (upt.isSuccessful()) {

                                        // הצלחה מלאה
                                        signUpSuccess.setValue(true);

                                    } else {

                                        // גם אם עדכון שם נכשל – עדיין מאפשרים כניסה
                                        Log.d("SignupVM",
                                                "Profile update failed, proceeding anyway");

                                        signUpSuccess.setValue(true);
                                    }
                                });
                    }
                });
    }

    //בדיקה אם משתמש מחובר כרגע
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}