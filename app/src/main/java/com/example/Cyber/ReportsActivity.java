package com.example.Cyber;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import com.example.Cyber.Adapter.ReportDataAdapter;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.databinding.ActivityReportsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

        Objects.requireNonNull(getSupportActionBar()).hide();
        transactionReportList = new ArrayList<>();

        reportSettingType = getString(R.string.report1);

        ArrayList<String> reportType = new ArrayList<>();
        reportType.add(getString(R.string.report1));
        reportType.add(getString(R.string.report2));
        reportType.add(getString(R.string.report3));
        reportType.add(getString(R.string.report5));
        reportType.add(getString(R.string.report4));

        ArrayAdapter<String> reportTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reportType
        );

        binding.reportType.setAdapter(reportTypeAdapter);

        binding.reportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportSettingType = reportType.get(position);

                binding.reportTvType.setText(reportSettingType);
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
        ArrayList<TransactionModel> allTransactions = InternalDataBase.getInstance(this).getAllTransactions();
        Collections.reverse(allTransactions);

        double revenue = 0, profit = 0, purchases = 0, cashFlow = 0, waterLessProfit = 0;

        for(TransactionModel transaction: allTransactions){
            Date date = new Date(transaction.getTimeInMillis());

            if(dateFormat.format(date).equals(datePicked)){
                if(Objects.equals(reportSettingType, getString(R.string.report1)) &&
                    transaction.getTransactionType().equals("Sale")){
                    transactionReportList.add(transaction);
                    revenue += transaction.getTotalAmount();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report3)) &&
                        transaction.getTransactionType().equals("Sale")) {
                    transactionReportList.add(transaction);
                    profit += transaction.getProfit();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report2)) &&
                        transaction.getTransactionType().equals("Purchase")){
                    transactionReportList.add(transaction);
                    purchases += transaction.getTotalAmount();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report4))){
                    transactionReportList.add(transaction);
                    cashFlow += transaction.getTotalAmount();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report5)) &&
                        transaction.getTransactionType().equals("Sale")){
                    transactionReportList.add(transaction);
                    waterLessProfit += transaction.getWaterlessProfit();
                }
            }
        }

        ReportDataAdapter adapter = new ReportDataAdapter(transactionReportList);

        binding.reportRecView.setLayoutManager(new LinearLayoutManager(ReportsActivity.this));
        binding.reportRecView.setHasFixedSize(true);
        binding.reportRecView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(Objects.equals(reportSettingType, getString(R.string.report1)))
            binding.reportRevenue.setText(String.valueOf(revenue));
        else if(Objects.equals(reportSettingType, getString(R.string.report3)))
            binding.reportRevenue.setText(String.format("%.1f",profit));
        else if(Objects.equals(reportSettingType, getString(R.string.report2)))
            binding.reportRevenue.setText(String.valueOf(-purchases));
        else if(Objects.equals(reportSettingType, getString(R.string.report4)))
            binding.reportRevenue.setText(String.valueOf(cashFlow));
        else if(Objects.equals(reportSettingType, getString(R.string.report5)))
            binding.reportRevenue.setText(String.format("%.1f",waterLessProfit));

        progressDialog.dismiss();
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