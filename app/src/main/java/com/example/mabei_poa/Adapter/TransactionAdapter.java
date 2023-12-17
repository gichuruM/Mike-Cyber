package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Interface.TransactionClickedInterface;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<TransactionModel> transactionModels;
    private TransactionClickedInterface transactionClickedInterface;
    private int notSynced = 0;
    ArrayList<ProductModel> productsList = new ArrayList<>();

    public TransactionAdapter(Context context, ArrayList<TransactionModel> transactionModels, TransactionClickedInterface transactionClickedInterface, int notSynced) {
        this.context = context;
        this.transactionModels = transactionModels;
        this.transactionClickedInterface = transactionClickedInterface;
        this.notSynced = notSynced;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_transaction_layout,parent,false);
        return new MyViewHolder(view, transactionClickedInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionModel transaction = transactionModels.get(position);
        Date date = new Date(transaction.getTimeInMillis());

        String time = new SimpleDateFormat("h:mm a  d/M/yyyy").format(date);

        holder.transactionDateTime.setText(time);
        holder.transactionReceivedAmount.setText(String.valueOf(transaction.getReceivedAmount()));
        holder.transactionChange.setText(String.valueOf(transaction.getChangeAmount()));
        holder.transactionTotal.setText(String.valueOf(transaction.getTotalAmount()));
        holder.transactionMethod.setText(transaction.getPaymentMethod());
        holder.transactionType.setText(transaction.getTransactionType());

        if(holder.getAdapterPosition() < notSynced){
            holder.transactionNotSynced.setVisibility(View.VISIBLE);
        }

        if(transaction.getNote().isEmpty())
            holder.transactionNoteLayout.setVisibility(View.GONE);
        else
            holder.transactionNote.setText(transaction.getNote());

        //Creating the recyclerview for the products in the transaction
        productsList.clear();
        Map<String, Double> cartDetails = transaction.getCartDetails();

        if(cartDetails != null){
            Log.d(TAG, "onBindViewHolder: "+cartDetails.size());
            Log.d(TAG, "onBindViewHolder: "+transaction.getNote());
            Log.d(TAG, "onBindViewHolder: total "+transaction.getTotalAmount());
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
        } else {
            TransactionCartProductAdapter adapter = new TransactionCartProductAdapter(context,productsList,cartDetails,transaction.getTransactionType());

            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setHasFixedSize(true);
        }
    }

    @Override
    public int getItemCount() {
        return transactionModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        LinearLayout transactionNoteLayout;
        TextView transactionDateTime, transactionReceivedAmount, transactionTotal, transactionChange, transactionMethod, transactionNote, transactionType;
        RecyclerView recyclerView;
        ImageView deleteTransaction, transactionNotSynced;

        public MyViewHolder(@NonNull View itemView, TransactionClickedInterface transactionClickedInterface) {
            super(itemView);

            transactionDateTime = itemView.findViewById(R.id.transactionDateTime);
            transactionReceivedAmount = itemView.findViewById(R.id.transactionReceivedAmount);
            transactionTotal = itemView.findViewById(R.id.transactionTotal);
            transactionChange = itemView.findViewById(R.id.transactionChange);
            transactionMethod = itemView.findViewById(R.id.transactionMethod);
            transactionNote = itemView.findViewById(R.id.transactionNote);
            transactionNoteLayout = itemView.findViewById(R.id.transactionNoteLayout);
            recyclerView = itemView.findViewById(R.id.transactionProductsRecView);
            transactionType = itemView.findViewById(R.id.typeOfTransaction);
            deleteTransaction = itemView.findViewById(R.id.deleteTransaction);
            transactionNotSynced = itemView.findViewById(R.id.transactionNotSynced);

            deleteTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;

                    transactionClickedInterface.transactionClicked(pos);
                }
            });
        }
    }
}
