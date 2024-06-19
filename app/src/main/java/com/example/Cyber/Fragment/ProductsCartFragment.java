package com.example.Cyber.Fragment;

import static com.example.Cyber.SaleToCustomerActivity.cartAdapter;

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

public class ProductsCartFragment extends Fragment {

    public static RecyclerView cartRecView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartRecView = view.findViewById(R.id.cartRecView);
        cartRecView.setHasFixedSize(true);
        cartRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        cartRecView.setAdapter(cartAdapter);
    }
}