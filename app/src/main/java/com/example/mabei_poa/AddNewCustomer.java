package com.example.mabei_poa;

import static com.example.mabei_poa.HomeActivity.customerDebtDBRef;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.databinding.ActivityAddNewCustomerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class AddNewCustomer extends AppCompatActivity {

    ActivityAddNewCustomerBinding binding;
    boolean newCustomer = true;
    CustomerModel editingCustomerDebt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editingCustomerDebt = (CustomerModel) getIntent().getSerializableExtra("CustomerModel");

        if(editingCustomerDebt != null){
            newCustomer = false;
            fillCustomerDebtDetails(editingCustomerDebt);
        }

        binding.newCustomerSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.newCustomerName.getText().toString(), progression;
                String phoneNumber = binding.newCustomerNumber.getText().toString();
                int maxDebt, currentDebt;

                if(name.isEmpty()){
                    Toast.makeText(AddNewCustomer.this, "Name is Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(phoneNumber.isEmpty()){
                    Toast.makeText(AddNewCustomer.this, "Phone Number is Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(binding.newCustomerMaxDebt.getText().toString().isEmpty()){
                    Toast.makeText(AddNewCustomer.this, "Max Debt is Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(binding.newCustomerCurrentDebt.getText().toString().isEmpty()){
                    Toast.makeText(AddNewCustomer.this, "Current Debt is Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                maxDebt = Integer.parseInt(binding.newCustomerMaxDebt.getText().toString());
                currentDebt = Integer.parseInt(binding.newCustomerCurrentDebt.getText().toString());
                progression = binding.newCustomerDebtProgression.getText().toString();

                if(newCustomer){
                    String uuid = UUID.randomUUID().toString();

                    CustomerModel customer = new CustomerModel(
                            uuid,
                            name,
                            phoneNumber,
                            currentDebt,
                            maxDebt,
                            new Date(),
                            new Date(),
                            progression,
                            0,
                            "null");

                    saveNewCustomer(customer);
                } else {
                    //Editing details about an existingCustomer
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                            .getReference("customersDebt")
                            .child(editingCustomerDebt.getCustomerId());

                    if(!name.equals(editingCustomerDebt.getCustomerName())){
                        Toast.makeText(AddNewCustomer.this, "Name has changed", Toast.LENGTH_SHORT).show();
                        databaseRef.child("customerName").setValue(name);
                    }
                    if(!phoneNumber.equals(editingCustomerDebt.getPhoneNumber())){
                        Toast.makeText(AddNewCustomer.this, "Phone Number has changed", Toast.LENGTH_SHORT).show();
                        databaseRef.child("phoneNumber").setValue(phoneNumber);
                    }
                    if(currentDebt != editingCustomerDebt.getCurrentDebt()){
                        Toast.makeText(AddNewCustomer.this, "Current Debt has changed", Toast.LENGTH_SHORT).show();
                        databaseRef.child("currentDebt").setValue(currentDebt);
                    }
                    if(maxDebt != editingCustomerDebt.getMaxDebt()){
                        Toast.makeText(AddNewCustomer.this, "Max debt has changed", Toast.LENGTH_SHORT).show();
                        databaseRef.child("maxDebt").setValue(maxDebt);
                    }
                    if(!progression.equals(editingCustomerDebt.getDebtProgress())){
                        Toast.makeText(AddNewCustomer.this, "Debt progression has changed", Toast.LENGTH_SHORT).show();
                        databaseRef.child("debtProgress").setValue(progression);
                    }

                    startActivity(new Intent(AddNewCustomer.this,Customers.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }

            private void saveNewCustomer(CustomerModel customer){

                ProgressDialog progressDialog = new ProgressDialog(AddNewCustomer.this);
                progressDialog.setTitle("Uploading...");
                progressDialog.setMessage("New Customer");
                progressDialog.show();

                customerDebtDBRef
                        .child(customer.getCustomerId())
                        .setValue(customer)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();

                                startActivity(new Intent(AddNewCustomer.this,Customers.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                                Toast.makeText(AddNewCustomer.this, "New customer saved!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();

                                Toast.makeText(AddNewCustomer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });
    }

    private void fillCustomerDebtDetails(CustomerModel existingCustomer) {
        binding.newCustomerName.setText(existingCustomer.getCustomerName());
        binding.newCustomerNumber.setText(existingCustomer.getPhoneNumber());
        binding.newCustomerMaxDebt.setText(String.valueOf(existingCustomer.getMaxDebt()));
        binding.newCustomerCurrentDebt.setText(String.valueOf(existingCustomer.getCurrentDebt()));
        binding.newCustomerDebtProgression.setText(existingCustomer.getDebtProgress());

        String initialDate = new SimpleDateFormat("d/M/yy").format(existingCustomer.getInitialDate());
        String lastEditDate = new SimpleDateFormat("d/M/yy").format(existingCustomer.getEditDate());

        binding.newCustomerInitialDate.setText(initialDate);
        binding.newCustomerLastEdited.setText(lastEditDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(!newCustomer){
            getMenuInflater().inflate(R.menu.deletecustomermenu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(!newCustomer){
            if(item.getItemId() == R.id.deleteCustomer){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to delete this customer?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                                .getReference("customersDebt")
                                .child(editingCustomerDebt.getCustomerId());

                        databaseRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(AddNewCustomer.this, "Customer deleted successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AddNewCustomer.this,Customers.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddNewCustomer.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}