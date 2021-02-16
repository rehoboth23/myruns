package com.example.myruns;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkPermission(Context context) {
        if(Build.VERSION.SDK_INT < 23) return false;

        // check if app has permission to write to external storage and use camera and read external storage
        return  ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void requestPermissions(Activity activity) {
        // request permission to write to external storage and use camera and read external storage if not grated
        ActivityCompat.requestPermissions(activity,
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                }, 0);
    }

    public static boolean imageFileHelper(File profileImageFile, ImageView profileImage) {
        if(profileImageFile.exists()){
            Uri profileImageUri = Uri.fromFile(profileImageFile); // get uri prom file
            profileImage.setImageURI(profileImageUri); // set image uri to profile image uri
            return true; // indicated that the change was made
        }
        return false; // indicated no change was made
    }

    public static boolean checkEmailView(Context context, EditText emailView, TextView emailLabel) {
        String s = emailView.getText().toString();
        if(s.isEmpty()) return true;
        Pattern pattern = Pattern.compile("^(.+)@(.+)((\\.(.+))+)");
        Matcher matcher = pattern.matcher(s);
        if(!matcher.find()) {
            emailView.setTextColor(context.getColor(R.color.inputErrorColor));
            emailLabel.setTextColor(context.getColor(R.color.inputErrorColor));
            Toast.makeText(context, "Email is invalid", Toast.LENGTH_SHORT).show();
            return false;
        }
        emailView.setTextColor(context.getColor(R.color.textColor));
        emailLabel.setTextColor(context.getColor(R.color.textColor));
        return true;
    }

    public static String getMonthString(int month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                            "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }

    //
    public static double distance(LatLng l1, LatLng l2, double r) {
        double lat1 = l1.latitude, lon1 = l1.longitude;
        double lat2 = l2.latitude, lon2 = l2.longitude;

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // calculate the result
        return(c * r);
    }
}
