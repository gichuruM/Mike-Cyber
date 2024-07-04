package com.example.Cyber.ExtraClasses;

import static com.example.Cyber.ProductsActivity.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.Cyber.Model.CartModel;
import com.example.Cyber.Model.NoteModel;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.TransactionModel;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InternalDataBase {

    private static InternalDataBase instance;
    //creating a sharedPref to make SaleToCustomerActivity faster, remove loading
    public SharedPreferences sharedPref;
    public String ALL_PRODUCTS = "ALL_PRODUCTS";
    public String UNSAVED_NOTES = "UNSAVED_NOTES";
    public String SYNC_STATUS = "SYNC_STATUS";
    public String MONEY_TRACKING = "MONEY_TRACKING";
    public String FLOAT_TRACKING = "FLOAT_TRACKING";
    public String CART_TYPE = "CART_TYPE";
    public String PRODUCTS_IN_CART = "PRODUCTS_IN_CART";
    public String ALL_TRANSACTION = "ALL_TRANSACTIONS";
    public String OFFLINE_TRANSACTIONS = "OFFLINE_TRANSACTIONS";
    public String CUSTOMER_DEBT_TRACKING = "CUSTOMER_DEBT_TRACKING";
    public String UPDATE_CUSTOMER_DEBT_CART = "UPDATE_CUSTOMER_DEBT_CART";
    public String OFFLINE_DEBT_UPDATES = "OFFLINE_DEBT_UPDATES";
    public String DAY_DEBT_SUMMARY = "DAY_DEBT_SUMMARY";

    private InternalDataBase(Context context){
        SharedPreferences.Editor editor;
        Gson gson = new Gson();

        sharedPref = context.getSharedPreferences("Products_db", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        if(getAllProducts() == null){
            editor.putString(ALL_PRODUCTS,gson.toJson(new ArrayList<ProductModel>()));
            editor.apply();
        }
        if(getUnsavedNotes() == null){
            editor.putString(UNSAVED_NOTES,gson.toJson(new ArrayList<NoteModel>()));
            editor.apply();
        }
        if(getTrackingData() == null){
            Map<String, Integer> map = new HashMap<>();
            editor.putString(MONEY_TRACKING,gson.toJson(map));
            editor.apply();
        }
        if(getFloatData() == null){
            Map<String, Integer> map = new HashMap<>();
            editor.putString(FLOAT_TRACKING,gson.toJson(map));
            editor.apply();
        }
        if(getCartType() == null){
            editor.putString(CART_TYPE,"noType");
            editor.apply();
        }
        if(getCart() == null){
            editor.putString(PRODUCTS_IN_CART,gson.toJson(new ArrayList<CartModel>()));
            editor.apply();
        }
        if(getAllTransactions() == null){
            editor.putString(ALL_TRANSACTION,gson.toJson(new ArrayList<TransactionModel>()));
            editor.apply();
        }
        if(getOfflineTransactions() == null){
            editor.putString(OFFLINE_TRANSACTIONS,gson.toJson(new ArrayList<TransactionModel>()));
            editor.apply();
        }
    }

    public void batchAdditionToAllProducts(ArrayList<ProductModel> manyProducts){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        //Log.d(TAG, "batchAdditionToAllProducts: adding many products");
        editor.remove(ALL_PRODUCTS);
        editor.putString(ALL_PRODUCTS,gson.toJson(manyProducts));
        editor.apply();
    }

    public void batchAdditionToAllTransactions(ArrayList<TransactionModel> manyTransactions){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        Log.d(TAG, "batchAdditionToAllTransactions: adding many transactions");
        editor.remove(ALL_TRANSACTION);
        editor.putString(ALL_TRANSACTION,gson.toJson(manyTransactions));
        editor.apply();
    }

    public boolean addToAllProducts(ProductModel productModel){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<ProductModel> allProducts = getAllProducts();
        Log.d(TAG, "addToAllProducts: Adding one product to arraylist");
        if(allProducts == null) return false;

        if(allProducts.add(productModel)){
            editor.remove(ALL_PRODUCTS);
            editor.putString(ALL_PRODUCTS,gson.toJson(allProducts));
            editor.apply();
            return true;
        }
        return false;
    }

    public boolean addToOfflineTransactions(TransactionModel transaction){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<TransactionModel> offlineTransactions = getOfflineTransactions();

        if(offlineTransactions.add(transaction)){
            editor.remove(OFFLINE_TRANSACTIONS);
            editor.putString(OFFLINE_TRANSACTIONS,gson.toJson(offlineTransactions));
            editor.apply();
            return true;
        }
        return false;
    }
    public boolean editProduct(String id, Double quantity){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<ProductModel> allProducts = getAllProducts();
        Log.d(TAG, "addToAllProducts: editing a product");
        if(allProducts == null) return false;

        for(ProductModel product: allProducts){
            if(product.getId().equals(id)){
                product.setQuantity(quantity);
                break;
            }
        }

        editor.remove(ALL_PRODUCTS);
        editor.putString(ALL_PRODUCTS,gson.toJson(allProducts));
        editor.apply();

        for(ProductModel product: getAllProducts()){
            if(product.getId().equals(id)){
                Log.d(TAG, "editProduct: Product "+product.getName()+" quantity "+product.getQuantity());
                break;
            }
        }

        return true;
    }

    public boolean addToMoneyTracking(String date, int capital){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        Map<String, Integer> moneyTracking = getTrackingData();

        if(moneyTracking == null) return false;

        moneyTracking.put(date,capital);
        editor.remove(MONEY_TRACKING);
        editor.putString(MONEY_TRACKING,gson.toJson(moneyTracking));
        editor.apply();

        return true;
    }

    public boolean addToFloatTracking(String date, int capital){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        Map<String, Integer> floatTracking = getFloatData();

        if(floatTracking == null) return false;
        Log.d(TAG, "addToFloatTracking: Adding Starting Floating Capital");

        floatTracking.put(date,capital);
        editor.remove(FLOAT_TRACKING);
        editor.putString(FLOAT_TRACKING,gson.toJson(floatTracking));
        editor.apply();

        return true;
    }

    public Map<String, Integer> getTrackingData(){
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        String json = sharedPref.getString(MONEY_TRACKING,"null");
        Gson gson = new Gson();
        return gson.fromJson(json,type);
    }

    public Map<String, Integer> getFloatData(){
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        String json = sharedPref.getString(FLOAT_TRACKING,"null");
        Gson gson = new Gson();
        return gson.fromJson(json,type);
    }

    public boolean removeFromUnsavedNotes(NoteModel note){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<NoteModel> noteModels = getUnsavedNotes();

        if(noteModels == null) return false;

        for(NoteModel n: noteModels){
            if(n.getId().equals(note.getId())){
                if(noteModels.remove(n)){
                    editor.remove(UNSAVED_NOTES);
                    editor.putString(UNSAVED_NOTES,gson.toJson(noteModels));
                    editor.apply();
                    Log.d(TAG, "removeFromUnsavedNotes: successfully removed from unsaved list");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFromUnsavedTransactions(TransactionModel transaction){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<TransactionModel> transactionModels = getOfflineTransactions();

        for(TransactionModel t: transactionModels){
            if(t.getTransactionId().equals(transaction.getTransactionId())){
                if(transactionModels.remove(t)){
                    editor.remove(OFFLINE_TRANSACTIONS);
                    editor.putString(OFFLINE_TRANSACTIONS,gson.toJson(transactionModels));
                    editor.apply();
                    Log.d(TAG, "removeFromUnsavedTransactions: successfully removed from unsaved transactions");
                    return  true;
                }
            }
        }

        return false;
    }

    public void clearAllUnsavedNotes(){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        editor.remove(UNSAVED_NOTES);
        editor.putString(UNSAVED_NOTES,gson.toJson(new ArrayList<NoteModel>()));
        setSyncStatus(false);
        editor.apply();
    }

    public boolean addToUnsavedNotes(NoteModel note){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        ArrayList<NoteModel> noteModels = getUnsavedNotes();

        if(noteModels == null) return false;
        Log.d(TAG, "addToUnsavedNotes: current num notes "+noteModels.size());
        if(noteModels.add(note)){
            editor.remove(UNSAVED_NOTES);
            editor.putString(UNSAVED_NOTES,gson.toJson(noteModels));
            editor.apply();
            Log.d(TAG, "addToUnsavedNotes: successfully added to unsaved notes. Size "+noteModels.size());
            return true;
        }
        return false;
    }

    public void setSyncStatus(boolean value){
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.remove(SYNC_STATUS);
        editor.putBoolean(SYNC_STATUS,value);
        editor.apply();
    }

    public void setCartType(String cart){
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.remove(CART_TYPE);
        editor.putString(CART_TYPE,cart);
        editor.apply();
    }

    public void setNewCart(ArrayList<CartModel> newCart){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();

        editor.remove(PRODUCTS_IN_CART);
        editor.putString(PRODUCTS_IN_CART,gson.toJson(newCart));
        editor.apply();
    }

    public boolean getSyncStatus(){
        return(sharedPref.getBoolean(SYNC_STATUS,false));
    }

    public ArrayList<NoteModel> getUnsavedNotes(){
        Type type = new TypeToken<ArrayList<NoteModel>>(){}.getType();
        String json = sharedPref.getString(UNSAVED_NOTES,"null");
        Gson gson = new Gson();
        return gson.fromJson(json,type);
    }

    public ArrayList<ProductModel> getAllProducts(){
        Type type = new TypeToken<ArrayList<ProductModel>>(){}.getType();
        String json = sharedPref.getString(ALL_PRODUCTS,"null");
        Gson gson = new Gson();

        return gson.fromJson(json,type);
    }

    public String getCartType() {
        return sharedPref.getString(CART_TYPE,"noType");
    }

    public ArrayList<CartModel> getCart(){
        Type type = new TypeToken<ArrayList<CartModel>>(){}.getType();
        String json = sharedPref.getString(PRODUCTS_IN_CART,"null");
        Gson gson = new Gson();

        return gson.fromJson(json,type);
    }

    public ArrayList<TransactionModel> getAllTransactions(){
        Type type = new TypeToken<ArrayList<TransactionModel>>(){}.getType();
        String json = sharedPref.getString(ALL_TRANSACTION,"null");
        Gson gson = new Gson();
        
        return gson.fromJson(json,type);
    }

    public ArrayList<TransactionModel> getOfflineTransactions(){
        Type type = new TypeToken<ArrayList<TransactionModel>>(){}.getType();
        String json = sharedPref.getString(OFFLINE_TRANSACTIONS,"null");
        Gson gson = new Gson();

        return gson.fromJson(json,type);
    }

    public static InternalDataBase getInstance(Context context){
        if(instance == null){
            instance = new InternalDataBase(context);
            return instance;
        } else
            return instance;
    }
}
