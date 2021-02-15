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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkPermission(Activity activity) {
        if(Build.VERSION.SDK_INT < 23) return false;

        // check if app has permission to write to external storage and use camera and read external storage
        return  ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkPermission(Context context) {
        if(Build.VERSION.SDK_INT < 23) return false;

        // check if app has permission to write to external storage and use camera and read external storage
        return  ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity) {
        // request permission to write to external storage and use camera and read external storage if not grated
        ActivityCompat.requestPermissions(activity,
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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
}
