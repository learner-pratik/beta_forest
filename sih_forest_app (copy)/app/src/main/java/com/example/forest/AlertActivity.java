package com.example.forest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlertActivity extends AppCompatActivity {
    private static final String TAG = "AlertActivity";

    public static HashMap<String, List<String>> map = new HashMap();
    public static HashMap<String, List<String>> map_c = new HashMap();
    Button alertButton;

    public static List<List<String>> map1 = new ArrayList<>();
    public static List<List<String>> map2 = new ArrayList<>();
    FileInputStream is;
    BufferedReader reader;
    File folder;
    File file;
    Button alertButton1;

    ListView listView;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        listView = (ListView) findViewById(R.id.mobile_list);

        if (getIntent().getIntExtra("callingActivity", 0) == 1001) {
            readFromFile();
            getAlerts();
        } else {
            getNotificationAlerts();
        }

    }

    private void readFromFile() {
        map1.clear();
        map2.clear();

        folder = new File(getFilesDir()+"/forest");
        file = new File(folder.getAbsolutePath()+"/alert.txt");

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
                Log.d(TAG, line);
                String[] values = line.split("/");
                if (values.length == 4) {
                    map1.add(Arrays.asList(values));
                } else {
                    map2.add(Arrays.asList(values));
                }
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendLocation1(View view) {
        alertButton1 = (Button) findViewById(R.id.label1);
        System.out.println("method called");

        String text = (String) alertButton1.getText();
        String[] values = text.split(" ");
        String id = values[4];

        int val = 0;
        List<String> list;
        if (values[0].equals("Alert")) {
            for (int i=0; i<map1.size(); i++) {
                if (map1.get(i).get(0) == id){
                    val = i;
                    break;
                }
            }
            list = map1.get(val);
            System.out.println(list);
        } else {
            for (int i=0; i<map2.size(); i++) {
                if (map2.get(i).get(0) == id){
                    val = i;
                    break;
                }
            }
            list = map2.get(val);
            System.out.println(list);
        }

        String latitude = list.get(1);
        String longitude = list.get(2);

        Intent intent = new Intent(AlertActivity.this, AlertMap.class);
        intent.putExtra("latitude", Double.parseDouble(latitude));
        intent.putExtra("longitude", Double.parseDouble(longitude));
        startActivity(intent);
    }

    public void sendLocation(View view) {
        alertButton = (Button) findViewById(R.id.label);
        System.out.println("method called");

        String text = (String) alertButton.getText();
        String[] values = text.split(" ");
        String id = values[4];
        System.out.println(map.size());

        List<String> list;
        if (values[0].equals("Alert")) {
            list = map.get(id);
            System.out.println(list);
        } else {
            list = map_c.get(id);
            System.out.println(list);
        }

        String latitude = list.get(0);
        String longitude = list.get(1);

        alertButton.setBackgroundColor(Color.GREEN);

        Intent intent = new Intent(AlertActivity.this, AlertMap.class);
        intent.putExtra("latitude", Double.parseDouble(latitude));
        intent.putExtra("longitude", Double.parseDouble(longitude));
        startActivity(intent);
    }

    private void getNotificationAlerts() {
        Log.d(TAG, "notification adatper");
        final List<String> camera = new ArrayList<>();

        for (Map.Entry<String, List<String>> set : map.entrySet()) {
            String s = set.getKey();
            List<String> t = set.getValue();
            camera.add("Alert spotted at camera "+s+" at time "+ t.get(2));
        }
        for (Map.Entry<String, List<String>> set : map_c.entrySet()) {
            String s = set.getKey();
            List<String> t = set.getValue();
            camera.add("Camera broken at camera "+s+" at time "+t.get(2));
        }
        String[] camera_id = camera.toArray(new String[0]);

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, camera_id);

        listView.setAdapter(adapter);

//        map.clear();
//        map_c.clear();
    }

    private void getAlerts() {
        Log.d(TAG, "app alerts adatper");
        final List<String> camera = new ArrayList<>();

        for (List<String> strings : map1) {
            String s = strings.get(0);
            String t = strings.get(3);
            camera.add("Alert spotted at camera "+s+" at time "+t);
        }
        for (List<String> strings : map2) {
            String s = strings.get(0);
            String t = strings.get(3);
            camera.add("Camera broken at camera "+s+" at time "+t);
        }
        String[] camera_id = camera.toArray(new String[0]);

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview1, camera_id);

        listView.setAdapter(adapter);
    }
}
