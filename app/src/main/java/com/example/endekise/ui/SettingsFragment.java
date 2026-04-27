package com.example.endekise.ui;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.PinLockActivity;
import com.example.endekise.R;
import com.example.endekise.entity.Transaction;
import com.example.endekise.utils.ImageUtils;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private DashboardViewModel viewModel;
    private SharedPreferences prefs;
    private List<Transaction> allTransactions = new ArrayList<>();
    private TextView textCloudStatus;

    // --- REAL FIREBASE VARS ---
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final long SEVEN_DAYS_IN_MS = 7L * 24 * 60 * 60 * 1000;

    // --- REAL GOOGLE LOGIN LAUNCHER ---
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(getContext(), "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

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

        textCloudStatus = view.findViewById(R.id.textCloudStatus);

        // --- INIT REAL FIREBASE ---
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // --- FREE TRIAL INITIALIZATION ---
        long installDate = prefs.getLong("install_date", 0);
        if (installDate == 0) {
            prefs.edit().putLong("install_date", System.currentTimeMillis()).apply();
        }

        // --- 1. PROFILE SETUP ---
        TextView textSettingsName = view.findViewById(R.id.textSettingsName);
        ImageView imgSettingsProfile = view.findViewById(R.id.imgSettingsProfile);

        textSettingsName.setText(prefs.getString("user_name", "Guest User"));
        ImageUtils.loadProfileImage(requireContext(), imgSettingsProfile);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            new ProfileBottomSheet().show(getParentFragmentManager(), "ProfileBottomSheet");
        });

        // --- 2. SECURITY SETUP ---
        SwitchMaterial switchPinLock = view.findViewById(R.id.switchPinLock);
        boolean isLocked = prefs.getBoolean("is_app_locked", false);
        switchPinLock.setChecked(isLocked);

        switchPinLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("is_app_locked", isChecked).apply();
            if (isChecked) {
                if (prefs.getString("user_pin", "").isEmpty()) {
                    Toast.makeText(getContext(), "Please create a PIN first!", Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(getActivity(), PinLockActivity.class));
                        requireActivity().finish();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "PinLockActivity not found yet!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "App Lock Enabled! PIN required on next launch.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "App Lock Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 3. DATA & BACKUP ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
        });

        view.findViewById(R.id.btnExportCSV).setOnClickListener(v -> exportDataToCSV());

        // THE REAL CLOUD SYNC LOGIC
        updateCloudStatusText();
        view.findViewById(R.id.btnCloudSync).setOnClickListener(v -> handleCloudSyncClick());

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
                        try {
                            startActivity(new Intent(getActivity(), PinLockActivity.class));
                            requireActivity().finish();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // --- REAL FIREBASE CLOUD SYNC METHODS ---
    private void updateCloudStatusText() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            textCloudStatus.setText("Synced to " + currentUser.getEmail() + " ✅");
            textCloudStatus.setTextColor(android.graphics.Color.parseColor("#388E3C")); // Green
        } else {
            textCloudStatus.setText("Sync to Firebase (Requires Sign-In)");
            textCloudStatus.setTextColor(android.graphics.Color.parseColor("#9E9E9E")); // Gray
        }
    }

    private void handleCloudSyncClick() {
        boolean isProPurchased = prefs.getBoolean("is_pro_purchased", false);
        boolean isTrialActive = (System.currentTimeMillis() - prefs.getLong("install_date", System.currentTimeMillis())) < SEVEN_DAYS_IN_MS;
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (isProPurchased || isTrialActive) {
            if (currentUser != null) {
                String[] syncOptions = {"Upload Local Data to Cloud ⬆️", "Download Cloud Data to Phone ⬇️", "Sign Out ❌"};

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Cloud Sync Options")
                        .setItems(syncOptions, (dialog, which) -> {
                            if (which == 0) {
                                syncDataToFirestore(); // Upload
                            } else if (which == 1) {
                                downloadDataFromFirestore(); // Download
                            } else if (which == 2) {
                                // Real Logout
                                mAuth.signOut();
                                mGoogleSignInClient.signOut();
                                updateCloudStatusText();
                                Toast.makeText(getContext(), "Signed out of Firebase", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Enable Cloud Sync")
                        .setMessage(isTrialActive ? "Your 7-Day Free Trial is active! Sign in with Google to enable Cloud Sync." : "Sign in with Google to enable Firebase Cloud Sync.")
                        .setPositiveButton("Sign In with Google", (dialog, which) -> {
                            // LAUNCH REAL GOOGLE LOGIN!
                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            googleSignInLauncher.launch(signInIntent);
                        }).setNegativeButton("Cancel", null).show();
            }
        } else {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("EndeKise PRO Required")
                    .setMessage("Your 7-day free trial has ended. Please upgrade to EndeKise PRO to keep syncing your data to the cloud.")
                    .setPositiveButton("Upgrade to PRO", (dialog, which) -> {
                        prefs.edit().putBoolean("is_pro_purchased", true).apply();
                        Toast.makeText(getContext(), "PRO Unlocked! You can now sign in.", Toast.LENGTH_SHORT).show();
                    }).setNegativeButton("Cancel", null).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(getContext(), "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                updateCloudStatusText();

                // Update Profile Name automatically
                prefs.edit().putString("user_name", user.getDisplayName()).apply();
                TextView textSettingsName = getView().findViewById(R.id.textSettingsName);
                if (textSettingsName != null) textSettingsName.setText(user.getDisplayName());

            } else {
                Toast.makeText(getContext(), "Firebase Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- REAL FIRESTORE CLOUD DOWNLOAD ---
    private void downloadDataFromFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Downloading from Cloud... ☁️", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Go to this specific user's cloud folder
        db.collection("users").document(user.getUid()).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No cloud data found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int restoredCount = 0;
                    // Loop through the cloud data and save it to our local SQLite Database!
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        double amount = doc.getDouble("amount");
                        long dateTimestamp = doc.getLong("dateTimestamp");
                        String note = doc.getString("note");
                        String category = doc.getString("category");

                        // Create the transaction and save it locally!
                        // Note: We use accountId 1 (default) since we didn't sync wallets yet.
                        Transaction restoredTx = new Transaction(1, amount, dateTimestamp, note, category, 0.0, 0.0, true);
                        viewModel.insertTransaction(restoredTx);
                        restoredCount++;
                    }

                    Toast.makeText(getContext(), "Restored " + restoredCount + " transactions! ✅", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Download Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- REAL FIRESTORE CLOUD SYNC ---
    private void syncDataToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || allTransactions == null || allTransactions.isEmpty()) {
            Toast.makeText(getContext(), "No data to sync or not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Syncing to Cloud... Please wait.", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // We use a WriteBatch to send all transactions at the exact same time!
        WriteBatch batch = db.batch();

        for (Transaction t : allTransactions) {
            Map<String, Object> cloudTransaction = new HashMap<>();
            cloudTransaction.put("amount", t.getAmount());
            cloudTransaction.put("dateTimestamp", t.getDateTimestamp());
            cloudTransaction.put("note", t.getNote() != null ? t.getNote() : "");
            cloudTransaction.put("category", t.getPayee() != null ? t.getPayee() : "General");

            // Save it under the User's specific Google ID!
            DocumentReference docRef = db.collection("users")
                    .document(user.getUid())
                    .collection("transactions")
                    .document(String.valueOf(t.getDateTimestamp())); // Use timestamp as unique ID

            batch.set(docRef, cloudTransaction);
        }

        // Execute the upload
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Cloud Sync Successful! ☁️✅", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Sync Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // --- CSV EXPORT ---
    private void exportDataToCSV() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            Toast.makeText(getContext(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvData = new StringBuilder();
        csvData.append("Date,Category,Type,Amount,Note\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (Transaction t : allTransactions) {
            String date = sdf.format(new Date(t.getDateTimestamp()));
            String category = t.getPayee() != null ? t.getPayee() : "General";
            String type = t.getAmount() > 0 ? "Income" : "Expense";
            String note = t.getNote() != null ? t.getNote().replace(",", " ") : "";

            csvData.append(date).append(",")
                    .append(category).append(",")
                    .append(type).append(",")
                    .append(Math.abs(t.getAmount())).append(",")
                    .append(note).append("\n");
        }

        try {
            String fileName = "EndeKise_Export_" + System.currentTimeMillis() + ".csv";
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);

            Uri uri = requireContext().getContentResolver().insert(android.provider.MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                java.io.OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(csvData.toString().getBytes());
                    outputStream.close();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Export Successful ✅")
                            .setMessage("Your transaction history has been saved as a .csv file in your phone's Downloads folder!\n\nFile: " + fileName)
                            .setPositiveButton("Awesome", null)
                            .show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Export Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- CONTACT US LOGIC ---
    private void showContactDialog() {
        String[] options = {"Telegram (@feyami2)", "Email (miftafeysel1@gmail.com)", "Call (+251962080847)"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Contact Us")
                .setItems(options, (dialog, which) -> {
                    Intent intent = null;
                    try {
                        if (which == 0) intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=feyami2"));
                        else if (which == 1) {
                            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:miftafeysel1@gmail.com"));
                            intent.putExtra(Intent.EXTRA_SUBJECT, "EndeKise App Support");
                        } else if (which == 2) intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+251962080847"));

                        if (intent != null) startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "App not found to perform this action.", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
}