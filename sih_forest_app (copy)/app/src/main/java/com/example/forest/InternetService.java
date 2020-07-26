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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.example.forest.Appcontroller.CHANNEL_ID;

public class InternetService extends Service {

    public static final String TAG = "InternetService";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private NotificationManagerCompat notificationManagerCompat;

    private File folder;
    private File file;
    private FileInputStream is;
    private BufferedReader reader;
    OutputStreamWriter file_writer;
    BufferedWriter buffered_writer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "internet service started");

        db.collection("camera").document("hunter").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, error.toString());
                    return;
                }
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "hunter method called");
                    Map<String, Object> camera_details = documentSnapshot.getData();

                    if (!camera_details.isEmpty()) {
                        String camera_id = camera_details.get("camera_id").toString();
                        String latitude = camera_details.get("latitude").toString();
                        String longitude = camera_details.get("longitude").toString();

                        String final_camera_details = camera_id+"/"+latitude+"/"+longitude+"\n";

                        List<String> list = new ArrayList<>();
                        list.add(latitude);
                        list.add(longitude);

                        writeToFile(final_camera_details);
                        AlertActivity.map.put(camera_id, list);

                        String[] msg = camera_id.split("-");
                        sendNotification(msg[1]);
                    }
                }
            }
        });

        db.collection("camera").document("status").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, e.toString());
                    return;
                }
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "camera method called");
                    Map<String, Object> camera_details = documentSnapshot.getData();

                    if (!camera_details.isEmpty()) {
                        String camera_id = camera_details.get("camera_id").toString();
                        String latitude = camera_details.get("latitude").toString();
                        String longitude = camera_details.get("longitude").toString();

                        String final_camera_details = camera_id+"/"+latitude+"/"+longitude+"/c"+"\n";

                        List<String> list = new ArrayList<>();
                        list.add(latitude);
                        list.add(longitude);

                        writeToFile(final_camera_details);
                        AlertActivity.map_c.put(camera_id, list);

                        String[] msg = camera_id.split("-");
                        sendNotification(msg[1]);
                    }

                }
            }
        });

        return START_STICKY;
    }

    public void sendNotification(String id) {
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

    public void writeToFile(String data) {
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
        List<String> alerts = new LinkedList<String>(Arrays.asList(values));
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

    public String readFromFile(File file) {
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
