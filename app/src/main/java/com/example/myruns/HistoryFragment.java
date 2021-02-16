package com.example.myruns;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment {
    private static class HistoryListAdapter extends ArrayAdapter<Entry> {
        Context context;
        int resource;
        List<Entry> entries;
        SharedPreferences preferences;

        public HistoryListAdapter(@NonNull Context context, int resource, @NonNull List<Entry> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            entries = objects;

            Activity activity = (Activity) context;
            preferences = activity.getSharedPreferences(
                    CodeKeys.MAIN_PREFERENCES, Context.MODE_PRIVATE
            );
        }

        @SuppressLint("ViewHolder")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(resource, null, false);
            final Entry entry = entries.get(position);
            int unit = preferences.getInt(CodeKeys.UNIT_TYPE, 0);
            if (entry.getEntryType().equals("Manual Entry")) {
                return startManualEntry(view, entry, unit);
            }
            return startMapEntry(view, entry, unit);
        }

        public View startMapEntry(View convertView, final Entry entry, final int unit) {

            final String entryType = entry.getEntryType(),
                    activityType = entry.getActivityType();

            Calendar c = entry.getTimeStamp();
            // retrieve and format time stamp
            String hour = c.get(Calendar.HOUR_OF_DAY)+"", minute = c.get(Calendar.MINUTE)+"";
            if(hour.length() == 1) hour = "0"+hour;
            if(minute.length() == 1) minute = "0"+minute;

            final String timestamp =  hour+":"+minute+":00 "+
                    Util.getMonthString(c.get(Calendar.MONTH))+" "+
                    c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.YEAR);

            // merge to form main sentence
            String entryTypeTime = entryType+": "+activityType+", "+ timestamp;

            // retrieve and format distance
            String distance;
            if(unit == 0 || unit == R.id.imperial_option){
                float distanceValue = entry.getImperialDistance();
                distance = distanceValue + " Miles";
            }else {
                float distanceValue = entry.getMetricDistance();
                distance = distanceValue + " Kilometers";
            }

            // retrieve and format time lapse
            final float duration = entry.getDurationInMinutes();
            int minutes = (int)Math.floor(duration);
            int seconds = (int) Math.floor((duration - minutes) * 60);
            String timeLapse = "";
            if(minutes > 0) timeLapse += minutes + "mins ";
            timeLapse += seconds + "secs";

            // merge distance and time lapse
            String entryDetails = distance + " " + timeLapse;

            // set view text
            TextView v1 = convertView.findViewById(R.id.entry_type_time);
            v1.setText(entryTypeTime);
            TextView v2 = convertView.findViewById(R.id.entry_details);
            v2.setText(entryDetails);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent entryIntent = new Intent(context, MapEntryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("activityType", activityType);
                    bundle.putString("calories", entry.getCalories()+"");
                    bundle.putInt("unit", unit);
                    ArrayList<String> locs = new ArrayList<>();
                    for (double[] l: entry.getLocations()) {
                        locs.add(l[0]+","+l[1]+","+l[2]+","+l[3]);
                    }
                    bundle.putStringArrayList("locations", locs);
                    bundle.putInt("id", entry.getId());
                    entryIntent.putExtras(bundle);

                    Activity activity = (Activity) context;
                    activity.startActivity(entryIntent);
                }
            });

            return convertView;
        }

        public View startManualEntry(View convertView, final Entry entry, int unit) {

            final String entryType = entry.getEntryType(),
                    activityType = entry.getActivityType();
            Calendar c = entry.getTimeStamp();

            // retrieve and format time stamp
            String hour = c.get(Calendar.HOUR_OF_DAY)+"", minute = c.get(Calendar.MINUTE)+"";
            if(hour.length() == 1) hour = "0"+hour;
            if(minute.length() == 1) minute = "0"+minute;

            final String timestamp =  hour+":"+minute+":00 "+
                    Util.getMonthString(c.get(Calendar.MONTH))+" "+
                    c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.YEAR);

            // merge to form main sentence
            String entryTypeTime = entryType+": "+activityType+", "+ timestamp;

            // retrieve and format distance
            String distance;
            if(unit == 0 || unit == R.id.imperial_option){
                float distanceValue = entry.getImperialDistance();
                distance = distanceValue + " Miles";
            }else {
                float distanceValue = entry.getMetricDistance();
                distance = distanceValue + " Kilometers";
            }

            // retrieve and format time lapse
            final float duration = entry.getDurationInMinutes();
            int minutes = (int)Math.floor(duration);
            int seconds = (int) Math.floor((duration - minutes) * 60);
            String timeLapse = "";
            if(minutes > 0) timeLapse += minutes + "mins ";
            timeLapse += seconds + "secs";

            // merge distance and time lapse
            String entryDetails = distance + " " + timeLapse;

            // set view text
            TextView v1 = convertView.findViewById(R.id.entry_type_time);
            v1.setText(entryTypeTime);
            TextView v2 = convertView.findViewById(R.id.entry_details);
            v2.setText(entryDetails);

            // set on click listener
            final String finalTimeLapse = timeLapse;
            final String finalDistance = distance;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent entryIntent = new Intent(context, MEntryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("entryType", entryType);
                    bundle.putString("activityType", activityType);
                    bundle.putString("timestamp", timestamp);
                    bundle.putString("distance", finalDistance);
                    bundle.putString("duration", finalTimeLapse);
                    bundle.putString("calories", entry.getCalories()+"");
                    bundle.putString("heartRate", entry.getHeartRate()+"");
                    bundle.putInt("id", entry.getId());
                    entryIntent.putExtras(bundle);

                    Activity activity = (Activity) context;
                    activity.startActivity(entryIntent);
                }
            });

            return convertView;
        }
    }
    View view;
    Context context;
    HistoryListAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // assert context not null
        context = getContext();
        assert context != null;

        Activity activity = getActivity();
        Log.d("ACTIVITY NULL", (activity == null)+"");

        view = inflater.inflate(R.layout.fragment_history, container, false);

        setListAdapter();

        // Inflate the layout for this fragment
        return view;
    }


    public void setListAdapter() {
        try{
            final Activity activity = getActivity();
            assert activity != null;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    DataBaseUtil dataBaseUtil = new DataBaseUtil(Objects.requireNonNull(getActivity()).getApplicationContext());
                    final List<Entry> entries = dataBaseUtil.getEntries();
                    activity.runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            // make adapter
                            adapter = new HistoryListAdapter(context, R.layout.layout_history_item, entries);

                            // get view, list view in view and set adapter to list view
                            ListView listView = view.findViewById(R.id.list_view);
                            listView.setAdapter(adapter);
                        }
                    });
                }
            });

            t.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}