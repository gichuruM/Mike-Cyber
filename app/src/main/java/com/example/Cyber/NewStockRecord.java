package com.example.Cyber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.example.Cyber.Adapter.StockAdjustmentAdapter;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.StockAdjustmentModel;
import com.example.Cyber.databinding.ActivityCommissionBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewStockRecord extends AppCompatActivity {

    ActivityCommissionBinding binding;
    private ProductModel selectedProduct;
    private ArrayList<ProductModel> productList;
    private ArrayAdapter<String> productAdapter;

    // RecyclerView components
    private RecyclerView stockHistoryRecyclerView;
    private StockAdjustmentAdapter stockAdjustmentAdapter;
    private ArrayList<StockAdjustmentModel> stockAdjustmentList;
    private ValueEventListener stockHistoryListener;
    private Query stockHistoryQuery;
    private Switch filterSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the product list and stock adjustment list
        productList = new ArrayList<>();
        stockAdjustmentList = new ArrayList<>();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up the product selection spinner
        setupProductSpinner();

        // Load products from the database
        loadProducts();

        // Initialize the Switch
        filterSwitch = binding.filterSwitch;

        filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadStockAdjustmentHistory();
        });

        // Handle the submission of new stock
        binding.submitButton.setOnClickListener(v -> recordNewStock());
    }

    private void setupRecyclerView() {
        stockHistoryRecyclerView = binding.stockHistoryRecyclerView;
        stockHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockAdjustmentAdapter = new StockAdjustmentAdapter(stockAdjustmentList);
        stockHistoryRecyclerView.setAdapter(stockAdjustmentAdapter);
    }

    private void loadProducts() {
        // Fetch products from the database and populate productList
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                List<String> productNames = new ArrayList<>();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    ProductModel product = productSnapshot.getValue(ProductModel.class);
                    productList.add(product);
                }

                // Sort the product list alphabetically by name
                Collections.sort(productList, new Comparator<ProductModel>() {
                    @Override
                    public int compare(ProductModel p1, ProductModel p2) {
                        return p1.getName().compareToIgnoreCase(p2.getName());
                    }
                });

                // Update the product names list after sorting
                for (ProductModel product : productList) {
                    productNames.add(product.getName());
                }

                productAdapter.clear();
                productAdapter.addAll(productNames);
                productAdapter.notifyDataSetChanged();

                // Set default selection
                if (!productList.isEmpty()) {
                    selectedProduct = productList.get(0);
                    binding.productSpinner.setSelection(0);
                    // Manually call onItemSelected to initialize
                    onProductSelected(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewStockRecord.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onProductSelected(int position) {
        selectedProduct = productList.get(position);
        // Display current prices
        binding.buyingPriceInput.setText(String.valueOf(selectedProduct.getPurchasePrice()));
        binding.sellingPriceInput.setText(String.valueOf(selectedProduct.getSellingPrice()));
        // Display current stock
        binding.oldStockValue.setText(String.valueOf(selectedProduct.getQuantity()));
        // Reload stock adjustment history if filter is on
        if (filterSwitch.isChecked()) {
            loadStockAdjustmentHistory();
        }
    }

    private void setupProductSpinner() {
        productAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>()
        );
        binding.productSpinner.setAdapter(productAdapter);
        binding.productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                onProductSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedProduct = null;
                binding.oldStockValue.setText("0");
            }
        });
    }

    private void recordNewStock() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product.", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityStr = binding.quantityInput.getText().toString().trim();
        String buyingPriceStr = binding.buyingPriceInput.getText().toString().trim();
        String sellingPriceStr = binding.sellingPriceInput.getText().toString().trim();

        if (quantityStr.isEmpty() || buyingPriceStr.isEmpty() || sellingPriceStr.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        double newQuantity;
        double newBuyingPrice;
        double newSellingPrice;

        try {
            newQuantity = Double.parseDouble(quantityStr);
            newBuyingPrice = Double.parseDouble(buyingPriceStr);
            newSellingPrice = Double.parseDouble(sellingPriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input entered.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the product's quantity and prices
        double quantityOld = selectedProduct.getQuantity();
        double updatedQuantity = quantityOld + newQuantity;

        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("products")
                .child(selectedProduct.getId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", updatedQuantity);
        updates.put("purchasePrice", newBuyingPrice);
        updates.put("sellingPrice", newSellingPrice);

        productRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Stock and prices updated successfully.", Toast.LENGTH_SHORT).show();
                    // Update the old stock display
                    binding.oldStockValue.setText(String.valueOf(updatedQuantity));
                    // Update the selectedProduct's quantity and prices
                    selectedProduct.setQuantity(updatedQuantity);
                    selectedProduct.setPurchasePrice(newBuyingPrice);
                    selectedProduct.setSellingPrice(newSellingPrice);
                    // Record this transaction
                    recordStockTransaction(selectedProduct.getId(), selectedProduct.getName(), quantityOld, newQuantity, newBuyingPrice, newSellingPrice);
                    // Reset input fields
                    binding.quantityInput.setText("");
                    // Reload stock adjustment history
                    loadStockAdjustmentHistory();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update stock.", Toast.LENGTH_SHORT).show();
                });
    }

    private void recordStockTransaction(String productId, String productName, double quantityOld, double quantityAdded, double buyingPrice, double sellingPrice) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("stock_transactions").push();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("productId", productId);
        transactionData.put("productName", productName);
        transactionData.put("quantityOld", quantityOld);
        transactionData.put("quantityAdded", quantityAdded);
        transactionData.put("newBuyingPrice", buyingPrice);
        transactionData.put("newSellingPrice", sellingPrice);
        transactionData.put("timestamp", ServerValue.TIMESTAMP);

        transactionRef.setValue(transactionData)
                .addOnSuccessListener(aVoid -> {
                    // Transaction recorded successfully
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void loadStockAdjustmentHistory() {
        if (stockHistoryListener != null && stockHistoryQuery != null) {
            stockHistoryQuery.removeEventListener(stockHistoryListener);
        }

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
                .getReference("stock_transactions");
        if (filterSwitch.isChecked() && selectedProduct != null) {
            // Filter by selected product
            stockHistoryQuery = transactionsRef.orderByChild("productId").equalTo(selectedProduct.getId());
        } else {
            // Load all transactions
            stockHistoryQuery = transactionsRef.orderByChild("timestamp");
        }

        stockHistoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stockAdjustmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    StockAdjustmentModel adjustment = dataSnapshot.getValue(StockAdjustmentModel.class);
                    stockAdjustmentList.add(adjustment);
                }
                // Sort adjustments by timestamp descending
                Collections.sort(stockAdjustmentList, new Comparator<StockAdjustmentModel>() {
                    @Override
                    public int compare(StockAdjustmentModel o1, StockAdjustmentModel o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });

                stockAdjustmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewStockRecord.this, "Failed to load stock history.", Toast.LENGTH_SHORT).show();
            }
        };

        stockHistoryQuery.addValueEventListener(stockHistoryListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStockAdjustmentHistory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stockHistoryListener != null && stockHistoryQuery != null) {
            stockHistoryQuery.removeEventListener(stockHistoryListener);
        }
    }
}
