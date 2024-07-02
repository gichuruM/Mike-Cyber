package com.example.Cyber.Adapter;

import static com.example.Cyber.ReportsActivity.reportSettingType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cyber.Model.ProductTransactionModel;
import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportProductTransactionAdapter extends RecyclerView.Adapter<ReportProductTransactionAdapter.MyViewHolder> {

    Context context;
    ArrayList<ProductTransactionModel> productTransactions;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mma");

    public ReportProductTransactionAdapter(Context context, ArrayList<ProductTransactionModel> productTransactions) {
        this.context = context;
        this.productTransactions = productTransactions;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_product_transaction_report, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProductTransactionModel productTransaction = productTransactions.get(position);

        if(reportSettingType.equals("Sales")){
            holder.reportProductAmount.setText(String.format("%.1f", productTransaction.getProductRevenue()));
        } else if(reportSettingType.equals("Profit") || reportSettingType.equals("Shop Profit")){
            holder.reportProductAmount.setText(String.format("%.1f", productTransaction.getProductProfit()));
        }

        holder.reportProductNum.setText(String.valueOf(productTransaction.getProductNum()));

        Date date = new Date(productTransaction.getTimeInMillis());

        holder.reportProductDate.setText(simpleDateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return productTransactions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView reportProductDate, reportProductNum, reportProductAmount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            reportProductDate = itemView.findViewById(R.id.reportProductDate);
            reportProductNum = itemView.findViewById(R.id.reportProductNum);
            reportProductAmount = itemView.findViewById(R.id.reportProductAmount);
        }
    }
}
