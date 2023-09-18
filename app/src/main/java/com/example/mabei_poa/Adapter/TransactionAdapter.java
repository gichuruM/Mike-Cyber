package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TransactionModel> transactionModels;
    ArrayList<ProductModel> productsList = new ArrayList<>();

    public TransactionAdapter(Context context, ArrayList<TransactionModel> transactionModels) {
        this.context = context;
        this.transactionModels = transactionModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_transaction_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionModel transaction = transactionModels.get(position);

        String time = new SimpleDateFormat("h:mm a  d/M/yyyy").format(transaction.getTime());

        holder.transactionDateTime.setText(time);
        holder.transactionReceivedAmount.setText(String.valueOf(transaction.getReceivedAmount()));
        holder.transactionChange.setText(String.valueOf(transaction.getChangeAmount()));
        holder.transactionTotal.setText(String.valueOf(transaction.getTotalAmount()));
        holder.transactionMethod.setText(transaction.getPaymentMethod());

        if(transaction.getNote().isEmpty())
            holder.transactionNoteLayout.setVisibility(View.GONE);
        else
            holder.transactionNote.setText(transaction.getNote());

        //Creating the recyclerview for the products in the transaction
        productsList.clear();
        Map<String, Double> cartDetails = transaction.getCartDetails();

        for(String ids : cartDetails.keySet()){
            ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
            for(ProductModel p: allProducts){
                if(ids.equals(p.getId())){
                    productsList.add(p);
                }
            }
        }

        TransactionCartProductAdapter adapter = new TransactionCartProductAdapter(context,productsList,cartDetails,transaction.getTransactionType());

        holder.recyclerView.setAdapter(adapter);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setHasFixedSize(true);
    }

    @Override
    public int getItemCount() {
        return transactionModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        LinearLayout transactionNoteLayout;
        TextView transactionDateTime, transactionReceivedAmount, transactionTotal, transactionChange, transactionMethod, transactionNote;
        RecyclerView recyclerView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            transactionDateTime = itemView.findViewById(R.id.transactionDateTime);
            transactionReceivedAmount = itemView.findViewById(R.id.transactionReceivedAmount);
            transactionTotal = itemView.findViewById(R.id.transactionTotal);
            transactionChange = itemView.findViewById(R.id.transactionChange);
            transactionMethod = itemView.findViewById(R.id.transactionMethod);
            transactionNote = itemView.findViewById(R.id.transactionNote);
            transactionNoteLayout = itemView.findViewById(R.id.transactionNoteLayout);
            recyclerView = itemView.findViewById(R.id.transactionProductsRecView);
        }
    }
}
