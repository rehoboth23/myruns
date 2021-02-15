package com.example.myruns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataBaseUtil extends SQLiteOpenHelper {
    private final String ENTRY_TABLE = "ENTRY", ENTRY_TYPE = "entry_type",
                         ACTIVITY_TYPE = "activity_type", COMMENT = "comment",
                         DISTANCE = "distance", DURATION = "duration",
                         CALORIES = "calories", HEART_RATE = "heart_rate",
                         ENTRY_TIME_STAMP = "time_stamp";
    Context mContext;





    public DataBaseUtil(@Nullable Context context) {
        super(context, "entry.db", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create event table statement

        String createStatement = "create table " + ENTRY_TABLE + "("+
                                "id integer primary key autoincrement not null,"+
                                ENTRY_TYPE + " text(13) not null,"+
                                ACTIVITY_TYPE + " text(21) not null,"+
                                COMMENT +" text(200),"+
                                DISTANCE + " decimal(4, 2) not null,"+
                                DURATION + " decimal(4, 2) not null,"+
                                CALORIES +  " int,"+
                                HEART_RATE + " int,"+
                                ENTRY_TIME_STAMP + " text(22));";

        // execute create statement
        db.execSQL(createStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addOne(Entry entry) {
        // get writable

        // set content values
        final ContentValues cv = new ContentValues();
        cv.put(ENTRY_TYPE, entry.getEntryType());
        cv.put(ACTIVITY_TYPE, entry.getActivityType());
        cv.put(COMMENT, entry.getComment());
        cv.put(DISTANCE, entry.getImperialDistance());
        cv.put(DURATION, entry.getDurationInMinutes());
        cv.put(CALORIES, entry.getCalories());
        cv.put(HEART_RATE, entry.getHeartRate());

        // format and put date; format and add start time; use dateString and startTimeString to format a time stamp

        Calendar c = entry.getTimeStamp();
        String dateString = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String startTimeString = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
        String timeStamp = dateString + " " + startTimeString;

        cv.put(ENTRY_TIME_STAMP, timeStamp);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(ENTRY_TABLE, null, cv);
        db.close();
    }

    public List<Entry> getEntries() {
        List<Entry> entries = new ArrayList<>();

        // select query
        String selectQuery = "SELECT * FROM " + ENTRY_TABLE;

        // get readable database; run query on data base
        SQLiteDatabase db = getReadableDatabase();

        // make cursor to ?
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
                String entryType = cursor.getString(1),
                        activityType = cursor.getString(2),
                        comment = cursor.getString(3);
                float distance = (float) cursor.getDouble(4),
                        duration = (float) cursor.getDouble(5);
                int calories = cursor.getInt(6),
                        heartRate = cursor.getInt(7);
                String timeStamp = cursor.getString(8);
                String[] timeStampParts = timeStamp.trim().split(" ");
                // format calendar from time stamp
                String[] datePart = timeStampParts[0].trim().split("-");
                int year = Integer.parseInt(datePart[0]),
                        month = Integer.parseInt(datePart[1]),
                        day = Integer.parseInt(datePart[2]);
                String[] timePart = timeStampParts[1].trim().split(":");
                int hour = Integer.parseInt(timePart[0]),
                        minute = Integer.parseInt(timePart[1]);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);

                Entry entry = new Entry(entryType, activityType, comment,
                        distance, duration, c, calories, heartRate);

                entry.setId(cursor.getInt(0));

                entries.add(entry);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return entries;
    }

    public void deleteEntry(final int id) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try(SQLiteDatabase db = getWritableDatabase()) {
                    String whereClause = "id=?";
                    String[] whereArgs = {id+""};
                    db.delete(ENTRY_TABLE, whereClause, whereArgs);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }

}
