package com.codepath.kathyxing.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

public class PostDetailsActivity extends AppCompatActivity {

    Post post;
    TextView tvUsername;
    TextView tvTimeAgo;
    ImageView ivImage;
    TextView tvDescription;

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

        post = getIntent().getParcelableExtra(Post.class.getSimpleName());

        tvUsername.setText(post.getUser().getUsername());
        tvTimeAgo.setText(post.getCreatedAt().toString());
        tvDescription.setText(post.getDescription());

        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(context).load(image.getUrl()).into(ivImage);
        }
        else {
            ivImage.setVisibility(View.GONE);
        }

    }
}