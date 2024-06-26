package com.example.Cyber.ExtraClasses;

import static com.example.Cyber.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangingQuantityRunnable implements Runnable{

    private Context context;
    private String productId;
    private Double productQuantity;

    public ChangingQuantityRunnable(Context context, String productId, Double productQuantity) {
        this.context = context;
        this.productId = productId;
        this.productQuantity = productQuantity;
    }

    @Override
    public void run() {

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        Log.d(TAG, "run: changing qty now");
        dataRef.child("quantity").setValue(productQuantity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: successfully changed quantity");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to adjust quantity "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: successfully changed quantity");
                    }
                });
    }
}
