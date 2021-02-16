package com.example.myruns;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

public class StartFragment extends Fragment {
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_start, container, false);
        context = getContext();

        // get spinners and assign to variables
        final Spinner inputTypeSpinner = view.findViewById(R.id.input_type_spinner),
                activityTypeSpinner = view.findViewById(R.id.activity_type_spinner);

        // make array adapters for the spinners
        ArrayAdapter<CharSequence> inputTypeAdapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.input_type_options, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> activityTypeAdapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.activity_type_options, android.R.layout.simple_spinner_dropdown_item);

        // specify layout to use when list of options appears
        inputTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // link adapters to spinners
        inputTypeSpinner.setAdapter(inputTypeAdapter);
        activityTypeSpinner.setAdapter(activityTypeAdapter);

        // set on click listener for start
        view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get spinner and get spinner value
                Spinner inputTypeSpinner = view.findViewById(R.id.input_type_spinner);
                String inputType = inputTypeSpinner.getSelectedItem().toString().toLowerCase();

                if (inputType.equals("manual entry")) {
                    Intent manualInputIntent = new Intent(getActivity(), ManualEntryActivity.class);
                    // add activity type to intent
                    manualInputIntent.putExtra("activity_type",
                            activityTypeSpinner.getSelectedItem().toString());

                    startActivity(manualInputIntent);
                } else {
                    Intent gpsInputIntent = new Intent(getActivity(), GpsInputActivity.class);

                    if (inputType.equals("automatic")) {
                        // add activity type to intent
                        gpsInputIntent.putExtra("entry_type", "Automatic");

                        // add activity type to intent
                        gpsInputIntent.putExtra("activity_type",
                                "Unknown");

                    } else {
                        // add activity type to intent
                        gpsInputIntent.putExtra("activity_type",
                                activityTypeSpinner.getSelectedItem().toString());

                        // add activity type to intent
                        gpsInputIntent.putExtra("entry_type", "GPS");
                    }

                    // discard any lingering prefs
                    SharedPreferences prefs = context.getSharedPreferences(CodeKeys.GPS_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove(CodeKeys.PREV_LOC);
                    edit.apply();

                    startActivity(gpsInputIntent);
                }
            }

        });

        return view;
    }

}