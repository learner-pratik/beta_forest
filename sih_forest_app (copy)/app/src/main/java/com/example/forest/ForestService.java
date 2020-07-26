package com.example.forest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static com.example.forest.MqttService.ipAddress;

public class ForestService extends Service {

    private final String protocol = "tcp";
    private final String port = "1883";
    private final String url = protocol + "://" + ipAddress + ":" + port;

    private final String officer_id = "f-234";
    private final String officer_topic = "forest/forest-officer";
    private final String topic = officer_topic+"/"+officer_id;

    private static final String TAG = "ForestService";
    private final String clientId = MqttClient.generateClientId();
    private Intent intent;

    private static MqttAndroidClient client;
    private static IMqttToken token;

    private double latitude;
    private double longitude;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (HomeActivity.location_access == true) {

            this.intent = intent;

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(ForestService.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {

                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(ForestService.this)
                                    .removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                                latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                sendOfficerCoordinates();
                            }
                        }
                    }, Looper.getMainLooper());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendOfficerCoordinates() {

        final String officer_payload = String.valueOf(latitude)+","+String.valueOf(longitude);
        Log.d(TAG, officer_payload);
        client = new MqttAndroidClient(getApplicationContext(), url, clientId);

        try {
            token = client.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // We are connected
                System.out.println("Inside success");
                byte[] encodedPayload = new byte[0];
                MqttMessage message;
                try {
                    encodedPayload = officer_payload.getBytes("UTF-8");
                    message = new MqttMessage(encodedPayload);
                    client.publish(officer_topic, message);
                    System.out.println("message published");
                } catch (NullPointerException | UnsupportedEncodingException | MqttException e) {
                    System.out.println("error occured");
                    e.printStackTrace();
                }
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println("Inside failure");
                Log.d(TAG, String.valueOf(exception));
            }
        });

    }
}
