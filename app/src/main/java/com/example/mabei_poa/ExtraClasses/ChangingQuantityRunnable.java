package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("products").document(productId);

        documentReference.update("quantity",Double.valueOf(productQuantity))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        InternalDataBase.getInstance(context).editProduct(productId,productQuantity);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Adjusting quantity failed "+e.getMessage());
                    }
                });
    }
}
