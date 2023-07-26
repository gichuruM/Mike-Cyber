package com.example.mabei_poa;

import static com.example.mabei_poa.ExtraClasses.ConnectivityReceiver.noConnectivity;
import static com.example.mabei_poa.ProductsActivity.TAG;
import static com.example.mabei_poa.SaleToCustomerActivity.cartProductsList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

import com.example.mabei_poa.ExtraClasses.ConnectivityReceiver;
import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.ExtraClasses.SyncNotesRunnable;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.databinding.ActivityHomeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity{

    ActivityHomeBinding binding;
    Animation rotateAnimation;

    private HandlerThread handlerThread = new HandlerThread("SyncHandler");
    private Handler threadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

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
                startActivity(new Intent(HomeActivity.this,SaleToCustomerActivity.class));
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
        CardView adjustStock = view.findViewById(R.id.adjustStock);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //clearing the cart if it has any items
        cartProductsList.clear();
        if(InternalDataBase.getInstance(HomeActivity.this).getSyncStatus())
            binding.syncUnsavedData.setVisibility(View.VISIBLE);
        else
            binding.syncUnsavedData.setVisibility(View.GONE);
        //Finding out about network capabilities
//        if(checkConnection(HomeActivity.this))
//            Toast.makeText(this, "Connected to internet", Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
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