package com.example.endekise.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.R;
import com.example.endekise.utils.ImageUtils;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileBottomSheet extends BottomSheetDialogFragment {

    private DashboardViewModel viewModel;
    private TextView textUserName, textTotalLogs, textActiveWallets;
    private ImageView imgUserAvatar;
    private SharedPreferences prefs;

    // --- THE MODERN IMAGE PICKER ---
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // 1. Save it safely to internal storage! (Prevents SecurityException crashes)
                    String safeUri = ImageUtils.saveImageToInternalStorage(requireContext(), uri);

                    if (safeUri != null) {
                        // 2. Save the permanent URI to SharedPreferences
                        prefs.edit().putString("user_avatar_uri", safeUri).apply();

                        // 3. Update UI immediately
                        imgUserAvatar.setImageURI(Uri.parse(safeUri));
                        imgUserAvatar.setImageTintList(null);

                        Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- FIND UI ELEMENTS ---
        textUserName = view.findViewById(R.id.textUserName);
        textTotalLogs = view.findViewById(R.id.textTotalLogs);
        textActiveWallets = view.findViewById(R.id.textActiveWallets);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);

        prefs = requireActivity().getSharedPreferences("EndeKisePrefs", Context.MODE_PRIVATE);

        // --- 1. LOAD SAVED USER DATA ---
        String savedName = prefs.getString("user_name", "Guest User");
        textUserName.setText(savedName);

        // Safely load the avatar using our utility class (Replaces the messy try/catch!)
        ImageUtils.loadProfileImage(requireContext(), imgUserAvatar);

        // --- 2. CLICK TO CHANGE AVATAR ---
        view.findViewById(R.id.cardAvatarContainer).setOnClickListener(v -> {
            // Launch the photo gallery! Mime type "image/*" means any photo.
            pickImageLauncher.launch("image/*");
        });

        // --- 3. LOAD DYNAMIC STATS ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) textTotalLogs.setText(String.valueOf(transactions.size()));
        });

        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) textActiveWallets.setText(String.valueOf(accounts.size()));
        });

        // --- 4. EDIT PROFILE NAME ---
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setText(prefs.getString("user_name", "Guest User"));
            input.setSelectAllOnFocus(true);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Edit Profile")
                    .setMessage("What should we call you?")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            prefs.edit().putString("user_name", newName).apply();
                            textUserName.setText(newName);
                            Toast.makeText(getContext(), "Name updated!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // --- 5. RESET DATA BUTTON ---
        view.findViewById(R.id.btnResetData).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("⚠️ Warning")
                    .setMessage("This will wipe your entire database. Are you sure?")
                    .setPositiveButton("WIPE EVERYTHING", (dialog, which) -> {
                        Toast.makeText(getContext(), "Database Reset Logic coming soon...", Toast.LENGTH_LONG).show();
                        dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}