package com.example.Cyber.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Interface.TransactionClickedInterface;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<TransactionModel> transactionModels;
    private ArrayList<TransactionModel> filteredTransactions;
    private TransactionClickedInterface transactionClickedInterface;
    private int notSynced = 0;
    ArrayList<ProductModel> productsList = new ArrayList<>();

    public TransactionAdapter(Context context, ArrayList<TransactionModel> transactionModels, TransactionClickedInterface transactionClickedInterface, int notSynced) {
        this.context = context;
        this.transactionModels = transactionModels;
        this.transactionClickedInterface = transactionClickedInterface;
        this.notSynced = notSynced;
        filteredTransactions = new ArrayList<>();
        filteredTransactions.addAll(transactionModels);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_transaction_layout,parent,false);
        return new MyViewHolder(view, transactionClickedInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionModel transaction = filteredTransactions.get(position);
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

            for(String ids : cartDetails.keySet()){
                ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
                for(ProductModel p: allProducts){
                    if(ids.equals(p.getId())){
                        productsList.add(p);
                        break;
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
        return filteredTransactions.size();
    }

    @Override
    public Filter getFilter() {
        return transactionFilter;
    }

    private final Filter transactionFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<TransactionModel> filtered = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filtered.addAll(transactionModels);
            } else {
                String searchWord = constraint.toString().toLowerCase().trim();

                for(TransactionModel t: transactionModels){
                    long last48HoursInMillis = System.currentTimeMillis() - (48*60*60*1000);
                    //Only filtering transaction of the last 24 hours to reduce lag
                    if(t.getTimeInMillis() >= last48HoursInMillis){
                        Map<String, Double> cartDetails = t.getCartDetails();
                        boolean included = false;

                        if(cartDetails != null){
                            ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
                            //searching through the each product in each transaction
                            for(String id: cartDetails.keySet()){
                                for(ProductModel p: allProducts){
                                    if(p.getId().equals(id)){
                                        //searching the name of the product to see it contains the search term
                                        if(p.getName().toLowerCase().contains(searchWord)){
                                            filtered.add(t);
                                            included = true;
                                            break;
                                        }
                                    }
                                }
                                if(included)    //if transaction already contains the desired product, go to the next transaction
                                    break;
                            }
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtered;
            results.count = filtered.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredTransactions.clear();
            filteredTransactions.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

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
