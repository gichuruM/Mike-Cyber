package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartAdapter;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mabei_poa.AddNewProductActivity;
import com.example.mabei_poa.Model.CartModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.ProductsActivity;
import com.example.mabei_poa.R;

import java.util.ArrayList;

public class AllProductsAdapter extends RecyclerView.Adapter<AllProductsAdapter.MyViewHolder> implements Filterable {

    public static ArrayList<ProductModel> fullProductModelArrayList = new ArrayList<>();
    public static ArrayList<CartModel> temporaryCartList = new ArrayList<>();
    public static String currentFragment = "";
    private Context context;
    private ArrayList<ProductModel> finalProductArrayList;
    private ArrayList<ProductModel> productModelArrayList;

    public AllProductsAdapter(Context context) {
        this.context = context;
        productModelArrayList = new ArrayList<>();
        finalProductArrayList = new ArrayList<>();
    }

    public void initializingFragmentArray(){
        //declared as new array because clear is bringing problems
        productModelArrayList.clear();
        finalProductArrayList.clear();

        if(currentFragment.equals("All")){
            productModelArrayList.addAll(fullProductModelArrayList);
        } else if(currentFragment.equals("Kitchen")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Kitchen"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Flour")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Flour"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Cereals")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Cereals"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Drinks")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Drinks"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Toiletries")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Toiletries"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Others")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Others"))
                    productModelArrayList.add(p);
            }
        }

        for(ProductModel p: productModelArrayList){
            finalProductArrayList.add(p);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_product_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ProductModel productModel = finalProductArrayList.get(position);

        holder.productName.setText(productModel.getName());
        holder.productPrice.setText(String.valueOf(productModel.getSellingPrice()));
        holder.productQuantity.setText(String.valueOf(productModel.getQuantity()));
        holder.productUnits.setText(productModel.getUnits());
        holder.productInCartMark.setVisibility(View.GONE);
        //checking if a product is in the cart
        for(CartModel p: cartProductsList){
            if(p.getProductModel().getId().equals(productModel.getId())){
                holder.productInCartMark.setVisibility(View.VISIBLE);
//                Log.d(TAG, "onBindViewHolder: Name passing "+holder.productName.getText().toString());
            }
        }

        Glide.with(context)
                .load(productModel.getImage())
                .into(holder.productImage);

        holder.wholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ProductsActivity.activityType.equals("Default")) {
                    Intent intent = new Intent(context, AddNewProductActivity.class);
                    intent.putExtra("model",productModel);
                    context.startActivity(intent);
                } else if(ProductsActivity.activityType.equals("Cart")){

                    boolean inCart = false;

                    for(CartModel p: cartProductsList){
                        if(p.getProductModel().getId().equals(productModel.getId())){
                            inCart = true;
                            int position = cartProductsList.indexOf(p);
                            cartProductsList.remove(p);
                            cartAdapter.notifyItemRemoved(position);
                            holder.productInCartMark.setVisibility(View.GONE);

                            for(CartModel c: temporaryCartList){
                                if(c.getProductModel().getId().equals(productModel.getId())){
                                    temporaryCartList.remove(c);
                                    break;
                                }
                            }
//                            Log.d(TAG, "onClick: Removed cartsize: "+cartProductsList.size()+" temporaryCartList "+temporaryCartList.size());
                            break;
                        }
                    }

                    if(!inCart) {    //Not in cart therefore adding
                        CartModel cartModel = new CartModel(productModel,1,productModel.getSellingPrice(),productModel.getSellingPrice());
                        cartProductsList.add(cartModel);
                        cartAdapter.notifyItemInserted(cartProductsList.indexOf(cartModel));
                        holder.productInCartMark.setVisibility(View.VISIBLE);
                        //Adding product to temp 
                        temporaryCartList.add(cartModel);
//                        Log.d(TAG, "onClick: Added to cart. cartsize: "+cartProductsList.size()+" temporaryCartList "+temporaryCartList.size());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return finalProductArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<ProductModel> filteredProducts = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredProducts.addAll(productModelArrayList);
            } else {
                String searchWord = constraint.toString().toLowerCase().trim();

                for(ProductModel p: productModelArrayList){
                    if(p.getName().toLowerCase().contains(searchWord)){

                        filteredProducts.add(p);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredProducts;
            results.count = filteredProducts.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            finalProductArrayList.clear();
            finalProductArrayList.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView productImage;
        private TextView productName, productPrice, productUnits, productQuantity;
        private LinearLayout wholeItem;
        private RelativeLayout productInCartMark;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productUnits = itemView.findViewById(R.id.productUnits);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            wholeItem = itemView.findViewById(R.id.wholeItem);
            productInCartMark = itemView.findViewById(R.id.productInCartMark);
        }
    }
}
