package com.example.mabei_poa;

import static com.example.mabei_poa.HomeActivity.SHOP_USER_UID;
import static com.example.mabei_poa.HomeActivity.transactionDBRef;
import static com.example.mabei_poa.HomeActivity.userUID;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.AllProductsAdapter;
import com.example.mabei_poa.Adapter.TransactionAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Interface.TransactionClickedInterface;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity implements TransactionClickedInterface {

    ActivityTransactionBinding binding;
    TransactionAdapter transactionAdapter;
    ArrayList<TransactionModel> transactionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        transactionsList = new ArrayList<>();
        binding.transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionRecView.setHasFixedSize(true);
        getTransactions();
    }

    private void getTransactions() {

        ArrayList<TransactionModel> allTransactions;
        allTransactions = InternalDataBase.getInstance(this).getAllTransactions();

        ProgressDialog progressDialog = new ProgressDialog(TransactionActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();

        deletingOldTransaction();
        
        if(allTransactions.isEmpty())
            updateTransactions(progressDialog);
        else
            updatingUIWithTransactions(progressDialog, allTransactions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);

        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Find a product");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                transactionAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void updateTransactions(ProgressDialog progressDialog){

        transactionDBRef.orderByChild("timeInMillis").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TransactionModel> updatedAllTransactions = new ArrayList<>();

                for(DataSnapshot snap: snapshot.getChildren()){
                    TransactionModel transaction = snap.getValue(TransactionModel.class);

                    if(transaction != null)
                        updatedAllTransactions.add(transaction);
                }
                Collections.reverse(updatedAllTransactions);
                //Log.d(TAG, "onDataChange: Transactions product size "+updatedAllTransactions.size());
                InternalDataBase.getInstance(TransactionActivity.this).batchAdditionToAllTransactions(updatedAllTransactions);
                updatingUIWithTransactions(progressDialog, updatedAllTransactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatingUIWithTransactions(ProgressDialog progressDialog, ArrayList<TransactionModel> transactions){

        int unSyncedSize = 0;
        transactionsList = transactions;

        ArrayList<TransactionModel> unsavedTransactions = InternalDataBase.getInstance(this).getOfflineTransactions();

        int count = 0;
        for(int i = unsavedTransactions.size()-1; i >= 0; i--){
            transactions.add(count,unsavedTransactions.get(i));
            count++;
        }

        unSyncedSize = unsavedTransactions.size();

        transactionAdapter = new TransactionAdapter(
                TransactionActivity.this,transactionsList, this,unSyncedSize);

        binding.transactionRecView.setAdapter(transactionAdapter);
        transactionAdapter.notifyDataSetChanged();

        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void deletingOldTransaction() {
        //getting time in millis a week ago
        long weekAgoInMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        Query oldTransactionQuery = transactionDBRef.orderByChild("timeInMillis").endAt(weekAgoInMillis);

        new Thread(new Runnable() {
            @Override
            public void run() {
                oldTransactionQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap: snapshot.getChildren()){
                            snap.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: Error retrieve old transactions "+error.getMessage());
                    }
                });
            }
        }).start();
    }

    @Override
    public void transactionClicked(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TransactionModel transaction = transactionsList.get(position);
        String id = transaction.getTransactionId();
        String transactionType = transaction.getTransactionType();
        String transactionAmount = String.valueOf(transaction.getTotalAmount());
        Date transactionDate = new Date(transaction.getTimeInMillis());

        builder.setTitle("Alert");
        builder.setMessage("Delete "+transactionType+" transaction of amount "+transactionAmount+" at "+transactionDate+" ?");

        TransactionModel transactionModel = transactionsList.get(position);
//        Log.d(TAG, "transactionClicked: ID: "+id);
//        Log.d(TAG, "transactionClicked: "+transactionModel.getTransactionType());
//        Log.d(TAG, "transactionClicked: "+transactionModel.getCartDetails());

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(userUID.equals(SHOP_USER_UID)){
                    Toast.makeText(TransactionActivity.this, "Action Restricted", Toast.LENGTH_SHORT).show();
                } else {
                    transactionsList.remove(position);
                    transactionAdapter.notifyItemRemoved(position);

                    DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

                    transactionRef.child(id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(TransactionActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(TransactionActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TransactionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).show();
    }
}