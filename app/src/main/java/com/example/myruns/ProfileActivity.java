package com.example.myruns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity {

    private Uri tempImageUri;
    private boolean cImage = false;
    private SharedPreferences preferences;
    private ImageView profileImage;
    private EditText name, email, phone, my_class, major;
    private RadioGroup gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get views
        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.name); // name edit text
        email = findViewById(R.id.email); // email edit text
        phone = findViewById(R.id.phone); // phone edit text
        my_class = findViewById(R.id.my_class); // class edit text
        major = findViewById(R.id.major); // major edit text
        gender = findViewById(R.id.gender);

        // retrieve shared preferences
        preferences = getSharedPreferences("PROFILE_PREFS", MODE_PRIVATE);
        tempImageUri = null;


        // set the profile image: use uir in shared preferences if available
        if(!preferences.contains("imageUri")) {
            // check if profile image file exists; set the profile image
            File profileImageFile = new File(getExternalFilesDir(null), CodeKeys.PROFILE_IMAGE_FILE_NAME);
            Util.imageFileHelper(profileImageFile, profileImage); // set the profile image
        }
        else {
            tempImageUri = Uri.parse(preferences.getString("imageUri", ""));
            profileImage.setImageURI(null);
            profileImage.setImageURI(tempImageUri);
            cImage = true;
        }


        // set the fields using data in shared preferences
        fieldSetterHelper();

        // get permissions set
        if(Util.checkPermission(this)) Util.requestPermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu); // inflate custom menu
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CodeKeys.IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            makeTempImage(data);
        }
        else if(requestCode == CodeKeys.IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            Uri resultUri = data.getData();

            // crop the image using crop image view
            Crop.of(resultUri, tempImageUri).asSquare().start(this);

            cImage = true;
        }
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {

            // set the image uri to cropped image uri
            profileImage.setImageURI(null);
            profileImage.setImageURI(tempImageUri);

            // make edits
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("imageUri", tempImageUri.toString());
            edit.apply();

            cImage = true;
        }
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_CANCELED) {

            // set image view uri to original image uri
            profileImage.setImageURI(null);
            profileImage.setImageURI(tempImageUri);

            // set change in shared preferences
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("imageUri", tempImageUri.toString());
            edit.apply();

            // indicate the image was changed for when save is called
            cImage = true;
        }
    }

    // change image
    public void changeImage(View v) {
        // get permissions set
        if(Util.checkPermission(this)) Util.requestPermissions(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // initialize layout inflater and get view
        LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.fragment_image_change, null);

        builder.setView(view);
        final Dialog dialog = builder.create();

        if(tempImageUri == null){
            // make a temporary file and uri for the captured image
            File tempImageFile = new File(getExternalFilesDir(null), CodeKeys.PROFILE_TEMP_IMAGE_NAME);
            tempImageUri = FileProvider.getUriForFile(this, "com.example.myruns", tempImageFile);
        }

        // set on click listeners
        view.findViewById(R.id.select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent imagePickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imagePickerIntent, CodeKeys.IMAGE_PICKER_REQUEST);//zero can be replaced with any action code (called requestCode)
            }
        });

        view.findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // make intent for camera activity
                Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // put uri as output target location
                imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);

                // ensure resource is available: prevents application clashes
                if(imageCaptureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(imageCaptureIntent, CodeKeys.IMAGE_CAPTURE_REQUEST);
                }
            }
        });

        dialog.show();
    }

    public void makeTempImage(@Nullable Intent data) {
        if(tempImageUri != null){
            Crop.of(tempImageUri, tempImageUri).asSquare().start(this);
        }
        else { // use bitmap from extras otherwise
            // assert the intent data is not null
            assert data != null;
            Bundle extras = data.getExtras();
            assert extras != null; // ensure that the extras bundle exists
            Bitmap imageBitMap = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(imageBitMap); // set the image bitmap using data from extras
        }
    }

    // save profile
    public void profileSave(View view) throws FileNotFoundException {
        // get permissions set
        if(Util.checkPermission(this)) Util.requestPermissions(this);

        if(cImage) {
            // copy data at temp file uri into permanent profile image file
            byte[] imageData = new byte[1024];
            File profileImageFile = new File(getExternalFilesDir(null), CodeKeys.PROFILE_IMAGE_FILE_NAME);
            InputStream in = getContentResolver().openInputStream(tempImageUri);
            OutputStream out = new FileOutputStream(profileImageFile);
            assert (in != null);
            copyFileHelper(in , out, imageData);

            // CLEAN UP
            removeImageUriFromPreferences();
            cImage = false; // indicate that changed image is deal with
            tempImageUri = null; // set tempImageUri to null
            deleteTempFile(); // delete temporary file name
        }
        // validate email
        boolean emailCheck = Util.checkEmailView(this, email, (TextView)findViewById(R.id.emailId));

        if(emailCheck) {
            // make a preference editor and set the field values
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("name", name.getText().toString());
            editor.putString("email", email.getText().toString());
            editor.putString("phone", phone.getText().toString());
            editor.putString("class", my_class.getText().toString());
            editor.putString("major", major.getText().toString());
            editor.putInt("gender", gender.getCheckedRadioButtonId());
            editor.apply(); // apply changes to editor

            // let user know changes have been made
            Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
        }
    }

    // clean out changes and reset to original
    public void profileCancel(View view) {
        // get permissions set
        if(Util.checkPermission(this)) Util.requestPermissions(this);

        if(cImage) {
            // check if profile image file exists; set the profile image
            File profileImageFile = new File(getExternalFilesDir(null), CodeKeys.PROFILE_IMAGE_FILE_NAME);
            boolean imageSet = Util.imageFileHelper(profileImageFile, profileImage);

            if(!imageSet) {
                // set image source to default image resource
                profileImage.setImageResource(R.mipmap.ic_default_profile_image);
            }

            // CLEAN UP
            removeImageUriFromPreferences();
            cImage = false; // image is set to original
            tempImageUri = null; // null temp image uri
            deleteTempFile(); // delete temporary file name
        }

        // set field values using shared preferences
        fieldSetterHelper();

        if(view != null) finish(); // finish activity
    }




/**
 * HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS HELPERS  HELPERS
 */
    private void deleteTempFile() {
        // delete file with fileName
        File tempImageFile = new File(getExternalFilesDir(null), CodeKeys.PROFILE_TEMP_IMAGE_NAME);
        if(tempImageFile.exists()) {
            if(tempImageFile.delete()) Log.d("del", "Deleted " + CodeKeys.PROFILE_TEMP_IMAGE_NAME);
        }
    }

    private void fieldSetterHelper() {
        // set field values using stored values from shared preferences
        name.setText(preferences.getString("name", ""));
        email.setText(preferences.getString("email", ""));
        phone.setText(preferences.getString("phone", ""));
        gender.check(preferences.getInt("gender", -1));
        my_class.setText(preferences.getString("class", ""));
        major.setText(preferences.getString("major", ""));
    }

    public void copyFileHelper(InputStream in, OutputStream out, byte[] data) {
        try{
            int bytesRead;
            while((bytesRead = in.read(data)) > 0) {
                out.write(Arrays.copyOfRange(data, 0, Math.max(0, bytesRead))); // write bytes to output stream
            }
            // close streams
            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeImageUriFromPreferences(){
        SharedPreferences.Editor edit = preferences.edit();
        edit.remove("imageUri");
        edit.apply();
    }
}