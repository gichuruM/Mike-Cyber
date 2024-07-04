package com.example.Cyber.Fragment;

import static com.example.Cyber.ReportsActivity.productSummaryAdapter;

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

public class ProductSummaryFragment extends Fragment {

    RecyclerView reportProductRanking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportProductRanking = view.findViewById(R.id.reportProductRanking);
        reportProductRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        reportProductRanking.setHasFixedSize(true);

        reportProductRanking.setAdapter(productSummaryAdapter);

        productSummaryAdapter.notifyDataSetChanged();
    }
}