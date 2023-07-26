package com.example.mabei_poa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.TransactionAdapter;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding binding;
    TransactionAdapter transactionAdapter;
    private ArrayList<TransactionModel> transactionModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        transactionModelList = new ArrayList<>();
        binding.transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionRecView.setHasFixedSize(true);
        getTransactions();
    }

    private void getTransactions() {
        ProgressDialog progressDialog = new ProgressDialog(TransactionActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();

        FirebaseFirestore.getInstance()
                .collection("Transactions")
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();

                        for(DocumentSnapshot ds: documentSnapshots){
                            TransactionModel transactionModel = ds.toObject(TransactionModel.class);
                            transactionModelList.add(transactionModel);
                        }

                        transactionAdapter = new TransactionAdapter(TransactionActivity.this,transactionModelList);
                        binding.transactionRecView.setAdapter(transactionAdapter);
                        transactionAdapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TransactionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }
}