package com.example.mabei_poa;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.databinding.ActivityAddNewProductBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
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
        categories.add(getString(R.string.category6));
        categories.add(getString(R.string.category1));
        categories.add(getString(R.string.category2));
        categories.add(getString(R.string.category3));
        categories.add(getString(R.string.category4));
        categories.add(getString(R.string.category5));

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
        
        binding.newSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = binding.newProductName.getText().toString().trim();
                String purchasePrice = binding.purchasePrice.getText().toString().trim();
                String sellingPrice = binding.sellingPrice.getText().toString();
                String quantity = binding.quantity.getText().toString();
                String barcodeNum = binding.barcodeNumber.getText().toString();

                if(name.isEmpty() || purchasePrice.isEmpty() || sellingPrice.isEmpty() || quantity.isEmpty() || barcodeNum.isEmpty() || (imageUri == null && newProduct)){
                    Toast.makeText(AddNewProductActivity.this, "Enter all fields to proceed", Toast.LENGTH_SHORT).show();
                } else {
                    ProductModel product;

                    if(newProduct){
                        id = UUID.randomUUID().toString();
                        product = new ProductModel(id,name,null,categoryPicked,Double.parseDouble(purchasePrice),Double.parseDouble(sellingPrice),Double.parseDouble(quantity),unitsPicked,Long.parseLong(barcodeNum));
                        saveProductToServer(product);
                    } else {
                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("products").document(existingProduct.getId());

                        if(!existingProduct.getName().equals(name)){
                            Toast.makeText(AddNewProductActivity.this, "Name has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("name",name);
                        }
                        if(existingProduct.getPurchasePrice() != Double.parseDouble(purchasePrice)){
                            Toast.makeText(AddNewProductActivity.this, "Purchase price has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("purchasePrice",Double.valueOf(purchasePrice));
                        }
                        if(existingProduct.getSellingPrice() != Double.parseDouble(sellingPrice)){
                            Toast.makeText(AddNewProductActivity.this, "Selling price has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("sellingPrice",Double.valueOf(sellingPrice));
                        }
                        if(existingProduct.getQuantity() != Double.parseDouble(quantity)){
                            Toast.makeText(AddNewProductActivity.this, "Quantity has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("quantity",Double.valueOf(quantity));
                        }
                        if(existingProduct.getBarcodeNum() != Long.parseLong(barcodeNum)){
                            Toast.makeText(AddNewProductActivity.this, "Barcode has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("barcodeNum",Long.valueOf(barcodeNum));
                        }
                        if(!existingProduct.getCategory().equals(categoryPicked)){
                            Toast.makeText(AddNewProductActivity.this, "Category picked has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("category",categoryPicked);
                        }
                        if(!existingProduct.getUnits().equals(unitsPicked)){
                            Toast.makeText(AddNewProductActivity.this, "Units has changed", Toast.LENGTH_SHORT).show();
                            documentReference.update("units",unitsPicked);
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
                                                    documentReference.update("image",newUri);
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

                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
                        FirebaseFirestore.getInstance()
                                .collection("products")
                                .document(existingProduct.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        FirebaseStorage.getInstance()
                                                .getReference("products/"+existingProduct.getId()+".png")
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(AddNewProductActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class));
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(AddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

        iNum = 0;
        for(String y: units){
            if(y.equals(existingProduct.getUnits())){
                binding.unitsMeasure.setSelection(iNum);
                unitsPicked = existingProduct.getUnits();
                break;
            }
            iNum++;
        }

        binding.barcodeNumber.setText(String.valueOf(existingProduct.getBarcodeNum()));

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

                                        FirebaseFirestore.getInstance()
                                                .collection("products")
                                                .document(product.getId())
                                                .set(product)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        if(progressDialog.isShowing())
                                                            progressDialog.dismiss();

                                                        Toast.makeText(AddNewProductActivity.this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(AddNewProductActivity.this,ProductsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                        InternalDataBase.getInstance(AddNewProductActivity.this).addToAllProducts(product);
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

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            binding.barcodeNumber.setText(result.getContents());
        }
    });
}