package com.example.myruns;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Objects;

public class ManualEntryActivity extends AppCompatActivity {

    private final class ManualInputAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final int resource;
        private final String[] labels;

        public ManualInputAdapter(@NonNull final Context context, int resource, @NonNull String[] objects) {
            super(context, resource, objects);
            this.context = context; // set the context
            this.resource = resource; // set the resource
            labels = objects; // set the button labels array

            // set on save click listener
            findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearPreferences();
                    removeLabel();
                    finish();
                    Toast.makeText(context, "Entry discarded", Toast.LENGTH_SHORT).show();
                }
            });

            // set on cancel click listener
            findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = (Activity) context;
                    String entryType = "Manual Entry",
                            activityType = activity.getIntent().getStringExtra("activity_type"),
                            comment = preferences.getString("comment", "");
                    float distance = Float.parseFloat(Objects.requireNonNull(preferences.getString("distance", "0"))),
                            duration = Float.parseFloat(Objects.requireNonNull(preferences.getString("duration", "0")));

                    Calendar c = Calendar.getInstance();
                    LocalDate d = LocalDate.now();
                    LocalTime t = LocalTime.now();
                    c.set(Calendar.YEAR, preferences.getInt("year", d.getYear()));
                    c.set(Calendar.MONTH, preferences.getInt("month", d.getMonthValue()));
                    c.set(Calendar.DAY_OF_MONTH, preferences.getInt("dayOfMonth", d.getDayOfMonth()));
                    c.set(Calendar.HOUR_OF_DAY, preferences.getInt("hour", t.getHour()));
                    c.set(Calendar.MINUTE, preferences.getInt("minute", t.getMinute()));
                    c.set(Calendar.SECOND, 0);

                    int calories = Integer.parseInt(Objects.requireNonNull(preferences.getString("calories", "0"))),
                            heartRate = Integer.parseInt(Objects.requireNonNull(preferences.getString("heart rate", "0")));

                    final Entry entry = new Entry(entryType, activityType, comment,
                            distance, duration, c, calories, heartRate);

                    final DataBaseUtil db = new DataBaseUtil(context);

                    // insert cv into the table
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                db.addManualEntry(entry);

                                // indicate that the data source has been updated
                                SharedPreferences.Editor edit = preferences.edit();
                                edit.putBoolean("DB_UPDATED", true);
                                edit.apply();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();


                    clearPreferences();
                    removeLabel();
                    finish();
                }
            });
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // get an inflater from the context
            LayoutInflater inflater = LayoutInflater.from(context);

            // make the row view; retrieve and set the text for the row texView
            @SuppressLint("ViewHolder") View row = inflater.inflate(resource, parent, false);
            TextView buttonText = row.findViewById(R.id.button_text);
            buttonText.setText(labels[position]);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManualInputAdapter.this.onClick(v);
                }
            });

            // return the row view
            return row;
        }



        private void onClick(final View v) {
            TextView textView = v.findViewById(R.id.button_text);

            label = textView.getText().toString();
            makeDialog();
        }

        private void makeDialog() {
            switch (label.toLowerCase()) {
                case "date":
                    final LocalDate date = LocalDate.now();

                    // get initial values
                    final int preYear =  preferences.getInt("year", date.getYear()),
                            preMonth = preferences.getInt("month", date.getMonthValue()-1),
                            preDayOfMonth = preferences.getInt("dayOfMonth", date.getDayOfMonth());

                    // instantiate date picker
                    datePickerDialog = new DatePickerDialog(context, null, preYear, preMonth, preDayOfMonth);

                    // apply changes on ok click
                    datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override // apply changes on ok click
                        public void onClick(DialogInterface dialog, int which) {
                            removeLabel();
                            dialog.dismiss(); // dismiss dialog
                        }
                    });
                    // cancel changes on cancel click
                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override // cancel changes on cancel click
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == DialogInterface.BUTTON_NEGATIVE){ // get the shared preferences and make an editor
                                SharedPreferences.Editor edit =preferences.edit();

                                // put in current date in case no changes are met
                                edit.putInt("year",preYear );
                                edit.putInt("month", preMonth);
                                edit.putInt("dayOfMonth", preDayOfMonth);
                                edit.apply();
                            }
                            removeLabel();
                            dialog.dismiss(); // dismiss dialog
                        }
                    });

                    DatePicker dp = datePickerDialog.getDatePicker();
                    dp.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // get the shared preferences and make an editor
                            SharedPreferences.Editor edit =preferences.edit();

                            edit.putInt("year",year );
                            edit.putInt("month", monthOfYear);
                            edit.putInt("dayOfMonth", dayOfMonth);
                            edit.apply();
                        }
                    });

                    // show dialog
                    datePickerDialog.show();

                    // set button color;
                    datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent));
                    datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorAccent));

                    break;
                case "time":
                    LocalTime localTime = LocalTime.now();
                    TimePicker timePicker = new TimePicker(context);
                    timePicker.setIs24HourView(LocalTime.MAX.getHour() == 23);

                    // set initial hour and minute
                    timePicker.setHour(preferences.getInt("hour", localTime.getHour()));
                    timePicker.setMinute(preferences.getInt("minute", localTime.getMinute()));

                    // make alert dialog with time picker widget
                    AlertDialog.Builder timePickerBuilder = new AlertDialog.Builder(context);
                    timePickerBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeLabel();
                            dialog.dismiss(); // dismiss dialog
                        }
                    });

                    timePickerBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // get the shared preferences and make an editor
                            SharedPreferences.Editor edit = preferences.edit();

                            //put in current date in case no changes are met
                            edit.remove("hour");
                            edit.remove("minute");
                            edit.apply();

                            removeLabel();
                            dialog.dismiss(); // dismiss dialog
                        }
                    });

                    timePickerBuilder.setView(timePicker);

                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putInt("hour", hourOfDay);
                            edit.putInt("minute", minute);
                            edit.apply();
                        }
                    });

                    timePickerDialog =  timePickerBuilder.create();
                    
                    timePickerDialog.show();

                    timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorAccent));
                    timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent));

                    break;
                case "duration": case "distance": case "calories": case "heart rate": case "comment":

                    // get activity and assert not null
                    Activity activity = (Activity) context;

                    // Use the Builder class for convenient dialog construction
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    // inflate view using view inflate
                    final View view = View.inflate(context, R.layout.fragment_text_dialog_view, null);

                    // set label of view
                    TextView viewLabel = view.findViewById(R.id.text_dialog_label);
                    viewLabel.setText(label);

                    // get edit text  in view and set the value to what is in preferences @label
                    final EditText editText = view.findViewById(R.id.dialog_text);
                    editText.setText(preferences.getString(label.toLowerCase(), ""));

                    // set input type (also set input hint in case of comments is clicked
                    if(label.toLowerCase().equals("comment")) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText.setHint("How did it go? Notes here.");
                    }
                    else if(label.toLowerCase().equals("calories") || label.toLowerCase().equals("heart rate")) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }

                    // set cancel button on click listener; use dialog dismiss
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // dismiss dialog
                            removeLabel();
                            dialog.dismiss();
                        }
                    });
                    // set ok button on click listener; use dialog dismiss
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // get the shared preferences and make an editor
                            SharedPreferences.Editor edit = preferences.edit();

                            //set text value at @label in preferences
                            edit.putString(label.toLowerCase(), editText.getText().toString());
                            // apply change
                            edit.apply();

                            // dismiss dialog
                            removeLabel();
                            dialog.dismiss();

                        }
                    });

                    // set builder view to view and get dialog
                    builder.setView(view);
                    textDialog = builder.create();

                    // show dialog
                    textDialog.show();

                    // change button color
                    textDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorAccent));
                    textDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent));

                    break;
                default:
                    Log.d("unknown", "Method received unknown input. Potential Threat!");
                    break;
            }
        }

        // clear out the shared prefs
        public void clearPreferences() {

            // array keys used in the shared preferences
            String[] keys = {"year", "month", "dayOfMonth", "hour", "minute", "duration", "distance", "calories", "heart rate", "comment"};

            // editor
            SharedPreferences.Editor edit = preferences.edit();

            // loop through and remove all keys
            for(String key: keys) {
                if(preferences.contains(key)) edit.remove(key);
            }

            // apply removals
            edit.apply();
        }
    }

    private final String[] BUTTONS_LABELS = {"Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment"};
    private SharedPreferences preferences;
    private DatePickerDialog datePickerDialog;
    private AlertDialog timePickerDialog;
    private AlertDialog textDialog;
    private String label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        // get the list view and set it
        ListView listView = findViewById(R.id.manual_input_list);

        // make an adapter for the list view using the ManualInputAdapter class
        ManualInputAdapter manualInputAdapter = new ManualInputAdapter(this, R.layout.manual_input_row, BUTTONS_LABELS);

        // link adapter to list view
        listView.setAdapter(manualInputAdapter);

        // set preferences
        preferences = getSharedPreferences(CodeKeys.MANUAL_INPUT_PREFS, MODE_PRIVATE);

        // try to recreate any dialogs
        label = preferences.getString("label", null);
        if(label != null) manualInputAdapter.makeDialog();

    }

    @Override
    protected void onDestroy() {

        // dismiss any running dialogs
        if(datePickerDialog != null && datePickerDialog.isShowing()) datePickerDialog.dismiss();
        else if(timePickerDialog != null && timePickerDialog.isShowing()) timePickerDialog.dismiss();
        else if(textDialog != null && textDialog.isShowing()) {
            // make shared preferences edit and save current text in preferences
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("label", label);
            EditText editText = textDialog.findViewById(R.id.dialog_text);

            assert editText != null;
            edit.putString(label.toLowerCase(), editText.getText().toString());
            edit.apply();

            textDialog.dismiss();
        }

        // save label if available
        if(label != null){
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("label", label);
            edit.apply();
        }

        super.onDestroy();
    }



    public void removeLabel(){
        label = null;
        SharedPreferences.Editor edit = preferences.edit();
        edit.remove("label");
        edit.apply();
    }

}