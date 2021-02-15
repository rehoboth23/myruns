package com.example.myruns;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MyService extends Service {

    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult != null) {
                Location location = locationResult.getLastLocation();
                double lgt = location.getLongitude(), ltd = location.getLatitude();
                Log.d("Service Update", lgt +", "+ ltd);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(CodeKeys.ACTION_START_LOCATION_SERVICE)) {
                startLocationService();
            } else if (action.equals(CodeKeys.ACTION_STOP_LOCATION_SERVICE)) {
                stopLocationService();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void startLocationService() {
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) ==  null) {
                NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(channel);
            }
        }

        LocationRequest request = new LocationRequest();
        request.setInterval(4000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Util.checkPermission((Activity)getApplicationContext())) {
            Util.requestPermissions((Activity)getApplicationContext());
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(request, locationCallback, Looper.getMainLooper());

        startForeground(CodeKeys.LOCATION_SERVICE_CODE, builder.build());
    }

    public void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);

        stopForeground(true);
        stopSelf();
    }
}
