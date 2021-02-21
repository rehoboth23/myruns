package com.example.myruns;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class GpsInputActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection {

    public class MyHandler extends Handler {
        public MyHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
           Bundle bundle = msg.getData();

           if (msg.what == CodeKeys.SERVICE_LOCATION_MSG) {
               double ltd = bundle.getDouble(CodeKeys.LTD_KEY),
                       lgt = bundle.getDouble(CodeKeys.LGT_KEY),
                       alt = bundle.getDouble(CodeKeys.ALT_KEY),
                       speed = bundle.getDouble(CodeKeys.SPEED_KEY);

               addLocation(lgt, ltd, alt, speed, false);

           }

           if (msg.what == CodeKeys.SENSOR_SERVICE_MSG) {
               activityType = bundle.getString(CodeKeys.CLASS_ACT_KEY);
               TextView t = findViewById(R.id.activity_type_label);
               String type = "Type: " + activityType;
               t.setText(type);
           }
        }
    }

    GoogleMap map;
    Context mContext;
    Activity mActivity;
    ArrayList<double[]> locations;
    MyHandler handler;
    MyLocationService.MyBinder binder;
    SensorClassifierService.SensorBinder sBinder;
    SharedPreferences prefs;
    boolean discard = false;
    String activityType, entryType, unitType, perHour;
    TextView activity, avgSpeed, curSpeed, climb, calories, distance;
    double totalDistance, speedSum, totalClimb;
    int calorieCount;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_input);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBindMe();
                discard = true;
                finish();
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBindMe();
                discard = true;
                saveActivity();
                finish();
            }
        });

        // get permissions
        if (Util.checkPermission(this)) Util.requestPermissions(this);

        // set title
        setTitle("Map");

        // get shared prefs
        prefs = getSharedPreferences(CodeKeys.GPS_PREFS, MODE_PRIVATE);

        // retrieve relevant data
        Bundle bundle = getIntent().getExtras();
        activityType = bundle != null ? bundle.getString("activity_type") : "Unknown";
        entryType = bundle != null ? bundle.getString("entry_type") : "GPS";

        // get unit from main prefs
        SharedPreferences mainPrefs = getSharedPreferences(CodeKeys.MAIN_PREFERENCES, MODE_PRIVATE);
        int unit = mainPrefs.getInt(CodeKeys.UNIT_TYPE, R.id.metric_option);
        if (unit == R.id.metric_option) {
            unitType = "Kilometers";
            perHour = "km/h";
        } else {
            unitType = "Miles";
            perHour = "m/h";
        }

        // get views
        activity = findViewById(R.id.activity_type_label); String aType = "Type: "+activityType; activity.setText(aType);
        avgSpeed = findViewById(R.id.avg_speed);
        curSpeed = findViewById(R.id.current_Speed);
        climb = findViewById(R.id.climb);
        calories = findViewById(R.id.calorie_count);String calorieText = "Calories: 0"; calories.setText(calorieText);
        distance = findViewById(R.id.distance_count);
        String c = "Climb: 0 " + unitType;
        climb.setText(c);

        // instantiate locations
        locations = new ArrayList<>();

        // set context and activity; needed for call backs and listeners
        mContext = getApplicationContext();
        mActivity = this;

        // set handler
        handler = new MyHandler(getMainLooper());

        setMapFragment();
        startListening();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(Util.checkPermission(this)) Util.requestPermissions(this);
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        if(!locations.isEmpty()) {
            LatLng latLng = new LatLng(locations.get(0)[0], locations.get(0)[1]);
            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.title("Start Point");
            map.addMarker(options);

        }
        retrievePrefs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {

            if (grantResults[2] == -1) finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!discard) {
            Set<String> locSet = new HashSet<>();
            for (double[] l: locations) {
                locSet.add(l[0] +","+ l[1] +","+ l[2] +","+ l[3]);
            }
            SharedPreferences.Editor edit = prefs.edit();
            edit.putStringSet(CodeKeys.PREV_LOC, locSet);
            if (map != null) {
                edit.putFloat(CodeKeys.GPS_ZOOM, map.getCameraPosition().zoom);
            }
            if(entryType.equals("Automatic")) {
                edit.putString(CodeKeys.ACT_TYPE_KEY, activityType);
            }
            edit.apply();
        } else {
            discardPrefs();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("Component Name", name.getShortClassName());
        if (name.getShortClassName().equals(".MyLocationService")) {
            binder = (MyLocationService.MyBinder) service;
            binder.setHandler(handler);
            binder.startRequests();
        } else if (name.getShortClassName().equals(".SensorClassifierService")) {
            sBinder = (SensorClassifierService.SensorBinder) service;
            sBinder.start();
            sBinder.setHandler(handler);
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    public void unBindMe() {
        if(sBinder != null) sBinder.stop();
        binder.stopRequests();
        mActivity.getApplicationContext().unbindService(this);
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
        map.animateCamera(CameraUpdateFactory.newLatLng(l2));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(l2,
                prefs.getFloat(CodeKeys.GPS_ZOOM, map.getCameraPosition().zoom)));
        if (prefs.contains(CodeKeys.GPS_ZOOM)) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove(CodeKeys.GPS_ZOOM);
            edit.apply();
        }
    }

    public void addLocation(final double lgt, final double ltd, final double alt, final double speed, boolean isAll) {
        if (!locations.isEmpty()) {
            double[] lastInfo = locations.get(locations.size()-1);
            LatLng last = new LatLng(lastInfo[0], lastInfo[1]);
            if (last.longitude != lgt || last.latitude != ltd) {
                LatLng latLng = new LatLng(ltd, lgt);
                locations.add(new double[]{ltd, lgt, alt, speed});
                makePolyline(last, latLng);
            }
        }
        else {
            locations.add(new double[]{ltd, lgt, alt, speed});
            if(map != null) {
                LatLng latLng = new LatLng(locations.get(0)[0], locations.get(0)[1]);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title("Start Point");
                map.addMarker(options);
            }
        }
        makeSpeedDistance(isAll);
    }

    public void retrievePrefs() {
        Set<String> prevLocations = prefs.getStringSet(CodeKeys.PREV_LOC, null);
        if (prevLocations != null) {
            for (String l: prevLocations) {
                String[] cords = l.split(",");
                double ltd = Double.parseDouble(cords[0].trim()),
                        lgt = Double.parseDouble(cords[1].trim()),
                        alt = Double.parseDouble(cords[2].trim()),
                        speed = Double.parseDouble(cords[3].trim());
                addLocation(lgt, ltd, alt, speed, true);
            }
        }

        activityType = prefs.getString(CodeKeys.ACT_TYPE_KEY, activityType);
    }

    public void makeSpeedDistance(final boolean isAll) {
        Thread th = new Thread(new Runnable() {

            @Override
            public void run() {
                double d1 = 0, avg_s = 0, s = 0, c = 0;

                // Radius of earth in kilometers. Use
                // for miles
                double r = unitType.equals("Miles") ?  3956d : 6371d;

                if (!locations.isEmpty()) {

                    if(isAll) {
                        for(int i = 1; i < locations.size(); i++) {
                            double[] l1 = locations.get(i), l2 = locations.get(i-1);
                            LatLng lt1 = new LatLng(l1[0], l1[1]), lt2 = new LatLng(l2[0], l2[1]);
                            d1 += Util.distance(lt1, lt2, r);
                            speedSum += l1[3];
                            c += Math.abs(l1[2]-l2[2])/1000;
                        }
                        totalDistance = d1;
                        totalClimb = c;

                    } else if (locations.size() >= 2) {
                        double[] l1 = locations.get(locations.size()-1), l2 = locations.get(locations.size()-2);
                        LatLng lt1 = new LatLng(l1[0], l1[1]), lt2 = new LatLng(l2[0], l2[1]);
                        totalDistance += Util.distance(lt1, lt2, r);
                        totalClimb += Math.abs(l1[2]-l2[2])/1000;
                        d1 = totalDistance;
                        c = totalClimb;
                        speedSum += l1[3];
                    }
                    avg_s = speedSum/locations.size();
                }

                if (locations.size() >= 2) {
                    double[] last = locations.get(locations.size()-1);
                    s = last[3];
                }

                // (0.035 * body weight in kg) + ((Velocity in m/s ^ 2) / Height in m)) * (0.029) * (body weight in kg)
                calorieCount = (int) Math.floor((0.035 * 90) + (Math.pow((s*0.277778), 2) / 1.2) * 0.029 * 95);

                String calorieText = "Calories: " + calorieCount;
                calories.setText(calorieText);

                if (unitType.equals("Miles")) {
                    s /= 1.60934f;
                    avg_s /= 1.60934f;
                    c /= 1.60934f;
                }

                @SuppressLint("DefaultLocale") final String distanceString = "Distance: " + String.format("%.2f", d1) + " " + unitType,
                        avgSpeedString = "Avg Speed: "  + String.format("%.2f", avg_s) + " " + perHour,
                        speedString = "Cur Speed: " + String.format("%.2f", s) + " " + perHour,
                        climbString = "Climb: " + String.format("%.2f", c) + " " + unitType;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        curSpeed.setText(speedString);
                        distance.setText(distanceString);
                        avgSpeed.setText(avgSpeedString);
                        climb.setText(climbString);
                    }
                });
            }
        });
        th.start();
    }

    public void discardPrefs() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(CodeKeys.PREV_LOC);
        edit.remove(CodeKeys.ACT_TYPE_KEY);
        edit.putFloat(CodeKeys.GPS_ZOOM, 17);
        edit.apply();
    }

    public void startListening() {
        // start location service
        Intent locationServiceIntent = new Intent(mActivity, MyLocationService.class);
        startService(locationServiceIntent);
        getApplicationContext().bindService(locationServiceIntent, this, BIND_AUTO_CREATE);

        // start sensor service
        if (entryType.equals("Automatic")) {
            Intent sensorServiceIntent = new Intent(mActivity, SensorClassifierService.class);
            startService(sensorServiceIntent);
            mActivity.getApplicationContext().bindService(sensorServiceIntent, this, BIND_AUTO_CREATE);
        }
    }

    public void saveActivity() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Calendar timestamp = binder.getCalendar();
                int duration = binder.getTimeElapsed();

                if (entryType.equals("Automatic")) {
                    activityType = sBinder.getPredictedActivity();
                }

                Entry newEntry = new Entry (entryType, activityType, locations, (float) totalDistance, duration, timestamp, calorieCount);

                DataBaseUtil db = new DataBaseUtil(getApplicationContext());
                db.addGpsEntry(newEntry);
                db.close();
            }
        });
        t.start();

        SharedPreferences preferences = getSharedPreferences(CodeKeys.MANUAL_INPUT_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("DB_UPDATED", true);
        edit.apply();
    }
}