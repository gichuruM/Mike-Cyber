package com.example.mabei_poa.Fragment;

import static com.example.mabei_poa.Adapter.AllProductsAdapter.temporaryCartList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.AllProductsAdapter;
import com.example.mabei_poa.AddNewProductActivity;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.ProductsActivity;
import com.example.mabei_poa.R;
import com.example.mabei_poa.SaleToCustomerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllProductsFragment extends Fragment {

    FloatingActionButton addProduct, finishSelecting;
    RecyclerView recyclerView;
    ArrayList<ProductModel> allProductsList;
    SwitchMaterial lowStockSwitch;

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

        //Stock switch has been turned on or off
        lowStockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataInitialization(isChecked);
            }
        });

        if(ProductsActivity.activityType.equals("Cart")){
            finishSelecting.setVisibility(View.VISIBLE);
            addProduct.setVisibility(View.GONE);
            lowStockSwitch.setVisibility(View.GONE);
        } else if(ProductsActivity.activityType.equals("Default")){
            finishSelecting.setVisibility(View.GONE);
            addProduct.setVisibility(View.VISIBLE);
            lowStockSwitch.setVisibility(View.VISIBLE);
        }
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
                dataInitialization(lowStockSwitch.isChecked());
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
        allProductsList = new ArrayList<>();

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("products");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore.getInstance().collection("products")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();

                        for(DocumentSnapshot ds: dsList){
                            ProductModel product = ds.toObject(ProductModel.class);
                            if(!lowStock)
                                allProductsList.add(product);
                            else if(lowStock && product.getQuantity() <= product.getLowStockAlert())
                                allProductsList.add(product);
                        }

                        ProductsActivity.productsAdapter = new AllProductsAdapter(getActivity());

                        AllProductsAdapter.fullProductModelArrayList.clear();
                        AllProductsAdapter.fullProductModelArrayList.addAll(allProductsList);
                        //updating sharedPref arrayList
                        InternalDataBase.getInstance(getActivity()).batchAdditionToAllProducts(allProductsList);

                        AllProductsAdapter.currentFragment = getString(R.string.category0);
                        ProductsActivity.productsAdapter.initializingFragmentArray();
                        
                        recyclerView.setAdapter(ProductsActivity.productsAdapter);
                        ProductsActivity.productsAdapter.notifyDataSetChanged();

                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });
    }
}