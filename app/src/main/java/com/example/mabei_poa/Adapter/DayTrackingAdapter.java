package com.example.mabei_poa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.Model.DebtTrackerSummaryModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DayTrackingAdapter extends RecyclerView.Adapter<DayTrackingAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<DebtTrackerSummaryModel> daysDebtSummary;

    public DayTrackingAdapter(Context context, ArrayList<DebtTrackerSummaryModel> daysDebtSummary) {
        this.context = context;
        this.daysDebtSummary = daysDebtSummary;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_day_debt_tracker, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DebtTrackerSummaryModel debtSummary  = daysDebtSummary.get(position);
        String time = new SimpleDateFormat("hh:mma").format(debtSummary.getDebtTrackerDate());

        holder.summaryTime.setText(time);
        holder.summaryName.setText(debtSummary.getCustomerName());
        if(debtSummary.getDebtTrackerType().equals("DEBT"))
            holder.summaryType.setText("Debt");
        else if(debtSummary.getDebtTrackerType().equals("DEBT_PAYMENT"))
            holder.summaryType.setText("Payment");
        holder.summaryAmount.setText(String.valueOf(debtSummary.getDebtTrackerAmount()));
    }

    @Override
    public int getItemCount() {
        return daysDebtSummary.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView summaryTime, summaryName, summaryType, summaryAmount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            summaryTime = itemView.findViewById(R.id.daySummaryTime);
            summaryName = itemView.findViewById(R.id.daySummaryName);
            summaryType = itemView.findViewById(R.id.daySummaryType);
            summaryAmount = itemView.findViewById(R.id.daySummaryAmount);
        }
    }
}
