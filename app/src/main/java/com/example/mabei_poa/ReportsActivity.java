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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.ReportDataAdapter;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityReportsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    ActivityReportsBinding binding;
    String thisYear, thisMonth, today, datePicked = "";
    ArrayList<TransactionModel> transactionReportList;
    
    public static String reportSettingType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        transactionReportList = new ArrayList<>();

        reportSettingType = getString(R.string.report1);

        ArrayList<String> reportType = new ArrayList<>();
        reportType.add(getString(R.string.report1));
        reportType.add(getString(R.string.report2));
        reportType.add(getString(R.string.report3));

        ArrayList<String> reportDuration = new ArrayList<>();
        reportDuration.add(getString(R.string.reportDuration1));
        reportDuration.add(getString(R.string.reportDuration2));
        reportDuration.add(getString(R.string.reportDuration3));

        ArrayAdapter<String> reportTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reportType
        );

        ArrayAdapter<String> reportDurationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reportDuration
        );

        binding.reportType.setAdapter(reportTypeAdapter);
        binding.reportDuration.setAdapter(reportDurationAdapter);

        binding.reportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportSettingType = reportType.get(position);
                if(reportSettingType == getString(R.string.report1))
                    binding.reportTvType.setText("Revenue");
                else if(reportSettingType == getString(R.string.report3))
                    binding.reportTvType.setText("Profit");
                else if(reportSettingType == getString(R.string.report2))
                    binding.reportTvType.setText("Purchase");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        thisYear = new SimpleDateFormat("yyyy",Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        datePicked = today+"/"+thisMonth+"/"+thisYear;

        binding.btnReportPickDate.setText(datePicked);

        binding.btnReportPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker();
            }
        });

        binding.reportSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTransactions();
            }
        });
    }

    private void getTransactions() {

        ProgressDialog progressDialog = new ProgressDialog(ReportsActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        transactionReportList.clear();
        FirebaseFirestore.getInstance().collection("Transactions")
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        double revenue = 0, profit = 0;

                        for(DocumentSnapshot ds: documentSnapshots){
                            TransactionModel transaction = ds.toObject(TransactionModel.class);
                            if(dateFormat.format(transaction.getTime()).equals(datePicked)){
                                transactionReportList.add(transaction);
                                if(reportSettingType == getString(R.string.report1))
                                    revenue += transaction.getTotalAmount();
                                else if(reportSettingType == getString(R.string.report3)) {
                                    //additions of profit
                                    profit += transaction.getProfit();
                                }
                            }
                        }
                        //Log.d(TAG, "onSuccess: size "+transactionReportList.size());
                        ReportDataAdapter adapter = new ReportDataAdapter(transactionReportList);

                        binding.reportRecView.setLayoutManager(new LinearLayoutManager(ReportsActivity.this));
                        binding.reportRecView.setHasFixedSize(true);
                        binding.reportRecView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        if(reportSettingType == getString(R.string.report1))
                            binding.reportRevenue.setText(String.valueOf(revenue));
                        else if(reportSettingType == getString(R.string.report3)) {
                            binding.reportRevenue.setText(String.format("%.1f",profit));
                        }

                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: Report transactions not found");
                        progressDialog.dismiss();
                    }
                });
    }

    private void DatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                datePicked = dayOfMonth+"/"+month+"/"+year;
                binding.btnReportPickDate.setText(datePicked);
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }
}