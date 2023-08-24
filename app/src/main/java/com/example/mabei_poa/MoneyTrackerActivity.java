package com.example.mabei_poa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.mabei_poa.databinding.ActivityMoneyTrackerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MoneyTrackerActivity extends AppCompatActivity {

    ActivityMoneyTrackerBinding binding;
    String thisYear, thisMonth, today, todaysDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoneyTrackerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        thisYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        todaysDate = today+"/"+thisMonth+"/"+thisYear;

        binding.todaysDateMoneyTracking.setText(todaysDate);
        binding.btnTrackingDate.setText(todaysDate);

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
                Integer capital = Integer.parseInt(binding.todaysStartingCapital.getText().toString());
                startingCapital.put(todaysDate,capital);

                FirebaseFirestore.getInstance()
                        .collection("MoneyTracker")
                        .document(today+"-"+thisMonth+"-"+thisYear)
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
    }

    private void DatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                todaysDate = dayOfMonth+"/"+month+"/"+year;
                binding.btnTrackingDate.setText(todaysDate);
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }
}