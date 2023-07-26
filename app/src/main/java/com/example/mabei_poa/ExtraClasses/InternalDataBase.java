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

public class InternalDataBase {

    private static InternalDataBase instance;
    //creating a sharedPref to make SaleToCustomerActivity faster, remove loading
    public static SharedPreferences sharedPref;
    public static String ALL_PRODUCTS = "ALL_PRODUCTS";
    public static String UNSAVED_NOTES = "UNSAVED_NOTES";
    public static String SYNC_STATUS = "SYNC_STATUS";

    private static ArrayList<NoteModel> unsavedNotes;

    private InternalDataBase(Context context){
        SharedPreferences.Editor editor;
        Gson gson = new Gson();

        sharedPref = context.getSharedPreferences("Products_db", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        if(getAllProducts() == null){
            editor.putString(ALL_PRODUCTS,gson.toJson(new ArrayList<ProductModel>()));
            editor.commit();
        }
        if(getUnsavedNotes() == null){
            editor.putString(UNSAVED_NOTES,gson.toJson(new ArrayList<NoteModel>()));
            editor.commit();
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
            editor.commit();
            return true;
        }
        return false;
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
                    editor.commit();
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
        editor.commit();
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
            editor.commit();
            Log.d(TAG, "addToUnsavedNotes: successfully added to unsaved notes. Size "+noteModels.size());
            return true;
        }
        return false;
    }

    public void setSyncStatus(boolean value){
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.remove(SYNC_STATUS);
        editor.putBoolean(SYNC_STATUS,value);
        editor.commit();
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

    public static InternalDataBase getInstance(Context context){
        if(instance == null){
            instance = new InternalDataBase(context);
            return instance;
        } else
            return instance;
    }
}
