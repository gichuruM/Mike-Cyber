package com.example.Cyber;

import static com.example.Cyber.HomeActivity.checkConnection;
import static com.example.Cyber.HomeActivity.transactionDBRef;
import static com.example.Cyber.ProductsActivity.TAG;
import static com.example.Cyber.SaleToCustomerActivity.cartProductsList;
import static com.example.Cyber.SaleToCustomerActivity.totalAmount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.Cyber.ExtraClasses.ChangingQuantityRunnable;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.CartModel;
import com.example.Cyber.Model.NoteModel;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.databinding.ActivityCheckOutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
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
    public static HandlerThread handlerThread = new HandlerThread("changingQuantity");
    public static Handler handler;

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

        //Making the received amount equal to the total amount by default
        binding.receivedAmount.setText(String.valueOf(totalAmount));
        changeCalculation();

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

                int paymentMethodCheckedId = binding.paymentMethod.getCheckedRadioButtonId();
                if (paymentMethodCheckedId == R.id.cashPayment) {
                    payment = "cash";
                } else if (paymentMethodCheckedId == R.id.tillPayment) {
                    payment = "till";
                } else if (paymentMethodCheckedId == R.id.cashTillPayment) {
                    payment = "Both";
                } else {
                    payment = "None";
                }

                if(payment.equals("None"))
                    Toast.makeText(CheckOutActivity.this, "Select method of payment to proceed", Toast.LENGTH_SHORT).show();
                else if(binding.receivedAmount.getText().toString().equals(""))
                    Toast.makeText(CheckOutActivity.this, "Received amount is empty", Toast.LENGTH_SHORT).show();
                else {  //Getting details of the transaction
                    Long timeInMillis = new Date().getTime();
                    double receivedAmount = Double.parseDouble(binding.receivedAmount.getText().toString());
                    double total = totalAmount;
                    double totalProfit = 0, nonWaterProfit = 0;

                    double changeAmount = Double.parseDouble(binding.customersChange.getText().toString());
                    String note = binding.transactionNote.getText().toString();
                    String randomId = UUID.randomUUID().toString();

                    ArrayList<CartModel> newCartProductsList = new ArrayList<>();
                    ArrayList<String> sortedProducts = new ArrayList<>();

                    for(int i = 0; i < cartProductsList.size(); i++){
                        CartModel mainProduct = cartProductsList.get(i);
                        //Log.d(TAG, "onClick: Starting loop price "+mainProduct.getProductTotal());
                        boolean sorted = false;
                        //Ensuring we don't go through a product that has already been added to a main product
                        for(String id: sortedProducts){
                            if(mainProduct.getProductId().equals(id)){
                                sorted = true;
                                break;
                            }
                        }
                        //Log.d(TAG, "onClick: sorted boolean "+sorted);
                        if(!sorted){
                            int j = i+1;
                            boolean lastProduct = true;
                            for( ; j < cartProductsList.size(); j++){
                                CartModel similarProduct = cartProductsList.get(j);

                                if(mainProduct.getProductId().equals(similarProduct.getProductId())){
                                    double newQty = mainProduct.getQuantity() + similarProduct.getQuantity();
                                    double newProductTotal = mainProduct.getProductTotal() + similarProduct.getProductTotal();

                                    mainProduct.setQuantity(newQty);
                                    mainProduct.setProductTotal(newProductTotal);
                                    //Log.d(TAG, "onClick: Similar product found "+mainProduct.getProductTotal());
                                }
                                if(j == cartProductsList.size()-1){
                                    lastProduct = false;
                                    //The last element of the array -->> additing th combination of all the products
                                    newCartProductsList.add(mainProduct);
                                    sortedProducts.add(mainProduct.getProductId());
                                }
                            }
                            if(lastProduct && j == cartProductsList.size()){
                                //To ensure the last product in the cart is also sorted
                                newCartProductsList.add(mainProduct);
                                sortedProducts.add(mainProduct.getProductId());
                            }
                        }
                    }

                    //rounding off total amount to 2 decimal places
                    total = Math.round(total * 100)/100;

                    String cartType = InternalDataBase.getInstance(CheckOutActivity.this).getCartType();
                    //Inverting values in case it's a purchase, counting profit for sale
                    if(cartType.equals("Purchase")){
                        receivedAmount = -receivedAmount;
                        total = -total;
                    } else if(cartType.equals("Sale")){
                        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(CheckOutActivity.this).getAllProducts();

                        for(CartModel c: newCartProductsList){
                            for(ProductModel p: allProducts){
                                if(c.getProductId().equals(p.getId())){
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

                    Map<String, Double> cartDetails = new HashMap<>();

                    for(CartModel c: newCartProductsList)
                        cartDetails.put(c.getProductId(),c.getQuantity());

                    TransactionModel transaction = new TransactionModel(randomId,timeInMillis,
                            cartDetails,total,receivedAmount,changeAmount,payment,note,totalProfit,
                            InternalDataBase.getInstance(CheckOutActivity.this).getCartType(),nonWaterProfit);

                    //Adjusting quantities if theres an internet connection
                    boolean internetConnection = checkConnection(CheckOutActivity.this);
                    if(internetConnection)
                        changeQty(CheckOutActivity.this, transaction);

                    //Saving the transaction
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(!note.equals("") && internetConnection){
                                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a d/M/yy");
                                NoteModel transactionNote = new NoteModel(dateFormat.format(new Date()),note,randomId,new Date(),false);

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
                            if(internetConnection){
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

    public static void changeQty(Context context, TransactionModel newTransaction){

        if(handler == null){
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();

        if(newTransaction.getTransactionType().equals("Purchase")){
            for(String key: newTransaction.getCartDetails().keySet()){
                for(ProductModel p: allProducts){
                    if(key.equals(p.getId())){
                        ChangingQuantityRunnable editQuantity = new ChangingQuantityRunnable(context,p.getId(),p.getQuantity()+newTransaction.getCartDetails().get(key));
                        handler.post(editQuantity);
                    }
                }
            }
        }
        if(newTransaction.getTransactionType().equals("Sale")){
            for(String key: newTransaction.getCartDetails().keySet()){
                for(ProductModel p: allProducts){
                    if(key.equals(p.getId())){
                        //Adjusting quantity after transaction
                        double adjustedQty = Math.round((p.getQuantity() - newTransaction.getCartDetails().get(key)) * 100);
                        adjustedQty = adjustedQty/100;
                        ChangingQuantityRunnable editQuantity = new ChangingQuantityRunnable(context,p.getId(),adjustedQty);
                        handler.post(editQuantity);
                    }
                }
            }
        }
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