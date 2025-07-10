package com.example.taskmanager.ui.theme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import com.example.taskmanager.R;
public class SplashScreen extends AppCompatActivity{

    //Duration of the splash screen in ms
    private static final int splashDuration = 3000; //3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Link to your splash screen layout

        // Use a Handler to delay the transition to the LoginScreen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create an Intent to go from SplashScreen to LoginScreen
            Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
            startActivity(intent); // Start the LoginScreen

            // Finish the SplashScreen activity so the user cannot go back to it
            finish();
        }, splashDuration); // The delay duration
    }
}
