package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.adapter.WalletAdapter;
import com.example.endekise.entity.Account;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class WalletsFragment extends Fragment {

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // 1. SETUP GRID RECYCLER VIEW
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewWalletsGrid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
        recyclerView.setHasFixedSize(true);

        WalletAdapter adapter = new WalletAdapter();
        recyclerView.setAdapter(adapter);

        // --- HANDLE LONG PRESS TO DELETE WALLET ---
        adapter.setOnWalletLongClickListener(account -> {
            // We use MaterialAlertDialogBuilder for a premium native look
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Wallet?")
                    .setMessage("Are you sure you want to delete '" + account.getName() + "'?\n\nWarning: This will remove the wallet, but its past transactions will remain in the ledger.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteAccount(account);
                        Toast.makeText(getContext(), account.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        // 2. OBSERVE WALLETS AND CALCULATE TOTAL BALANCE
        TextView textTotalWalletBalance = view.findViewById(R.id.textTotalWalletBalance);

        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            adapter.setWallets(accounts); // Update Grid

            // Calculate Total Available Balance across ALL wallets
            double totalWealth = 0;
            if (accounts != null) {
                for (Account acc : accounts) {
                    totalWealth += acc.getCurrentBalance();
                }
            }
            textTotalWalletBalance.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalWealth));
        });

        // 3. FULLY FUNCTIONAL "+ ADD" BUTTON LOGIC
        MaterialCardView btnAddWallet = view.findViewById(R.id.btnAddWallet);
        btnAddWallet.setOnClickListener(v -> showAddWalletDialog());
    }

    private void showAddWalletDialog() {
        // Create a beautiful Bottom Sheet Dialog
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        dialog.setContentView(dialogView);

        TextInputEditText editName = dialogView.findViewById(R.id.editWalletName);
        TextInputEditText editBalance = dialogView.findViewById(R.id.editInitialBalance);
        MaterialAutoCompleteTextView dropdownType = dialogView.findViewById(R.id.dropdownWalletType);
        View btnSave = dialogView.findViewById(R.id.btnSaveNewWallet);

        // Setup Account Types Dropdown
        String[] types = new String[]{"Bank", "Mobile Money", "Cash", "Credit Card", "Savings"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        dropdownType.setAdapter(typeAdapter);
        dropdownType.setText(types[0], false); // Default to Bank

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String balanceStr = editBalance.getText().toString().trim();
            String type = dropdownType.getText().toString().trim();

            if (name.isEmpty()) {
                editName.setError("Name is required");
                return;
            }

            double startingBalance = balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);

            // Create the new Wallet!
            Account newAccount = new Account(name, type, startingBalance, "ETB");
            viewModel.insertAccount(newAccount);

            Toast.makeText(getContext(), name + " Account Created!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}