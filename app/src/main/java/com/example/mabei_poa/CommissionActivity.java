package com.example.mabei_poa;

import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.example.mabei_poa.Adapter.ReportDataAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityCommissionBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CommissionActivity extends AppCompatActivity {

    ActivityCommissionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getTransactions();
    }

    private void getTransactions() {

        ArrayList<TransactionModel> transactionReportList = new ArrayList<>();

        ProgressDialog progressDialog = new ProgressDialog(CommissionActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        String datePicked = new SimpleDateFormat("d", Locale.getDefault()).format(new Date())+"/"+
                new SimpleDateFormat("M", Locale.getDefault()).format(new Date())+"/"+
                new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

        ArrayList<TransactionModel> allTransactions = InternalDataBase.getInstance(this).getAllTransactions();
        Collections.reverse(allTransactions);

        double revenue = 0;

        for(TransactionModel transaction: allTransactions){
            Date date = new Date(transaction.getTimeInMillis());

            if(dateFormat.format(date).equals(datePicked)){
                if(transaction.getTransactionType().equals("Sale")){
                    transactionReportList.add(transaction);
                    revenue += transaction.getTotalAmount();
                }
            }
        }

        progressDialog.cancel();
        double commission = (revenue * 110)/10000;

        //Rounding up the total to the nearest multiple of 5
        int remainder = (int) (commission % 5);
        if (remainder != 0)
            commission -= remainder;

        binding.commissionAmount.setText(String.valueOf(commission));
    }
}