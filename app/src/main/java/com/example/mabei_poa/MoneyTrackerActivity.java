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
import android.widget.Toast;

import com.example.mabei_poa.Adapter.MoneyTrackerAdapter;
import com.example.mabei_poa.Adapter.ReportDataAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityMoneyTrackerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoneyTrackerActivity extends AppCompatActivity {

    ActivityMoneyTrackerBinding binding;
    String thisYear, thisMonth, today, datePicked = "", datePickedAlternative = "";
    ArrayList<TransactionModel> moneyTrackerList;
    Map<String, Integer> trackingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoneyTrackerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        moneyTrackerList = new ArrayList<>();

        thisYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        datePicked = today+"/"+thisMonth+"/"+thisYear;
        datePickedAlternative = today+"-"+thisMonth+"-"+thisYear;

        binding.btnTrackingDate.setText(datePicked);

        trackingData = InternalDataBase.getInstance(this).getTrackingData();

        if(trackingData.containsKey(datePicked))
            binding.todaysStartingCapital.setText(trackingData.get(datePicked).toString());
        else {
            binding.todaysStartingCapital.setText("0");
            InternalDataBase.getInstance(this).addToMoneyTracking(datePicked,0);
        }

        binding.trackingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.trackingRecyclerView.setHasFixedSize(true);

        binding.btnTrackingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker();
            }
        });

        binding.todaysStartingCapitalSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Integer> startingCapital = new HashMap<>();

                if(binding.todaysStartingCapital.getText().toString().equals(""))
                    return;

                int capital = Integer.parseInt(binding.todaysStartingCapital.getText().toString());
                startingCapital.put(datePicked,capital);

                InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,capital);

                FirebaseFirestore.getInstance()
                        .collection("MoneyTracker")
                        .document(datePickedAlternative)
                        .set(startingCapital)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MoneyTrackerActivity.this, "New starting amount saved", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MoneyTrackerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        getTransactionsForTracking();
    }

    private void getTransactionsForTracking() {
        ProgressDialog progressDialog = new ProgressDialog(MoneyTrackerActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        moneyTrackerList.clear();

        //First get the initial starting capital
        FirebaseFirestore.getInstance()
                .collection("MoneyTracker")
                .document(datePickedAlternative)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int startingAmount = 0;
                        Map<String, Object> data = documentSnapshot.getData();

                        if(data != null)
                            startingAmount = Integer.parseInt(data.get(datePicked).toString());

                        binding.todaysStartingCapital.setText(String.valueOf(startingAmount));
                        InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,startingAmount);
                        finalMoneyTrackingStep(startingAmount);
                    }

                    private void finalMoneyTrackingStep(int amount) {
                        //Then get all the transaction
                        FirebaseFirestore.getInstance().collection("Transactions")
                                .orderBy("time", Query.Direction.ASCENDING)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();

                                        for(DocumentSnapshot ds: documentSnapshots){
                                            TransactionModel transaction = ds.toObject(TransactionModel.class);
                                            if(dateFormat.format(transaction.getTime()).equals(datePicked))
                                                moneyTrackerList.add(transaction);
                                        }
                                        //Log.d(TAG, "onSuccess: size "+transactionReportList.size());
                                        MoneyTrackerAdapter adapter = new MoneyTrackerAdapter(MoneyTrackerActivity.this,moneyTrackerList, amount);

                                        binding.trackingRecyclerView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();

                                        progressDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MoneyTrackerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onFailure: Report transactions not found");
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MoneyTrackerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.todaysStartingCapital.setText("0");
                        InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,0);
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
                binding.btnTrackingDate.setText(datePicked);

                getTransactionsForTracking();
//                if(trackingData.containsKey(datePicked)){
//                    Log.d(TAG, "onDateSet: Internal memory");
//                    binding.todaysStartingCapital.setText(trackingData.get(datePicked).toString());
//                }
//                else {
//                    Log.d(TAG, "onDateSet: External memory");
//                    String date = dayOfMonth +"-"+ month +"-"+year;
//                    String altDate = dayOfMonth+"/"+month+"/"+year;
//                    FirebaseFirestore.getInstance()
//                            .collection("MoneyTracker")
//                            .document(date)
//                            .get()
//                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    if(documentSnapshot.exists()){
//                                        Map<String, Object> data = documentSnapshot.getData();
//
//                                        binding.todaysStartingCapital.setText(data.get(altDate).toString());
//                                        InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,Integer.parseInt(data.get(altDate).toString()));
//                                    } else {
//                                        Log.d(TAG, "onSuccess: document doesn't exist");
//                                        binding.todaysStartingCapital.setText("1500");
//                                        InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,1500);
//                                    }
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(MoneyTrackerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    binding.todaysStartingCapital.setText("1500");
//                                    InternalDataBase.getInstance(MoneyTrackerActivity.this).addToMoneyTracking(datePicked,1500);
//                                }
//                            });
//                }
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }
}