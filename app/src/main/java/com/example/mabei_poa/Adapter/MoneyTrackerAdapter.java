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
    private int trackingMoneyTotal = 0;

    public MoneyTrackerAdapter(Context context, ArrayList<TransactionModel> transactionModels, int trackingMoneyTotal) {
        this.context = context;
        this.transactionModels = transactionModels;
        this.trackingMoneyTotal = trackingMoneyTotal;
        timeFormat = new SimpleDateFormat("h:mm a");
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

        int cumulativeTotal = 0;
        for(int i = 0; i <= position; i++){
            cumulativeTotal += transactionModels.get(i).getTotalAmount();
        }
        cumulativeTotal += trackingMoneyTotal;
        holder.accumulatingAmount.setText(String.valueOf(cumulativeTotal));
        holder.transactionAmount.setText(String.valueOf(transaction.getTotalAmount()));
        holder.transactionType.setText(transaction.getTransactionType());
    }

    @Override
    public int getItemCount() {
        return transactionModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView time, accumulatingAmount, transactionType, transactionAmount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.trackTransactionTime);
            accumulatingAmount = itemView.findViewById(R.id.trackTransactionCash);
            transactionType = itemView.findViewById(R.id.transactionType);
            transactionAmount = itemView.findViewById(R.id.trackingTransactionAmount);
        }
    }
}
