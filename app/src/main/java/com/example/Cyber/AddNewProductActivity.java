package com.example.Cyber;

import static com.example.Cyber.HomeActivity.productDBRef;
import static com.example.Cyber.ProductsActivity.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.databinding.ActivityAddNewProductBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNewProductActivity extends AppCompatActivity {

    ActivityAddNewProductBinding binding;
    ActivityResultLauncher<String> photo;
    private Uri imageUri = null;
    private String categoryPicked = "", unitsPicked = "", id = "";
    private boolean newProduct = true;
    private ProductModel existingProduct = null;
    private ArrayList<String> categories;
    private ArrayList<String> units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        photo = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
              imageUri = result;
              binding.imgNewProduct.setImageURI(result);
            }
        });

        categories = new ArrayList<>();
        categories.add(getString(R.string.category9));
        categories.add(getString(R.string.category1));
        categories.add(getString(R.string.category2));
        categories.add(getString(R.string.category3));
        categories.add(getString(R.string.category4));
        categories.add(getString(R.string.category5));
        categories.add(getString(R.string.category6));
        categories.add(getString(R.string.category7));
        categories.add(getString(R.string.category8));


        units = new ArrayList<>();
        units.add(getString(R.string.measurement1));
        units.add(getString(R.string.measurement2));
        units.add(getString(R.string.measurement3));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        ArrayAdapter<String> unitsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        );

        binding.categorySpinner.setAdapter(categoryAdapter);
        binding.unitsMeasure.setAdapter(unitsAdapter);

        //Checking if its a new product or editing an existing product
        existingProduct = (ProductModel) getIntent().getSerializableExtra("model");

        if(existingProduct != null){
            newProduct = false;
            fillDetailsExistingProduct();
        }

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryPicked = categories.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.unitsMeasure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                unitsPicked = units.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.imgNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo.launch("image/*");
            }
        });

        binding.barcodeScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode();
            }
        });

        binding.barcodeScanning2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode2();
            }
        });

        binding.barcodeScanning3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode3();
            }
        });
        
        binding.newSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.newProductName.getText().toString().trim();
                String purchasePrice = binding.purchasePrice.getText().toString().trim();
                String sellingPrice = binding.sellingPrice.getText().toString();
                String quantity = binding.quantity.getText().toString();
                String lowAlert = binding.lowestStockQuantity.getText().toString();
                String barcodeNum = binding.barcodeNumber.getText().toString();
                String barcodeNum2 = binding.barcodeNumber2.getText().toString();
                String barcodeNum3 = binding.barcodeNumber3.getText().toString();

                if(name.isEmpty() || purchasePrice.isEmpty() || sellingPrice.isEmpty() || quantity.isEmpty() ||
                        barcodeNum.isEmpty() || lowAlert.isEmpty() || (imageUri == null && newProduct)){
                    Toast.makeText(AddNewProductActivity.this, "Enter all fields to proceed", Toast.LENGTH_SHORT).show();
                } else {
                    ProductModel product;

                    Map<String, Double> barcodes = new HashMap<>();

                    barcodes.put(barcodeNum,1.0);
                    if(!barcodeNum2.isEmpty())
                        barcodes.put(barcodeNum2,0.5);
                    if(!barcodeNum3.isEmpty())
                        barcodes.put(barcodeNum3,0.25);

                    if(newProduct){
                        id = UUID.randomUUID().toString();
                        product = new ProductModel(id,name,null,categoryPicked,Double.parseDouble(purchasePrice),
                                Double.parseDouble(sellingPrice),Double.parseDouble(quantity),Double.parseDouble(lowAlert),unitsPicked,barcodes);
                        saveProductToServer(product);
                    } else {
//                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("products").document(existingProduct.getId());
                        DatabaseReference docRef = productDBRef.child(existingProduct.getId());
                        //checking if any of the barcodes have been edited
                        boolean barcodeChanged = false;
                        Map<String, Double> existingBarcodes = existingProduct.getBarcodes();
                        String existingKey = "",existingKey2 = "", existingKey3 ="";

                        for(String key: existingBarcodes.keySet()){
                            Double value = existingBarcodes.get(key);
                            if(value == 1.0)
                                existingKey = key;
                            else if(value == 0.5)
                                existingKey2 = key;
                            else if(value == 0.25)
                                existingKey3 = key;
                        }

                        if(!existingKey.equals(barcodeNum) || !existingKey2.equals(barcodeNum2) || !existingKey3.equals(barcodeNum3))
                            barcodeChanged = true;

                        if(barcodeChanged){
                            Toast.makeText(AddNewProductActivity.this, "Barcode has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("barcodes").setValue(barcodes);
                        }
                        if(existingProduct.getLowStockAlert() != Double.parseDouble(lowAlert)){
                            Toast.makeText(AddNewProductActivity.this, "Low Alert Qty has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("lowStockAlert").setValue(Double.parseDouble(lowAlert));
                        }
                        if(!existingProduct.getName().equals(name)){
                            Toast.makeText(AddNewProductActivity.this, "Name has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("name").setValue(name);
                        }
                        if(existingProduct.getPurchasePrice() != Double.parseDouble(purchasePrice)){
                            Toast.makeText(AddNewProductActivity.this, "Purchase price has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("purchasePrice").setValue(Double.valueOf(purchasePrice));
                        }
                        if(existingProduct.getSellingPrice() != Double.parseDouble(sellingPrice)){
                            Toast.makeText(AddNewProductActivity.this, "Selling price has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("sellingPrice").setValue(Double.valueOf(sellingPrice));
                        }
                        if(existingProduct.getQuantity() != Double.parseDouble(quantity)){
                            Toast.makeText(AddNewProductActivity.this, "Quantity has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("quantity").setValue(Double.valueOf(quantity));
                        }
                        if(!existingProduct.getCategory().equals(categoryPicked)){
                            Toast.makeText(AddNewProductActivity.this, "Category picked has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("category").setValue(categoryPicked);
                        }
                        if(!existingProduct.getUnits().equals(unitsPicked)){
                            Toast.makeText(AddNewProductActivity.this, "Units has changed", Toast.LENGTH_SHORT).show();
                            docRef.child("units").setValue(unitsPicked);
                        }
                        if(imageUri != null){
                            Toast.makeText(AddNewProductActivity.this, "Pic has changed", Toast.LENGTH_SHORT).show();
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference("products/"+existingProduct.getId()+".png");
                            storageReference.putFile(imageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String newUri = uri.toString();
                                                    docRef.child("image").setValue(newUri);
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class)
                                .putExtra("type","Default")
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(!newProduct){
            getMenuInflater().inflate(R.menu.deleteproductmenu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(!newProduct){
            if(item.getItemId() == R.id.deleteProduct){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to delete this product?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                                .getReference("products").child(existingProduct.getId());

                        FirebaseStorage.getInstance()
                                .getReference("products/"+existingProduct.getId()+".png")
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        databaseRef
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(AddNewProductActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class)
                                                                .putExtra("type","Default")
                                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillDetailsExistingProduct() {
        binding.newProductName.setText(existingProduct.getName());

        int iNum = 0;
        for(String i: categories){
            if(i.equals(existingProduct.getCategory())){
                binding.categorySpinner.setSelection(iNum);
                categoryPicked = existingProduct.getCategory();
                break;
            }
            iNum++;
        }

        binding.purchasePrice.setText(String.valueOf(existingProduct.getPurchasePrice()));
        binding.sellingPrice.setText(String.valueOf(existingProduct.getSellingPrice()));
        binding.quantity.setText(String.valueOf(existingProduct.getQuantity()));
        binding.lowestStockQuantity.setText(String.valueOf(existingProduct.getLowStockAlert()));

        iNum = 0;
        for(String y: units){
            if(y.equals(existingProduct.getUnits())){
                binding.unitsMeasure.setSelection(iNum);
                unitsPicked = existingProduct.getUnits();
                break;
            }
            iNum++;
        }

        Map<String, Double> barcodes = existingProduct.getBarcodes();

        for(String key: barcodes.keySet()){
            Double value = barcodes.get(key);
            if(value == 1.0)
                binding.barcodeNumber.setText(key);
            else if(value == 0.5)
                binding.barcodeNumber2.setText(key);
            else if(value == 0.25)
                binding.barcodeNumber3.setText(key);
        }

        Glide.with(this)
                .load(existingProduct.getImage())
                .into(binding.imgNewProduct);
    }

    private void saveProductToServer(ProductModel product) {

        ProgressDialog progressDialog = new ProgressDialog(AddNewProductActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("New Product");
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("products/"+product.getId()+".png");
        //If existing image has not been changed therefore imageUri remains null
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        product.setImage(uri.toString());

                                        productDBRef
                                                .child(product.getId())
                                                .setValue(product)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        if(progressDialog.isShowing())
                                                            progressDialog.dismiss();

                                                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class)
                                                                .putExtra("type","Default")
                                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                                                        InternalDataBase.getInstance(AddNewProductActivity.this).addToAllProducts(product);
                                                        Toast.makeText(AddNewProductActivity.this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "onSuccess: saved product id "+product.getId());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        if(progressDialog.isShowing())
                                                            progressDialog.dismiss();

                                                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if(progressDialog.isShowing())
                                            progressDialog.dismiss();

                                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();

                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scanBarcode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setOrientationLocked(true);
        options.setBeepEnabled(true);
        options.setCaptureActivity(ScanActivity.class);
        barLauncher.launch(options);
    }

    private void scanBarcode2() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setOrientationLocked(true);
        options.setBeepEnabled(true);
        options.setCaptureActivity(ScanActivity.class);
        barLauncher2.launch(options);
    }

    private void scanBarcode3() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setOrientationLocked(true);
        options.setBeepEnabled(true);
        options.setCaptureActivity(ScanActivity.class);
        barLauncher3.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            binding.barcodeNumber.setText(result.getContents());
        }
    });

    ActivityResultLauncher<ScanOptions> barLauncher2 = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            binding.barcodeNumber2.setText(result.getContents());
        }
    });

    ActivityResultLauncher<ScanOptions> barLauncher3 = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            binding.barcodeNumber3.setText(result.getContents());
        }
    });
}