package com.example.Cyber.Adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.Cyber.Fragment.AllProductsFragment;
import com.example.Cyber.Fragment.BeveragesFragment;
import com.example.Cyber.Fragment.ComputerAccessoriesFragment;
import com.example.Cyber.Fragment.IceFragment;
import com.example.Cyber.Fragment.PhoneAccessoriesFragment;
import com.example.Cyber.Fragment.PerfumesFragment;
import com.example.Cyber.Fragment.OthersFragment;
import com.example.Cyber.Fragment.NetworkProductsFragment;
import com.example.Cyber.Fragment.ElectronicsFragment;
import com.example.Cyber.Fragment.StationaryFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    String productId;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String productId) {
        super(fragmentActivity);
        this.productId = productId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position){
            case 0:
                AllProductsFragment productsFragment;

                if(!productId.isEmpty()){
                    Bundle bundle = new Bundle();
                    bundle.putString("productId", productId);
                    productsFragment = new AllProductsFragment();

                    productsFragment.setArguments(bundle);
                } else {
                    productsFragment = new AllProductsFragment();
                }

                return productsFragment;
            case 1:
                return new PerfumesFragment();
            case 2:
                return new ElectronicsFragment();
            case 3:
                return new BeveragesFragment();
            case 4:
                return new PhoneAccessoriesFragment();
            case 5:
                return new NetworkProductsFragment();
            case 6:
                return new ComputerAccessoriesFragment();
            case 7:
                return new IceFragment();
            case 8:
                return new StationaryFragment();
            case 9:
                return new OthersFragment();
            default:
                return new AllProductsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
