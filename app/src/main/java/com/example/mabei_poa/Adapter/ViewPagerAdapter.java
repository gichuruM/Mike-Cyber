package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mabei_poa.Fragment.AllProductsFragment;
import com.example.mabei_poa.Fragment.CerealsFragment;
import com.example.mabei_poa.Fragment.DrinksFragment;
import com.example.mabei_poa.Fragment.KitchenFragment;
import com.example.mabei_poa.Fragment.OthersFragment;
import com.example.mabei_poa.Fragment.SoapsSanitaryFragment;
import com.example.mabei_poa.Fragment.UngaFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position){
            case 0:
                return new AllProductsFragment();
            case 1:
                return new KitchenFragment();
            case 2:
                return new UngaFragment();
            case 3:
                return new CerealsFragment();
            case 4:
                return new DrinksFragment();
            case 5:
                return new SoapsSanitaryFragment();
            case 6:
                return new OthersFragment();
            default:
                return new AllProductsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
