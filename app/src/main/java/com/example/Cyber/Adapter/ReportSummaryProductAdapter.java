package com.example.Cyber.Adapter;

import static com.example.Cyber.ReportsActivity.reportSettingType;
import static com.example.Cyber.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.ProductTransactionModel;
import com.example.Cyber.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportSummaryProductAdapter extends RecyclerView.Adapter<ReportSummaryProductAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<ProductTransactionModel> SummaryProductTransactions;
    private Map<String, String> productMap = new HashMap<>();

    public ReportSummaryProductAdapter(Context context, ArrayList<ProductTransactionModel> SummaryProductTransactions) {
        this.context = context;
        this.SummaryProductTransactions = SummaryProductTransactions;

        //Creating a map of id to productName
        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
        productMap.clear();
        for(ProductModel product : allProducts){
            productMap.put(product.getId(), product.getName());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_product_summary_report, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProductTransactionModel productTransaction = SummaryProductTransactions.get(position);

        holder.reportSummaryNum.setText(String.valueOf(position+1));
        holder.reportSummaryProductName.setText(productMap.get(productTransaction.getProductId()));
        holder.reportSummaryProductNum.setText(String.valueOf(productTransaction.getProductTotalNum()));
        if(reportSettingType.equals("Sales"))
            holder.reportSummaryProductTotal.setText(String.valueOf(productTransaction.getProductTotalRevenue()));
        else if(reportSettingType.equals("Profit") || reportSettingType.equals("Shop Profit")){
            holder.reportSummaryProductTotal.setText(String.format("%.1f",productTransaction.getProductTotalProfit()));
        }
    }

    @Override
    public int getItemCount() {
        return SummaryProductTransactions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView reportSummaryNum, reportSummaryProductName;
        TextView reportSummaryProductNum, reportSummaryProductTotal;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            reportSummaryNum = itemView.findViewById(R.id.reportSummaryNum);
            reportSummaryProductName = itemView.findViewById(R.id.reportSummaryProductName);
            reportSummaryProductNum = itemView.findViewById(R.id.reportSummaryProductNum);
            reportSummaryProductTotal = itemView.findViewById(R.id.reportSummaryProductTotal);
        }
    }
}
