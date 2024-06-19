package com.example.Cyber;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.Cyber.Adapter.StockAnalysisAdapter;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.databinding.ActivityStockAnalysisBinding;

import java.util.ArrayList;

public class StockAnalysis extends AppCompatActivity {

    ActivityStockAnalysisBinding binding;
    ArrayList<ProductModel> productModels;

    public static String stockType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productModels = new ArrayList<>();

        ArrayList<String> stockAnalysisType = new ArrayList<>();
        stockAnalysisType.add(getString(R.string.costOfBuying));
        stockAnalysisType.add(getString(R.string.costOfSelling));

        ArrayAdapter<String> stockAnalysisAdapter = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                stockAnalysisType
        );

        binding.stockType.setAdapter(stockAnalysisAdapter);
        binding.stockAnalysisRecView.setHasFixedSize(true);
        binding.stockAnalysisRecView.setLayoutManager(new LinearLayoutManager(this));

        binding.stockType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stockType = stockAnalysisType.get(position);
                binding.stockResultTitle.setText(stockType);
                getProductData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void getProductData() {

        productModels.clear();

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(this).getAllProducts();
        double finalResult = 0;

        for(ProductModel ds: allProducts){
            //Removing water from stock counting
            if(!(ds.getId().equals("61cfbdc7-63fa-4012-9f1f-220f2e0d0863") ||
                    ds.getId().equals("53fbab78-2f31-40c9-b4bb-690096416bc7"))){
                productModels.add(ds);
                if(stockType.equals(getString(R.string.costOfBuying)))
                    finalResult += (ds.getPurchasePrice()*ds.getQuantity());
                else
                    finalResult += (ds.getSellingPrice()*ds.getQuantity());
            }
        }

        StockAnalysisAdapter stockAdapter = new StockAnalysisAdapter(StockAnalysis.this, productModels);
        binding.stockAnalysisRecView.setAdapter(stockAdapter);
        String result = String.format("%.1f",finalResult);
        binding.stockResultSum.setText(result);
        stockAdapter.notifyDataSetChanged();

    }
}