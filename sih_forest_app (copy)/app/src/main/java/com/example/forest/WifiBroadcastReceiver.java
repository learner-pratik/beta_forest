package com.example.forest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiBroadcastReceiver";

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    public WifiBroadcastReceiver(WifiManager wifiManager, ConnectivityManager connectivityManager) {
        this.wifiManager = wifiManager;
        this.connectivityManager = connectivityManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            context.stopService(HomeActivity.internetService);
            context.stopService(HomeActivity.mqttService);
            context.stopService(HomeActivity.forestService);

            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiManager.WIFI_STATE_ENABLED) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isNetworkAvailable()) {
                    HomeActivity.mqttflag = false;
                    context.startService(HomeActivity.internetService);
                } else {
                    HomeActivity.mqttflag = true;
                    context.startService(HomeActivity.mqttService);
                    context.startService(HomeActivity.forestService);
                }
            } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                Log.d(TAG,"receiver toast called");
                Toast.makeText(context, "Wifi is disabled. Enable Wifi", Toast.LENGTH_SHORT).show();
                if (isMobileDataEnabled()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isNetworkAvailable()) {
                        Toast.makeText(context, "Using Mobile data", Toast.LENGTH_SHORT).show();
                        HomeActivity.mqttflag = false;
                        context.startService(HomeActivity.internetService);
                    } else {
                        HomeActivity.mqttflag = true;
                        context.startService(HomeActivity.mqttService);
                        context.startService(HomeActivity.forestService);
                    }
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false; // Assume disabled
        try {
            Class cmClass = Class.forName(connectivityManager.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(connectivityManager);
        } catch (Exception e) {
            // Some problem accessible private API
            e.printStackTrace();
        }
        return mobileDataEnabled;
    }
}
