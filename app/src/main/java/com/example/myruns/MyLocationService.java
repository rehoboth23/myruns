package com.example.myruns;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MyLocationService extends Service {
    GpsInputActivity.MyHandler handler;
    Timer timer;
    int seconds;
    Calendar c;

    private class SecondCount extends TimerTask {
        @Override
        public void run() {
            seconds += 1;
            timer.schedule(new SecondCount(), 1000);
        }
    }

    public class MyBinder extends Binder {
        public void setHandler(GpsInputActivity.MyHandler h) {
            handler = h;
        }

        public void startRequests() {
            startLocationService();
        }

        public void stopRequests() {
            stopLocationService();
        }

        public Calendar getCalendar() {
            return c;
        }

        public int getTimeElapsed() {
            return seconds/60;
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Location location = locationResult.getLastLocation();

            double lgt = location.getLongitude(),
                    ltd = location.getLatitude(),
                    alt = location.getAltitude(),
                    speed = location.getSpeed();

            Bundle bundle = new Bundle();
            bundle.putDouble(CodeKeys.LGT_KEY, lgt);
            bundle.putDouble(CodeKeys.LTD_KEY, ltd);
            bundle.putDouble(CodeKeys.ALT_KEY, alt);
            bundle.putDouble(CodeKeys.SPEED_KEY, speed);

            Message msg = handler.obtainMessage();
            msg.setData(bundle);
            msg.what = CodeKeys.SERVICE_LOCATION_MSG;
            handler.sendMessage(msg);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public void startLocationService() {

        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        // make content intent for the notification
        Intent resultIntent = new Intent(this, GpsInputActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // set content intent for the notification
        builder.setContentIntent(contentIntent);


        builder.setSmallIcon(R.drawable.mapd);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(channel);
            }
        }

        LocationRequest request = LocationRequest.create();
        request.setInterval(4000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(request, locationCallback, Looper.getMainLooper());

        Notification notification = builder.build();
        startForeground(CodeKeys.LOCATION_SERVICE_CODE, notification);

        timer.schedule(new SecondCount(), 1000);
        LocalDateTime time = LocalDateTime.now();

        c = Calendar.getInstance();
        c.set(Calendar.YEAR, time.getYear());
        c.set(Calendar.MONTH, time.getMonthValue());
        c.set(Calendar.DAY_OF_MONTH, time.getDayOfMonth());
        c.set(Calendar.HOUR_OF_DAY, time.getHour());
        c.set(Calendar.MINUTE, time.getMinute());
        c.set(Calendar.SECOND, 0);
    }

    public void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);

        stopForeground(true);
        stopSelf();
    }
}
