package com.example.Cyber.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ReportFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.Cyber.Fragment.DefaultReportFragment;
import com.example.Cyber.Fragment.ProductReportFragment;
import com.example.Cyber.Fragment.ProductsCartFragment;

public class ReportViewAdapter extends FragmentStateAdapter {

    public ReportViewAdapter(FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new DefaultReportFragment();
            default:
                return new ProductReportFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
