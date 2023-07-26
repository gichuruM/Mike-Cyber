package com.example.mabei_poa.Fragment;

import static com.example.mabei_poa.Adapter.AllProductsAdapter.temporaryCartList;

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

import com.example.mabei_poa.Adapter.AllProductsAdapter;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.ProductsActivity;
import com.example.mabei_poa.R;
import com.example.mabei_poa.SaleToCustomerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class KitchenFragment extends Fragment {

    FloatingActionButton finishSelecting;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kitchen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        finishSelecting = view.findViewById(R.id.finishSelectingProducts);

        if(ProductsActivity.activityType.equals("Cart"))
            finishSelecting.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.kitchenRecview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        recyclerView.setHasFixedSize(true);

        ProductsActivity.productsAdapter = new AllProductsAdapter(getActivity());

        finishSelecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearing the list of all things added previously
                temporaryCartList.clear();
                startActivity(new Intent(getActivity(), SaleToCustomerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        AllProductsAdapter.currentFragment = getString(R.string.category1);
        ProductsActivity.productsAdapter.initializingFragmentArray();
        recyclerView.setAdapter(ProductsActivity.productsAdapter);
    }
}