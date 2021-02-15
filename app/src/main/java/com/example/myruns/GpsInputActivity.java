package com.example.myruns;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class GpsInputActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection {

    public class MyHandler extends Handler {
        public MyHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
           Bundle bundle = msg.getData();
           double ltd = bundle.getDouble(CodeKeys.LTD_KEY);
           double lgt = bundle.getDouble(CodeKeys.LGT_KEY);
           addLocation(lgt, ltd);
        }
    }

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap map;
    Context mContext;
    Activity mActivity;
    LocationRequest locationRequest;
    ArrayList<LatLng> locations;
    MyHandler handler;
    MyLocationService.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_input);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBindMe();
                finish();
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // set title
        setTitle("Map");

        // instantiate locations
        locations = new ArrayList<>();

        // set context and activity; needed for call backs and listeners
        mContext = getApplicationContext();
        mActivity = this;

        // get location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

        // set location request
        if (Util.checkPermission(this)) Util.requestPermissions(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // set handler
        handler = new MyHandler(getMainLooper());

        // start location service
        Intent serviceIntent = new Intent(mActivity, MyLocationService.class);
        mActivity.startService(serviceIntent);
        mActivity.bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (currentLocation != null && !Util.checkPermission(this)) {
            map = googleMap;
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setMyLocationEnabled(true);
            LatLng pos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            locations.add(pos);

            // camera look to location
            map.moveCamera(CameraUpdateFactory.newLatLng(pos));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
        } else {
            fetchLastLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println(locations.size());
        binder.stopRequests();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MyLocationService.MyBinder) service;
        binder.setHandler(handler);
        binder.startRequests();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    public void unBindMe() {
        unbindService(this);
    }


    public void fetchLastLocation() {
        // get permissions
        if(Util.checkPermission(this)) Util.requestPermissions(this);

        Task<Location> task = fusedLocationProviderClient.getLastLocation();

        // add on success listener
        task.addOnSuccessListener(new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    currentLocation = location;
                    setMapFragment();
                }
            }
        });

    }

    public void setMapFragment() {
        // set map fragment async
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gps_input_map);
        if(mapFragment != null) mapFragment.getMapAsync(this);
    }

    public void  makePolyline(LatLng l1, LatLng l2) {
        PolylineOptions polyline = new PolylineOptions();
        polyline.color(Color.BLUE);
        polyline.geodesic(true);
        polyline.width(10);
        polyline.add(l1);
        polyline.add(l2);
        map.addPolyline(polyline);
    }

    public void addLocation(final double lgt, final double ltd) {
        if (!locations.isEmpty()) {
            LatLng last = locations.get(locations.size()-1);
            if (last.longitude != lgt || last.latitude != ltd) {
                LatLng latLng = new LatLng(ltd, lgt);
                locations.add(latLng);
                makePolyline(last, latLng);
                System.out.println(last.latitude +","+ last.longitude);
                System.out.println(latLng.latitude +","+ latLng.longitude);
            }
        }
        else {
            LatLng latLng = new LatLng(lgt, ltd);
            locations.add(latLng);
        }
    }
}