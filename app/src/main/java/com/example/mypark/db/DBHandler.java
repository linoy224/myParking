package com.example.mypark.db;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// * מחלקה שאחראית על עבודה מול Firebase Firestore
// * מבצעת פעולות CRUD:
 //* Create - הוספה
 //* Read - קריאה
 //* Update - עדכון
 //* Delete - מחיקה*/
public class DBHandler {

    private static final String TAG = "DBHandler"; // לשימוש בלוגים
    private static final String COLLECTION = "parkings"; // שם האוסף במסד

    // יצירת חיבור למסד הנתונים
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    //interface-ממשק רק מגדיר פעולות: ריקות מתוכן
    // ממשק להחזרת רשימת חניות(רק לקריאה שלהם-Read )

    public interface OnParkingsLoadedListener {
        void onLoaded(java.util.List<Map<String, Object>> parkings); // מוחזר כשיש נתונים
        void onError(String message); // מוחזר במקרה של שגיאה
    }

    // ממשק לפעולות כלליות (הוספה, עדכון, מחיקה)/
    public interface OnOperationListener {
        void onSuccess(); // הצלחה
        void onError(String message); // שגיאה
    }

    //שליפת חניות פנויות בלבד
    public void loadAvailableParkings(OnParkingsLoadedListener listener) {

        db.collection(COLLECTION) // ניגש לאוסף "parkings"
                .whereEqualTo("reserved", false) // סינון: רק חניות לא תפוסות
                .get() // שליפת הנתונים
                .addOnCompleteListener(task -> { // פעולה אסינכרונית (לוקח זמן)

                    if (task.isSuccessful()) { // אם הפעולה הצליחה
                        // יצירת רשימה לאחסון התוצאות
                        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();

                        // מעבר על כל מסמך (חניה)
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            // שמירת הנתונים של המסמך במפה
                            Map<String, Object> data = new HashMap<>(doc.getData());

                            // הוספת ה-ID של המסמך (חשוב לעדכון/מחיקה בעתיד)
                            data.put("documentId", doc.getId());

                            // הוספת החניה לרשימה
                            results.add(data);
                        }
                        // החזרת כל החניות למי שקרא לפונקציה
                        listener.onLoaded(results);


                    } else { // אם הייתה שגיאה

                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        // אם יש שגיאה – קח את ההודעה, אחרת הודעה כללית

                        Log.e(TAG, "loadAvailableParkings failed: " + msg);
                        // הדפסת שגיאה ללוג

                        listener.onError(msg);
                        // החזרת השגיאה
                    }
                });
    }

    //הוספת חניה חדשה

    public void insertParking(String name, double lat, double lng, OnOperationListener listener) {
        // יצירת תאריך ושעה בפורמט קריא
        String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        // יצירת מבנה נתונים לחניה

        data.put("name", name); // שם החניה
        data.put("lat", lat); // קו רוחב
        data.put("lng", lng); // קו אורך
        data.put("time", formattedTime); // זמן יצירה
        data.put("reserved", false); // ברירת מחדל – לא תפוס

        db.collection(COLLECTION)
                .add(data) // הוספה למסד
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) { // הצלחה

                        String id = task.getResult().getId();
                        // קבלת ה-ID של החניה שנוצרה

                        Log.d(TAG, "Parking inserted: " + id);
                        // הדפסה ללוג

                        listener.onSuccess();
                        // החזרת הצלחה

                    } else { // שגיאה

                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";

                        Log.e(TAG, "insertParking failed: " + msg);

                        listener.onError(msg);
                        // החזרת שגיאה
                    }
                });
    }

    //סימון חניה כתפוסה
    public void reserveParking(String documentId, OnOperationListener listener) {

        db.collection(COLLECTION)
                .document(documentId) // בחירת מסמך לפי ID
                .update("reserved", true) // עדכון השדה ל-true
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "Parking reserved: " + documentId);

                        listener.onSuccess();

                    } else {

                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";

                        Log.e(TAG, "reserveParking failed: " + msg);

                        listener.onError(msg);
                    }
                });
    }

    //ביטול תפיסת חניה כרגע לא פועל ! אין קריאה

    public void unreserveParking(String documentId, OnOperationListener listener) {

        db.collection(COLLECTION)
                .document(documentId)
                .update("reserved", false) // שינוי חזרה ל-false
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "Parking unreserved: " + documentId);

                        listener.onSuccess();

                    } else {

                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";

                        Log.e(TAG, "unreserveParking failed: " + msg);

                        listener.onError(msg);
                    }
                });
    }

    // מחיקת חניה מהמסד אין קריאה עוד!
    public void deleteParking(String documentId, OnOperationListener listener) {

        db.collection(COLLECTION)
                .document(documentId) // בחירת המסמך
                .delete() // מחיקה
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "Parking deleted: " + documentId);

                        listener.onSuccess();

                    } else {

                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";

                        Log.e(TAG, "deleteParking failed: " + msg);

                        listener.onError(msg);
                    }
                });
    }
}