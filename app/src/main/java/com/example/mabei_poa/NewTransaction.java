package com.example.mabei_poa;

import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityNewTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

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

//        handlerThread.start();
//        handler = new Handler(handlerThread.getLooper());
//
//        FirebaseFirestore.getInstance()
//                .collection("Transactions")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
//
//                        for(DocumentSnapshot ds: documentSnapshots){
//                            TransactionModel transaction = ds.toObject(TransactionModel.class);
//                            double nonWaterProfit = 0;
//
//                            //Calculating nonWater profit
//                            Map<String,Double> details = transaction.getCartDetails();
//                            ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(NewTransaction.this).getAllProducts();
//
//                            Log.d(TAG, "onSuccess: "+transaction.getTime());
//                            for(String key: details.keySet()){
//                                for(ProductModel p: allProducts){
//                                    if(p.getId().equals(key)){
////                                        Log.d(TAG, "onSuccess: name"+p.getName());
//                                        double buyingPrice = p.getPurchasePrice()*details.get(key);
//                                        double sellingPrice = Math.round(p.getSellingPrice()*details.get(key));
//
//                                        int remainder = (int) (sellingPrice % 5);
//                                        if(remainder != 0)
//                                            sellingPrice += (5 - remainder);
//
//                                        if(!(key.equals("61cfbdc7-63fa-4012-9f1f-220f2e0d0863") ||
//                                                key.equals("53fbab78-2f31-40c9-b4bb-690096416bc7"))){
//                                            nonWaterProfit += (sellingPrice - buyingPrice);
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//
//                            TransactionModel2 transactionModel2 = new TransactionModel2(
//                                    transaction.getTransactionId(),
//                                    transaction.getTime(),
//                                    transaction.getCartDetails(),
//                                    transaction.getTotalAmount(),
//                                    transaction.getReceivedAmount(),
//                                    transaction.getChangeAmount(),
//                                    transaction.getPaymentMethod(),
//                                    transaction.getNote(),
//                                    transaction.getProfit(),
//                                    transaction.getTransactionType(),
//                                    nonWaterProfit
//                            );
//
//                            ConvertingTransactionsRunnable convertingTransactionsRunnable = new ConvertingTransactionsRunnable(transactionModel2);
//                            handler.post(convertingTransactionsRunnable);
//
//                            Log.d(TAG, "onSuccess: now "+nonWaterProfit+" target "+transaction.getProfit());
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: "+e.getMessage());
//                    }
//                });
    }

    private void savingNewTransaction() {

        String transactionType = "", transactionMethod = "";
        double transactionAmount = Double.parseDouble(binding.transactionAmount.getText().toString());
        String transactionReason = binding.transactionReason.getText().toString();

        switch (binding.transactionType.getCheckedRadioButtonId()){
            case R.id.depositTransaction: transactionType = "Deposit";
                break;
            case R.id.withdrawalTransaction: transactionType = "Withdrawal";
                break;
            default: transactionType = "None";
        }

        switch (binding.transactionMethod.getCheckedRadioButtonId()){
            case R.id.cashTransaction: transactionMethod = "cash";
                break;
            case R.id.PochiTransaction: transactionMethod = "Pochi";
                break;
            case R.id.bothTransaction: transactionMethod = "Both";
                break;
            default: transactionMethod = "None";
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
            Date transactionTime = new Date();
            Map<String, Double> emptyCart = new HashMap<>();
            Log.d(TAG, "savingNewTransaction: "+randomId);
            TransactionModel transaction = new TransactionModel(randomId,transactionTime,emptyCart,
                    transactionAmount,0,0,transactionMethod,transactionReason,0,transactionType,0);

            FirebaseFirestore.getInstance()
                    .collection("Transactions")
                    .document(randomId)
                    .set(transaction)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(NewTransaction.this, "New Transaction saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewTransaction.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            startActivity(new Intent(NewTransaction.this,HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }
}