package com.example.mabei_poa;

import static com.example.mabei_poa.Fragment.AllCustomerDebtsFragment.customerDebtAdapter;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.Adapter.CustomerActivityAdapter;
import com.example.mabei_poa.Adapter.CustomerDebtAdapter;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Interface.CustomerDebtInterface;
import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.databinding.ActivityCustomersBinding;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

public class Customers extends AppCompatActivity{

    ActivityCustomersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CustomerActivityAdapter activityAdapter = new CustomerActivityAdapter(this);
        binding.customerActivityViewPager.setAdapter(activityAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        SearchView searchView =(SearchView) menuItem.getActionView();
        searchView.setQueryHint("Find a product");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                customerDebtAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}