package com.example.myruns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class LocationDataBase extends SQLiteOpenHelper {
    Context mContext;

    private final String LOCATION_TABLE = "location", LATITUDE = "latitude",
            LONGITUDE = "longitude", ALTITUDE = "altitude",
            SPEED = "speed", ENTRY_ID = "entry";

    public LocationDataBase(@Nullable Context context) {
        super(context, "location.db", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createStatement = "create table " + LOCATION_TABLE +"("+
                                 "id integer primary key autoincrement not null,"+
                                 LATITUDE + " decimal(6, 2) not null,"+
                                 LONGITUDE + " decimal(6, 2) not null,"+
                                 ALTITUDE + " decimal(6, 2) not null,"+
                                 SPEED + " decimal(6, 2) not null,"+
                                 ENTRY_ID+ " integer not null);";

        db.execSQL(createStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void makeLocation(double[] data, long entry_id) {
        double lat = data[0], lng = data[1], alt = data[2], speed = data[3];

        // set content values
        final ContentValues cv = new ContentValues();
        cv.put(LATITUDE, lat);
        cv.put(LONGITUDE, lng);
        cv.put(ALTITUDE, alt);
        cv.put(SPEED, speed);
        cv.put(ENTRY_ID, entry_id);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(LOCATION_TABLE, null, cv);
        db.close();
    }

    public ArrayList<double[]> getLocations(int id) {
        ArrayList<double[]> locations = new ArrayList<>();

        // select query
        String selectQuery = "select * from " + LOCATION_TABLE + " where " + ENTRY_ID + " = "+id;

        // get readable database; run query on data base
        SQLiteDatabase db = getReadableDatabase();

        // make cursor to ?
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
               double[] loc = new double[]{
                       cursor.getDouble(1),
                       cursor.getDouble(2),
                       cursor.getDouble(3),
                       cursor.getDouble(4)
               };
               locations.add(loc);
            } while(cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return locations;
    }

    public void delete(final int id, final boolean self) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try(SQLiteDatabase db = getWritableDatabase()) {
                    String whereClause;
                    if (self) whereClause = "id=?";
                    else whereClause = ENTRY_ID+"=?";
                    String[] whereArgs = {id+""};
                    db.delete(LOCATION_TABLE, whereClause, whereArgs);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }
}
