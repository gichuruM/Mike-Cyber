package com.example.mabei_poa.Fragment;

import static com.example.mabei_poa.Adapter.AllProductsAdapter.temporaryCartList;
import static com.example.mabei_poa.ProductsActivity.TAG;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.example.mabei_poa.Adapter.AllProductsAdapter;
import com.example.mabei_poa.AddNewProductActivity;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.HomeActivity;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.ProductsActivity;
import com.example.mabei_poa.R;
import com.example.mabei_poa.SaleToCustomerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllProductsFragment extends Fragment {

    FloatingActionButton addProduct, finishSelecting;
    RecyclerView recyclerView;
    SwitchMaterial lowStockSwitch;

    static public DatabaseReference productDBRef = FirebaseDatabase.getInstance().getReference("products");
    static public ValueEventListener productEventListener;

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

        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}