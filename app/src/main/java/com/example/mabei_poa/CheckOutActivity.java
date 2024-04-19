package com.example.mabei_poa;

import static com.example.mabei_poa.HomeActivity.checkConnection;
import static com.example.mabei_poa.HomeActivity.transactionDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;
import static com.example.mabei_poa.SaleToCustomerActivity.totalAmount;

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
import java.util.Iterator;
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

                    Log.d(TAG, "onClick: size "+cartProductsList.size());
                    for(CartModel c: cartProductsList)
                        Log.d(TAG, "onClick: Before"+c.getProductId()+": "+c.getQuantity());

                    // Combining similar products in the cart
//                    Iterator<CartModel> iterator = cartProductsList.iterator();
//                    while (iterator.hasNext()) {
//                        CartModel mainProduct = iterator.next();
//
//                        for (CartModel similarProducts : cartProductsList) {
//                            // Ensuring we only work with the first product of that kind
//                            if (cartProductsList.indexOf(similarProducts) <= cartProductsList.indexOf(mainProduct))
//                                continue;
//                            // The same product -> Add the quantities
//                            if (similarProducts.getProductId().equals(mainProduct.getProductId())) {
//                                double newQty = mainProduct.getQuantity() + similarProducts.getQuantity();
//                                similarProducts.setQuantity(newQty);
//                                iterator.remove(); // Remove similarProducts using the iterator
//                            }
//                        }
//                    }
                    ArrayList<CartModel> newCartProductsList = new ArrayList<>();
                    ArrayList<String> sortedProducts = new ArrayList<>();

                    for(int i = 0; i < cartProductsList.size(); i++){
                        CartModel mainProduct = cartProductsList.get(i);
                        boolean sorted = false;
//                        Log.d(TAG, "onClick: *-------------------------*");
//                        Log.d(TAG, "onClick: Outer product "+cartProductsList.get(i).getProductTotal());

                        //Ensuring we don't go through a product that has already been added to a main product
                        for(String id: sortedProducts){
                            if(mainProduct.getProductId().equals(id)){
                                sorted = true;
                                break;
                            }
                        }

                        if(!sorted){
                            for(int j = i+1; j < cartProductsList.size(); j++){
                                CartModel similarProduct = cartProductsList.get(j);
                                Log.d(TAG, "onClick: Inner product "+cartProductsList.get(j).getProductTotal());
                                if(mainProduct.getProductId().equals(similarProduct.getProductId())){
                                    double newQty = mainProduct.getQuantity() + similarProduct.getQuantity();
                                    Log.d(TAG, "onClick: product has matched");
                                    mainProduct.setQuantity(newQty);
                                }
                                if(j == cartProductsList.size()-1){
                                    //The last element of the array
                                    newCartProductsList.add(mainProduct);
                                    sortedProducts.add(mainProduct.getProductId());
//                                    Log.d(TAG, "onClick: this has happened "+sortedProducts.size());
                                }
                            }
                        }
                    }

//                    Log.d(TAG, "onClick: New size "+newCartProductsList.size());
//                    for(CartModel c: newCartProductsList)
//                        Log.d(TAG, "onClick: After"+c.getProductId()+": "+c.getQuantity());

                    //rounding off total amount to 2 decimal places
                    total = Math.round(total * 100)/100;
                    Log.d(TAG, "onClick: interim total "+total);

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
                                    Log.d(TAG, "onClick: profit "+(productTotalSelling - productTotalBuying));
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

                    Map<String, Double> cartDetails = new HashMap<>();

                    for(CartModel c: newCartProductsList)
                        cartDetails.put(c.getProductId(),c.getQuantity());
                    Log.d(TAG, "onClick: "+cartDetails);
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
                            Log.d(TAG, "run: Saving transaction in background");
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
                        Log.d(TAG, "onClick: adjusted qty "+adjustedQty+" old qty "+p.getQuantity());
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