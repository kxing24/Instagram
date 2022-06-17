package com.codepath.kathyxing.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Date;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    Post post;
    TextView tvUsername;
    TextView tvTimeAgo;
    ImageView ivImage;
    TextView tvDescription;
    Button btnLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Context context = this;

        // initialize the views
        tvUsername = findViewById(R.id.tvUsername);
        tvTimeAgo = findViewById(R.id.tvTimeAgo);
        ivImage = findViewById(R.id.ivImage);
        tvDescription = findViewById(R.id.tvDescription);
        btnLike = findViewById(R.id.btnLike);

        post = getIntent().getParcelableExtra(Post.class.getSimpleName());

        // set the values
        tvUsername.setText(post.getUser().getUsername());
        tvTimeAgo.setText(calculateTimeAgo(post.getCreatedAt()) + "ago");
        tvDescription.setText(post.getDescription());

        // click handler for the like button
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(post);
            }
        });

        // load in image with glide
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(context).load(image.getUrl()).into(ivImage);
        }
        else {
            ivImage.setVisibility(View.GONE);
        }

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goMainActivity();
            }
        });

    }

    // a method that takes in a date and returns the time ago
    private static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void likePost(Post post) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> likeRelation = currentUser.getRelation("likes");
        likeRelation.add(post);
        Log.i(TAG, "liked post");
        currentUser.saveInBackground();
        Toast.makeText(this, "Liked post!", Toast.LENGTH_SHORT).show();
    }
}