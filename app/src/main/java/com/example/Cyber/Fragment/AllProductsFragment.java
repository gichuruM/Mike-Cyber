package com.example.Cyber.Fragment;

import static com.example.Cyber.Adapter.AllProductsAdapter.temporaryCartList;
import static com.example.Cyber.HomeActivity.productDBRef;
import static com.example.Cyber.ProductsActivity.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.Cyber.Adapter.AllProductsAdapter;
import com.example.Cyber.AddNewProductActivity;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.ProductsActivity;
import com.example.Cyber.R;
import com.example.Cyber.SaleToCustomerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllProductsFragment extends Fragment {

    FloatingActionButton addProduct, finishSelecting;
    RecyclerView recyclerView;
    SwitchMaterial lowStockSwitch;
    int scrollPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addProduct = view.findViewById(R.id.addProduct);
        finishSelecting = view.findViewById(R.id.finishSelectingProducts);

        recyclerView = view.findViewById(R.id.allProductRecview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        recyclerView.setHasFixedSize(true);

        lowStockSwitch = view.findViewById(R.id.lowStockSwitch);

        //Functions to scroll to the previously corrected product
        if(getArguments() != null){
            String productId = getArguments().getString("productId");
            ArrayList<ProductModel> allProductsList = InternalDataBase.getInstance(getActivity()).getAllProducts();

            for(ProductModel p: allProductsList){
                if(p.getId().equals(productId)){
                    scrollPosition = allProductsList.indexOf(p);
                    break;
                }
            }
        }

        if(ProductsActivity.activityType.equals("Cart")){
            finishSelecting.setVisibility(View.VISIBLE);
            addProduct.setVisibility(View.GONE);
            lowStockSwitch.setVisibility(View.GONE);
        } else if(ProductsActivity.activityType.equals("Default")){
            finishSelecting.setVisibility(View.GONE);
            addProduct.setVisibility(View.VISIBLE);
            lowStockSwitch.setVisibility(View.VISIBLE);
        }

        //Stock switch has been turned on or off
        lowStockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataInitialization(isChecked);
            }
        });

        //Log.d(TAG, "onViewCreated: on creation tempsize "+temporaryCartList.size());
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddNewProductActivity.class));
            }
        });

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
        if(ProductsActivity.activityType.equals("Default")){
            dataInitialization(lowStockSwitch.isChecked());
        } else if(ProductsActivity.activityType.equals("Cart")){
            ArrayList<ProductModel> productsList = InternalDataBase.getInstance(getActivity()).getAllProducts();

            if(productsList == null)
                dataInitialization(false);
            else {
                ProductsActivity.productsAdapter = new AllProductsAdapter(getActivity());

                AllProductsAdapter.fullProductModelArrayList.clear();
                AllProductsAdapter.fullProductModelArrayList.addAll(productsList);

                AllProductsAdapter.currentFragment = getString(R.string.category0);
                ProductsActivity.productsAdapter.initializingFragmentArray();

                recyclerView.setAdapter(ProductsActivity.productsAdapter);
            }
        }
    }

    private void dataInitialization(boolean lowStock) {
        boolean updateInternalDB = true;
        ArrayList<ProductModel> allProductsList = InternalDataBase.getInstance(getActivity()).getAllProducts();

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("products");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(allProductsList.isEmpty())
            updateAllProducts(progressDialog);
        else
            updatingUIWithProducts(progressDialog,allProductsList);
    }

    public void updateAllProducts(ProgressDialog progressDialog) {
        Log.d(TAG, "updateAllProducts: single value event listener triggered");
        productDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ProductModel> updatedAllProducts = new ArrayList<>();

                for(DataSnapshot snap: snapshot.getChildren()){
                    ProductModel product = snap.getValue(ProductModel.class);

                    if(product != null)
                        updatedAllProducts.add(product);
                }

                InternalDataBase.getInstance(getActivity()).batchAdditionToAllProducts(updatedAllProducts);
                updatingUIWithProducts(progressDialog,updatedAllProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatingUIWithProducts(ProgressDialog progressDialog, ArrayList<ProductModel> allProducts){

        ArrayList<ProductModel> filteredProductList = new ArrayList<>();

        for(ProductModel p: allProducts){
            if(!lowStockSwitch.isChecked())
                filteredProductList.add(p);
            else if(p.getQuantity() <= p.getLowStockAlert())
                filteredProductList.add(p);
        }

        ProductsActivity.productsAdapter = new AllProductsAdapter(getActivity());

        AllProductsAdapter.fullProductModelArrayList.clear();
        AllProductsAdapter.fullProductModelArrayList.addAll(filteredProductList);

        AllProductsAdapter.currentFragment = "All";
        ProductsActivity.productsAdapter.initializingFragmentArray();

        recyclerView.setAdapter(ProductsActivity.productsAdapter);
        ProductsActivity.productsAdapter.notifyDataSetChanged();

        if(scrollPosition != 0){
            recyclerView.scrollToPosition(scrollPosition);
            scrollPosition = 0;
        }

        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}