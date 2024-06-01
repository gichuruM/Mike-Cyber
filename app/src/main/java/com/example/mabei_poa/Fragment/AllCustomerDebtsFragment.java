package com.example.mabei_poa.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.CustomerDebtAdapter;
import com.example.mabei_poa.AddNewCustomer;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Interface.CustomerDebtInterface;
import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class AllCustomerDebtsFragment extends Fragment implements CustomerDebtInterface {

    public static CustomerDebtAdapter customerDebtAdapter;
    ArrayList<CustomerModel> allCustomerDebt;
    RecyclerView customerRecView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_customer_debts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addCustomer = view.findViewById(R.id.addCustomer);
        customerRecView = view.findViewById(R.id.customerRecView);
        allCustomerDebt = new ArrayList<>();

        addCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddNewCustomer.class));
            }
        });

        getCustomerDebtDate();
    }

    private void getCustomerDebtDate() {
        allCustomerDebt = InternalDataBase.getInstance(getActivity()).getAllCustomerDebts();

        if(allCustomerDebt == null){
            Toast.makeText(getActivity(), "Customer data not available", Toast.LENGTH_SHORT).show();
        } else {
            customerRecView.setHasFixedSize(true);
            customerRecView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            customerDebtAdapter = new CustomerDebtAdapter(getActivity(),allCustomerDebt, this);

            customerRecView.setAdapter(customerDebtAdapter);
            customerDebtAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void CustomerClicked(int position, ArrayList<CustomerModel> customerList) {
        CustomerModel customer = customerList.get(position);

        Intent intent = new Intent(getActivity(), AddNewCustomer.class);
        intent.putExtra("CustomerModel",customer);
        startActivity(intent);
    }
}