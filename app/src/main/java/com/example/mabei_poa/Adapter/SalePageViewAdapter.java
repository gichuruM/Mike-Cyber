package com.example.mabei_poa.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mabei_poa.Fragment.CustomerDebtFragment;
import com.example.mabei_poa.Fragment.ProductsCartFragment;

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
            case 1:
                return new CustomerDebtFragment();
            default:
                return new ProductsCartFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
