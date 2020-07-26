package com.example.forest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;


import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.graphics.BitmapFactory;
import android.os.Handler;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.example.forest.model.IssModel;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;


import androidx.core.app.ActivityCompat;
import retrofit2.Call;

import static com.example.forest.HomeActivity.animal_location_info;
import static com.example.forest.HomeActivity.mqttflag;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
;


public class MapActivity extends AppCompatActivity implements PermissionsListener {
    private static final String TAG = "SpaceStationActivity";
    Map<String, List<String>> h = new HashMap<String, List<String>>();
    String value1;
    private Handler handler;
    private Runnable runnable;
    private Call<IssModel> call;

    // apiCallTime is the time interval when we call the API in milliseconds, by default this is set
    // to 2000 and you should only increase the value, reducing the interval will only cause server
    // traffic, the latitude and longitude values aren't updated that frequently.
    private int apiCallTime = 11000;

    // Map variables
    private MapView mapView;
    private MapboxMap map;

    int tm = 0;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        HashMap<String, Object> temph = (HashMap<String, Object>) intent.getSerializableExtra("map");
        Iterator it = temph.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Log.d("this is last hope", (String) pair.getKey());
            h.put((String) pair.getKey(), convertObjectToList(pair.getValue()));
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map);

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                map = mapboxMap;

                mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        initSpaceStationSymbolLayer(style);
                        callApi();
                        Toast.makeText(MapActivity.this, R.string.space_station_toast, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = map.getLocationComponent();
            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(this, loadedMapStyle)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest locationEngineRequest = new LocationEngineRequest.
                Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        locationEngine.requestLocationUpdates(locationEngineRequest, callback, Looper.getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    private void callApi() {


// A handler is needed to called the API every x amount of seconds.

        handler = new Handler();
        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                for (Map.Entry<String, List<String>> entry : h.entrySet()) {
                    String key = entry.getKey();

                    for (String value : entry.getValue()) {
                        value1 = value;
                        //Log.d("This is value value1 ", value1);
                        if(!mqttflag){
                            DocumentReference docRef = db.collection("animals").document("location");
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        assert document != null;
                                        if (document.exists()) {
                                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                            //double latitude = document.getDouble("latitude");
                                            //double longitude = document.getDouble("longitude");
                                            List<String> res = convertObjectToList(document.get(value1));
                                            updateMarkerPosition(new LatLng(Double.parseDouble(res.get(0)), Double.parseDouble(res.get(1))), "sid" + tm);
                                            tm++;
                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        }
                        else{
                            List<String> res1 = animal_location_info.get(value1);
                            updateMarkerPosition(new LatLng(Double.parseDouble(res1.get(0)), Double.parseDouble(res1.get(1))), "sid" + tm);
                        }

                        tm++;
                    }
                }
                tm = 0;

                handler.postDelayed(this, apiCallTime);
            }
        };

        // The first time this runs we don't need a delay so we immediately post.
        handler.post(runnable);
    }

    private void initSpaceStationSymbolLayer(@NonNull Style style) {
        // h<key,value> --> key = animal_type --> value = List[animal id]
        int d=0;
        int m=0;
        int ic=0;
        for(Map.Entry<String, List<String>> entry : h.entrySet()) {
            String key = entry.getKey();
            Log.d("my debug key is ",key);
            d = getResources().getIdentifier("i_" + key, "drawable", getPackageName());
            style.addImage("icon"+ic,
                    BitmapFactory.decodeResource(
                            this.getResources(), d));
            for (String value : entry.getValue()) {
                Log.d("debug msg",value);
                style.addSource(new GeoJsonSource("sid"+m));
                style.addLayer(new SymbolLayer("layer-id"+m, "sid"+m).withProperties(
                        iconImage("icon"+ic),
                        iconIgnorePlacement(true),
                        iconAllowOverlap(true),
                        iconSize(.02f)
                ));
                m++;
            }
            ic++;
        }
    }

    private void updateMarkerPosition(LatLng position,String source_id) {
        // This method is where we update the marker position once we have new coordinates. First we
        // check if this is the first time we are executing this handler, the best way to do this is
        // check if marker is null;
        if (map.getStyle() != null) {
            Log.d("debug source id ",source_id);
            GeoJsonSource spaceStationSource = map.getStyle().getSourceAs(source_id);
            if (spaceStationSource != null) {
                spaceStationSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }

        // Lastly, animate the camera to the new position so the user
        // wont have to search for the marker and then return. uncomment bellow line
        //map.animateCamera(CameraUpdateFactory.newLatLng(position));
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // When the user returns to the activity we want to resume the API calling.
        if (handler != null && runnable != null) {
            handler.post(runnable);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        // When the user leaves the activity, there is no need in calling the API since the map
        // isn't in view.
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public static List<String> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        List<String> r = new ArrayList<String>(1);
        r.add("All");
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[])obj);
            List<String> value = new ArrayList<String>(list.size());
            for (Object object : list) {
                value.add(object != null ? object.toString() : null);
            }
            return(value);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>)obj);
            List<String> value = new ArrayList<String>(list.size());
            for (Object object : list) {
                value.add(object != null ? object.toString() : null);
            }
            return(value);
        }
        r.add(obj.toString());
        return r;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Plz Enable the permissions...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            map.getStyle(new Style.OnStyleLoaded(){
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        }
        else{
            Toast.makeText(this, "Permissions are not granted...", Toast.LENGTH_SHORT).show();
        }
    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult>{
        private final WeakReference<MapActivity> activityWeakReference;

        private LocationChangeListeningActivityLocationCallback(MapActivity activity) {
            this.activityWeakReference= new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(LocationEngineResult result) {
            MapActivity activity=activityWeakReference.get();
            if (activity!=null){
                Location location=result.getLastLocation();
                if (location==null){
                    return;
                }
//                Toast.makeText(activity, "Location Co-ordinates: "+location.getLatitude()+
//                                " & "+location.getLongitude()+" & Thread name: "+Thread.currentThread().getName(),
//                                Toast.LENGTH_SHORT).show();
//                new MainActivity().AddLocationUpdatetoFirebase(location);

                //AddLocationUpdatetoFirebase(location);
                if (activity.map!=null&&result.getLastLocation()!=null){
                    activity.map.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            MapActivity activity=activityWeakReference.get();
            if (activity!=null){
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}