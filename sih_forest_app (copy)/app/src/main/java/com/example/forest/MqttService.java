package com.example.forest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.forest.Appcontroller.CHANNEL_ID;
import static com.example.forest.HomeActivity.animal_info;
import static com.example.forest.HomeActivity.animal_location_info;
import static com.example.forest.HomeActivity.mqttflag;

public class MqttService extends Service {
    private static final String TAG = "ServiceMqtt";

    public static final String ipAddress = "192.168.0.104";

    private final String protocol = "tcp";
    private final String port = "1883";
    private final String url = protocol + "://" + ipAddress + ":" + port;

    private final String hunter_topic = "forest/hunter";
    private final String camera_topic = "forest/camera-alert";
    private final String map_alert_topic = "forest/map-alert/#";
    private final int qos = 1;

    private final String clientId = MqttClient.generateClientId();
    private final MqttAndroidClient client =
            new MqttAndroidClient(this, url, clientId);

    private NotificationManagerCompat notificationManagerCompat;

    private File folder;
    private File file;
    private FileInputStream is;
    private BufferedReader reader;
    OutputStreamWriter file_writer;
    BufferedWriter buffered_writer;

    ObjectMapper mapper = new ObjectMapper();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.d(TAG, "Mqtt service started");

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    mqttflag = true;
                    Log.d(TAG, "onSuccess");
                    try {
                        String[] topics = {hunter_topic, camera_topic, map_alert_topic};
                        int[] qos_array = {1, 1, 1};
                        IMqttToken subToken = client.subscribe(topics, qos_array);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                                Log.d(TAG, "subscribed");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Log.d(TAG, "not subscribed");
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "mqtt connection lost");
                mqttflag = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "message arrived");
                if (topic.contains("forest/map-alert/")) {
                    String[] content = topic.split("/");
                    if (content[2].equals("animals")) {
                        Log.d(TAG, message.toString());
                        animal_info = mapper.readValue(message.toString(), Map.class);
                    } else {
                        Log.d(TAG, message.toString());
                        animal_location_info = mapper.readValue(message.toString(), Map.class);
                    }
                } else {
                    String[] values = message.toString().split("/");
                    List<String> list = new ArrayList<>();
                    list.add(values[1]);
                    list.add(values[2]);
                    list.add(values[3]);

                    Log.d(TAG, topic);
                    if (topic.equals("forest/hunter")) {
                        Log.d(TAG, "written to hunter maps");
                        writeToFile(message.toString()+"\n");
                        AlertActivity.map.put(values[0], list);
                    } else {
                        Log.d(TAG, "written to camera maps");
                        writeToFile(message.toString()+"/c"+"\n");
                        AlertActivity.map_c.put(values[0], list);
                    }
                    String[] msg = values[0].split("-");
                    sendNotification(msg[1]);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "delivery complete");
            }
        });

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

    private void sendNotification(String id) {
        Log.d(TAG, "notification called");

        Intent intent = new Intent(this, AlertActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("hunter")
                .setContentText("location")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setSound(Uri.parse("uri://sadfasdfasdf.mp3"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManagerCompat.notify(Integer.parseInt(id), notification);
    }

    private void writeToFile(String data) {
        folder = new File(getFilesDir()+"/forest");
        file = new File(folder.getAbsolutePath()+"/alert.txt");
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String oldData = readFromFile(file);
        String[] values = oldData.split("\n");
        List<String> alerts = Arrays.asList(values);
        if (alerts.size() > 20) {
            alerts.remove(alerts.size()-1);
        }
        oldData = "";
        for (String s : alerts) {
            oldData += s+"\n";
        }

        try {
            OutputStreamWriter file_writer = new OutputStreamWriter(new FileOutputStream(file));
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
            buffered_writer.write("");
            buffered_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            OutputStreamWriter file_writer = new OutputStreamWriter(new FileOutputStream(file, true));
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
            buffered_writer.write(data);
            buffered_writer.write(oldData);
            buffered_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFromFile(File file) {
        String ret = "";

        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(line != null){
                ret += line+"\n";
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

}
