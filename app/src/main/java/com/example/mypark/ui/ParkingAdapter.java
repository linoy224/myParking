package com.example.mypark.ui;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypark.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private final ArrayList<Parking> parkingList;
    private final OnItemClickListener clickListener;
    private final OnReserveClickListener reserveListener;
    private LatLng userLocation;

    public interface OnItemClickListener {
        void onItemClick(Parking parking);
    }

    public interface OnReserveClickListener {
        void onReserveClick(Parking parking, int position);
    }

    public ParkingAdapter(ArrayList<Parking> parkingList,
                          OnItemClickListener clickListener,
                          OnReserveClickListener reserveListener) {
        this.parkingList = parkingList;
        this.clickListener = clickListener;
        this.reserveListener = reserveListener;
    }

    public void setUserLocation(LatLng location) {
        this.userLocation = location;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_card, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        Parking parking = parkingList.get(position);

        holder.tvName.setText(parking.getName());

        String timeText = parking.getTime().isEmpty() ? "Recently added" : "Added: " + parking.getTime();
        holder.tvTime.setText(timeText);

        // Show distance if user location is known
        if (userLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    parking.getLat(), parking.getLng(), results);
            float distMeters = results[0];
            if (distMeters < 1000) {
                holder.tvDistance.setText(String.format(Locale.US, "%.0fm away", distMeters));
            } else {
                holder.tvDistance.setText(String.format(Locale.US, "%.1fkm away", distMeters / 1000));
            }
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(parking);
        });

        holder.btnReserve.setOnClickListener(v -> {
            if (reserveListener != null) reserveListener.onReserveClick(parking, position);
        });
    }

    @Override
    public int getItemCount() {
        return parkingList != null ? parkingList.size() : 0;
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvDistance;
        Button btnReserve;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvParkingName);
            tvTime = itemView.findViewById(R.id.tvParkingTime);
            tvDistance = itemView.findViewById(R.id.tvParkingDistance);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}
