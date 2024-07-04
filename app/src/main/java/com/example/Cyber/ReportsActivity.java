package com.example.Cyber;

import static com.example.Cyber.Adapter.ReportProductsAdapter.selectedProducts;
import static com.example.Cyber.ProductsActivity.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import androidx.appcompat.widget.SearchView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.Cyber.Adapter.ReportDataAdapter;
import com.example.Cyber.Adapter.ReportProductTransactionAdapter;
import com.example.Cyber.Adapter.ReportProductsAdapter;
import com.example.Cyber.Adapter.ReportSummaryProductAdapter;
import com.example.Cyber.Adapter.ReportViewAdapter;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Interface.ReportProductClickedInterface;
import com.example.Cyber.Model.ProductModel;
import com.example.Cyber.Model.ProductTransactionModel;
import com.example.Cyber.Model.TransactionModel;
import com.example.Cyber.databinding.ActivityReportsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ReportsActivity extends AppCompatActivity implements ReportProductClickedInterface {

    ActivityReportsBinding binding;
    public static String reportSettingType = "";
    ReportViewAdapter reportViewAdapter;
    public static ReportProductsAdapter reportProductAdapter;
    public static ReportProductTransactionAdapter transactionAdapter;
    public static ReportSummaryProductAdapter productSummaryAdapter;
    public static String datePicked = "";
    SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
    public static String thisYear, thisMonth, today;
    ArrayList<ProductTransactionModel> productTransactions;
    ArrayList<ProductTransactionModel> summaryProductTransactions;
    ArrayList<ProductTransactionModel> interimSummaryTransactions;
    public static ReportDataAdapter reportDataAdapter;
    public static ArrayList<TransactionModel> transactionReportList;

    enum ReportType {
        defaultReport,
        productReport,
        summaryReport
    }

    ReportType currentReportType = ReportType.defaultReport;
    double revenue = 0, profit = 0, waterLessProfit = 0;
    double totalProductRevenue = 0, totalProductProfit = 0;
    double summaryTotalRevenue = 0, summaryTotalProfit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        transactionReportList = new ArrayList<>();
        reportDataAdapter = new ReportDataAdapter(this, transactionReportList);

        //Settings for the report
        reportSettingType = getString(R.string.report1);

        ArrayList<String> reportType = new ArrayList<>();
        reportType.add(getString(R.string.report1));
        reportType.add(getString(R.string.report2));
        reportType.add(getString(R.string.report3));

        ArrayAdapter<String> reportTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reportType
        );

        binding.reportType.setAdapter(reportTypeAdapter);

        binding.reportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportSettingType = reportType.get(position);

                binding.reportTvType.setText(reportSettingType);
                //Updating the default fragment transactions
                if(currentReportType == ReportType.defaultReport){
                    getDefaultTransactions();
                }
                else if(currentReportType == ReportType.productReport){
                    getProductTransactions();
                }
                else if(currentReportType == ReportType.summaryReport){
                    getSummaryTransactions();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.btnReportPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker();
            }
        });

        binding.reportViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 0){
                    currentReportType = ReportType.defaultReport;
                    binding.reportRevenue.setText("0");

                    getDefaultTransactions();
                }
                else if(position == 1){
                    currentReportType = ReportType.productReport;
                    binding.reportRevenue.setText("0");

                    getProductTransactions();
                } else if(position == 2){
                    currentReportType = ReportType.summaryReport;
                    binding.reportRevenue.setText("0");

                    getSummaryTransactions();
                }
            }
        });

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(this).getAllProducts();
        reportProductAdapter = new ReportProductsAdapter(this, this::reportProductClicked ,allProducts);

        reportViewAdapter = new ReportViewAdapter(this);
        binding.reportViewPager.setAdapter(reportViewAdapter);

        thisYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        thisMonth = new SimpleDateFormat("M", Locale.getDefault()).format(new Date());
        today = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());

        datePicked = today+"/"+thisMonth+"/"+thisYear;

        binding.btnReportPickDate.setText(datePicked);

        //Initialize the transaction adapter
        productTransactions = new ArrayList<>();
        transactionAdapter = new ReportProductTransactionAdapter(this, productTransactions);

        //Initialize the Summary adapter
        summaryProductTransactions = new ArrayList<>();
        interimSummaryTransactions = new ArrayList<>();
        productSummaryAdapter = new ReportSummaryProductAdapter(this, summaryProductTransactions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search product");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                reportProductAdapter.getFilter().filter(s);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void reportProductClicked() {
        getProductTransactions();
    }

    private void getSummaryTransactions() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();

        ArrayList<TransactionModel> allTransaction = InternalDataBase.getInstance(this).getAllTransactions();
        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(this).getAllProducts();
        interimSummaryTransactions.clear();
        summaryTotalProfit = 0; summaryTotalRevenue = 0;

        Collections.reverse(allTransaction);
        summaryProductTransactions.clear();
        productSummaryAdapter.notifyDataSetChanged();

        //Adding all products to the ProductTransactions array
        int count = 0;
        Map<String, Integer> productTransactionsIndex = new HashMap<>();
        for(ProductModel p: allProducts){
            ProductTransactionModel productTransaction = new ProductTransactionModel(p.getId(), 0L, 0, 0, 0, 0, 0, 0);
            interimSummaryTransactions.add(productTransaction);
            productTransactionsIndex.put(p.getId(), count);
            count++;
        }

        for (TransactionModel t : allTransaction) {
            Date date = new Date(t.getTimeInMillis());
            if(!t.getTransactionType().equals("Sale"))
                continue;
            //Getting transactions for the chosen date
            if (dateFormat.format(date).equals(datePicked)) {
                //Getting transactions with the required product
                Map<String, Double> cartDetails = t.getCartDetails();

                if (cartDetails != null) {
                    //searching through the each product in each transaction
                    for (String id : cartDetails.keySet()) {
                        for(ProductModel p: allProducts) {
                            if (p.getId().equals(id)) {
                                ProductTransactionModel productTransaction = interimSummaryTransactions.get(productTransactionsIndex.get(id));

                                if (productTransaction != null) {
                                    double totalRevenue = productTransaction.getProductTotalRevenue();
                                    double totalProfit = productTransaction.getProductTotalProfit();
                                    double totalNum = productTransaction.getProductTotalNum();

                                    double productNum = cartDetails.get(id);
                                    int productRevenue = (int) (productNum * p.getSellingPrice());

                                    int remainder = (int) (productRevenue % 5);
                                    if(remainder != 0)
                                        productRevenue += (5 - remainder);

                                    double productProfit = productRevenue - (p.getPurchasePrice() * productNum);

                                    totalNum += productNum;
                                    totalRevenue += productRevenue;
                                    totalProfit += productProfit;

                                    summaryTotalRevenue += productRevenue;
                                    summaryTotalProfit += productProfit;

                                    productTransaction.setProductTotalNum(totalNum);
                                    productTransaction.setProductTotalRevenue(totalRevenue);
                                    productTransaction.setProductTotalProfit(totalProfit);
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }

        for(ProductTransactionModel p: interimSummaryTransactions){
            if(p.getProductTotalNum() != 0){
                ProductTransactionModel copyP = p;
                summaryProductTransactions.add(copyP);
            }
        }
        Log.d(TAG, "getSummaryTransactions: summarySize "+summaryProductTransactions.size());

        if(Objects.equals(reportSettingType, getString(R.string.report1))) {
            binding.reportRevenue.setText(String.valueOf(summaryTotalRevenue));
            sortByTotalRevenue();
        } else if(Objects.equals(reportSettingType, getString(R.string.report2)) ||
                Objects.equals(reportSettingType, getString(R.string.report3))) {
            binding.reportRevenue.setText(String.format("%.1f", summaryTotalProfit));
            sortByTotalProfit();
        }

        progressDialog.dismiss();
        productSummaryAdapter.notifyDataSetChanged();
    }

    private void getProductTransactions(){

        ArrayList<TransactionModel> allTransaction = InternalDataBase.getInstance(this).getAllTransactions();
        totalProductRevenue = 0; totalProductProfit = 0;

        Collections.reverse(allTransaction);
        productTransactions.clear();
        transactionAdapter.notifyDataSetChanged();

        if(!selectedProducts.isEmpty()) {
            for (TransactionModel t : allTransaction) {
                Date date = new Date(t.getTimeInMillis());
                if(!t.getTransactionType().equals("Sale"))
                    continue;
                //Getting transactions for the chosen date
                if (dateFormat.format(date).equals(datePicked)) {
                    //Getting transactions with the required product
                    Map<String, Double> cartDetails = t.getCartDetails();

                    if (cartDetails != null) {
                        //searching through the each product in each transaction
                        for (ProductModel p : selectedProducts) {
                            for (String id : cartDetails.keySet()) {
                                //Found the selected product in the transactions
                                if (p.getId().equals(id)) {
                                    Long time = t.getTimeInMillis();
                                    double productNum = cartDetails.get(id);
                                    double productRevenue = productNum * p.getSellingPrice();

                                    int remainder = (int) (productRevenue % 5);
                                    if(remainder != 0)
                                        productRevenue += (5 - remainder);

                                    double productProfit = productRevenue - (p.getPurchasePrice() * productNum);

                                    totalProductRevenue += productRevenue;
                                    totalProductProfit += productProfit;

                                    ProductTransactionModel productTransaction =
                                            new ProductTransactionModel(id, time, productNum, productRevenue, productProfit,
                                                    0,0,0);

                                    productTransactions.add(productTransaction);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(Objects.equals(reportSettingType, getString(R.string.report1))) {
                binding.reportRevenue.setText(String.valueOf(totalProductRevenue));
            } else if(Objects.equals(reportSettingType, getString(R.string.report2)) ||
                    Objects.equals(reportSettingType, getString(R.string.report3))) {
                binding.reportRevenue.setText(String.format("%.1f", totalProductProfit));
            }
        } else binding.reportRevenue.setText("0");

        transactionAdapter.notifyDataSetChanged();
        //Log.d(TAG, "getTransactions: size "+productTransactions.size());
    }

    private void getDefaultTransactions() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("transaction");
        progressDialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");

        revenue = 0; profit = 0; waterLessProfit = 0;

        transactionReportList.clear();
        ArrayList<TransactionModel> allTransactions = InternalDataBase.getInstance(this).getAllTransactions();
        Collections.reverse(allTransactions);

        for(TransactionModel transaction: allTransactions){
            Date date = new Date(transaction.getTimeInMillis());

            if(dateFormat.format(date).equals(datePicked)){
                if(Objects.equals(reportSettingType, getString(R.string.report1)) &&
                        transaction.getTransactionType().equals("Sale")){
                    transactionReportList.add(transaction);
                    revenue += transaction.getTotalAmount();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report2)) &&
                        transaction.getTransactionType().equals("Sale")) {
                    transactionReportList.add(transaction);
                    profit += transaction.getProfit();
                }
                else if(Objects.equals(reportSettingType, getString(R.string.report3)) &&
                        transaction.getTransactionType().equals("Sale")){
                    transactionReportList.add(transaction);
                    waterLessProfit += transaction.getWaterlessProfit();
                }
            }
        }

        if(Objects.equals(reportSettingType, getString(R.string.report1)))
            binding.reportRevenue.setText(String.valueOf(revenue));
        else if(Objects.equals(reportSettingType, getString(R.string.report2)))
            binding.reportRevenue.setText(String.format("%.1f",profit));
        else if(Objects.equals(reportSettingType, getString(R.string.report3)))
            binding.reportRevenue.setText(String.format("%.1f",waterLessProfit));

        progressDialog.dismiss();
        reportDataAdapter.notifyDataSetChanged();
    }

    private void DatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                datePicked = dayOfMonth+"/"+month+"/"+year;
                binding.btnReportPickDate.setText(datePicked);
                binding.reportRevenue.setText("0");

                if(currentReportType == ReportType.defaultReport)
                    getDefaultTransactions();
                else if(currentReportType == ReportType.productReport)
                    getProductTransactions();
                else if(currentReportType == ReportType.summaryReport)
                    getSummaryTransactions();
            }
        }, Integer.parseInt(thisYear), Integer.parseInt(thisMonth)-1, Integer.parseInt(today));

        dialog.show();
    }

    private void sortByTotalRevenue(){
        Collections.sort(summaryProductTransactions, new Comparator<ProductTransactionModel>() {
            @Override
            public int compare(ProductTransactionModel o1, ProductTransactionModel o2) {
                return Double.compare(o1.getProductTotalRevenue(), o2.getProductTotalRevenue());
            }
        });
        Collections.reverse(summaryProductTransactions);
    }

    private void sortByTotalProfit(){
        Collections.sort(summaryProductTransactions, new Comparator<ProductTransactionModel>() {
            @Override
            public int compare(ProductTransactionModel o1, ProductTransactionModel o2) {
                return Double.compare(o1.getProductTotalProfit(), o2.getProductTotalProfit());
            }
        });
        Collections.reverse(summaryProductTransactions);
    }
}