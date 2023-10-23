package com.example.mabei_poa.Adapter;

import static com.example.mabei_poa.HomeActivity.SHOP_USER_UID;
import static com.example.mabei_poa.HomeActivity.userUID;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mabei_poa.AddNewProductActivity;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
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
        for(CartModel c: cartProductsList){
            if(c.getProductId().equals(productModel.getId())){
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
                    if(userUID.equals(SHOP_USER_UID))
                        Toast.makeText(context, "Access denied", Toast.LENGTH_SHORT).show();
                    else{
                        Intent intent = new Intent(context, AddNewProductActivity.class);
                        intent.putExtra("model",productModel);
                        context.startActivity(intent);
                    }
                } else if(ProductsActivity.activityType.equals("Cart")){

                    boolean restricted = false;

                    String[] allowedProducts = {
                            "bfbcb65e-0245-4705-8598-dca56550aa99", "a4c56a3e-a2cf-4a67-99f1-72e623ddb3b9",
                            "010c8495-2737-4467-9d1b-e9c17df48266", "1dadc5ab-27df-4d33-b949-8488d4651611",
                            "01d8c835-3e9a-4ea8-ad45-2eb50bb2331c"};

                    //Restricting shop account from handling purchases of various products
                    if(InternalDataBase.getInstance(context).getCartType().equals("Purchase") &&
                            userUID.equals(SHOP_USER_UID)){
                        restricted = true;
                        for(int i = 0; i < allowedProducts.length; i++){
                            if(productModel.getId().equals(allowedProducts[i])){
                                restricted = false;
                                break;
                            }
                        }
                    }
//                    Log.d(TAG, "onClick: "+"Name "+ productModel.getName()+" id "+productModel.getId());
                    if(!restricted){
                        boolean inCart = false;

                        for(CartModel p: cartProductsList){
                            if(p.getProductId().equals(productModel.getId())){
                                inCart = true;
                                int position = cartProductsList.indexOf(p);
                                cartProductsList.remove(p);
                                cartAdapter.notifyItemRemoved(position);
                                holder.productInCartMark.setVisibility(View.GONE);

                                for(CartModel c: temporaryCartList){
                                    if(c.getProductId().equals(productModel.getId())){
                                        temporaryCartList.remove(c);
                                        break;
                                    }
                                }

                                break;
                            }
                        }

                        if(!inCart) {    //Not in cart therefore adding
                            CartModel cartModel = new CartModel(productModel.getId(),1,productModel.getSellingPrice());
                            cartProductsList.add(cartModel);
                            cartAdapter.notifyItemInserted(cartProductsList.indexOf(cartModel));
                            holder.productInCartMark.setVisibility(View.VISIBLE);
                            //Adding product to temp
                            temporaryCartList.add(cartModel);
                        }
                        InternalDataBase.getInstance(context).setNewCart(cartProductsList);
                    } else {
                        Toast.makeText(context, "Access denied", Toast.LENGTH_SHORT).show();
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
