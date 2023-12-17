package com.example.mabei_poa;

import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.ExtraClasses.SyncNotesRunnable;
import com.example.mabei_poa.ExtraClasses.SyncTransactionRunnable;
import com.example.mabei_poa.Fragment.AllProductsFragment;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.Model.TransactionModel;
import com.example.mabei_poa.databinding.ActivityHomeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity{

    ActivityHomeBinding binding;
    Animation rotateAnimation;

    public static String SHOP_USER_UID = "zauL5ledkhPxy6NBpHmsO1upOrE2";
    public static String userUID = "";
    private HandlerThread handlerThread = new HandlerThread("SyncHandler");
    private Handler threadHandler;

    static public DatabaseReference transactionDBRef = FirebaseDatabase.getInstance().getReference("transactions");
    static public ValueEventListener transactionEventListener;
    static public DatabaseReference productDBRef = FirebaseDatabase.getInstance().getReference("products");
    static public ValueEventListener productEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Resetting the cartType to noType
        InternalDataBase.getInstance(HomeActivity.this).setCartType("noType");

        binding.products.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProductsActivity.class);
                intent.putExtra("type","Default");
                startActivity(intent);
            }
        });

        binding.addition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }
        });

        binding.saleToCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,SaleToCustomerActivity.class);
                InternalDataBase.getInstance(HomeActivity.this).setCartType("Sale");
                startActivity(intent);
            }
        });

        binding.transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,TransactionActivity.class));
            }
        });

        binding.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userUID.equals(SHOP_USER_UID))
                    Toast.makeText(HomeActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(HomeActivity.this,ReportsActivity.class));
            }
        });

        binding.notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, NotesActivity.class));
            }
        });

        binding.syncUnsavedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection(HomeActivity.this)){
                    rotateAnimation = AnimationUtils.loadAnimation(HomeActivity.this,R.anim.rotation);
                    binding.syncUnsavedData.startAnimation(rotateAnimation);

                    handlerThread.start();
                    threadHandler = new Handler(handlerThread.getLooper());

                    if(InternalDataBase.getInstance(HomeActivity.this).getUnsavedNotes().size() > 0)
                        SyncNotes();
                    if(InternalDataBase.getInstance(HomeActivity.this).getOfflineTransactions().size() > 0)
                        SyncTransactions();
                } else
                    Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        InternalDataBase.getInstance(this);

        ArrayList<ProductModel> allProducts = InternalDataBase.getInstance(this).getAllProducts();
        ArrayList<TransactionModel> allTransactions = InternalDataBase.getInstance(this).getAllTransactions();

