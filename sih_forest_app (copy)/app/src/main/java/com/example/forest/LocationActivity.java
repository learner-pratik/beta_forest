package com.example.forest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.forest.HomeActivity.animal_info;
import static com.example.forest.HomeActivity.mqttflag;


public class LocationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    Map<String,Object> option = new HashMap<String,Object>();

    // Set for storing keys of documents
    // animal_info and animal_location_info

    Set<String> k = new HashSet<String>();
    Set<String> kmqtt = new HashSet<String>();

    // Object value corresponding to key recieved in firebase response which will be converted to list of strings
    Object v ;

    // Temporary List for spinner2 adapter
    String[] vi = {"All"};

    //Debug tag
    String TAG = "DEBUG";

    // Spinner declaration
    Spinner spin1;
    Spinner spin2;

    // Button declaration
    Button btnLoc;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if(!mqttflag) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("animals_list")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    k.add(document.getId());
                                    option.put(document.getId(), (document.getData()).get("id"));
                                    Timber.d(document.getId() + " => " + document.getData());
                                }
                                k.add("All");
                                setGroup(k);
                            } else {
                                Timber.d(task.getException(), "Error getting documents: ");
                            }
                        }
                    });

        }
        else{
            kmqtt = animal_info.keySet();
            kmqtt.add("All");
            setGroup(kmqtt);
        }
        //Declare spinner
        spin1 = (Spinner) findViewById(R.id.spinner1);
        spin2 = (Spinner) findViewById(R.id.spinner2);


        //spinner 2
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, vi);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);
        spin2.setSelection(0);
        spin2.setOnItemSelectedListener(this);
        spin2.setEnabled(false);
        spin2.setClickable(false);


        // Button code
        btnLoc=findViewById(R.id.btnLoc);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send selected option from spinner as map to next activity
                Intent a = new Intent(LocationActivity.this,MapActivity.class);
                if(spin1.getSelectedItemPosition()==0){
                    if(!mqttflag) {
                        HashMap<String, Object> copy = new HashMap<String, Object>(option);
                        a.putExtra("map", copy);
                    }
                    else{
                        HashMap<String, Object> copy = new HashMap<String, Object>(animal_info);
                        a.putExtra("map", copy);
                    }
                }
                else{
                    if(spin2.getSelectedItemPosition()==0){
                        HashMap<String, Object> copy = new HashMap<String, Object>();
                        if(!mqttflag) {
                            copy.put(spin1.getSelectedItem().toString(), option.get(spin1.getSelectedItem().toString()));
                        }
                        else{
                            copy.put(spin1.getSelectedItem().toString(), animal_info.get(spin1.getSelectedItem().toString()));
                        }
                        a.putExtra("map", copy);
                    }
                    else{
                        HashMap<String, Object> copy = new HashMap<String, Object>();
                        copy.put(spin1.getSelectedItem().toString(),spin2.getSelectedItem());
                        a.putExtra("map",copy);
                    }
                }
                startActivity(a);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View arg1, int position,long id) {
        //Toast.makeText(getApplicationContext(), "Selected User: "+ key[position] ,Toast.LENGTH_SHORT).show();
        String temp;
        switch (parent.getId()){
            case R.id.spinner1:
                //Do something
                String[] varray;
                if(parent.getSelectedItemPosition()!=0){
                    spin2.setEnabled(true);
                    spin2.setClickable(true);
                    temp = parent.getSelectedItem().toString();
                    if(!mqttflag) {
                        v = option.get(temp);
                        assert v != null;
                        varray = convertObjectToList(v);
                    }
                    else{
                        List<String> listspin = animal_info.get(temp);
                        assert listspin != null;
                        varray = new String[listspin.size()+1];
                        varray[0] = "All";
                        for (int ispin =1; ispin < listspin.size()+1; ispin++)
                            varray[ispin] = listspin.get(ispin);
                    }
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, varray);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin2.setAdapter(adapter2);
                    spin2.setSelection(0);
                    spin2.setOnItemSelectedListener(this);
                }
                else{
                    spin2.setSelection(0);
                    spin2.setEnabled(false);
                    spin2.setClickable(false);
                }
                //Toast.makeText(this, "Selected: " + parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.spinner2:
                //Do another thing
                //Toast.makeText(this, "Selected: " + parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO - Custom Code


    }

    public static String[] convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        String[] r = new String[1];
        r[0] = "All";
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[])obj);
            String[] value = new String[list.size()+1];
            value[0] = "All";
            int j = 1;
            for (Object object : list) {
                value[j] = (object != null ? object.toString() : null);
                j++;
            }
            return(value);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>)obj);
            String[] value = new String[list.size()+1];
            value[0]="All";
            int j =1;
            for (Object object : list) {
                value[j] = (object != null ? object.toString() : null);
                j++;
            }
            return(value);
        }
        return r;
    }

    public void setGroup(Set<String> g){
        String[] my_key = new String[g.size()];
        int i=0;
        for(String x:g){
            my_key[i++] = x;
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, my_key);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter1);
        spin1.setSelection(0);
        spin1.setOnItemSelectedListener(this);
    }

}