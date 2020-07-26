package com.example.forest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.forest.Connectivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
//Volley
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
//JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskActivity extends AppCompatActivity {
    private ListView lv;
    public Adapter adapter;
    public ArrayList<Model> modelArrayList;
    public JSONArray data_array=new JSONArray();
    public JSONArray cache=new JSONArray();
    public int flag=0,flag1=0;

    Connectivity con=new Connectivity();

    private String urlJsonArry = "https://forestweb.herokuapp.com/apptask";
    private String urltask = "https://forestweb.herokuapp.com/gettask";

    private static String TAG = MainActivity.class.getSimpleName();

    private String jsonResponse;

    private String[] myList = new String[]{"Benz", "Bike",
            "Car","Carrera"
            ,"Ferrari","Harly",
            "Lamborghini","Silver"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        lv = (ListView) findViewById(R.id.listview);
        modelArrayList=populateList();
        Log.d("before",modelArrayList.toString());
        Log.d("internet",String.valueOf(con.isConnected(this)));

        if(con.isConnected(this))
        {
            new CountDownTimer(3000,1000){
                //send tasks from cache
                @Override
                public void onTick(long millisUntilFinished) {
                    try {
                        if(flag1==0) {
                            String data = readFromFile("cache.txt");
                            Log.d("cache data", data);
                            if (data.length() != 0) {
                                cache = new JSONArray(data);
                                JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, urlJsonArry, cache,
                                        new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray response) {

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                                    }
                                });
                                Appcontroller.getInstance().addToRequestQueue(req);
                            }
                        }
                        flag1=1;
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("cache.txt", MODE_PRIVATE));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    new CountDownTimer(3000, 1000) {
                        //get new task

                        public void onTick(long millisUntilFinished) {
                            if (flag==0){
                                Log.d("wait","wait function");

                                Log.d("wait","wait function continue");
                                try {
                                    modelArrayList=makeJsonArrayRequest();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("hello",modelArrayList.toString());
                            }
                            flag=1;
                        }

                        public void onFinish() {
//                adapter = new Adapter(this,modelArrayList);
//                lv.setAdapter(adapter);
                            next();
                        }
                    }.start();

                }
            }.start();

        }
        else{
            //Offline
            String data=readFromFile("tasks.txt");
            final ArrayList<Model> list = new ArrayList<>();
            Log.d("from cache",data);
            try {
                JSONArray tasks = new JSONArray(data);

                Log.d("list",tasks.toString());

                for(int i = 0; i < tasks.length(); i++){
                    Model model = new Model();
                    JSONObject curr = tasks.getJSONObject(i);
                    model.setName(curr.getString("task_info"));
                    list.add(model);
                }

                Log.d("add list",list.toString());
                data_array=tasks;
                modelArrayList=list;
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            next();
        }

//        adapter = new Adapter(this,modelArrayList);
//        lv.setAdapter(adapter);
//
//        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
//                new SwipeToDismissTouchListener<>(
//                        new ListViewAdapter(lv),
//                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
//                            @Override
//                            public boolean canDismiss(int position) {
//                                return true;
//                            }
//
//                            @Override
//                            public void onDismiss(ListViewAdapter view, int position) {
//                                adapter.remove(position);
//                            }
//                        });
//
//        lv.setOnTouchListener(touchListener);
//        lv.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (touchListener.existPendingDismisses()) {
//                    touchListener.undoPendingDismiss();
//                } else {
//                    Toast.makeText(TaskActivity.this, "Position " + position, LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    public void next(){
        Log.d("check list",modelArrayList.toString());
        //if list is empty display no task
        try {
        adapter = new Adapter(this,modelArrayList);
            lv.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No Task" , LENGTH_SHORT).show();
        }


        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(lv),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                adapter.remove(position);
                                try {
                                    sendrequest(position);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        lv.setOnTouchListener(touchListener);
        lv.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    Toast.makeText(TaskActivity.this, "Position " + position, LENGTH_SHORT).show();
                }
            }
        });
    }

    private ArrayList<Model> populateList(){

        ArrayList<Model> list = new ArrayList<>();

        for(int i = 0; i < myList.length; i++){
            Model model = new Model();
            model.setName(myList[i]);
            list.add(model);
        }

        return list;

    }

    //server request
    private ArrayList<Model> makeJsonArrayRequest() throws JSONException {

//        showpDialog();
        final ArrayList<Model> list = new ArrayList<>();
        String temp = "";
        FileInputStream fin = null;
        try {
            fin = openFileInput("mytextfile.txt");


            int c;
            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }

            fin.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject d=new JSONObject();
        d.put("id",temp);
        JSONArray xyz=new JSONArray();
        xyz.put(temp);
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,urltask,xyz,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("json array", response.toString());

                        try {
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("task", response.toString());
                            writeToFile(response.toString());

                            JSONArray tasks = new JSONArray(response.toString());

                            Log.d("list",tasks.toString());

                            for(int i = 0; i < tasks.length(); i++){
                                Model model = new Model();
                                JSONObject curr = tasks.getJSONObject(i);
                                model.setName(curr.getString("task_info"));
                                list.add(model);
                            }

                            Log.d("add list",list.toString());
                            data_array=tasks;
//                            return(list);

                        }
                     catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

//         Adding request to request queue
        Appcontroller.getInstance().addToRequestQueue(req);
        Log.d("function",list.toString());
        return list;
    }

    private void sendrequest(int i) throws JSONException {
        if(con.isConnected(this)) {
            JSONObject rem = (JSONObject) data_array.get(i);
            rem.remove("status");
            rem.put("status", "complete");
            JsonObjectRequest req = new JsonObjectRequest(urlJsonArry, rem,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject data) {
                            Log.d("response", data.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_SHORT).show();
                            //                hidepDialog();
                        }
                    });
            // Adding request to request queue
            Appcontroller.getInstance().addToRequestQueue(req);
        }

        Connectivity con=new Connectivity();
        if(!con.isConnected(this))
        {
            JSONObject rem = (JSONObject) data_array.get(i);
            rem.remove("status");
            rem.put("status", "complete");
            cache.put(rem);
            writeToFilecache(cache.toString());
        }

        int len=data_array.length();
        JSONArray temp=new JSONArray();
        for (int x=0;x<len;x++)
        {
            //Excluding the item at position
            if (x != i)
            {
                temp.put(data_array.get(x));
            }
        }
        data_array=temp;
        writeToFile(data_array.toString());


        Log.d("function",modelArrayList.toString());

    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("tasks.txt", MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToFilecache(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("cache.txt", MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String file) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

}


