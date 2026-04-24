package com.example.endekise;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.endekise.ui.AddTransactionBottomSheet;
import com.example.endekise.ui.BudgetFragment;
import com.example.endekise.ui.HomeFragment;
import com.example.endekise.ui.SettingsFragment;
import com.example.endekise.ui.WalletsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. ENFORCE APP SECURITY (PIN LOCK) ---
        android.content.SharedPreferences prefs = getSharedPreferences("EndeKisePrefs", android.content.Context.MODE_PRIVATE);
        boolean isAppLocked = prefs.getBoolean("is_app_locked", false);
        String savedPin = prefs.getString("user_pin", "");

        // This checks if the user JUST unlocked the app in this session
        boolean isJustUnlocked = getIntent().getBooleanExtra("is_just_unlocked", false);

        // If the app is locked, they have a PIN, and they haven't unlocked it yet... Kick them out!
        if (isAppLocked && !savedPin.isEmpty() && !isJustUnlocked) {
            startActivity(new android.content.Intent(this, PinLockActivity.class));
            finish();
            return;
        }
        // 1. The Orange FAB Button
        FloatingActionButton fabAddExpense = findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(view -> {
            AddTransactionBottomSheet bottomSheet = new AddTransactionBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddTransactionBottomSheet");
        });


        // --- 2. THE BOTTOM NAVIGATION BAR (Smart Routing) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.getMenu().getItem(2).setEnabled(false); // Middle placeholder

        // Create the fragments ONCE!
        final Fragment homeFragment = new HomeFragment();
        final Fragment walletsFragment = new WalletsFragment();
        final Fragment budgetFragment = new BudgetFragment();
        final Fragment settingsFragment = new SettingsFragment();
        final Fragment[] activeFragment = {homeFragment};

        // Add all of them to the container immediately, but hide the ones we aren't looking at!
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "4").hide(settingsFragment)
                .add(R.id.fragment_container, budgetFragment, "3").hide(budgetFragment)
                .add(R.id.fragment_container, walletsFragment, "2").hide(walletsFragment)
                .add(R.id.fragment_container, homeFragment, "1").commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) selectedFragment = homeFragment;
            else if (itemId == R.id.nav_wallets) selectedFragment = walletsFragment;
            else if (itemId == R.id.nav_budget) selectedFragment = budgetFragment;
            else if (itemId == R.id.nav_settings) selectedFragment = settingsFragment;

            if (selectedFragment != null && selectedFragment != activeFragment[0]) {
                // Hide the old one, SHOW the new one! No destruction!
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(activeFragment[0])
                        .show(selectedFragment)
                        .commit();
                activeFragment[0] = selectedFragment;
                return true;
            }
            return false;
        });

        // 3. Set the Default Screen (Load Home Fragment when app opens)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}