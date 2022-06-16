package com.codepath.kathyxing.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.kathyxing.instagram.fragments.ComposeFragment;
import com.codepath.kathyxing.instagram.fragments.FeedFragment;
import com.codepath.kathyxing.instagram.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    Context context = this;

    private static final String TAG = "MainActivity";
    private int check = 0;
    private NDSpinner sUserDropdownMenu;

    private RelativeLayout rlPreview;
    private Button btnSubmitProfile;
    private Bitmap selectedProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        rlPreview = findViewById(R.id.rlPreview);
        btnSubmitProfile = findViewById(R.id.btnSubmitProfile);
        rlPreview.setVisibility(View.GONE);
        sUserDropdownMenu = findViewById(R.id.sUserDropdownMenu);

        // click handler for submit profile button
        btnSubmitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                saveProfilePicture(currentUser);
            }
        });

        // set up the spinner
        String[] userMenuOptions = getResources().getStringArray(R.array.user_menu_options);
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, userMenuOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sUserDropdownMenu.setAdapter(spinnerAdapter);
        sUserDropdownMenu.setOnItemSelectedListener(this);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        // define your fragments here
        final Fragment fragment1 = new FeedFragment();
        final Fragment fragment2 = new ComposeFragment();
        final Fragment fragment3 = new ProfileFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        fragment = fragment1;
                        break;
                    case R.id.action_capture:
                        fragment = fragment2;
                        break;
                    case R.id.action_profile:
                        fragment = fragment3;
                        break;
                    default: return true;
                }
                fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    private Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void saveProfilePicture(ParseUser currentUser) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selectedProfileImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();

        // Create and save the ParseFile
        ParseFile file = new ParseFile("profile.png", image);
        file.saveInBackground();

        // Set the file as the user's profile image and save it
        currentUser.put("profileImage", file);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(context, "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i(TAG, "Profile picture save was successful");
                    Toast.makeText(context, "Successfully save profile picture!", Toast.LENGTH_SHORT).show();
                    //ivPostImage.setImageResource(0);
                }
                rlPreview.setVisibility(View.GONE);
            }
        });
    }

    private void logoutUser() {
        Log.i(TAG, "Logging out");
        ParseUser.logOutInBackground();
        goLoginActivity();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if ((data != null) && requestCode == PICK_PHOTO_CODE) {
                rlPreview.setVisibility(View.VISIBLE);

                Uri photoUri = data.getData();

                // Load the image located at photoUri into selectedImage
                selectedProfileImage = loadFromUri(photoUri);

                // Set the image as the profile picture
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(selectedProfileImage);

                //TODO: create an activity to show the profile picture preview
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (++check > 1 && parent.getId() == R.id.sUserDropdownMenu) {
            String valueFromSpinner = parent.getItemAtPosition(position).toString();
            if (valueFromSpinner.equals(getString(R.string.logout))) {
                Log.i("FeedActivity", "logout selected");
                logoutUser();
            }
            if (valueFromSpinner.equals(getString(R.string.add_profile_picture))) {
                Log.i("FeedActivity", "add profile picture selected");
                // TODO: call an activity that lets the user select a photo
                onPickPhoto();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}