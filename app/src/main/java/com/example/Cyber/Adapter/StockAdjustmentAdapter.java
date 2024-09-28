package com.example.Cyber.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cyber.Model.StockAdjustmentModel;
import com.example.Cyber.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StockAdjustmentAdapter extends RecyclerView.Adapter<StockAdjustmentAdapter.ViewHolder> {

    private ArrayList<StockAdjustmentModel> adjustmentList;

    public StockAdjustmentAdapter(ArrayList<StockAdjustmentModel> adjustmentList) {
        this.adjustmentList = adjustmentList;
    }

    @NonNull
    @Override
    public StockAdjustmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_adjustment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StockAdjustmentAdapter.ViewHolder holder, int position) {
        StockAdjustmentModel adjustment = adjustmentList.get(position);

        holder.productName.setText(adjustment.getProductName());
        holder.quantityAdded.setText("+" + adjustment.getQuantityAdded());
        holder.quantityOld.setText(String.valueOf(adjustment.getQuantityOld()));
        holder.timestamp.setText(formatTimestamp(adjustment.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return adjustmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView productName, quantityAdded, quantityOld, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            quantityAdded = itemView.findViewById(R.id.quantityAdded);
            quantityOld = itemView.findViewById(R.id.quantityOld);
            timestamp = itemView.findViewById(R.id.timestamp);

            Log.d("ViewHolder", "productName is null: " + (productName == null));
            Log.d("ViewHolder", "quantityAdded is null: " + (quantityAdded == null));
            Log.d("ViewHolder", "quantityOld is null: " + (quantityOld == null));
            Log.d("ViewHolder", "timestamp is null: " + (timestamp == null));
        }
    }

    private String formatTimestamp(long timestamp) {
        // Format timestamp to readable date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}