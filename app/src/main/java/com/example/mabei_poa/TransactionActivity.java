package com.example.mabei_poa;

import static com.example.mabei_poa.HomeActivity.SHOP_USER_UID;
import static com.example.mabei_poa.HomeActivity.userUID;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity implements TransactionClickedInterface {

    ActivityTransactionBinding binding;
    TransactionAdapter transactionAdapter;
    ArrayList<TransactionModel> transactionsList;

    static public DatabaseReference transactionDBRef = FirebaseDatabase.getInstance().getReference("transactions");
    static public ValueEventListener transactionEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        transactionsList = new ArrayList<>();
        binding.transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionRecView.setHasFixedSize(true);
        getTransactions();
    }

    private void getTransactions() {

        ArrayList<TransactionModel> allTransactions = new ArrayList<>();
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
    
    public void updateTransactions(ProgressDialog progressDialog){
        Log.d(TAG, "updateTransactions: single value event listener triggered");
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
                Log.d(TAG, "onDataChange: Transactions product size "+updatedAllTransactions.size());
                InternalDataBase.getInstance(TransactionActivity.this).batchAdditionToAllTransactions(updatedAllTransactions);
                updatingUIWithTransactions(progressDialog, updatedAllTransactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatingUIWithTransactions(ProgressDialog progressDialog, ArrayList<TransactionModel> transactions){

        transactionsList = transactions;

        transactionAdapter = new TransactionAdapter(
                TransactionActivity.this,transactionsList, this);

        binding.transactionRecView.setAdapter(transactionAdapter);
        transactionAdapter.notifyDataSetChanged();
        
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void deletingOldTransaction() {
        //getting time in millis a week ago
        long weekAgoInMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        Query oldTransactionQuery = transactionDBRef.orderByChild("timeInMillis").endAt(weekAgoInMillis);

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

    @Override
    public void transactionClicked(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Are you sure you want to delete this transaction?");
        String id = transactionsList.get(position).getTransactionId();
        Log.d(TAG, "transactionClicked: ID: "+id);

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