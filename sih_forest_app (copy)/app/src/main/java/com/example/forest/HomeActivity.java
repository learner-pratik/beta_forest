package com.example.forest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    //Button btnMap,btnPrediction,btnTask;
    private static final String TAG = "HomeActivity";

    public static boolean iflag = false;
    public static boolean mqttflag = false;

    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static boolean location_access = false;

    public static Map<String, List<String>> animal_info;
    public static Map<String, List<String>> animal_location_info;

    LinearLayout cvMap,cvPrediction,cvTask,cvAlert;

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    public static Intent internetService;
    public static Intent mqttService;
    public static Intent forestService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cvMap=findViewById(R.id.cvMap);
        cvPrediction=findViewById(R.id.cvPrediction);
        cvTask=findViewById(R.id.cvTask);
        cvAlert=findViewById(R.id.cvAlert);

        internetService = new Intent(HomeActivity.this, InternetService.class);
        mqttService = new Intent(HomeActivity.this, MqttService.class);
        forestService = new Intent(HomeActivity.this, ForestService.class);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        broadcastReceiver = new WifiBroadcastReceiver(wifiManager, connectivityManager);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.EXTRA_WIFI_STATE);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        if (hasPermission()) {
            getCurrentLocation();
        } else {
            requestPermission();
        }

        if (wifiManager.isWifiEnabled()) {
            if (isNetworkAvailable()) {
                mqttflag = false;
                startService(new Intent(getApplicationContext(), InternetService.class));
            } else {
                if (isMobileDataEnabled() && isNetworkAvailable()) {
                    mqttflag = false;
                    Toast.makeText(this, "Using Mobile Data", Toast.LENGTH_SHORT).show();
                    startService(new Intent(getApplicationContext(), InternetService.class));
                } else {
                    mqttflag = true;
                    startService(new Intent(getApplicationContext(), MqttService.class));
                    startService(new Intent(getApplicationContext(), ForestService.class));
                }
            }
        } else {
            Log.d(TAG,"home toast called");
            Toast.makeText(this, "Wifi is disabled. Enable Wifi", Toast.LENGTH_SHORT).show();
            if (isMobileDataEnabled() && isNetworkAvailable()) {
                Toast.makeText(this, "Using Mobile Data", Toast.LENGTH_SHORT).show();
                mqttflag = false;
                startService(new Intent(getApplicationContext(), InternetService.class));
            }
        }

        cvTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(HomeActivity.this,TaskActivity.class);
                startActivity(a);
            }
        });

        cvMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(HomeActivity.this,LocationActivity.class);
                startActivity(a);
            }
        });

        cvPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(HomeActivity.this,Prediction.class);
                startActivity(a);
            }
        });

        cvAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(HomeActivity.this, AlertActivity.class);
                a.putExtra("callingActivity", 1001);
                startActivity(a);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void getCurrentLocation() {
        Log.d(TAG, "permission granted");
        location_access = true;
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                getCurrentLocation();
            } else {
                requestPermission();
            }
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_LOCATION)) {
                Toast.makeText(
                        HomeActivity.this,
                        "Location permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[] {PERMISSION_LOCATION}, PERMISSIONS_REQUEST);
        }
    }
}

