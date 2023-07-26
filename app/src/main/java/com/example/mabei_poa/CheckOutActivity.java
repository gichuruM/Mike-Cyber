package com.example.mabei_poa;

import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;
import static com.example.mabei_poa.SaleToCustomerActivity.totalAmount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityCheckOutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CheckOutActivity extends AppCompatActivity {

    ActivityCheckOutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.totalCost.setText(String.valueOf(totalAmount));
        changeCalculation();

        binding.receivedAmount.setText("");
        binding.receivedAmount.requestFocus();

//        Log.d(TAG, "onCreate: Size of cart "+cartProductsList.size());
//        for(CartModel c: cartProductsList){
//            Log.d(TAG, "onCreate: Name "+c.getProductModel().getName()+" buying "+c.getProductModel().getPurchasePrice()+" profit "+(c.getProductModel().getSellingPrice()-c.getProductModel().getPurchasePrice()));
//        }

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
                    case R.id.cashTillPayment: payment = "cash and Pochi";
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
                else {  //saving the transaction
                    Date transactionTime = new Date();
                    double receivedAmount = Double.parseDouble(binding.receivedAmount.getText().toString());
                    double changeAmount = Double.parseDouble(binding.customersChange.getText().toString());
                    String note = binding.transactionNote.getText().toString();
                    String randomId = UUID.randomUUID().toString();

                    //Calculating profit on transaction
                    double totalProfit = 0;
                    for(CartModel c: cartProductsList){
                        totalProfit += (c.getProductModel().getSellingPrice() - c.getProductModel().getPurchasePrice())*c.getQuantity();
                    }
                    Log.d(TAG, "onClick: Total profit "+totalProfit);
                    Log.d(TAG, "onClick: transaction id "+randomId);

                    TransactionModel transaction = new TransactionModel(randomId,transactionTime,
                            cartProductsList,totalAmount,receivedAmount,changeAmount,payment,note,totalProfit);

                    Handler handler = new Handler();

                    startActivity(new Intent(CheckOutActivity.this,HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    //Log.d(TAG, "onClick: saving initially "+InternalDataBase.getInstance(CheckOutActivity.this).getUnsavedNotes().size());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Saving transaction in background");
                            if(!note.equals("")){
                                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a d/M/yy");
                                NoteModel transactionNote = new NoteModel(dateFormat.format(new Date()),note,randomId,new Date(),false);

                                FirebaseFirestore.getInstance().collection("Notes")
                                        .document(randomId)
                                        .set(transactionNote)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                saveTransaction(randomId,transaction);
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
                                saveTransaction(randomId,transaction);
                        }

                        private void saveTransaction(String randomId, TransactionModel transaction){
                            //Log.d(TAG, "saveTransaction run: offline size "+ InternalDataBase.getInstance(CheckOutActivity.this).getUnsavedNotes().size());
                            FirebaseFirestore.getInstance().collection("Transactions")
                                    .document(randomId)
                                    .set(transaction)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(CheckOutActivity.this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "Success saveTransaction run: offline size "+ InternalDataBase.getInstance(CheckOutActivity.this).getUnsavedNotes().size());
                                                }
                                            });
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
                        }
                    }).start();
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
}