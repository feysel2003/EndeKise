package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.R;
import com.example.endekise.entity.Account;
import com.example.endekise.entity.Category;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private DashboardViewModel viewModel;
    private List<Account> accountList = new ArrayList<>();
    private List<Category> allCategoriesList = new ArrayList<>(); // Store ALL categories from DB

    private int selectedFromAccountId = -1;
    private int selectedToAccountId = -1;
    private MaterialAutoCompleteTextView dropdownCategory; // We need this to update it dynamically

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // UI Elements
        TextInputEditText editTextAmount = view.findViewById(R.id.editTextAmount);
        TextInputEditText editTextNote = view.findViewById(R.id.editTextNote);
        Button buttonSave = view.findViewById(R.id.buttonSaveTransaction);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupType);

        // Wallet Dropdowns
        MaterialAutoCompleteTextView dropdownWallet = view.findViewById(R.id.dropdownWallet);
        TextInputLayout layoutWalletTo = view.findViewById(R.id.layoutWalletTo);
        MaterialAutoCompleteTextView dropdownWalletTo = view.findViewById(R.id.dropdownWalletTo);

        // Category Dropdowns
        dropdownCategory = view.findViewById(R.id.dropdownCategory);
        TextInputLayout layoutCategory = view.findViewById(R.id.layoutCategory);

        // --- 1. DYNAMIC UI: Show/Hide Wallets and Categories based on Toggle ---
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonTransfer) {
                    layoutWalletTo.setVisibility(View.VISIBLE);
                    layoutCategory.setVisibility(View.GONE);
                    editTextNote.setVisibility(View.GONE);
                } else {
                    layoutWalletTo.setVisibility(View.GONE);
                    layoutCategory.setVisibility(View.VISIBLE);
                    editTextNote.setVisibility(View.VISIBLE);

                    // --- SMART CATEGORY FILTERING ---
                    // If they click Income or Expense, immediately refresh the dropdown list!
                    updateCategoryDropdownBasedOnToggle(checkedId);
                }
            }
        });

        // --- 2. FETCH WALLETS ---
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            accountList = accounts;
            List<String> accountNames = new ArrayList<>();
            for (Account acc : accounts) {
                // Show the balance in the dropdown so the user knows what they have!
                accountNames.add(acc.getName() + " (ETB " + String.format(Locale.getDefault(), "%,.0f", acc.getCurrentBalance()) + ")");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames);
            dropdownWallet.setAdapter(adapter);
            dropdownWalletTo.setAdapter(adapter);

            if (!accounts.isEmpty()) {
                dropdownWallet.setText(accountNames.get(0), false);
                selectedFromAccountId = accounts.get(0).getAccountId();
            }
        });

        dropdownWallet.setOnItemClickListener((parent, v, position, id) -> selectedFromAccountId = accountList.get(position).getAccountId());
        dropdownWalletTo.setOnItemClickListener((parent, v, position, id) -> selectedToAccountId = accountList.get(position).getAccountId());

        // --- 3. FETCH CATEGORIES (And store them!) ---
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategoriesList = categories;
            // Initially populate the dropdown based on the default selected toggle (Expense)
            updateCategoryDropdownBasedOnToggle(toggleGroup.getCheckedButtonId());
        });

        // Auto-focus Keyboard
        editTextAmount.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // --- 4. THE SMART SAVE LOGIC ---
        buttonSave.setOnClickListener(v -> {
            String amountStr = editTextAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                editTextAmount.setError("Required");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // LOGIC 1: TRANSFER
            if (toggleGroup.getCheckedButtonId() == R.id.buttonTransfer) {
                if (selectedToAccountId == -1 || selectedFromAccountId == selectedToAccountId) {
                    Toast.makeText(getContext(), "Please select a different TO wallet", Toast.LENGTH_SHORT).show();
                    return;
                }

                // OVERDRAFT PROTECTION FOR TRANSFERS
                Account fromAccount = viewModel.getAccountById(selectedFromAccountId);
                if (fromAccount != null && fromAccount.getCurrentBalance() < amount) {
                    editTextAmount.setError("Insufficient funds in " + fromAccount.getName());
                    Toast.makeText(getContext(), "Transfer failed: Wallet balance is only ETB " + fromAccount.getCurrentBalance(), Toast.LENGTH_LONG).show();
                    return;
                }

                viewModel.executeTransfer(selectedFromAccountId, selectedToAccountId, amount);
                Toast.makeText(getContext(), "Transfer complete!", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            // LOGIC 2: EXPENSE
            if (toggleGroup.getCheckedButtonId() == R.id.buttonExpense) {

                // OVERDRAFT PROTECTION FOR EXPENSES
                Account fromAccount = viewModel.getAccountById(selectedFromAccountId);
                if (fromAccount != null && fromAccount.getCurrentBalance() < amount) {
                    editTextAmount.setError("Insufficient funds");
                    Toast.makeText(getContext(), "You only have ETB " + String.format(Locale.getDefault(), "%,.2f", fromAccount.getCurrentBalance()) + " in this wallet!", Toast.LENGTH_LONG).show();
                    return; // BLOCK THE SAVE!
                }

                amount = amount * -1; // Make it negative for the database
            }

            // LOGIC 3: INCOME (No overdraft protection needed, you can always add money!)

            String note = editTextNote.getText().toString().trim();
            String selectedCategory = dropdownCategory.getText().toString();

            // Create transaction
            Transaction newTransaction = new Transaction(selectedFromAccountId, amount, System.currentTimeMillis(), note, selectedCategory, 0.0, 0.0, true);
            viewModel.insertTransaction(newTransaction);

            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    // --- HELPER METHOD: Filter Categories based on the Toggle Button! ---
    private void updateCategoryDropdownBasedOnToggle(int checkedToggleId) {
        if (allCategoriesList == null || allCategoriesList.isEmpty()) return;

        List<String> filteredNames = new ArrayList<>();
        String targetType = (checkedToggleId == R.id.buttonExpense) ? "Expense" : "Income";

        for (Category cat : allCategoriesList) {
            // Only add the category to the dropdown if its Type matches the toggle!
            if (cat.getType().equalsIgnoreCase(targetType)) {
                filteredNames.add(cat.getName());
            }
        }

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, filteredNames);
        dropdownCategory.setAdapter(catAdapter);

        // Auto-select the first valid category so the box isn't empty
        if (!filteredNames.isEmpty()) {
            dropdownCategory.setText(filteredNames.get(0), false);
        } else {
            dropdownCategory.setText("", false); // Clear it if no categories exist for that type
        }
    }
}