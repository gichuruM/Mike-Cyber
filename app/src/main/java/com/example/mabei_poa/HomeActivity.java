package com.example.mabei_poa;

import static com.example.mabei_poa.Adapter.AllProductsAdapter.fullProductModelArrayList;
import static com.example.mabei_poa.ExtraClasses.ConnectivityReceiver.noConnectivity;
import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.mabei_poa.Adapter.AllProductsAdapter;
import com.example.mabei_poa.ExtraClasses.ConnectivityReceiver;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.ExtraClasses.SyncNotesRunnable;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.Model.ProductModel;
import com.example.mabei_poa.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity{

    ActivityHomeBinding binding;
    Animation rotateAnimation;

    public static String SHOP_USER_UID = "zauL5ledkhPxy6NBpHmsO1upOrE2";
    public static String userUID = "";
    private HandlerThread handlerThread = new HandlerThread("SyncHandler");
    private Handler threadHandler;

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
                SyncNotes();
            }
        });

        InternalDataBase.getInstance(this);

        ArrayList<ProductModel> productsList = InternalDataBase.getInstance(this).getAllProducts();
        //Initializing internal data base product list if it's empty
        if(productsList == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fullProductModelArrayList.clear();

                    FirebaseFirestore.getInstance().collection("products")
                            .orderBy("name", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();

                                    for(DocumentSnapshot ds: dsList){
                                        ProductModel product = ds.toObject(ProductModel.class);
                                        fullProductModelArrayList.add(product);
                                    }

                                    InternalDataBase.getInstance(HomeActivity.this).batchAdditionToAllProducts(fullProductModelArrayList);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                }
            });
        }
    }

    private void SyncNotes() {
        if(checkConnection(HomeActivity.this)){
            rotateAnimation = AnimationUtils.loadAnimation(HomeActivity.this,R.anim.rotation);
            binding.syncUnsavedData.startAnimation(rotateAnimation);

            ArrayList<NoteModel> unsavedNotes = InternalDataBase.getInstance(HomeActivity.this).getUnsavedNotes();
            Log.d(TAG, "run: Uploading...");

            handlerThread.start();
            threadHandler = new Handler(handlerThread.getLooper());

            for(NoteModel n: unsavedNotes){
                Log.d(TAG, "run: posting");
                SyncNotesRunnable noteSync = new SyncNotesRunnable(HomeActivity.this, n, binding.syncUnsavedData);
                threadHandler.post(noteSync);
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
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