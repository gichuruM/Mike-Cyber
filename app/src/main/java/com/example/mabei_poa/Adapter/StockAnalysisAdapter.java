package com.example.mabei_poa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.R;
import com.example.mabei_poa.StockAnalysis;

import java.util.ArrayList;

public class StockAnalysisAdapter extends RecyclerView.Adapter<StockAnalysisAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<ProductModel> productsList;

    public StockAnalysisAdapter(Context context, ArrayList<ProductModel> productsList) {
        this.context = context;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_stock_analysis_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProductModel product = productsList.get(position);

        holder.productName.setText(product.getName());
        holder.productQty.setText(String.valueOf(product.getQuantity()));
        if(StockAnalysis.stockType.equals("Cost of Buying"))
            holder.productCost.setText(String.format("%.1f",product.getPurchasePrice()*product.getQuantity()));
        else if(StockAnalysis.stockType.equals("Cost of Selling"))
            holder.productCost.setText(String.format("%.1f",product.getSellingPrice()*product.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView productName, productQty, productCost;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.stockAnalysisName);
            productQty = itemView.findViewById(R.id.stockAnalysisQty);
            productCost = itemView.findViewById(R.id.stockAnalysisProductCost);
        }
    }
}
