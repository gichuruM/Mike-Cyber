package com.example.mabei_poa;

import static com.example.mabei_poa.HomeActivity.checkConnection;
import static com.example.mabei_poa.HomeActivity.transactionDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;
import static com.example.mabei_poa.SaleToCustomerActivity.totalAmount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.ExtraClasses.ChangingQuantityRunnable;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityCheckOutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CheckOutActivity extends AppCompatActivity {

    ActivityCheckOutBinding binding;
    private HandlerThread handlerThread = new HandlerThread("changingQuantity");
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.totalCost.setText(String.valueOf(totalAmount));
        changeCalculation();

        binding.receivedAmount.setText("");
        binding.receivedAmount.requestFocus();

        binding.autofillReceivedAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.receivedAmount.setText(String.valueOf(totalAmount));
            }
        });

        binding.receivedAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("")){
                    changeCalculation();
                }
            }
        });

        binding.saveTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payment = "";

                switch (binding.paymentMethod.getCheckedRadioButtonId()){
                    case R.id.cashPayment: payment = "cash";
                        break;
                    case R.id.tillPayment: payment = "Pochi";
                        break;
                    case R.id.cashTillPayment: payment = "Both";
                        break;
                    default: payment = "None";
                }

                if(payment.equals("None"))
                    Toast.makeText(CheckOutActivity.this, "Select method of payment to proceed", Toast.LENGTH_SHORT).show();
                else if(totalAmount == 0)
                    Toast.makeText(CheckOutActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                else if(binding.receivedAmount.getText().toString().equals(""))
                    Toast.makeText(CheckOutActivity.this, "Received amount is empty", Toast.LENGTH_SHORT).show();
                else if(cartProductsList.size() <= 0)
                    Toast.makeText(CheckOutActivity.this, "There are 0 products in the cart", Toast.LENGTH_SHORT).show();
                else {  //Getting details of the transaction
                    Long timeInMillis = new Date().getTime();
                    double receivedAmount = Double.parseDouble(binding.receivedAmount.getText().toString());
                    double total = totalAmount;
                    double totalProfit = 0, nonWaterProfit = 0;

                    double changeAmount = Double.parseDouble(binding.customersChange.getText().toString());
                    String note = binding.transactionNote.getText().toString();
                    String randomId = UUID.randomUUID().toString();

                    //rounding off total amount to 2 decimal places
                    total = Math.round(total * 100)/100;
                    Log.d(TAG, "onClick: interim total "+total);

                    //Thread for changing the quantity of items after a sale
                    handlerThread.start();
                    handler = new Handler(handlerThread.getLooper());

                    if(InternalDataBase.getInstance(CheckOutActivity.this).getCartType().equals("Purchase")){
                        receivedAmount = -receivedAmount;
                        total = -total;
                        //Adjusting quantity after transaction
                        for(CartModel c: cartProductsList){
                            ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(CheckOutActivity.this).getAllProducts();
                            for(ProductModel p: allProducts){
                                if(c.getProductId().equals(p.getId())){

                                    ChangingQuantityRunnable editQuantity = new ChangingQuantityRunnable(CheckOutActivity.this,p.getId(),p.getQuantity()+c.getQuantity());
                                    handler.post(editQuantity);
                                }
                            }
                        }
                    }
                    if(InternalDataBase.getInstance(CheckOutActivity.this).getCartType().equals("Sale")){
                        for(CartModel c: cartProductsList){
                            ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(CheckOutActivity.this).getAllProducts();
                            for(ProductModel p: allProducts){
                                if(c.getProductId().equals(p.getId())){
                                    //Adjusting quantity after transaction
                                    double adjustedQty = Math.round((p.getQuantity() - c.getQuantity()) * 100);
                                    adjustedQty = adjustedQty/100;
                                    Log.d(TAG, "onClick: adjusted qty "+adjustedQty+" old qty "+p.getQuantity());
                                    ChangingQuantityRunnable editQuantity = new ChangingQuantityRunnable(CheckOutActivity.this,p.getId(),adjustedQty);
                                    handler.post(editQuantity);

                                    //totalProfit += (p.getSellingPrice() - p.getPurchasePrice())*c.getQuantity();
                                    double productTotalSelling = Math.round(p.getSellingPrice()*c.getQuantity());
                                    double productTotalBuying = p.getPurchasePrice()*c.getQuantity();

                                    int remainder = (int) (productTotalSelling % 5);
                                    if(remainder != 0)
                                        productTotalSelling += (5 - remainder);
                                    
                                    totalProfit += (productTotalSelling - productTotalBuying);
                                    //Calculating the non-water profit
                                    if(!(c.getProductId().equals("61cfbdc7-63fa-4012-9f1f-220f2e0d0863") ||
                                        c.getProductId().equals("53fbab78-2f31-40c9-b4bb-690096416bc7"))){
                                        nonWaterProfit += (productTotalSelling - productTotalBuying);
                                    }
                                }
                            }
                        }
                    }

                    Log.d(TAG, "onClick: Total profit "+totalProfit);
                    Log.d(TAG, "onClick: transaction id "+randomId);

                    Map<String, Double> cartDetails = new HashMap<>();

                    for(CartModel c: cartProductsList)
                        cartDetails.put(c.getProductId(),c.getQuantity());

                    TransactionModel transaction = new TransactionModel(randomId,timeInMillis,
                            cartDetails,total,receivedAmount,changeAmount,payment,note,totalProfit,
                            InternalDataBase.getInstance(CheckOutActivity.this).getCartType(),nonWaterProfit);

                    //Saving the transaction
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Saving transaction in background");
                            if(!note.equals("") && checkConnection(CheckOutActivity.this)){
                                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a d/M/yy");
                                NoteModel transactionNote = new NoteModel(dateFormat.format(new Date()),note,randomId,new Date(),false);
                                Log.d(TAG, "run: Note before: "+transaction.getNote());
                                FirebaseFirestore.getInstance().collection("Notes")
                                        .document(randomId)
                                        .set(transactionNote)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                saveTransaction(transaction);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(CheckOutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                            } else
                                saveTransaction(transaction);
                        }

                        private void saveTransaction(TransactionModel transaction){

                            //Checking for internet connection
                            if(checkConnection(CheckOutActivity.this)){
                                transactionDBRef.child(transaction.getTransactionId())
                                        .setValue(transaction)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG, "onSuccess: Transaction saved successfully");
                                                Toast.makeText(CheckOutActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: Error saving transaction "+e.getMessage());
                                                Toast.makeText(CheckOutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                if(InternalDataBase.getInstance(CheckOutActivity.this).addToOfflineTransactions(transaction)){
                                    InternalDataBase.getInstance(CheckOutActivity.this).setSyncStatus(true);
                                    Log.d(TAG, "saveTransaction: offline transaction saved");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CheckOutActivity.this, "Transaction saved in offline mode", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }).start();

                    startActivity(new Intent(CheckOutActivity.this,HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        });
    }

    private void changeCalculation() {
        if(!binding.receivedAmount.getText().toString().trim().equals("")){
            Double change = Double.parseDouble(binding.receivedAmount.getText().toString()) - totalAmount;
            binding.customersChange.setText(String.valueOf(change));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}