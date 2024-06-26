package com.example.Cyber.Adapter;

import static com.example.Cyber.HomeActivity.SHOP_USER_UID;
import static com.example.Cyber.HomeActivity.userUID;
import static com.example.Cyber.SaleToCustomerActivity.cartAdapter;
import static com.example.Cyber.SaleToCustomerActivity.cartProductsList;

import android.content.Context;
import android.content.Intent;
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
import com.example.Cyber.AddNewProductActivity;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.CartModel;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.ProductsActivity;
import com.example.Cyber.R;

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
        } else if(currentFragment.equals("Perfumes")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Perfumes"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Electronics")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Electronics"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Beverages")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Beverages"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Phone accessories")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Phone accessories"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Network product")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Network product"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Computer Accessories")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Computer Accessories"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Ice")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Ice"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Stationary")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Stationary"))
                    productModelArrayList.add(p);
            }
        } else if(currentFragment.equals("Others")){
            for(ProductModel p: fullProductModelArrayList){
                if(p.getCategory().equals("Others"))
                    productModelArrayList.add(p);
            }
        }

        finalProductArrayList.addAll(productModelArrayList);
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

                    //Allowed Products: Brown bread BB, Bread BB, Bread half BB, Bread Tpremium, Bread Tosha, stars
                    //Cakes za 5, Cakes, Airtel airtime, Safaricom airtime, njugu, crips, Elliots, Fine bread
                    //festive, festive brown, straight
                    String[] allowedProducts = {
                            "bd29755d-b6bc-4b9f-9e62-7dabeaff086b", "bfbcb65e-0245-4705-8598-dca56550aa99",
                            "1dadc5ab-27df-4d33-b949-8488d4651611", "010c8495-2737-4467-9d1b-e9c17df48266",
                            "211d4583-5286-49be-a39e-2d990f9128c6", "01d8c835-3e9a-4ea8-ad45-2eb50bb2331c",
                            "71416c57-b1f3-4034-bac5-6eb3c166e262", "bd945ac5-b3c4-45f4-8383-606eb03cda3f",
                            "0df2787c-f9c3-452a-bed5-1037c4f5ab4c", "656abdc4-6df7-4cd1-b19d-01bfa9450f2c",
                            "6af31eb4-561a-4275-8402-8095a67045a9", "c1c36681-ad6b-4a8c-8022-ced0ba96b417",
                            "4cf81390-7e76-47de-be26-a2cedaf56dd1", "815d8478-57e8-46e6-b454-b5b4089f6ebf",
                            "b49afbc6-e840-4109-a054-c6b527d7d950", "c4bf90d8-ff7f-4843-b842-66cf19caf74d",
                            "15c348c2-3c48-4559-9da8-9764850cd4b7", "5fcd47bf-bc34-4ddc-8d0d-1142f0b897e7",
                            "0a7ff1d9-b32a-4d26-97e2-7728c2bcf4d1", "92ca2317-8e88-44e7-ae1f-bbfe4529b123",
                            "67173dc1-78fd-4429-b204-afdf1a4ddda2", "9c612bef-60aa-40c7-9978-345134eca29a",
                            "07d98efc-cfff-4488-90ea-a41d22775b96", "8d91e74a-db72-41c5-8b9d-5b5b699ef561",
                            "e684168c-d602-49a7-bae2-bde7e0eece44", "95d2e56a-d266-4c68-9400-3b0c0ab1fd4c"};
//                    Log.d(TAG, "onClick: "+productModel.getName()+" id: "+productModel.getId());

                    //Restricting handling purchases of various products
                    if(InternalDataBase.getInstance(context).getCartType().equals("Purchase")){
                        restricted = true;
                        for(int i = 0; i < allowedProducts.length; i++){
                            if(productModel.getId().equals(allowedProducts[i])){
                                restricted = false;
                                break;
                            }
                        }
                    }

                    if(!restricted){
                        boolean inCart = false;
                        //checking if the product is in the cart, and if it is, remove it
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
