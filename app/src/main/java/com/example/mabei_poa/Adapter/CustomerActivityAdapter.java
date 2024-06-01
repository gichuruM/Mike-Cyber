package com.example.mabei_poa.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mabei_poa.Fragment.DayDebtTrackingFragment;
import com.example.mabei_poa.Fragment.AllCustomerDebtsFragment;

public class CustomerActivityAdapter extends FragmentStateAdapter {

    public CustomerActivityAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new AllCustomerDebtsFragment();
            case 1:
                return new DayDebtTrackingFragment();
            default:
                return new AllCustomerDebtsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
