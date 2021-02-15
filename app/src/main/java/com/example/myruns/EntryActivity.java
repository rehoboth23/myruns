package com.example.myruns;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EntryActivity extends AppCompatActivity {

    private static class EntryAdapter extends ArrayAdapter<String> {
        Context context;
        int resource;
        String[] objects;
        private String[] labels = {"Input Type", "Activity Type", "Date and Time", "Duration",
                                    "Distance", "Calories", "HeartRate"};
        public EntryAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            this.objects = objects;
        }

        @SuppressLint({"ViewHolder", "InflateParams"})
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, null, true);
            TextView t = convertView.findViewById(R.id.field_label);
            EditText e = convertView.findViewById(R.id.field_value);

            // set label
            t.setText(labels[position]);
            e.setText(objects[position]);
            e.setKeyListener(null);
            e.setFocusable(true);
            e.setCursorVisible(false);

            return convertView;
        }
    }

    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String[] info = {bundle.getString("entryType"),
                         bundle.getString("activityType"),
                         bundle.getString("timestamp"),
                         bundle.getString("duration"),
                         bundle.getString("distance"),
                         bundle.getString("calories"),
                         bundle.getString("heartRate")};
        id = bundle.getInt("id");
        ListView lv = findViewById(R.id.entry_list_view);
        EntryAdapter entryAdapter = new EntryAdapter(getApplicationContext(), R.layout.layout_entry_item, info);
        lv.setDivider(null);
        lv.setAdapter(entryAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_menu, menu); // inflate custom menu
        MenuItem item = menu.findItem(R.id.delete);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(id != -1) {
                    DataBaseUtil util = new DataBaseUtil(getApplicationContext());
                    util.deleteEntry(id);

                    finish();
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
}