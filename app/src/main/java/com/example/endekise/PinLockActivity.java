package com.example.endekise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class PinLockActivity extends AppCompatActivity {

    private String currentPin = "";
    private String savedPin = "";
    private boolean isSettingUpPin = false;

    // Security Tracker
    private int failedAttempts = 0;

    private TextView textInstruction;
    private View[] dots = new View[4];

    // Hint Elements
    private LinearLayout layoutHintSetup;
    private TextInputEditText editCustomHint;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        textInstruction = findViewById(R.id.textPinInstruction);
        layoutHintSetup = findViewById(R.id.layoutHintSetup);
        editCustomHint = findViewById(R.id.editCustomHint);

        dots[0] = findViewById(R.id.dot1);
        dots[1] = findViewById(R.id.dot2);
        dots[2] = findViewById(R.id.dot3);
        dots[3] = findViewById(R.id.dot4);

        prefs = getSharedPreferences("EndeKisePrefs", MODE_PRIVATE);
        savedPin = prefs.getString("user_pin", "");

        if (savedPin.isEmpty()) {
            isSettingUpPin = true;
            textInstruction.setText("Create a 4-Digit PIN");

            // Show the Custom Hint Setup UI!
            layoutHintSetup.setVisibility(View.VISIBLE);
        } else {
            layoutHintSetup.setVisibility(View.GONE);
        }

        ViewGroup keypad = findViewById(R.id.keypadGrid);
        for (int i = 0; i < keypad.getChildCount(); i++) {
            View child = keypad.getChildAt(i);
            if (child instanceof TextView && child.getTag() != null) {
                String number = child.getTag().toString();
                child.setOnClickListener(v -> onNumberPressed(number));
            }
        }
        findViewById(R.id.btnDeletePin).setOnClickListener(v -> onDeletePressed());
    }

    private void onNumberPressed(String number) {
        if (currentPin.length() < 4) {
            currentPin += number;
            updateDots();
            if (currentPin.length() == 4) {
                processCompletePin();
            }
        }
    }

    private void onDeletePressed() {
        if (currentPin.length() > 0) {
            currentPin = currentPin.substring(0, currentPin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < 4; i++) {
            if (i < currentPin.length()) dots[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            else dots[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#33FFFFFF")));
        }
    }

    private void processCompletePin() {
        if (isSettingUpPin) {
            // Save the PIN and their custom hint phrase
            String userHint = editCustomHint.getText().toString().trim();

            // Allow them to skip the hint if they want to
            if (userHint.isEmpty()) {
                userHint = "No hint provided.";
            }

            prefs.edit()
                    .putString("user_pin", currentPin)
                    .putString("user_pin_hint", userHint)
                    .apply();

            Toast.makeText(this, "PIN Saved Successfully!", Toast.LENGTH_SHORT).show();
            unlockApp();
        } else {
            // Verifying the PIN
            if (currentPin.equals(savedPin)) {
                unlockApp();
            } else {
                failedAttempts++;
                textInstruction.setText("Incorrect PIN.");

                // Shake Animation for wrong PIN
                textInstruction.animate().translationX(20).setDuration(50).withEndAction(() ->
                        textInstruction.animate().translationX(-20).setDuration(50).withEndAction(() ->
                                textInstruction.animate().translationX(0).setDuration(50).start()
                        ).start()
                ).start();

                currentPin = "";
                updateDots();

                // --- SHOW CUSTOM HINT AFTER 4 FAILS ---
                if (failedAttempts >= 4) {
                    showCustomHintDialog();
                }
            }
        }
    }

    private void showCustomHintDialog() {
        // Fetch the exact phrase they typed when they created the PIN
        String savedHint = prefs.getString("user_pin_hint", "No hint saved");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Forgot your PIN?")
                .setMessage("Your Hint:\n\n\"" + savedHint + "\"")
                .setPositiveButton("Try Again", null)
                .show();

        failedAttempts = 0; // Reset counter so it doesn't spam them every single time
    }

    private void unlockApp() {
        // Send flag to MainActivity to prevent the infinite loop
        Intent intent = new Intent(PinLockActivity.this, MainActivity.class);
        intent.putExtra("is_just_unlocked", true);
        startActivity(intent);
        finish();
    }
}