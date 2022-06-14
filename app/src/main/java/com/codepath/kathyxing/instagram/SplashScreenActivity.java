package com.codepath.kathyxing.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // s̶e̶t̶C̶o̶n̶t̶e̶n̶t̶V̶i̶e̶w̶(̶)̶;
        doFirstRunCheckup();
    }

    private void doFirstRunCheckup() {
        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        finish();
    }
}