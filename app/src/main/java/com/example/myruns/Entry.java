package com.example.myruns;


import android.annotation.SuppressLint;

import java.util.Calendar;

public class Entry {
    int id = -1;
    String entryType, activityType, comment;
    float distanceInMiles, durationInMinutes;
    int calories, heartRate;
    Calendar timeStamp;

    public Entry(String entryType, String activityType, String comment,
                 float distance, float duration, Calendar c, int calories, int heartRate)
    {
        this.comment = comment;
        this.entryType = entryType;
        this.activityType = activityType;
        distanceInMiles = distance;
        durationInMinutes = duration;
        this.calories = calories;
        this.heartRate = heartRate;
        timeStamp = c;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getComment() { return comment;}

    public float getImperialDistance() {
        return distanceInMiles;
    }

    public float getMetricDistance() {
        @SuppressLint("DefaultLocale") String floatString = String.format("%.2f", distanceInMiles * 1.60934f);
        return Float.parseFloat(floatString);
    }

    public float getDurationInMinutes() {
        return durationInMinutes;
    }

    public int getCalories() {return calories;}

    public int getHeartRate() {return heartRate;}

    public Calendar getTimeStamp() { return timeStamp; }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {return id;}
}
