package com.example.mabei_poa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class MoneyTrackerAdapter extends RecyclerView.Adapter<MoneyTrackerAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<TransactionModel> transactionModels;
    private SimpleDateFormat timeFormat;
    private String datePicked;
    private double trackingMoneyTotal = 0;

    public MoneyTrackerAdapter(Context context, ArrayList<TransactionModel> transactionModels, String datePicked) {
        this.context = context;
        this.transactionModels = transactionModels;
        this.datePicked = datePicked;
        timeFormat = new SimpleDateFormat("h:mm a");
        Map<String, Integer> tracking = InternalDataBase.getInstance(context).getTrackingData();
        if(tracking.containsKey(datePicked)){
            trackingMoneyTotal = tracking.get(datePicked).doubleValue();
        } else {
            trackingMoneyTotal = 1500;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_transaction_tracking_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionModel transaction = transactionModels.get(position);

        String time = timeFormat.format(transaction.getTime());
        holder.time.setText(time);
        trackingMoneyTotal += transaction.getTotalAmount();
        holder.amount.setText(String.valueOf(trackingMoneyTotal));
    }

    @Override
    public int getItemCount() {
        return transactionModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView time, amount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.trackTransactionTime);
            amount = itemView.findViewById(R.id.trackTransactionCash);
        }
    }
}
