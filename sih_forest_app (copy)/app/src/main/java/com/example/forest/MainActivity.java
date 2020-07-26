package com.example.forest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    EditText etUsername,etPassword;
    Button btnLogin;
    private String urlJsonArry = "https://forestweb.herokuapp.com/applogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUsername=findViewById(R.id.etUsername);
        etPassword=findViewById(R.id.etPassword);
        btnLogin=findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                  String username=etUsername.getText().toString().trim();
                   String password=etPassword.getText().toString().trim();

                    if(TextUtils.isEmpty(username)){
                        Toast.makeText(MainActivity.this, "Please Enter Username", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(TextUtils.isEmpty(password)){
                        Toast.makeText(MainActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                      return;
                  }

                    sendrequest(etUsername,etPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
             //   Intent a = new Intent(MainActivity.this,HomeActivity.class);
             //   startActivity(a);

            }
        });
    }

    private void sendrequest(EditText user, EditText pass) throws JSONException {
        Connectivity con =new Connectivity();
        if(con.isConnected(this)) {
            final JSONObject rem = new JSONObject();
//        rem.remove("status");
            rem.put("username", user.getText());
            rem.put("password", pass.getText());
            Log.d("Param", rem.toString());
            final JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, urlJsonArry, rem,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject data) {
                            Log.d("response", data.toString());
                            try {
                                Log.d("response", data.get("id").toString());
                                //if id=='-1' error function

                                    if (data.get("id").equals("-1")) {
                                        Toast.makeText(MainActivity.this, "Username or Password is incorrect", Toast.LENGTH_SHORT).show();
                                        etUsername.setText("");
                                        etPassword.setText("");
                                    } else {

                                        Intent a = new Intent(MainActivity.this,HomeActivity.class);
                                        startActivity(a);

                                    }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            FileOutputStream fileout = null;
                            try {
                                fileout = openFileOutput("mytextfile.txt", MODE_APPEND);
                                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                                outputWriter.write(data.get("id").toString());
                                Log.d("opening", "file");
                                outputWriter.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.e("Error: ", error.getMessage());
                        }
                    });

            Appcontroller.getInstance().addToRequestQueue(req);
        }
        //else
        //no internet so some error shown
    }
}