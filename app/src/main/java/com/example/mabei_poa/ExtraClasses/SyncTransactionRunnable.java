package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.HomeActivity.transactionDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mabei_poa.CheckOutActivity;
import com.example.mabei_poa.Model.TransactionModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SyncTransactionRunnable implements Runnable{

    private Context context;
    private TransactionModel transaction;
    private View view;

    public SyncTransactionRunnable(Context context, TransactionModel transaction, View view) {
        this.context = context;
        this.transaction = transaction;
        this.view = view;
    }

    @Override
    public void run() {

        CheckOutActivity.changeQty(context,transaction);

        transactionDBRef.child(transaction.getTransactionId())
                .setValue(transaction)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        InternalDataBase.getInstance(context).removeFromUnsavedTransactions(transaction);
                        if(InternalDataBase.getInstance(context).getOfflineTransactions().size() == 0 &&
                                InternalDataBase.getInstance(context).getOfflineDebtUpdates().size() == 0){
                            InternalDataBase.getInstance(context).setSyncStatus(false);
                            view.clearAnimation();
                            view.setVisibility(View.GONE);
                            Toast.makeText(context, "Synced successfully", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "onSuccess: Transaction saved successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Error saving transaction "+e.getMessage());
                    }
                });
    }
}
