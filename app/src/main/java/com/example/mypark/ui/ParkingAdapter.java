package com.example.mypark.ui;

// לחישוב מרחקים
import android.location.Location;

// יצירת Views מתוך XML
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// רכיבי UI
import android.widget.Button;
import android.widget.TextView;

// אנוטציות
import androidx.annotation.NonNull;

// RecyclerView – רשימות
import androidx.recyclerview.widget.RecyclerView;

// משאבים
import com.example.mypark.R;

// מיקום במפה
import com.google.android.gms.maps.model.LatLng;

// מבני נתונים
import java.util.ArrayList;
import java.util.Locale;

//Adapter של RecyclerView אחראי לחבר בין רשימת החניות לבין התצוגה (UI)
public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    // רשימת החניות להצגה
    private final ArrayList<Parking> parkingList;

    // מאזין ללחיצה על פריט
    private final OnItemClickListener clickListener;

    // מאזין ללחיצה על כפתור Reserve
    private final OnReserveClickListener reserveListener;

    // מיקום המשתמש (כדי לחשב מרחק)
    private LatLng userLocation;

    //ממשק ללחיצה על פריט
    public interface OnItemClickListener {
        void onItemClick(Parking parking); // מחזיר את החניה שנלחצה
    }

    //ממשק ללחיצה על כפתור Reserve
    public interface OnReserveClickListener {
        void onReserveClick(Parking parking, int position);
    }

    //Constructor – מקבל את הנתונים והמאזינים
    public ParkingAdapter(ArrayList<Parking> parkingList,
                          OnItemClickListener clickListener,
                          OnReserveClickListener reserveListener) {

        this.parkingList = parkingList; // שמירת הרשימה
        this.clickListener = clickListener; // שמירת מאזין ללחיצה
        this.reserveListener = reserveListener; // שמירת מאזין לריזרב
    }

    //קבלת מיקום המשתמש מבחוץ
    public void setUserLocation(LatLng location) {
        this.userLocation = location;
    }

    //יצירת ViewHolder חדש (כל פריט ברשימה)
    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // יצירת View מתוך XML של פריט
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_card, parent, false);

        // יצירת ViewHolder שמחזיק את ה-View
        return new ParkingViewHolder(view);
    }

    //קישור נתונים ל-View (מתבצע לכל פריט)
    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {

        // קבלת החניה לפי מיקום
        Parking parking = parkingList.get(position);

        // הצגת שם החניה
        holder.tvName.setText(parking.getName());

        //טיפול בזמן
        String timeText = parking.getTime().isEmpty()
                ? "Recently added" // אם אין זמן
                : "Added: " + parking.getTime(); // אחרת מציג זמן

        holder.tvTime.setText(timeText);

        //חישוב מרחק מהמשתמש
        if (userLocation != null) {

            float[] results = new float[1]; // מערך לתוצאה

            Location.distanceBetween(
                    userLocation.latitude, // מיקום המשתמש
                    userLocation.longitude,
                    parking.getLat(), // מיקום החניה
                    parking.getLng(),
                    results // כאן נשמר המרחק
            );

            float distMeters = results[0]; // המרחק במטרים

            if (distMeters < 1000) {
                // פחות מקילומטר → מציג במטרים
                holder.tvDistance.setText(
                        String.format(Locale.US, "%.0fm away", distMeters));
            } else {
                // יותר מקילומטר → מציג בקילומטרים
                holder.tvDistance.setText(
                        String.format(Locale.US, "%.1fkm away", distMeters / 1000));
            }

            holder.tvDistance.setVisibility(View.VISIBLE); // מציג טקסט

        } else {
            // אם אין מיקום משתמש → מסתיר
            holder.tvDistance.setVisibility(View.GONE);
        }

        //לחיצה על כל הפריט
        holder.itemView.setOnClickListener(v -> {

            if (clickListener != null)
                clickListener.onItemClick(parking);
        });

        //לחיצה על כפתור Reserve
        holder.btnReserve.setOnClickListener(v -> {

            if (reserveListener != null)
                reserveListener.onReserveClick(parking, position);
        });
    }

    //מחזיר כמה פריטים יש ברשימה
    @Override
    public int getItemCount() {

        return parkingList != null ? parkingList.size() : 0;
        // אם הרשימה לא null → מחזיר גודל
        // אחרת → 0
    }

    // ViewHolder – מחזיק את רכיבי ה-UI של כל פריט
    public static class ParkingViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvTime, tvDistance; // טקסטים
        Button btnReserve; // כפתור

        //מקשר את הרכיבים מה־XML
        // (כמו טקסטים וכפתור) למשתנים בקוד, כדי שניתן יהיה להשתמש בהם לכל פריט ב־RecyclerView.
        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);

            // קישור רכיבים מתוך ה-XML
            tvName = itemView.findViewById(R.id.tvParkingName);
            tvTime = itemView.findViewById(R.id.tvParkingTime);
            tvDistance = itemView.findViewById(R.id.tvParkingDistance);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}