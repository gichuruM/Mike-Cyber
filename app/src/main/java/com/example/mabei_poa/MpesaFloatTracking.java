package com.example.mabei_poa;

import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.MoneyTrackerAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityMpesaFloatTrackingBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MpesaFloatTracking extends AppCompatActivity {

    ActivityMpesaFloatTrackingBinding binding;
    String thisYear, thisMonth, today, datePicked = "", datePickedAlternative = "";
    ArrayList<TransactionModel> floatTrackingList;
    Map<String, Integer> floatTrackingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMpesaFloatTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        floatTrackingList = new ArrayList<>();

        thisYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        datePicked = today+"/"+thisMonth+"/"+thisYear;
        datePickedAlternative = today+"-"+thisMonth+"-"+thisYear;

        binding.btnTrackingDateFloat.setText(datePicked);

        floatTrackingData = InternalDataBase.getInstance(this).getFloatData();

        if(floatTrackingData.containsKey(datePicked))
            binding.todaysStartingFloat.setText(floatTrackingData.get(datePicked).toString());
        else {
            binding.todaysStartingFloat.setText("0");
            InternalDataBase.getInstance(this).addToFloatTracking(datePicked,0);
        }

        binding.trackingMpesaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.trackingMpesaRecyclerView.setHasFixedSize(true);

        binding.btnTrackingDateFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker();
            }
        });

        binding.todaysStartingFloatSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Integer> startingCapital = new HashMap<>();

                if(binding.todaysStartingFloat.getText().toString().equals(""))
                    return;

                int capital = Integer.parseInt(binding.todaysStartingFloat.getText().toString());
                startingCapital.put(datePicked,capital);

                InternalDataBase.getInstance(MpesaFloatTracking.this).addToFloatTracking(datePicked,capital);

                FirebaseFirestore.getInstance()
                        .collection("FloatTracking")
                        .document(datePickedAlternative)
                        .set(startingCapital)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MpesaFloatTracking.this, "New float amount saved", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MpesaFloatTracking.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.mpesaTransactionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mpesaTransactionType = "None";
                double transactionAmount = Double.parseDouble(binding.mpesaAmount.getText().toString());
                String randomId = UUID.randomUUID().toString();
                Long timeInMillis = new Date().getTime();

                switch (binding.mpesaTransactionType.getCheckedRadioButtonId()){
                    case R.id.mpesaDeposit:
                        mpesaTransactionType = "Mpesa Deposit";
                        transactionAmount = -transactionAmount;
                        break;
                    case R.id.mpesaWithdrawal:
                        mpesaTransactionType = "Mpesa Withdrawal";
                        break;
                    default: mpesaTransactionType = "None";
                }

                if(mpesaTransactionType.equals("None")){
                    Toast.makeText(MpesaFloatTracking.this, "Kindly fill in whether it's a deposit or withdrawal", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(MpesaFloatTracking.this);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("transaction");
                progressDialog.show();

                TransactionModel transaction = new TransactionModel(
                        randomId,timeInMillis,new HashMap<>(),transactionAmount,0,
                        0,"Mpesa", "",0,mpesaTransactionType,0);

                DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

                transactionRef.child(transaction.getTransactionId())
                        .setValue(transaction)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MpesaFloatTracking.this, "Transaction saved", Toast.LENGTH_SHORT).show();

                                binding.mpesaAmount.setText("");
                                binding.mpesaTransactionType.clearCheck();
                                getMpesaTransactions();
                                progressDialog.cancel();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Error saving transaction "+e.getMessage());
                                Toast.makeText(MpesaFloatTracking.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();
                            }
                        });
            }
        });

        getMpesaTransactions();
    }

    private void getMpesaTransactions() {
        ProgressDialog progressDialog = new ProgressDialog(MpesaFloatTracking.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        floatTrackingList.clear();

        //First get the initial starting capital
        FirebaseFirestore.getInstance()
                .collection("FloatTracking")
                .document(datePickedAlternative)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int startingAmount = 0;
                        Map<String, Object> data = documentSnapshot.getData();

                        if(data != null)
                            startingAmount = Integer.parseInt(data.get(datePicked).toString());

                        binding.todaysStartingFloat.setText(String.valueOf(startingAmount));
                        InternalDataBase.getInstance(MpesaFloatTracking.this).addToFloatTracking(datePicked,startingAmount);
                        finalMoneyTrackingStep(startingAmount);
                    }

                    private void finalMoneyTrackingStep(int amount) {
                        //Then get all the transaction
                        // TODO: Actions if transactions can't be found
                        ArrayList<TransactionModel> transactions = InternalDataBase.getInstance(MpesaFloatTracking.this).getAllTransactions();
                        Collections.reverse(transactions);
                        for(TransactionModel t: transactions){
                            Date date = new Date(t.getTimeInMillis());
                            if(dateFormat.format(date).equals(datePicked)){
                                if(t.getPaymentMethod().equals("Mpesa")){
                                    floatTrackingList.add(t);
                                }
                            }
                        }

                        MoneyTrackerAdapter adapter = new MoneyTrackerAdapter(MpesaFloatTracking.this,floatTrackingList, amount);

                        binding.trackingMpesaRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MpesaFloatTracking.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.todaysStartingFloat.setText("0");
                        InternalDataBase.getInstance(MpesaFloatTracking.this).addToMoneyTracking(datePicked,0);
                    }
                });
    }

    private void DatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                datePicked = dayOfMonth+"/"+month+"/"+year;
                datePickedAlternative = dayOfMonth+"-"+month+"-"+year;
                binding.btnTrackingDateFloat.setText(datePicked);

                getMpesaTransactions();
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }
}