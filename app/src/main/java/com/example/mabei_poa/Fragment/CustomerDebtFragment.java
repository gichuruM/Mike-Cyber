package com.example.mabei_poa.Fragment;

import static com.example.mabei_poa.SaleToCustomerActivity.updateCustomerDebtAdapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mabei_poa.Adapter.UpdateCustomerDebtAdapter;
import com.example.mabei_poa.R;

import java.util.ArrayList;

public class CustomerDebtFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_debt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView updateDebtRecView = view.findViewById(R.id.updateCustomerDebt);
        updateDebtRecView.setHasFixedSize(true);
        updateDebtRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateDebtRecView.setAdapter(updateCustomerDebtAdapter);
        updateCustomerDebtAdapter.notifyDataSetChanged();
    }
}