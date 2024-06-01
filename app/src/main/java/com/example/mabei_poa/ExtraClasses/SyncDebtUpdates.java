package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.HomeActivity.customerDebtDBRef;
import static com.example.mabei_poa.HomeActivity.dayDebtTrackerDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.Model.DebtTrackerSummaryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.UUID;

public class SyncDebtUpdates implements Runnable{

    private Context context;
    private CustomerModel c;
    private View view;

    public SyncDebtUpdates(Context context, CustomerModel customer, View view) {
        this.context = context;
        this.c = customer;
        this.view = view;
    }

    @Override
    public void run() {

        DatabaseReference docRef = customerDebtDBRef.child(c.getCustomerId());

        int newCurrentAmount  = c.getCurrentDebt();
        String newDebtProgress = c.getDebtProgress();
        Date newEditDate = new Date();

        if(c.getProposedAmountType().equals("DEBT")){
            newCurrentAmount = newCurrentAmount + c.getProposedAmount();
            if(!newDebtProgress.equals(""))
                newDebtProgress = newDebtProgress + " + ";
        } else if(c.getProposedAmountType().equals("DEBT_PAYMENT")){
            newCurrentAmount = newCurrentAmount - c.getProposedAmount();
            if(!newDebtProgress.equals(""))
                newDebtProgress = newDebtProgress + " - ";
        }
        newDebtProgress = newDebtProgress + c.getProposedAmount();

        //Customer has cleared their debt
        if(newCurrentAmount == 0){
            newDebtProgress = "";
            docRef.child("initialDate").setValue(newEditDate);
        } else if(c.getCurrentDebt() == 0){ //Initial debt
            docRef.child("initialDate").setValue(newEditDate);
        }

        String finalNewDebtProgress = newDebtProgress;
        docRef.child("currentDebt").setValue(newCurrentAmount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        docRef.child("debtProgress").setValue(finalNewDebtProgress)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        docRef.child("editDate").setValue(newEditDate);
                                        Log.d(TAG, "onSuccess: New debt updated successfully");
                                    }
                                });
                    }
                });

        //Creating and saving debt calculations for money tracking
        String debtId = UUID.randomUUID().toString();
        DebtTrackerSummaryModel debtSummary = new DebtTrackerSummaryModel(
                c.getCustomerId(),
                c.getCustomerName(),
                new Date(),
                c.getProposedAmountType(),
                c.getProposedAmount()
        );

        dayDebtTrackerDBRef.child(debtId).setValue(debtSummary)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: New debt tracker saved");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: New debt tracker saving failed "+e.getMessage());
                    }
                });

        InternalDataBase.getInstance(context).removeFromUnsavedUpdateDebts(c);
        if(InternalDataBase.getInstance(context).getOfflineDebtUpdates().size() == 0){
            InternalDataBase.getInstance(context).setSyncStatus(false);

            //Added this lines because of crush
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.clearAnimation();
                    view.setVisibility(View.GONE);
                }
            });

            Toast.makeText(context, "Debt Synced successfully", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "run: Offline debt update saved!");
    }
}
