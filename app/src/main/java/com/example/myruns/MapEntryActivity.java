package com.example.myruns;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapEntryActivity extends AppCompatActivity implements OnMapReadyCallback {

    String activityType, calories_count;
    int id, unit;
    GoogleMap map;
    ArrayList<LatLng> mLocations;
    Float zoomLevel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_entry_view);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        if (savedInstanceState != null) {
            zoomLevel = savedInstanceState.getFloat(CodeKeys.MAP_ZOOM, 17f);
        } else zoomLevel = 17f;

        activityType = bundle.getString("activityType");
        id = bundle.getInt("id");

        TextView activityTypeView = findViewById(R.id.activity_type_label);
        String typeString = "Type: " + activityType;
        activityTypeView.setText(typeString);

        calories_count = bundle.getString("calories");
        TextView calories = findViewById(R.id.calorie_count);
        String cString = "Calories: " + calories_count;
        calories.setText(cString);

        unit = bundle.getInt("unit");

        ArrayList<String> locations = bundle.getStringArrayList("locations");
        mLocations = new ArrayList<>();
        processLocation(locations, unit);

        setMapFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_menu, menu); // inflate custom menu
        MenuItem item = menu.findItem(R.id.delete);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    if(id != -1) {
                        DataBaseUtil util = new DataBaseUtil(getApplicationContext());
                        util.deleteEntry(id, activityType);
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences preferences = getSharedPreferences(CodeKeys.MANUAL_INPUT_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("DB_UPDATED", true);
                edit.apply();
                return id != -1;
            }
        });
        return true;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(CodeKeys.MAP_ZOOM, map.getCameraPosition().zoom);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        zoomLevel = savedInstanceState.getFloat(CodeKeys.MAP_ZOOM, 17);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(Util.checkPermission(this)) Util.requestPermissions(this);

        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if(!mLocations.isEmpty()) {
            if(mLocations.size() > 1) {
                for (int i = 1; i < mLocations.size(); i ++) {
                    makePolyline(mLocations.get(i-1), mLocations.get(i));
                }
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLng(mLocations.get(0)));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocations.get(0),zoomLevel));
            }

            // start point marker
            MarkerOptions o1 = new MarkerOptions().position(mLocations.get(0)).title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(o1);

            // end point marker
            MarkerOptions o2 = new MarkerOptions().position(mLocations.get(mLocations.size()-1)).title("Stop")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            map.addMarker(o2);
        }
    }

    public void processLocation(ArrayList<String> locations, int unit) {
        double totalSpeed = 0, totalDistance = 0, totalClimb = 0;
        Double preLat = null, preLng = null, preAlt = null;

        String perHour, unitType;
        double r;

        if (unit == R.id.imperial_option) {
            r = 3956d;
            perHour = "m/h";
            unitType = "Miles";
        } else {
            r = 6371d;
            perHour = "km/h";
            unitType = "Kilometers";
        }

        for (String l: locations) {
            String[] info = l.split(",");
            double lat = Double.parseDouble(info[0].trim()),
                    lng = Double.parseDouble(info[1].trim()),
                    alt = Double.parseDouble(info[2].trim()),
                    speed = Double.parseDouble(info[3].trim());

            LatLng latLng = new LatLng(lat, lng);
            totalSpeed += speed;
            if (preLat == null) {
                preLat = lat;
                preLng = lng;
                preAlt = alt;
            }else {
                LatLng l2 = new LatLng(preLat, preLng);
                // Radius of earth in kilometers. Use
                // for miles
                double d = Util.distance(latLng, l2, r);
                double climb = Math.abs(alt-preAlt)/1000;
                totalDistance += d;
                totalClimb += climb;
            }
            mLocations.add(latLng);
        }

         double averageSpeed = totalDistance/totalSpeed;

        if (unit == R.id.imperial_option) {
            averageSpeed /= 1.60934f;
            totalClimb /= 1.60934f;
        }

        @SuppressLint("DefaultLocale") String distanceString = "Distance: " + String.format("%.2f", totalDistance) +" "+unitType;
        @SuppressLint("DefaultLocale") String climbString = "Climb: " + String.format("%.2f", totalClimb) +" "+unitType;
        @SuppressLint("DefaultLocale") String speedString = "Avg Speed" + String.format("%.2f", averageSpeed)+" "+perHour;
        String na = "Cur Speed: n/a";

        TextView distance = findViewById(R.id.distance_count);
        distance.setText(distanceString);

        TextView avg_speed = findViewById(R.id.avg_speed);
        avg_speed.setText(speedString);

        TextView cur_speed = findViewById(R.id.current_Speed);
        cur_speed.setText(na);

        TextView climb = findViewById(R.id.climb);
        climb.setText(climbString);
    }

    public void setMapFragment() {
        // set map fragment async
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gps_activity_map);
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(l2,zoomLevel));
    }
}
