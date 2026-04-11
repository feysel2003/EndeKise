package com.example.endekise.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.PinLockActivity;
import com.example.endekise.R;
import com.example.endekise.entity.Transaction;
import com.example.endekise.utils.ImageUtils;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private DashboardViewModel viewModel;
    private SharedPreferences prefs;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        prefs = requireActivity().getSharedPreferences("EndeKisePrefs", Context.MODE_PRIVATE);

        // --- 1. PROFILE SETUP ---
        TextView textSettingsName = view.findViewById(R.id.textSettingsName);
        ImageView imgSettingsProfile = view.findViewById(R.id.imgSettingsProfile);

        textSettingsName.setText(prefs.getString("user_name", "Guest User"));
        ImageUtils.loadProfileImage(requireContext(), imgSettingsProfile);

        // Click profile to open the exact same BottomSheet we built earlier!
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            new ProfileBottomSheet().show(getParentFragmentManager(), "ProfileBottomSheet");
        });

        // --- 2. SECURITY & APPEARANCE ---
        SwitchMaterial switchPinLock = view.findViewById(R.id.switchPinLock);
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // Load current states
        switchPinLock.setChecked(prefs.getString("user_pin", "").length() == 4);
        switchDarkMode.setChecked(prefs.getBoolean("is_dark_mode", false));

        switchPinLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // Remove PIN
                prefs.edit().putString("user_pin", "").apply();
                Toast.makeText(getContext(), "PIN Lock Disabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Logout and restart app to set new PIN", Toast.LENGTH_LONG).show();
            }
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("is_dark_mode", isChecked).apply();
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        });

        // --- 3. DATA & BACKUP ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
        });

        view.findViewById(R.id.btnExportCSV).setOnClickListener(v -> exportDataToCSV());

        view.findViewById(R.id.btnCloudSync).setOnClickListener(v ->
                Toast.makeText(getContext(), "Firebase Sync requires EndeKise Pro!", Toast.LENGTH_SHORT).show()
        );

        // --- 4. FEEDBACK & ACCOUNT ---
        view.findViewById(R.id.btnContactUs).setOnClickListener(v -> showContactDialog());

        view.findViewById(R.id.btnPrivacy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Your data is stored 100% locally on your device in SQLite.", Toast.LENGTH_LONG).show()
        );

        // --- 5. LOGOUT BUTTON ---
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out? You will need your PIN to log back in.")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        // Redirect to the PIN Lock Screen!
                        startActivity(new Intent(getActivity(), PinLockActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // --- CSV EXPORT LOGIC ---
    private void exportDataToCSV() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            Toast.makeText(getContext(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvData = new StringBuilder();
        // CSV Header
        csvData.append("Date,Category,Type,Amount,Note\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (Transaction t : allTransactions) {
            String date = sdf.format(new Date(t.getDateTimestamp()));
            String category = t.getPayee() != null ? t.getPayee() : "General";
            String type = t.getAmount() > 0 ? "Income" : "Expense";
            String note = t.getNote() != null ? t.getNote().replace(",", " ") : ""; // Remove commas to prevent CSV breaking

            // Append row
            csvData.append(date).append(",")
                    .append(category).append(",")
                    .append(type).append(",")
                    .append(Math.abs(t.getAmount())).append(",")
                    .append(note).append("\n");
        }

        // Share the CSV text instantly to WhatsApp, Telegram, Email, etc!
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Here is my EndeKise Export Data:\n\n" + csvData.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Export Transactions via...");
        startActivity(shareIntent);
    }

    // --- CONTACT US LOGIC (Intents) ---
    private void showContactDialog() {
        String[] options = {"Telegram (@feyami2)", "Email (miftafeysel1@gmail.com)", "Call (+251962080847)"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Contact Us")
                .setItems(options, (dialog, which) -> {
                    Intent intent = null;
                    try {
                        if (which == 0) {
                            // Telegram
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=feyami2"));
                        } else if (which == 1) {
                            // Email
                            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:miftafeysel1@gmail.com"));
                            intent.putExtra(Intent.EXTRA_SUBJECT, "EndeKise App Support");
                        } else if (which == 2) {
                            // Phone Dialer
                            intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+251962080847"));
                        }

                        if (intent != null) startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "App not found to perform this action.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}