package com.example.Cyber.ExtraClasses;

import static com.example.Cyber.HomeActivity.transactionDBRef;
import static com.example.Cyber.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.TransactionModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;

public class RecalculatingProfitRunnable implements Runnable{

    private Context context;
    private TransactionModel transaction;

    public RecalculatingProfitRunnable(Context context, TransactionModel transaction) {
        this.context = context;
        this.transaction = transaction;
    }

    @Override
    public void run() {
        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
        Map<String, Double> cartDetails = transaction.getCartDetails();
        double totalProfit = 0, nonWaterProfit = 0;
        double previousProfit = transaction.getProfit();

        for(String key: cartDetails.keySet()){
            for(ProductModel p: allProducts){
                if(key.equals(p.getId())){
                    double productTotalSelling = Math.round(p.getSellingPrice()*cartDetails.get(key));
                    double productTotalBuying = p.getPurchasePrice()*cartDetails.get(key);

                    int remainder = (int) (productTotalSelling % 5);
                    if(remainder != 0)
                        productTotalSelling += (5 - remainder);

                    totalProfit += (productTotalSelling - productTotalBuying);

                    //Calculating the non-water profit
                    if(!(key.equals("61cfbdc7-63fa-4012-9f1f-220f2e0d0863") ||
                            key.equals("53fbab78-2f31-40c9-b4bb-690096416bc7"))){
                        nonWaterProfit += (productTotalSelling - productTotalBuying);
                    }
                    break;
                }
            }
        }

        double finalTotalProfit = totalProfit;

//        SimpleDateFormat format = new SimpleDateFormat("hh:mma dd-MM-yy");
//        Date date = new Date(transaction.getTimeInMillis());
//        Log.d(TAG, "run: "+format.format(date)+" previous profit "+previousProfit+" current "+totalProfit);

        transactionDBRef.child(transaction.getTransactionId())
                .child("profit").setValue(totalProfit)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: profit changed successfully. Previous "+previousProfit+" current "+ finalTotalProfit);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update profit on transaction "+transaction.getTransactionId());
                    }
                });

        transactionDBRef.child(transaction.getTransactionId())
                .child("waterlessProfit").setValue(nonWaterProfit)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: waterlessProfit changed successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update waterlessProfit on transaction "+transaction.getTransactionId());
                    }
                });
    }
}
