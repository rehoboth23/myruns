package com.example.myruns;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MyMainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    PagerAdapter pagerAdapter;

    // view pager fragment adapter
    private static class PagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<Fragment> fragments = new ArrayList<>();

        // create fragment maker
        public void addFragment(Fragment fragment, String title) {
            titles.add(title); // add title
            fragments.add(fragment); // add fragment
        }

        public PagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position); // return fragment @position in fragments
        }

        @Override
        public int getCount() {
            return fragments.size(); // size of fragments list
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position); // return title @position in titles
        }

    }

    // unit preferences dialog fragment
    public static class UnitDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // get activity and assert not null
            Activity activity = getActivity();
            assert activity != null;

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            // initialize layout inflater and get view
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.fragment_units_dialog_view, null);

            // set cancel button on click listener; use dialog dismiss
            view.findViewById(R.id.unit_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog =  UnitDialog.this.getDialog();
                    assert dialog != null;
                    dialog.dismiss();
                }
            });

            // get radio group
            RadioGroup unitTypes = view.findViewById(R.id.unit_types);

            // get shared preferences
            SharedPreferences preferences = activity.getSharedPreferences(
                    CodeKeys.MAIN_PREFERENCES, MODE_PRIVATE
            );

            // set checked unit
            unitTypes.check(preferences.getInt(CodeKeys.UNIT_TYPE, -1));

            unitTypes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // get activity and assert not null
                    Activity activity = getActivity();
                    assert activity != null;

                    // get shared preferences and make editor
                    SharedPreferences preferences = activity.getSharedPreferences(
                            CodeKeys.MAIN_PREFERENCES, MODE_PRIVATE
                    );
                    SharedPreferences.Editor edit = preferences.edit();

                    // put id of checked into preferences
                    edit.putInt(CodeKeys.UNIT_TYPE, group.getCheckedRadioButtonId());
                    edit.apply(); // apply changes

                    // to update the history display
                    SharedPreferences preferences2 = getActivity().getSharedPreferences(
                            CodeKeys.MANUAL_INPUT_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor edit2 = preferences2.edit();
                    edit2.putBoolean("DB_UPDATED", true);
                    edit2.apply();

                    // close the dialog
                    Dialog dialog = UnitDialog.this.getDialog();
                    assert dialog != null;
                    dialog.cancel();

                }
            });
            // set builder view
            builder.setView(view);

            return builder.create();
        }
    }

    // comments dialog fragment
    public static class CommentsDialog extends DialogFragment {
        private final String COMMENT_TEXT = "comments_text";
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            // get activity and assert not null
            Activity activity = getActivity();
            assert activity != null;

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            // initialize layout inflater and get view
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            final View view = layoutInflater.inflate(R.layout.fragment_text_dialog_view, null);

            // get shared preferences
            SharedPreferences preferences = activity.getSharedPreferences(
                    CodeKeys.MAIN_PREFERENCES, MODE_PRIVATE
            );

            // get edit text for the comment and set comment text available
            EditText settingsComment = view.findViewById(R.id.dialog_text);
            settingsComment.setText(preferences.getString(COMMENT_TEXT, ""));
            settingsComment.setInputType(InputType.TYPE_CLASS_TEXT);

            // set cancel button on click listener
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // dismiss the dialog;
                    dialog.dismiss();
                }
            });
            // set ok button on click listener
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // get activity and assert not null
                    Activity activity = getActivity();
                    assert activity != null;

                    // get shared preferences and make editor
                    SharedPreferences preferences = activity.getSharedPreferences(
                            CodeKeys.MAIN_PREFERENCES, MODE_PRIVATE
                    );
                    SharedPreferences.Editor edit = preferences.edit();

                    // get edit text for the comment
                    EditText settingsComment = view.findViewById(R.id.dialog_text);

                    // put text in editor
                    edit.putString(COMMENT_TEXT, settingsComment.getText().toString());
                    edit.apply(); // apply changes

                    // dismiss the dialog;
                    dialog.dismiss();
                }
            });

            // set builder view
            builder.setView(view);

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            AlertDialog dialog = (AlertDialog) getDialog();
            assert dialog != null;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireActivity().getColor(R.color.colorAccent));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireActivity().getColor(R.color.colorAccent));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);

        if (Util.checkPermission(this)) Util.requestPermissions(this);

        // retrieve views
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1) updateDisplay();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        prepareViewPager(); // prepare view pager

        tabLayout.setupWithViewPager(viewPager); // set up view with pager
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }

    // update history display
    public void updateDisplay() {
        SharedPreferences entry_prefs = getSharedPreferences(CodeKeys.MANUAL_INPUT_PREFS, MODE_PRIVATE);
        boolean dbChange = entry_prefs.getBoolean("DB_UPDATED", false);

        if(dbChange) {
            SharedPreferences.Editor edit = entry_prefs.edit();
            edit.remove("DB_UPDATED");
            edit.apply();
            recreate();
        }
    }

    /**
 * HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS
 */
    private void prepareViewPager() {
        // initialize PagerAdapter
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        StartFragment startFragment = new StartFragment(); // initialize pager fragment
        HistoryFragment historyFragment = new HistoryFragment(); // initialize history fragment
        SettingsFragment settingsFragment = new SettingsFragment(); // initialize setting fragment

        pagerAdapter.addFragment(startFragment, "START"); // add start fragment
        pagerAdapter.addFragment(historyFragment, "HISTORY"); // add history fragment
        pagerAdapter.addFragment(settingsFragment, "SETTINGS"); // add settings fragment

        // set adapter
        viewPager.setAdapter(pagerAdapter);

        // set page change listener for updating history

    }

}
