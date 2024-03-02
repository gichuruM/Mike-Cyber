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
import java.util.Objects;

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
                Log.d(TAG, "afterTextChanged: *----------*");
                Log.d(TAG, "afterTextChanged: scanResult "+scanResult);
                Log.d(TAG, "afterTextChanged: previous "+previousString);
                if(scanResult.equals(previousString)){
                    Log.d(TAG, "afterTextChanged: passed");
                    if(!Objects.equals(scanFirstDigit, "")){
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

        if (!scannedBarcode.matches("\\d+")){
            Toast.makeText(this, "Invalid scan code!", Toast.LENGTH_SHORT).show();
            return;
        }


        //To take care of situations where the barcode starts with 0
        long barcode = Long.parseLong(scannedBarcode);

        boolean restricted = false;
        //Allowed Products: Brown bread BB, Bread BB, Bread half BB, Bread Tpremium, Bread Tosha, stars
        //Cakes za 5, Cakes, Airtel airtime, Safaricom airtime, njugu, crips, Elliots
        String[] allowedProducts = {
                "bd29755d-b6bc-4b9f-9e62-7dabeaff086b", "bfbcb65e-0245-4705-8598-dca56550aa99",
                "1dadc5ab-27df-4d33-b949-8488d4651611", "010c8495-2737-4467-9d1b-e9c17df48266",
                "211d4583-5286-49be-a39e-2d990f9128c6", "01d8c835-3e9a-4ea8-ad45-2eb50bb2331c",
                "71416c57-b1f3-4034-bac5-6eb3c166e262", "bd945ac5-b3c4-45f4-8383-606eb03cda3f",
                "0df2787c-f9c3-452a-bed5-1037c4f5ab4c", "656abdc4-6df7-4cd1-b19d-01bfa9450f2c",
                "6af31eb4-561a-4275-8402-8095a67045a9", "c1c36681-ad6b-4a8c-8022-ced0ba96b417",
                "4cf81390-7e76-47de-be26-a2cedaf56dd1", "815d8478-57e8-46e6-b454-b5b4089f6ebf",
                "b49afbc6-e840-4109-a054-c6b527d7d950", "c4bf90d8-ff7f-4843-b842-66cf19caf74d",
                "15c348c2-3c48-4559-9da8-9764850cd4b7", "5fcd47bf-bc34-4ddc-8d0d-1142f0b897e7",
                "0a7ff1d9-b32a-4d26-97e2-7728c2bcf4d1", "92ca2317-8e88-44e7-ae1f-bbfe4529b123",
                "67173dc1-78fd-4429-b204-afdf1a4ddda2", "9c612bef-60aa-40c7-9978-345134eca29a",
                "07d98efc-cfff-4488-90ea-a41d22775b96", "8d91e74a-db72-41c5-8b9d-5b5b699ef561",
                "e684168c-d602-49a7-bae2-bde7e0eece44", "95d2e56a-d266-4c68-9400-3b0c0ab1fd4c"};

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
                                    Toast.makeText(this, "New quantity "+productBarcodes.get(key), Toast.LENGTH_LONG).show();
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