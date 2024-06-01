package com.example.mabei_poa.Fragment;

import static com.example.mabei_poa.HomeActivity.transactionDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.mabei_poa.Adapter.DayTrackingAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.DebtTrackerSummaryModel;
import com.example.mabei_poa.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DayDebtTrackingFragment extends Fragment {

    String thisYear, thisMonth, today, datePicked = "";
    TextView daysDepts, daysPayments, daysDifference;
    Button btnPickDate;
    RecyclerView debtSummaryRecView;
    ArrayList<DebtTrackerSummaryModel> allDaysDebts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_debt_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnPickDate = view.findViewById(R.id.btnDebtTrackingPickDate);
        debtSummaryRecView = view.findViewById(R.id.recViewDayDebtTracking);
        daysDepts = view.findViewById(R.id.daysDepts);
        daysPayments = view.findViewById(R.id.daysPayments);
        daysDifference = view.findViewById(R.id.daysDifference);

        thisYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        datePicked = today+"/"+thisMonth+"/"+thisYear;
        btnPickDate.setText(datePicked);

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker();
            }
        });

        debtSummaryRecView.setHasFixedSize(true);
        debtSummaryRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        allDaysDebts = InternalDataBase.getInstance(getActivity()).getDaysDebtSummary();
        Collections.reverse(allDaysDebts);
        getDebtSummariesForTracking();
        deleteOldDebtsTracks();
    }

    private void deleteOldDebtsTracks(){
       //TODO: Delete old Debts for tracking
    }

    private void DatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                datePicked = dayOfMonth+"/"+month+"/"+year;
                btnPickDate.setText(datePicked);

                getDebtSummariesForTracking();
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }

    private void getDebtSummariesForTracking() {
        int totalDebts = 0, totalPayments = 0;
        ArrayList<DebtTrackerSummaryModel> selectedDayDebt = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        for(DebtTrackerSummaryModel d: allDaysDebts){
            String allDebtsDate = dateFormat.format(d.getDebtTrackerDate());

            if(allDebtsDate.equals(datePicked)){
                selectedDayDebt.add(d);
                if(d.getDebtTrackerType().equals("DEBT"))
                    totalDebts += d.getDebtTrackerAmount();
                else if(d.getDebtTrackerType().equals("DEBT_PAYMENT"))
                    totalPayments +=  d.getDebtTrackerAmount();
            }
        }

        daysDepts.setText(String.valueOf(totalDebts));
        daysPayments.setText(String.valueOf(totalPayments));
        daysDifference.setText(String.valueOf(totalDebts-totalPayments));
        DayTrackingAdapter dayDebtAdapter = new DayTrackingAdapter(getActivity(), selectedDayDebt);
        debtSummaryRecView.setAdapter(dayDebtAdapter);
        dayDebtAdapter.notifyDataSetChanged();
    }
}