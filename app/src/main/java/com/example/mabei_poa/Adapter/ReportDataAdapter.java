package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.ReportsActivity.reportSettingType;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class ReportDataAdapter extends RecyclerView.Adapter<ReportDataAdapter.MyViewHolder>{

    private ArrayList<TransactionModel> transactionModels;
    private SimpleDateFormat simpleDateFormat;

    public ReportDataAdapter(ArrayList<TransactionModel> transactionModels) {
        this.transactionModels = transactionModels;
        simpleDateFormat = new SimpleDateFormat("hh:mma");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_report_record, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionModel transactionModel = transactionModels.get(position);

        holder.reportDate.setText(simpleDateFormat.format(transactionModel.getTime()));
        holder.reportPayment.setText(transactionModel.getPaymentMethod());
        Map<String, Double> cartDetails = transactionModel.getCartDetails();

        holder.reportItemNum.setText(String.valueOf(cartDetails.size()));
        if(reportSettingType.equals("Sales") || reportSettingType.equals("Purchase") || reportSettingType.equals("Cash Flow"))
            holder.reportAmountTotal.setText(String.valueOf(transactionModel.getTotalAmount()));
        else if(reportSettingType.equals("Profit")){
            double profit =  transactionModel.getProfit();
            holder.reportAmountTotal.setText(String.format("%.1f", profit));
        }
        else if(reportSettingType.equals("Shop Profit")){
            double waterLess =  transactionModel.getWaterlessProfit();
            holder.reportAmountTotal.setText(String.format("%.1f", waterLess));
        }
    }

    @Override
    public int getItemCount() {
        return transactionModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView reportDate, reportPayment, reportItemNum, reportAmountTotal;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            reportDate = itemView.findViewById(R.id.reportDate);
            reportPayment = itemView.findViewById(R.id.reportPayment);
            reportItemNum = itemView.findViewById(R.id.reportItemNum);
            reportAmountTotal = itemView.findViewById(R.id.reportAmountTotal);
        }
    }
}
