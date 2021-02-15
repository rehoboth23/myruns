package com.example.myruns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View  view =  inflater.inflate(R.layout.fragment_settings, container, false);

        // get activity and assert not null
        Activity activity = getActivity();
        assert activity != null;

        // get shared preferences
        SharedPreferences preferences = activity.getSharedPreferences(
                CodeKeys.MAIN_PREFERENCES, Context.MODE_PRIVATE
        );

        // get check box
        CheckBox checkBox = view.findViewById(R.id.privacy_setting_checkbox);
        checkBox.setChecked(preferences.getBoolean( CodeKeys.PRIVACY, false));

        // set user profile on click listener
        view.findViewById(R.id.user_profile_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        // set anonymous setting on click listener
        view.findViewById(R.id.anonymous_setting_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = view.findViewById(R.id.privacy_setting_checkbox);
                checkBox.setChecked(!checkBox.isChecked());

                // get activity and assert not null
                Activity activity = getActivity();
                assert activity != null;

                // get shared preferences and make editor
                SharedPreferences preferences = activity.getSharedPreferences(
                        CodeKeys.MAIN_PREFERENCES, Context.MODE_PRIVATE
                );
                SharedPreferences.Editor edit = preferences.edit();

                // put boolean state of check box in preferences
                edit.putBoolean( CodeKeys.PRIVACY, checkBox.isChecked());
                edit.apply(); // apply changes
            }
        });

        // set unit preferences on click listener
        view.findViewById(R.id.unit_preferences_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUnitDialog();
            }
        });

        // set comments on click listener
        view.findViewById(R.id.comments_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommentsDialog();
            }
        });

        // set webpage on click listener
        view.findViewById(R.id.webpage_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.webpage_link))
                );
                startActivity(browserIntent);
            }
        });
        return view;
    }

    public void openUnitDialog() {
        // make new dialog using unit dialog fragment from main activity
        MyMainActivity.UnitDialog dialog = new MyMainActivity.UnitDialog();

        // get fragment manager and assert not null
        FragmentManager fragmentManager = getChildFragmentManager();

        // show dialog
        dialog.show(fragmentManager,  CodeKeys.UNIT_DIALOG_TAG);
    }

    public void openCommentsDialog() {
        // make new dialog using comments dialog fragment from main activity
        MyMainActivity.CommentsDialog dialog = new MyMainActivity.CommentsDialog();

        // get fragment manager and assert not null
        FragmentManager fragmentManager = getChildFragmentManager();

        // show dialog
        dialog.show(fragmentManager,  CodeKeys.COMMENTS_DIALOG_TAG);
    }

}