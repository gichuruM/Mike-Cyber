package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.R;

import java.util.ArrayList;

public class TransactionCartProductAdapter extends RecyclerView.Adapter<TransactionCartProductAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<ProductModel> productModels;
    private ArrayList<CartModel> cartModels;

    public TransactionCartProductAdapter(Context context, ArrayList<ProductModel> productModels, ArrayList<CartModel> cartModels) {
        this.context = context;
        this.productModels = productModels;
        this.cartModels = cartModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_transaction_cart_item_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProductModel product = productModels.get(position);
        //Log.d(TAG, "onBindViewHolder: Transaction cartModel size "+cartModels.size());
        holder.transactionProductName.setText(product.getName());
        holder.transactionProductPrice.setText(String.valueOf(cartModels.get(position).getAmount()));
        holder.transactionProductQty.setText(String.valueOf(cartModels.get(position).getQuantity()));
        holder.transactionProductUnits.setText(product.getUnits());
        holder.transactionProductAmount.setText(String.valueOf(cartModels.get(position).getProductTotal()));
    }

    @Override
    public int getItemCount() {
        return productModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView transactionProductName, transactionProductPrice, transactionProductQty, transactionProductUnits, transactionProductAmount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            transactionProductName = itemView.findViewById(R.id.transactionProductName);
            transactionProductPrice = itemView.findViewById(R.id.transactionProductPrice);
            transactionProductQty = itemView.findViewById(R.id.transactionProductQty);
            transactionProductUnits = itemView.findViewById(R.id.transactionProductUnits);
            transactionProductAmount = itemView.findViewById(R.id.transactionProductAmount);
        }
    }
}
