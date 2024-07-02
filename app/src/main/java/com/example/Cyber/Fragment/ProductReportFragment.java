package com.example.Cyber.Fragment;

import static com.example.Cyber.ReportsActivity.reportProductAdapter;
import static com.example.Cyber.ReportsActivity.transactionAdapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Cyber.R;

public class ProductReportFragment extends Fragment {

    RecyclerView transactionReportProductRecView, productReportRecView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionReportProductRecView = view.findViewById(R.id.transactionReportProductRecView);
        productReportRecView = view.findViewById(R.id.productReportRecView);

        transactionReportProductRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionReportProductRecView.setHasFixedSize(true);
        productReportRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        productReportRecView.setHasFixedSize(true);

        transactionReportProductRecView.setAdapter(transactionAdapter);
        productReportRecView.setAdapter(reportProductAdapter);

        reportProductAdapter.notifyDataSetChanged();
        transactionAdapter.notifyDataSetChanged();
    }
}