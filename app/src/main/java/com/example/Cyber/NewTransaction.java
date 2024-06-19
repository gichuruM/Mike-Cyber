package com.example.Cyber;

import static com.example.Cyber.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.databinding.ActivityNewTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewTransaction extends AppCompatActivity {

    ActivityNewTransactionBinding binding;
    private HandlerThread handlerThread = new HandlerThread("ConvertingTransactions");
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.transactionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savingNewTransaction();
            }
        });
    }

    private void savingNewTransaction() {

        String transactionType = "", transactionMethod = "";
        double transactionAmount = Double.parseDouble(binding.transactionAmount.getText().toString());
        String transactionReason = binding.transactionReason.getText().toString();

        int transactionTypeCheckedId = binding.transactionType.getCheckedRadioButtonId();
        if (transactionTypeCheckedId == R.id.depositTransaction) {
            transactionType = "Deposit";
        } else if (transactionTypeCheckedId == R.id.withdrawalTransaction) {
            transactionType = "Withdrawal";
        } else {
            transactionType = "None";
        }

        int transactionMethodCheckedId = binding.transactionMethod.getCheckedRadioButtonId();
        if (transactionMethodCheckedId == R.id.cashTransaction) {
            transactionMethod = "cash";
        } else if (transactionMethodCheckedId == R.id.tillTransaction) {
            transactionMethod = "Till";
        } else if (transactionMethodCheckedId == R.id.bothTransaction) {
            transactionMethod = "Both";
        } else {
            transactionMethod = "None";
        }


        if(transactionType.equals("None"))
            Toast.makeText(this, "Type of Transaction is empty", Toast.LENGTH_SHORT).show();
        else if(transactionMethod.equals("None"))
            Toast.makeText(this, "Mode of Transaction is empty", Toast.LENGTH_SHORT).show();
        else if(Double.isNaN(transactionAmount) || transactionAmount <= 0)
            Toast.makeText(this, "Fill in Transaction amount", Toast.LENGTH_SHORT).show();
        else if(transactionReason.equals("") || transactionReason.length() < 5)
            Toast.makeText(this, "Note is empty or it's too short", Toast.LENGTH_SHORT).show();
        else{
            if(transactionType.equals("Withdrawal"))
                transactionAmount = -transactionAmount;

            String randomId = UUID.randomUUID().toString();
            Long timeInMillis = new Date().getTime();
            Map<String, Double> emptyCart = new HashMap<>();
            Log.d(TAG, "savingNewTransaction: "+randomId);
            TransactionModel transaction = new TransactionModel(randomId,timeInMillis,emptyCart,
                    transactionAmount,0,0,transactionMethod,transactionReason,0,transactionType,0);

            DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

            transactionRef.child(transaction.getTransactionId())
                    .setValue(transaction)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: Transaction saved successfully");
                            Toast.makeText(NewTransaction.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error saving transaction "+e.getMessage());
                            Toast.makeText(NewTransaction.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            startActivity(new Intent(NewTransaction.this,HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }
}