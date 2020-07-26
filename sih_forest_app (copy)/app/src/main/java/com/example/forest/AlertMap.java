package com.example.forest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;


import java.lang.ref.WeakReference;
import java.util.List;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import android.os.Looper;
import android.widget.Toast;

public class AlertMap extends AppCompatActivity implements PermissionsListener {

    private MapView mapViewa;
    private MapboxMap map;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION1 = 99;
    private PermissionsManager permissionsManager1;
    private LocationEngine locationEngine1;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS1 = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME1 = DEFAULT_INTERVAL_IN_MILLISECONDS1 * 5;
    private AlertMap.LocationChangeListeningActivityLocationCallback callback =
            new AlertMap.LocationChangeListeningActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alert_map);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_alert_map);

        // Initialize the MapView
        mapViewa = findViewById(R.id.AlertMapView);
        mapViewa.onCreate(savedInstanceState);
        mapViewa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                map = mapboxMap;

                mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        initalertLayer(style);
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
            permissionsManager1 = new PermissionsManager((PermissionsListener) this);
            permissionsManager1.requestLocationPermissions(this);
        }
    }

    private void initLocationEngine() {
        locationEngine1 = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest locationEngineRequest = new LocationEngineRequest.
                Builder(DEFAULT_INTERVAL_IN_MILLISECONDS1)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME1)
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
                    MY_PERMISSIONS_REQUEST_LOCATION1);
            return;
        }
        locationEngine1.requestLocationUpdates(locationEngineRequest, callback, Looper.getMainLooper());
        locationEngine1.getLastLocation(callback);
    }

    private void initalertLayer(@NonNull Style style) {
        // h<key,value> --> key = animal_type --> value = List[animal id]
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        double lat = b.getDouble("latitude");
        double lon = b.getDouble("longitude");

        int d_alert = 0;
        d_alert = getResources().getIdentifier("i_elephant", "drawable", getPackageName());
        style.addImage("icon0", BitmapFactory.decodeResource(this.getResources(), d_alert));
        style.addSource(new GeoJsonSource("sid_alert"));
        style.addLayer(new SymbolLayer("layer-id", "sid_alert").withProperties(
                iconImage("icon0"),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(.02f)
        ));
        GeoJsonSource alertSource = map.getStyle().getSourceAs("sid_alert");
        if (alertSource != null) {
            alertSource.setGeoJson(FeatureCollection.fromFeature(
                    Feature.fromGeometry(Point.fromLngLat(lon,lat))
            ));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewa.onResume();
        // When the user returns to the activity we want to resume the API calling.
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapViewa.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapViewa.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewa.onPause();
        // When the user leaves the activity, there is no need in calling the API since the map
        // isn't in view.
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewa.onLowMemory();
    }
    //
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewa.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapViewa.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager1.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        private final WeakReference<AlertMap> activityWeakReference;

        private LocationChangeListeningActivityLocationCallback(AlertMap activity) {
            this.activityWeakReference= new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(LocationEngineResult result) {
            AlertMap activity=activityWeakReference.get();
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
            AlertMap activity=activityWeakReference.get();
            if (activity!=null){
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}