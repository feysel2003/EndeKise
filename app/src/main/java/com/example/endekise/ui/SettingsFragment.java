package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.endekise.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- DARK MODE TOGGLE ---
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(), "Dark Mode ON (coming soon)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Dark Mode OFF (coming soon)", Toast.LENGTH_SHORT).show();
            }
        });

        // --- BIOMETRIC TOGGLE ---
        SwitchMaterial switchBiometric = view.findViewById(R.id.switchBiometric);
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(), "App Lock enabled! (Logic coming soon)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "App Lock disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // --- BACKUP BUTTON ---
        View btnBackup = view.findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Preparing SQLite Backup...", Toast.LENGTH_SHORT).show();
            // We will call the BackupManager utility here next!
        });

        // --- CURRENCY BUTTON ---
        View btnCurrency = view.findViewById(R.id.btnCurrency);
        btnCurrency.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Currency selector opening...", Toast.LENGTH_SHORT).show();
        });
    }
}