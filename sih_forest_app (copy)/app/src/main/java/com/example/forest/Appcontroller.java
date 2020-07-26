package com.example.forest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Appcontroller extends Application {

    public static final String TAG = Appcontroller.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static Appcontroller mInstance;

    public static final String CHANNEL_ID = "hunter";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        createNotificationChannel();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Forest Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build();
            alertChannel.setDescription("This channel is used to show high alerts of hunter spottings in the forest");
            alertChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            alertChannel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
            alertChannel.setSound(Uri.parse("uri://sadfasdfasdf.mp3"), attributes);
            alertChannel.setBypassDnd(true);
            alertChannel.setShowBadge(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(alertChannel);
        }
    }

    public static synchronized Appcontroller getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        Log.d("response",req.toString());
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
