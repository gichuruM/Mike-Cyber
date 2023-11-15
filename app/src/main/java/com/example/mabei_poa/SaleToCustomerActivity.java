package com.example.mabei_poa;

import static com.example.mabei_poa.Adapter.AllProductsAdapter.fullProductModelArrayList;
import static com.example.mabei_poa.Adapter.CartAdapter.scanFirstDigit;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.CartAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Interface.CartItemClickedInterface;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.databinding.ActivitySaleToCustomerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.grpc.Internal;

public class SaleToCustomerActivity extends AppCompatActivity implements CartItemClickedInterface{

    static ActivitySaleToCustomerBinding binding;
    public static CartAdapter cartAdapter;
    public static ArrayList<CartModel> cartProductsList = new ArrayList<>();
    static double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaleToCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        String dateTime = new SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.getDefault()).format(new Date());
        binding.cartDateTime.setText(dateTime);

        binding.cartRecView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, this, binding.barcodeScanResults);
        binding.cartRecView.setHasFixedSize(true);
        binding.cartRecView.setAdapter(cartAdapter);

        binding.addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SaleToCustomerActivity.this,ProductsActivity.class).putExtra("type","Cart"));
            }
        });

        binding.goToCheckOutPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SaleToCustomerActivity.this,CheckOutActivity.class));
            }
        });

        binding.addProductScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCartScanning();
            }
        });

        binding.barcodeScanResults.requestFocus();

        binding.barcodeScanResults.addTextChangedListener(new TextWatcher() {

            String previousString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) return;

                String scanResult = s.toString().trim();

                if(scanResult.equals(previousString)){

                    if(scanFirstDigit != ""){
                        String newPreviousString = scanFirstDigit+previousString;
                        scanFirstDigit = "";
                        prepareScanningResults(newPreviousString);
                    } else
                        prepareScanningResults(previousString);
                    binding.barcodeScanResults.setText("");
                }

                previousString = scanResult;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //give an alert before clearing cart
        if(cartProductsList.size() > 0){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Warning");
            alert.setMessage("All products in the cart will be lost!");

            alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SaleToCustomerActivity.super.onBackPressed();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

            alert.show();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(InternalDataBase.getInstance(this).getCart().size() != cartProductsList.size()){
            Toast.makeText(this, "Products would have been lost!", Toast.LENGTH_SHORT).show();
            cartProductsList = InternalDataBase.getInstance(this).getCart();
            cartAdapter.notifyDataSetChanged();
        }
    }

    public static void totalCartAmount(){
        TextView totalCartAmount;

        totalAmount = 0;
        for(CartModel c: cartProductsList){
            totalAmount += c.getProductTotal();
        }

        binding.totalCartAmount.setText(String.valueOf(totalAmount));
    }

    private void AddCartScanning(){
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Volume up to flash");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(ScanActivity.class);
        barLauncher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result ->{
       if(result.getContents()!=null){
           prepareScanningResults(result.getContents());
       }
    });

    private void prepareScanningResults(String scannedBarcode){
        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(SaleToCustomerActivity.this).getAllProducts();

        if(allProducts == null || allProducts.size() <= 0){
            ProgressDialog progressDialog = new ProgressDialog(SaleToCustomerActivity.this);
            progressDialog.setTitle("Loading...");
            progressDialog.setMessage("Products");
            progressDialog.show();

            FirebaseFirestore.getInstance().collection("products")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();

                            for(DocumentSnapshot ds: dsList){
                                ProductModel product = ds.toObject(ProductModel.class);
                                fullProductModelArrayList.add(product);
                            }
                            //updating sharedPref arrayList
                            InternalDataBase.getInstance(SaleToCustomerActivity.this).batchAdditionToAllProducts(fullProductModelArrayList);
                            //Log.d(TAG, "onSuccess: Finding all products");
                            progressDialog.dismiss();
                            scanningResults(scannedBarcode, fullProductModelArrayList);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SaleToCustomerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        } else
            scanningResults(scannedBarcode, allProducts);
    }

    private void scanningResults(String scannedBarcode, ArrayList<ProductModel> scanningArray){
        //To take care of situations where the barcode starts with 0
        long barcode = Long.parseLong(scannedBarcode);

        boolean restricted = false;
        //Allowed Products: Brown bread BB, Bread BB, Bread half BB, Bread Tpremium, Bread Tosha, stars
        String[] allowedProducts = {
                "bd29755d-b6bc-4b9f-9e62-7dabeaff086b", "bfbcb65e-0245-4705-8598-dca56550aa99",
                "1dadc5ab-27df-4d33-b949-8488d4651611", "010c8495-2737-4467-9d1b-e9c17df48266",
                "211d4583-5286-49be-a39e-2d990f9128c6", "01d8c835-3e9a-4ea8-ad45-2eb50bb2331c"};

        if(cartProductsList.size() > 0){
            for(ProductModel p: scanningArray){
                Map<String, Double> productBarcodes = p.getBarcodes();

                for(String key: productBarcodes.keySet()){
                    if(scannedBarcode.equals(key) || barcode == Long.parseLong(key)){

                        //checking if it's a vendor purchase and if the product is restricted
                        if(InternalDataBase.getInstance(SaleToCustomerActivity.this).getCartType().equals("Purchase")){
                            restricted = true;
                            for(int i = 0; i < allowedProducts.length; i++){
                                if(p.getId().equals(allowedProducts[i])){
                                    restricted = false;
                                    break;
                                }
                            }
                        }

                        if(!restricted){
                            boolean inCart = false;

                            int pos = -1;
                            for(CartModel c: cartProductsList){
                                pos++;
                                if(c.getProductId().equals(p.getId())){
                                    double quantity = c.getQuantity() + productBarcodes.get(key);
                                    Toast.makeText(this, "Product already in cart quantity "+quantity, Toast.LENGTH_SHORT).show();
                                    cartProductsList.remove(cartProductsList.get(pos));
                                    cartAdapter.notifyItemRemoved(pos);

                                    CartModel cartModel = new CartModel(p.getId(),quantity,p.getSellingPrice()*quantity);
                                    cartProductsList.add(pos, cartModel);
                                    cartAdapter.notifyItemInserted(cartProductsList.indexOf(cartModel));
                                    binding.cartRecView.scrollToPosition(cartProductsList.indexOf(cartModel));
                                    inCart = true;
                                    break;
                                }
                            }

                            if(!inCart){
                                CartModel cartModel = new CartModel(p.getId(),productBarcodes.get(key),p.getSellingPrice());
                                cartProductsList.add(cartModel);
                                cartAdapter.notifyItemInserted(cartProductsList.indexOf(cartModel));
                                binding.cartRecView.scrollToPosition(cartProductsList.indexOf(cartModel));
                            }
                            InternalDataBase.getInstance(this).setNewCart(cartProductsList);
                        } else {
                            Toast.makeText(SaleToCustomerActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else {
            for(ProductModel p: scanningArray) {
                Map<String, Double> productBarcodes = p.getBarcodes();

                for(String key: productBarcodes.keySet()){
                    if(scannedBarcode.equals(key) || barcode == Long.parseLong(key)){

                        //checking if it's a vendor purchase and if the product is restricted
                        if(InternalDataBase.getInstance(SaleToCustomerActivity.this).getCartType().equals("Purchase")){
                            restricted = true;
                            for(int i = 0; i < allowedProducts.length; i++){
                                if(p.getId().equals(allowedProducts[i])){
                                    restricted = false;
                                    break;
                                }
                            }
                        }

                        if(!restricted){
                            CartModel cartModel = new CartModel(p.getId(),productBarcodes.get(key),p.getSellingPrice());
                            cartProductsList.add(cartModel);
                            InternalDataBase.getInstance(this).setNewCart(cartProductsList);
                            cartAdapter.notifyItemInserted(cartProductsList.indexOf(cartModel));
                        } else {
                            Toast.makeText(SaleToCustomerActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        cartProductsList.remove(cartProductsList.get(position));
        InternalDataBase.getInstance(this).setNewCart(cartProductsList);
        cartAdapter.notifyItemRemoved(position);
        totalCartAmount();
    }

    @Override
    public void onTextChange(int position, String text, TextView view) {
        if(text.equals("")) return;
        if(text.equals(".")) return;
        //Log.i(TAG, "onTextChange: text changing "+text);
        CartModel cartModel = cartProductsList.get(position);

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(SaleToCustomerActivity.this).getAllProducts();

        boolean found = false;

        for(ProductModel p: allProducts){
            if(cartModel.getProductId().equals(p.getId())){
                found = true;
                cartModel.setQuantity(Double.parseDouble(text));
                long productTotal = 0;
                if(InternalDataBase.getInstance(this).getCartType().equals("Purchase"))
                    productTotal = Math.round(cartModel.getQuantity()*p.getPurchasePrice());
                else if(InternalDataBase.getInstance(this).getCartType().equals("Sale")){
                    productTotal = Math.round(cartModel.getQuantity()*p.getSellingPrice());

                    //Rounding up the total to the nearest multiple of 5
                    int remainder = (int) (productTotal % 5);
                    if(remainder != 0)
                        productTotal += (5 - remainder);
                }

                cartModel.setProductTotal((double) productTotal);
                view.setText(String.valueOf(productTotal));
                totalCartAmount();
            }
        }

        if(!found){
            Toast.makeText(this, "Quantity adjustment NOT successful", Toast.LENGTH_SHORT).show();
        }
    }
}