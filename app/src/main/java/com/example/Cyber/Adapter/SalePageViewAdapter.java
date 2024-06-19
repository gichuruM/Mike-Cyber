package com.example.Cyber.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.Cyber.Fragment.ProductsCartFragment;

public class SalePageViewAdapter extends FragmentStateAdapter {

    public SalePageViewAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ProductsCartFragment();
            default:
                return new ProductsCartFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
