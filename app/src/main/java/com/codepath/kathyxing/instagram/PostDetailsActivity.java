package com.codepath.kathyxing.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    Post post;
    TextView tvUsername;
    TextView tvTimeAgo;
    ImageView ivImage;
    TextView tvDescription;
    ImageButton ibLikePost;
    ImageButton ibUnlikePost;

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
        ibLikePost = findViewById(R.id.ibLikePost);
        ibUnlikePost = findViewById(R.id.ibUnlikePost);

        post = getIntent().getParcelableExtra(Post.class.getSimpleName());

        // set the values
        tvUsername.setText(post.getUser().getUsername());
        tvTimeAgo.setText(calculateTimeAgo(post.getCreatedAt()) + "ago");
        tvDescription.setText(post.getDescription());

        // set the visibility of like button depending on whether the post is currently liked by the user
        postLiked(post);

        // click handler for the like button
        ibLikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(post);
                ibLikePost.setVisibility(View.GONE);
                ibUnlikePost.setVisibility(View.VISIBLE);
            }
        });

        // click handler for the unlike button
        ibUnlikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlikePost(post);
                ibUnlikePost.setVisibility(View.GONE);
                ibLikePost.setVisibility(View.VISIBLE);
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

    // set the like button visibilities based on whether the post is already liked
    private void postLiked(Post post) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> likeRelation = currentUser.getRelation("likes");
        likeRelation.getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e != null) {
                    // There was an error
                    Log.e(TAG, "issue with query", e);
                } else {
                    // results have all the Posts the current user liked.
                    if (containsPost(results, post)) {
                        Log.i(TAG, "post is liked by user!");
                        ibLikePost.setVisibility(View.GONE);
                        ibUnlikePost.setVisibility(View.VISIBLE);
                    }
                    else {
                        Log.i(TAG, "post is not liked by user!");
                        ibUnlikePost.setVisibility(View.GONE);
                        ibLikePost.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    // check if results contains the post
    private boolean containsPost(List<ParseObject> results, Post post) {
        for(int i = 0; i < results.size(); i++) {
            if (results.get(i).getObjectId().equals(post.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    private void likePost(Post post) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> likeRelation = currentUser.getRelation("likes");
        likeRelation.add(post);
        currentUser.saveInBackground();
        Log.i(TAG, "liked post");
        Toast.makeText(this, "Liked post!", Toast.LENGTH_SHORT).show();
    }

    private void unlikePost(Post post) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> likeRelation = currentUser.getRelation("likes");
        likeRelation.remove(post);
        currentUser.saveInBackground();
        Log.i(TAG, "unliked post");
        Toast.makeText(this, "Unliked post!", Toast.LENGTH_SHORT).show();
    }
}