package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.Model.ProductModel;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InternalDataBase {

    private static InternalDataBase instance;
    //creating a sharedPref to make SaleToCustomerActivity faster, remove loading
    public static SharedPreferences sharedPref;
    public static String ALL_PRODUCTS = "ALL_PRODUCTS";
    public static String UNSAVED_NOTES = "UNSAVED_NOTES";
    public static String SYNC_STATUS = "SYNC_STATUS";
    public static String MONEY_TRACKING = "MONEY_TRACKING";
    public static String CART_TYPE = "CART_TYPE";

    private static ArrayList<NoteModel> unsavedNotes;

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
        if(getCartType() == null){
            editor.putString(CART_TYPE,"noType");
            editor.apply();
        }
    }

    public void batchAdditionToAllProducts(ArrayList<ProductModel> manyProducts){
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        Log.d(TAG, "batchAdditionToAllProducts: adding many products");
        editor.remove(ALL_PRODUCTS);
        editor.putString(ALL_PRODUCTS,gson.toJson(manyProducts));
        editor.commit();
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
        Log.d(TAG, "addToMoneyTracking: Adding Starting Tracking Capital");

        moneyTracking.put(date,capital);
        editor.remove(MONEY_TRACKING);
        editor.putString(MONEY_TRACKING,gson.toJson(moneyTracking));
        editor.apply();

        return true;
    }

    public Map<String, Integer> getTrackingData(){
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        String json = sharedPref.getString(MONEY_TRACKING,"null");
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

    public static InternalDataBase getInstance(Context context){
        if(instance == null){
            instance = new InternalDataBase(context);
            return instance;
        } else
            return instance;
    }
}
