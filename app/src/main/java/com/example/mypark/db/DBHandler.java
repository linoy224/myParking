package com.example.mypark.db;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DBHandler {

    private static final String TAG = "DBHandler";
    private static final String COLLECTION = "parkings";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnParkingsLoadedListener {
        void onLoaded(java.util.List<Map<String, Object>> parkings);
        void onError(String message);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Load all non-reserved parkings from Firestore.
     */
    public void loadAvailableParkings(OnParkingsLoadedListener listener) {
        db.collection(COLLECTION)
                .whereEqualTo("reserved", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Map<String, Object> data = new HashMap<>(doc.getData());
                            data.put("documentId", doc.getId());
                            results.add(data);
                        }
                        listener.onLoaded(results);
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "loadAvailableParkings failed: " + msg);
                        listener.onError(msg);
                    }
                });
    }

    /**
     * Insert a new parking spot into Firestore.
     */
    public void insertParking(String name, double lat, double lng, OnOperationListener listener) {
        String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("time", formattedTime);
        data.put("reserved", false);

        db.collection(COLLECTION)
                .add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Parking inserted: " + task.getResult().getId());
                        listener.onSuccess();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "insertParking failed: " + msg);
                        listener.onError(msg);
                    }
                });
    }

    /**
     * Reserve a parking spot (set reserved = true).
     */
    public void reserveParking(String documentId, OnOperationListener listener) {
        db.collection(COLLECTION).document(documentId)
                .update("reserved", true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Parking reserved: " + documentId);
                        listener.onSuccess();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "reserveParking failed: " + msg);
                        listener.onError(msg);
                    }
                });
    }

    /**
     * Unreserve a parking spot (set reserved = false).
     */
    public void unreserveParking(String documentId, OnOperationListener listener) {
        db.collection(COLLECTION).document(documentId)
                .update("reserved", false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Parking unreserved: " + documentId);
                        listener.onSuccess();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "unreserveParking failed: " + msg);
                        listener.onError(msg);
                    }
                });
    }

    /**
     * Delete a parking spot from Firestore.
     */
    public void deleteParking(String documentId, OnOperationListener listener) {
        db.collection(COLLECTION).document(documentId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Parking deleted: " + documentId);
                        listener.onSuccess();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "deleteParking failed: " + msg);
                        listener.onError(msg);
                    }
                });
    }
}
