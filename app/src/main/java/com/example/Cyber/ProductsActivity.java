package com.example.Cyber;

import static com.example.Cyber.Adapter.AllProductsAdapter.temporaryCartList;
import static com.example.Cyber.SaleToCustomerActivity.cartProductsList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.Cyber.Adapter.AllProductsAdapter;
import com.example.Cyber.Adapter.ViewPagerAdapter;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.CartModel;
import com.example.Cyber.databinding.ActivityProductsBinding;
import com.google.android.material.tabs.TabLayout;

public class ProductsActivity extends AppCompatActivity {

    public static final String TAG = "BeiPoaApp";
    public static AllProductsAdapter productsAdapter;
    public static String activityType = "Default";
    ActivityProductsBinding binding;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activityType = getIntent().getStringExtra("type");

        //Passing data from the activity to the fragment
        String productId = getIntent().getStringExtra("productId");

        if(productId != null){
            viewPagerAdapter = new ViewPagerAdapter(this, productId);
            binding.viewPager2.setAdapter(viewPagerAdapter);
        } else {
            viewPagerAdapter = new ViewPagerAdapter(this, "");
            binding.viewPager2.setAdapter(viewPagerAdapter);
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.getTabAt(position).select();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //If back button is pressed, remove items that have just been added in cart previously
        if(activityType.equals("Cart")){
            if(temporaryCartList.size() > 0){
                for(CartModel t : temporaryCartList){
                    for(CartModel c : cartProductsList){
                        if(t.getProductId().equals(c.getProductId())){
                            //Log.d(TAG, "onBackPressed: Removing "+c.getProductModel().getName());
                            cartProductsList.remove(c);
                            InternalDataBase.getInstance(this).setNewCart(cartProductsList);
                            break;
                        }
                    }
                }
                temporaryCartList.clear();
                SaleToCustomerActivity.totalCartAmount();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);

        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Find something");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                productsAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}