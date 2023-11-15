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
import java.util.Map;

public class TransactionCartProductAdapter extends RecyclerView.Adapter<TransactionCartProductAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<ProductModel> productModels;
    private Map<String, Double> cartItemDetails;
    private String transactionType;

    public TransactionCartProductAdapter(Context context, ArrayList<ProductModel> productModels, Map<String, Double> cartItemDetails, String transactionType) {
        this.context = context;
        this.productModels = productModels;
        this.cartItemDetails = cartItemDetails;
        this.transactionType = transactionType;
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
        holder.transactionProductQty.setText(String.valueOf(cartItemDetails.get(product.getId())));
        holder.transactionProductUnits.setText(product.getUnits());

        if(transactionType.equals("Purchase")){
            holder.transactionProductPrice.setText(String.valueOf(product.getPurchasePrice()));
            Double qty = cartItemDetails.get(product.getId());
            if(qty != null)
                holder.transactionProductAmount.setText(String.valueOf(qty*product.getPurchasePrice()));
            else
                holder.transactionProductAmount.setText("N/A");
        } else if(transactionType.equals("Sale")){
            holder.transactionProductPrice.setText(String.valueOf(product.getSellingPrice()));
            Double qty = cartItemDetails.get(product.getId());
            if(qty != null)
                holder.transactionProductAmount.setText(String.valueOf(qty*product.getSellingPrice()));
            else
                holder.transactionProductAmount.setText("N/A");
        }
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
