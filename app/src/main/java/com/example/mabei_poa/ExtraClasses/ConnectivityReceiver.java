package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.NoteModel;

import java.util.ArrayList;

public class ConnectivityReceiver extends BroadcastReceiver {

    public static boolean noConnectivity = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if(noConnectivity){
//                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: Disconnected");
            } else{
//                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: Connected");
            }
        }
    }
}
