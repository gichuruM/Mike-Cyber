package com.example.mabei_poa.Adapter;


import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.Interface.CustomerDebtInterface;
import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CustomerDebtAdapter extends RecyclerView.Adapter<CustomerDebtAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<CustomerModel> allCustomerDebt;
    private ArrayList<CustomerModel> filteredCustomerDebts;
    private CustomerDebtInterface customerDebtInterface;

    public CustomerDebtAdapter(Context context, ArrayList<CustomerModel> allCustomerDebt, CustomerDebtInterface customerDebtInterface) {
        this.context = context;
        this.allCustomerDebt = allCustomerDebt;
        this.customerDebtInterface = customerDebtInterface;
        filteredCustomerDebts = new ArrayList<>(allCustomerDebt);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_customer_layout,parent,false);
        return new MyViewHolder(view, customerDebtInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomerModel customer = filteredCustomerDebts.get(position);
        String date = new SimpleDateFormat("d/M/yy").format(customer.getInitialDate());

        holder.customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        holder.customerName.setText(customer.getCustomerName());
        holder.customerPhoneNum.setText(customer.getPhoneNumber());
        holder.customerCurrentDebt.setText(String.valueOf(customer.getCurrentDebt()));
        holder.customerDate.setText(date);
        if(customer.getCurrentDebt() > customer.getMaxDebt())
            holder.customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        else
            holder.customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
    }

    @Override
    public int getItemCount() {
        return filteredCustomerDebts.size();
    }

    @Override
    public Filter getFilter() {
        return customerDebtFilter;
    }

    private final Filter customerDebtFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<CustomerModel> filtered = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filtered.addAll(allCustomerDebt);
            } else {
                String searchWord = constraint.toString().toLowerCase().trim();

                for(CustomerModel c: allCustomerDebt){
                    if(c.getCustomerName().toLowerCase().contains(searchWord)){
                        filtered.add(c);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtered;
            results.count = filtered.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredCustomerDebts.clear();
            filteredCustomerDebts.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView customerName, customerPhoneNum, customerCurrentDebt, customerDate;
        private LinearLayout customerStatus;
        private CardView customerSummary;

        public MyViewHolder(@NonNull View itemView, CustomerDebtInterface customerDebtInterface) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerName);
            customerPhoneNum = itemView.findViewById(R.id.customerPhoneNum);
            customerCurrentDebt = itemView.findViewById(R.id.customerCurrentDebt);
            customerDate = itemView.findViewById(R.id.customerInitialDate);
            customerStatus = itemView.findViewById(R.id.customerStatus);
            customerSummary = itemView.findViewById(R.id.oneCustomerSummaryLayout);

            customerSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;

                    customerDebtInterface.CustomerClicked(pos, filteredCustomerDebts);
                }
            });
        }
    }
}
