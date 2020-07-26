package com.example.forest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.InputStreamReader;

public class SplashActivity extends AppCompatActivity {
    Animation animation;
    TextView tvSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        animation = AnimationUtils.loadAnimation(this,R.anim.a1);
        //tvSplash=findViewById(R.id.tvSplash);


//        tvSplash.startAnimation(animation);


        new  Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("opening","app");
                int flag=0;

//                try {
//                    FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
//                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
//                    outputWriter.write("no open");
//                    outputWriter.close();
//
//                    //display file saved message
//                    Toast.makeText(getBaseContext(), "File saved successfully!",
//                            Toast.LENGTH_SHORT).show();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                try {
                    Log.d("atleast","trying");
                    FileInputStream fileIn=openFileInput("mytextfile.txt");
                    InputStreamReader InputRead= new InputStreamReader(fileIn);
//                    Log.d("atleast",InputRead.toString());
                    char[] inputBuffer= new char[100];
                    String s="";
                    int charRead;

                    while ((charRead=InputRead.read(inputBuffer))>0) {
                        // char to string conversion
                        String readstring=String.copyValueOf(inputBuffer,0,charRead);
                        Log.d("atleast","looping in char");
                        s +=readstring;
                    }
                    Log.d("reading",s);
//                    if(s.equals("open")){
//                        FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
//                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
//                        outputWriter.write("open already");
//                        outputWriter.close();
//                        Log.d("reading","file");
//                    }

//                    try {
//                        Thread.sleep(1000);
//                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    InputRead.close();
//                    tvSplash.setText(s);

//                    Intent a = new Intent(SplashActivity.this,HomeActivity.class);
//                    startActivity(a);
//                    finish();
                    flag=1;


                } catch (Exception e) {
                    Log.d("trying","failed");
                    try {
//                        FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
//                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
//                        outputWriter.write("open");
//                        Log.d("opening","file");
//                        outputWriter.close();
//                        Intent a = new Intent(SplashActivity.this,MainActivity.class);
//                        startActivity(a);
//                        finish();

                        //display file saved message
//                        Toast.makeText(getBaseContext(), "File saved successfully!",
//                                Toast.LENGTH_SHORT).show();

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                Log.d("opening","app1");
                try {
                    Thread.sleep(4000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (flag==1){
                    Intent a = new Intent(SplashActivity.this,HomeActivity.class);
                    startActivity(a);
                    finish();
                }
                else{
                    Intent a = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(a);
                    finish();
                }
//                Intent a = new Intent(SplashActivity.this,MainActivity.class);
//                startActivity(a);
//                finish();
            }
        }).start();
    }
}