package com.example.endekise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo = findViewById(R.id.imgSplashLogo);
        TextView textTitle = findViewById(R.id.textSplashTitle);
        TextView textBranding = findViewById(R.id.textBranding);

        // --- 1. THE ELEGANT ANIMATIONS ---

        // Animate the Logo (Fade in and Scale up smoothly)
        imgLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate the Title (Fade in and Slide up from below)
        textTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(300) // Wait slightly before text appears
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate the Bottom Branding Text (Slow fade in)
        textBranding.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(600)
                .start();

        // --- 2. ROUTING LOGIC (After 2 seconds) ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Check if the app is locked!
            SharedPreferences prefs = getSharedPreferences("EndeKisePrefs", MODE_PRIVATE);
            boolean isAppLocked = prefs.getBoolean("is_app_locked", false);
            String savedPin = prefs.getString("user_pin", "");

            Intent intent;
            if (isAppLocked && !savedPin.isEmpty()) {
                // Route to PIN Screen
                intent = new Intent(SplashActivity.this, PinLockActivity.class);
            } else {
                // Route directly to Dashboard
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }

            startActivity(intent);

            // Apply a smooth crossfade transition between the Splash and the next screen
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // Destroy the Splash screen so the user can't hit 'Back' to return to it!

        }, 2000); // 2000 milliseconds = 2 seconds total display time
    }
}