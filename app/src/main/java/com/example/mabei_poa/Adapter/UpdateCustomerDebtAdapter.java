package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.editedCustomers;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.CustomerModel;
import com.example.mabei_poa.R;

import java.util.ArrayList;

public class UpdateCustomerDebtAdapter extends RecyclerView.Adapter<UpdateCustomerDebtAdapter.MyViewHolder> implements Filterable {

    private Context context;
    public static ArrayList<CustomerModel> customerList;
    private ArrayList<CustomerModel> customerToUpdate;
    private View scanEditView;

    public UpdateCustomerDebtAdapter(Context context, View scanEditView) {
        this.context = context;
        this.customerList = InternalDataBase.getInstance(context).getAllCustomerDebts();
        this.customerToUpdate = new ArrayList<>(editedCustomers);
        this.scanEditView = scanEditView;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_customer_debt_update, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomerModel customer = customerToUpdate.get(position);
        CustomerModel customerCopy = new CustomerModel(customer);

        // Reset radio button state --> Needs to be reset to avoid bugs in recyclerView
        holder.debtUpdateType.setOnCheckedChangeListener(null); // Temporarily remove listener

        holder.customerName.setText(customerCopy.getCustomerName());
        holder.customerCurrentDebt.setText(String.valueOf(customerCopy.getCurrentDebt()));
        if(customerCopy.getCurrentDebt() > customerCopy.getMaxDebt()){
            holder.customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        }

        holder.proposedAmount.setText(String.valueOf(customerCopy.getProposedAmount()));

        if(customerCopy.getProposedAmountType().equals("DEBT"))
            holder.updateDebt.setChecked(true);
        else if(customerCopy.getProposedAmountType().equals("DEBT_PAYMENT"))
            holder.updateDebtPayment.setChecked(true);
        else {
            holder.debtUpdateType.clearCheck();
        }

        holder.debtUpdateType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                String debtType = "null";

                if(checkedId == R.id.updateDebt)
                    debtType = "DEBT";
                else if(checkedId == R.id.updateDebtPayment)
                    debtType = "DEBT_PAYMENT";

                if(!debtType.equals("null")){
                    //Log.d(TAG, "onCheckedChanged: debtType "+debtType);
                    boolean found = false;
                    for(CustomerModel c: editedCustomers){
                        if(c.getCustomerId().equals(customer.getCustomerId())){
                            c.setProposedAmountType(debtType);
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        customerCopy.setProposedAmountType(debtType);
                        //Log.d(TAG, "onCheckedChanged: ADDED");
                        editedCustomers.add(customerCopy);
                    }

                    int pos = holder.getAdapterPosition();

                    InternalDataBase.getInstance(context).setNewUpdateDebt(editedCustomers);
                    //To enable live changing of customer color as you adjust the debt
                    for(CustomerModel cm: editedCustomers){
                        if(customerToUpdate.get(pos).getCustomerId().equals(cm.getCustomerId())){
                            holder.liveUpdateCustomerStatus(cm.getCurrentDebt(), cm.getProposedAmount(),
                                    cm.getMaxDebt() ,debtType);
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerToUpdate.size();
    }

    @Override
    public Filter getFilter() {
        return updateDebtFilter;
    }

    private final Filter updateDebtFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<CustomerModel> filtered = new ArrayList<>();

            //Only showing customers with the searched name
            if(!(constraint == null || constraint.length() == 0)){
                String searchWord = constraint.toString().toLowerCase().trim();

                for(CustomerModel c: customerList){
                    if(c.getCustomerName().toLowerCase().contains(searchWord) ||
                        c.getPhoneNumber().contains(searchWord)){
                        boolean found = false;      //Ensuring we don't show a customer that is already visible
                        for(CustomerModel editedCustomer: editedCustomers){
                            if(editedCustomer.getCustomerId().equals(c.getCustomerId())){
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            filtered.add(c);
                        }
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
            customerToUpdate.clear();

            customerToUpdate.addAll(editedCustomers);

            customerToUpdate.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView customerName, customerCurrentDebt;
        EditText proposedAmount;
        RelativeLayout customerStatus;
        RadioGroup debtUpdateType;
        RadioButton updateDebt, updateDebtPayment;
        ImageView removeCustomerUpdate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            customerName = itemView.findViewById(R.id.updateDebtName);
            customerCurrentDebt = itemView.findViewById(R.id.updateDebtCurrentAmount);
            proposedAmount = itemView.findViewById(R.id.updateDebtProposal);
            customerStatus = itemView.findViewById(R.id.updateCustomerStatus);
            updateDebt = itemView.findViewById(R.id.updateDebt);
            updateDebtPayment = itemView.findViewById(R.id.updateDebtPayment);
            debtUpdateType = itemView.findViewById(R.id.debtUpdateType);
            removeCustomerUpdate  = itemView.findViewById(R.id.removeCustomerUpdate);

            proposedAmount.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if(event.getDevice().isExternal()){
                            if(event.getKeyCode() == KeyEvent.KEYCODE_0)
                                CartAdapter.scanFirstDigit = "0";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_1)
                                CartAdapter.scanFirstDigit = "1";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_2)
                                CartAdapter.scanFirstDigit = "2";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_3)
                                CartAdapter.scanFirstDigit = "3";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_4)
                                CartAdapter.scanFirstDigit = "4";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_5)
                                CartAdapter.scanFirstDigit = "5";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_6)
                                CartAdapter.scanFirstDigit = "6";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_7)
                                CartAdapter.scanFirstDigit = "7";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_8)
                                CartAdapter.scanFirstDigit = "8";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_9)
                                CartAdapter.scanFirstDigit = "9";
                            else
                                CartAdapter.scanFirstDigit = "";

                            scanEditView.requestFocus();
                            return true;
                        } else
                            return false;
                    } else return false;
                }
            });

            proposedAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;

                    if(s.toString().equals("")) return;

                    String input = s.toString();

                    //To avoid crushing if length of amount is too long -> Scanning in editing area
                    if(input.length() > 5) return;

                    int amount = Integer.parseInt(s.toString());
                    if(amount == 0) return;

                    CustomerModel newCustomer = customerToUpdate.get(pos);
                    CustomerModel newCustomerCopy = new CustomerModel(newCustomer);

                    boolean found = false;
                    for(CustomerModel c: editedCustomers){
                        if(c.getCustomerId().equals(newCustomer.getCustomerId())){
                            c.setProposedAmount(amount);
                            found = true;
                        }
                    }
                    if(!found){
                        newCustomerCopy.setProposedAmount(amount);
                        editedCustomers.add(newCustomerCopy);
                        //Log.d(TAG, "afterTextChanged: ADDED");
                    }

                    InternalDataBase.getInstance(context).setNewUpdateDebt(editedCustomers);
                    //To enable live changing of customer color as you adjust the debt
                    for(CustomerModel cm: editedCustomers){
                        if(customerToUpdate.get(pos).getCustomerId().equals(cm.getCustomerId())){
                            liveUpdateCustomerStatus(cm.getCurrentDebt(), amount,
                                    cm.getMaxDebt() ,cm.getProposedAmountType());
                            break;
                        }
                    }
                }
            });

            removeCustomerUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;

                    CustomerModel customer = customerToUpdate.get(pos);

                    for(CustomerModel c: editedCustomers){
                        if(c.getCustomerId().equals(customer.getCustomerId())){
                            c.setProposedAmount(0);
                            c.setProposedAmountType("null");
                            editedCustomers.remove(c);
                            break;
                        }
                    }

                    InternalDataBase.getInstance(context).setNewUpdateDebt(editedCustomers);
                    customerToUpdate.remove(customer);
                    notifyItemRemoved(pos);
                }
            });
        }

        //Updating the status of the customers debt: red or green depending on the amount
        void liveUpdateCustomerStatus(int currentAmount, int proposedAmount, int maxAmount ,String amountType){
            //Log.d(TAG, "liveUpdateCustomerStatus: proposedAmount "+proposedAmount+" currentAmount "+currentAmount);
            if(amountType.equals("DEBT")){
                proposedAmount = currentAmount + proposedAmount;
            } else if(amountType.equals("DEBT_PAYMENT")){
                proposedAmount = currentAmount - proposedAmount;
            }

            if(proposedAmount > maxAmount){
                customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
            } else {
                customerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
            }
        }
    }
}
