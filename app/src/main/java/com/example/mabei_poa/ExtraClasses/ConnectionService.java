package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

public class ConnectionService extends Service {


    public ConnectionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
//                while(isOnForeground()){
//                    Log.d(TAG, "run: service is running.. ");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                Log.d(TAG, "run: In background now");
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isOnForeground(){
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if(appProcesses == null)
            return false;

        final String packageName = getApplicationContext().getPackageName();
        for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses){
            if(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}