//        Initializing product event listener if it isn't initialized yet
        Log.d(TAG, "onCreate: producteventListener"+productEventListener);
        if(allProducts.isEmpty() || productEventListener == null){
            new Thread(() -> {
                Log.d(TAG, "onCreate: happening");
                //Ensuring there isn't more than one eventListener on the dataRef
                if(productEventListener != null){
                    productDBRef.removeEventListener(productEventListener);
                    productEventListener = null;
                    Log.d(TAG, "onCreate: also happening");
                }

                productEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<ProductModel> finalAllProductsList = new ArrayList<>();
//                          Log.d(TAG, "onDataChange: valueEventListener triggered");
                        for(DataSnapshot snap: snapshot.getChildren()){
                            ProductModel product = snap.getValue(ProductModel.class);

                            if(product != null)
                                finalAllProductsList.add(product);
                        }
                        Log.d(TAG, "onDataChange: products initialized for the 1st time, products updated");
                        InternalDataBase.getInstance(HomeActivity.this).batchAdditionToAllProducts(finalAllProductsList);
                        Toast.makeText(HomeActivity.this, "Products updated!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                productDBRef.addValueEventListener(productEventListener);
            }).start();
        }

        //Initializing transaction event listener if it isn't initialized yet
        Log.d(TAG, "onCreate: transactionEventListener"+transactionEventListener);
        if(allTransactions.isEmpty() || transactionEventListener == null) {
            new Thread(() -> {
                Log.d(TAG, "onCreate: happening");
                //Ensuring theres just one event listener in the database
                if(transactionEventListener != null){
                    transactionDBRef.removeEventListener(transactionEventListener);
                    transactionEventListener = null;
                    Log.d(TAG, "onCreate: also happening");
                }

                transactionEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<TransactionModel> allTransactions = new ArrayList<>();

                        for(DataSnapshot snap: snapshot.getChildren()){
                            TransactionModel transaction = snap.getValue(TransactionModel.class);

                            if(transaction != null)
                                allTransactions.add(transaction);
                        }
                        Collections.reverse(allTransactions);
                        Log.d(TAG, "onDataChange: Transactions product size "+allTransactions.size()+" transaction updated");
                        InternalDataBase.getInstance(HomeActivity.this).batchAdditionToAllTransactions(allTransactions);
                        Toast.makeText(HomeActivity.this, "Transactions updated!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: Error while retrieving transactions "+error.getMessage());
                    }
                };

                transactionDBRef.orderByChild("timeInMillis").addValueEventListener(transactionEventListener);
            }).start();
        }

        Log.d(TAG, "onCreate: transactionEventListener"+transactionEventListener);
        Log.d(TAG, "onCreate: productEventListener"+productEventListener);
    }

    private void SyncNotes() {
        ArrayList<NoteModel> unsavedNotes = InternalDataBase.getInstance(HomeActivity.this).getUnsavedNotes();
        Log.d(TAG, "run: Uploading notes...");

        for(NoteModel n: unsavedNotes){
            Log.d(TAG, "run: posting");
            SyncNotesRunnable noteSync = new SyncNotesRunnable(HomeActivity.this, n, binding.syncUnsavedData);
            threadHandler.post(noteSync);
        }
    }

    private void SyncTransactions(){
        ArrayList<TransactionModel> unsavedTransactions = InternalDataBase.getInstance(HomeActivity.this).getOfflineTransactions();
        Log.d(TAG, "SyncTransactions: uploading transactions...");

        for(TransactionModel t: unsavedTransactions){
            SyncTransactionRunnable transactionSync = new SyncTransactionRunnable(HomeActivity.this,t, binding.syncUnsavedData);
            threadHandler.post(transactionSync);
        }
    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet,(RelativeLayout) findViewById(R.id.homeActivity),false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        CardView purchaseFromVendor = view.findViewById(R.id.purchaseFromVendor);
        CardView moneyTracker = view.findViewById(R.id.moneyTracker);
        CardView newTransaction = view.findViewById(R.id.newTransaction);
        CardView stockAnalysis = view.findViewById(R.id.stockAnalysis);

        moneyTracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();

                Intent intent = new Intent(HomeActivity.this,MoneyTrackerActivity.class);
                startActivity(intent);
            }
        });

        purchaseFromVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();
                
                Intent intent = new Intent(HomeActivity.this,SaleToCustomerActivity.class);
                InternalDataBase.getInstance(HomeActivity.this).setCartType("Purchase");
                startActivity(intent);
            }
        });

        newTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();

                if(userUID.equals(SHOP_USER_UID))
                    Toast.makeText(HomeActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(HomeActivity.this,NewTransaction.class));
            }
        });

        stockAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();

                if(userUID.equals(SHOP_USER_UID))
                    Toast.makeText(HomeActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(HomeActivity.this,StockAnalysis.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //clearing the cart if it has any items
        cartProductsList.clear();
        InternalDataBase.getInstance(this).setNewCart(cartProductsList);
        if(InternalDataBase.getInstance(HomeActivity.this).getSyncStatus())
            binding.syncUnsavedData.setVisibility(View.VISIBLE);
        else
            binding.syncUnsavedData.setVisibility(View.GONE);
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
        Network network = connectivityManager.getActiveNetwork();
        if(network != null){
            NetworkCapabilities cap = connectivityManager.getNetworkCapabilities(network);

            if(cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                return true;
            else
                return false;
        }
        return false;
    }
}