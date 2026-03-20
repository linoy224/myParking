package com.example.mypark.ui;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import java.util.ArrayList;

    // שיניתי ל-ParkingAdapter ו-Parking (אותיות גדולות)
    public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

        private ArrayList<Parking> parkingList; // ודאי שזה תואם לשם הקובץ Parking.java
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Parking parking);
        }

        public ParkingAdapter(ArrayList<Parking> parkingList, OnItemClickListener listener) {
            this.parkingList = parkingList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ParkingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
            Parking currentParking = parkingList.get(position);
            holder.textView.setText(currentParking.getName());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(currentParking);
                }
            });
        }

        @Override
        public int getItemCount() {
            return parkingList != null ? parkingList.size() : 0;
        }

        public static class ParkingViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ParkingViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }

