package com.example.Cyber.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Cyber.Interface.ReportProductClickedInterface;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.R;

import java.util.ArrayList;
import static com.example.Cyber.ProductsActivity.TAG;

public class ReportProductsAdapter extends RecyclerView.Adapter<ReportProductsAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ReportProductClickedInterface clickedInterface;
    private ArrayList<ProductModel> productModels;
    private static ArrayList<ProductModel> filteredProducts;
    public static ArrayList<ProductModel> selectedProducts;

    public ReportProductsAdapter(Context context, ReportProductClickedInterface clickedInterface ,ArrayList<ProductModel> productModels) {
        this.context = context;
        this.clickedInterface = clickedInterface;
        this.productModels = productModels;
        filteredProducts = new ArrayList<>();
        selectedProducts = new ArrayList<>();
        Log.d(TAG, "ReportProductsAdapter: array size: "+productModels.size());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_product_report,parent,false);
        return new MyViewHolder(view, clickedInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProductModel product = filteredProducts.get(position);

        holder.reportProductName.setText(product.getName());
        holder.reportProductPrice.setText(String.valueOf(product.getSellingPrice()));

        //Checking if the product is selected
        holder.reportProductSelect.setVisibility(View.GONE);
        for(ProductModel p: selectedProducts){
            if(p.getId().equals(product.getId())){
                holder.reportProductSelect.setVisibility(View.VISIBLE);
            }
        }

        Glide.with(context)
                .load(product.getImage())
                .into(holder.reportProductImage);
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    @Override
    public Filter getFilter() {
        return performFiltering;
    }

    private final Filter performFiltering = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ProductModel> filtered = new ArrayList<>();

            //Only show customers with the searched name
            if(!(constraint == null || constraint.length() == 0)){
                String searchWord = constraint.toString().toLowerCase().trim();

                for(ProductModel p: productModels){
                    if(p.getName().toLowerCase().contains(searchWord) || p.getCategory().toLowerCase().contains(searchWord)){
                        //First checking if the product is already selected
                        boolean isAlreadySelected = false;
                        for(ProductModel s: selectedProducts) {
                            if (s.getId().equals(p.getId())) {
                                isAlreadySelected = true;
                                break;
                            }
                        }

                        if(!isAlreadySelected)
                            filtered.add(p);
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
            filteredProducts.clear();

            filteredProducts.addAll(selectedProducts);

            filteredProducts.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView reportProductImage;
        TextView reportProductName, reportProductPrice;
        CardView productContainer;
        RelativeLayout reportProductSelect;

        public MyViewHolder(@NonNull View itemView, ReportProductClickedInterface clickedInterface) {
            super(itemView);
            reportProductImage = itemView.findViewById(R.id.reportProductImage);
            reportProductName = itemView.findViewById(R.id.reportProductName);
            reportProductPrice = itemView.findViewById(R.id.reportProductPrice);
            productContainer = itemView.findViewById(R.id.reportProductContainer);
            reportProductSelect = itemView.findViewById(R.id.reportProductSelect);

            productContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;
                    ProductModel product = filteredProducts.get(pos);

                    if(reportProductSelect.getVisibility() == View.VISIBLE){
                        reportProductSelect.setVisibility(View.GONE);
                        selectedProducts.remove(product);
                    }else {
                        reportProductSelect.setVisibility(View.VISIBLE);
                        selectedProducts.add(product);
                    }

                    clickedInterface.reportProductClicked();
                }
            });
        }
    }
}
