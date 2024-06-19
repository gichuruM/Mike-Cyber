package com.example.Cyber.Adapter;

import static com.example.Cyber.ProductsActivity.TAG;
import static com.example.Cyber.SaleToCustomerActivity.cartProductsList;
import static com.example.Cyber.SaleToCustomerActivity.totalCartAmount;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Interface.CartItemClickedInterface;
import com.example.Cyber.Model.CartModel;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.R;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder>{

    private Context context;
    private final CartItemClickedInterface cartItemClickedInterface;
    private View scanEditView;

    public static String scanFirstDigit = "";

    public CartAdapter(Context context, CartItemClickedInterface cartItemClickedInterface, View scanEditView) {
        this.context = context;
        this.cartItemClickedInterface = cartItemClickedInterface;
        this.scanEditView = scanEditView;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_cart_item_layout,parent,false);
        return new MyViewHolder(view, cartItemClickedInterface, scanEditView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CartModel cartModel = cartProductsList.get(position);

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(context).getAllProducts();
        for(ProductModel p: allProducts){
            if(cartModel.getProductId().equals(p.getId())){
                ProductModel cartItem = p;

                if(InternalDataBase.getInstance(context).getCartType().equals("Purchase")){
                    holder.cartItemPrice.setText(String.valueOf(cartItem.getPurchasePrice()));
                    holder.cartItemTotal.setText(String.valueOf(cartModel.getQuantity()*cartItem.getPurchasePrice()));
                }
                else if(InternalDataBase.getInstance(context).getCartType().equals("Sale")){
                    holder.cartItemPrice.setText(String.valueOf(cartItem.getSellingPrice()));
                    holder.cartItemTotal.setText(String.valueOf(cartModel.getProductTotal()));
                } else {
                    Log.d(TAG, "onBindViewHolder: noType");
                }

                holder.cartItemName.setText(cartItem.getName());
                holder.cartItemUnits.setText(cartItem.getUnits());
                //holder.cartItemTotal.setText(String.valueOf(cartModel.getProductTotal()));
                holder.cartQuantity.setText(String.valueOf(cartModel.getQuantity()));

                Glide.with(context)
                        .load(cartItem.getImage())
                        .into(holder.cartItemImg);

                totalCartAmount();
            }
        }
    }

    @Override
    public int getItemCount() {
        return cartProductsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView cartItemName, cartItemPrice, cartItemUnits, cartItemTotal;
        private ImageView removeItemCart, cartItemImg;
        private EditText cartQuantity;

        public MyViewHolder(@NonNull View itemView, CartItemClickedInterface cartClickedInterface, View scanEditView) {
            super(itemView);

            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            cartItemUnits = itemView.findViewById(R.id.cartItemUnits);
            cartItemTotal = itemView.findViewById(R.id.cartItemTotal);
            removeItemCart = itemView.findViewById(R.id.removeItemCart);
            cartItemImg = itemView.findViewById(R.id.cartItemImg);
            cartQuantity = itemView.findViewById(R.id.cartQuantity);

            removeItemCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos == RecyclerView.NO_POSITION) return;

                    cartClickedInterface.onItemClick(pos);
                }
            });

            cartQuantity.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if(event.getDevice().isExternal()){
                            if(event.getKeyCode() == KeyEvent.KEYCODE_0)
                                scanFirstDigit = "0";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_1)
                                scanFirstDigit = "1";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_2)
                                scanFirstDigit = "2";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_3)
                                scanFirstDigit = "3";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_4)
                                scanFirstDigit = "4";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_5)
                                scanFirstDigit = "5";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_6)
                                scanFirstDigit = "6";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_7)
                                scanFirstDigit = "7";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_8)
                                scanFirstDigit = "8";
                            else if(event.getKeyCode() == KeyEvent.KEYCODE_9)
                                scanFirstDigit = "9";
                            else
                                scanFirstDigit = "";

                            scanEditView.requestFocus();
                            return true;
                        } else
                            return false;
                    } else return false;
                }
            });

            cartQuantity.addTextChangedListener(new TextWatcher() {
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

                    cartClickedInterface.onTextChange(pos,s.toString(), cartItemTotal);
                }
            });
        }
    }
}
