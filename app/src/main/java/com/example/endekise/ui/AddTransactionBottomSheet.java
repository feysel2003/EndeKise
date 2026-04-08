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
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private DashboardViewModel viewModel;
    private List<Account> accountList = new ArrayList<>();
    private int selectedFromAccountId = -1;
    private int selectedToAccountId = -1; // For transfers

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        TextInputEditText editTextAmount = view.findViewById(R.id.editTextAmount);
        TextInputEditText editTextNote = view.findViewById(R.id.editTextNote);
        Button buttonSave = view.findViewById(R.id.buttonSaveTransaction);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupType);

        MaterialAutoCompleteTextView dropdownWallet = view.findViewById(R.id.dropdownWallet);
        TextInputLayout layoutWalletTo = view.findViewById(R.id.layoutWalletTo);
        MaterialAutoCompleteTextView dropdownWalletTo = view.findViewById(R.id.dropdownWalletTo);

        // --- DYNAMIC UI: Show/Hide the "To" Wallet ---
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonTransfer) {
                    layoutWalletTo.setVisibility(View.VISIBLE);
                    editTextNote.setVisibility(View.GONE); // Hide notes for simple transfers
                } else {
                    layoutWalletTo.setVisibility(View.GONE);
                    editTextNote.setVisibility(View.VISIBLE);
                }
            }
        });

        // --- FETCH WALLETS ---
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            accountList = accounts;
            List<String> accountNames = new ArrayList<>();
            for (Account acc : accounts) {
                accountNames.add(acc.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames);
            dropdownWallet.setAdapter(adapter);
            dropdownWalletTo.setAdapter(adapter); // Give same list to second dropdown

            if (!accounts.isEmpty()) {
                dropdownWallet.setText(accounts.get(0).getName(), false);
                selectedFromAccountId = accounts.get(0).getAccountId();
            }
        });

        dropdownWallet.setOnItemClickListener((parent, v, position, id) -> selectedFromAccountId = accountList.get(position).getAccountId());
        dropdownWalletTo.setOnItemClickListener((parent, v, position, id) -> selectedToAccountId = accountList.get(position).getAccountId());

        // Keyboard popup
        editTextAmount.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        buttonSave.setOnClickListener(v -> {
            String amountStr = editTextAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                editTextAmount.setError("Required");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            // LOGIC 1: IS IT A TRANSFER?
            if (toggleGroup.getCheckedButtonId() == R.id.buttonTransfer) {
                if (selectedToAccountId == -1 || selectedFromAccountId == selectedToAccountId) {
                    Toast.makeText(getContext(), "Please select a different TO wallet", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Execute ACID Transfer!
                viewModel.executeTransfer(selectedFromAccountId, selectedToAccountId, amount);
                Toast.makeText(getContext(), "Transfer complete!", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            // LOGIC 2: IT IS AN INCOME OR EXPENSE
            if (toggleGroup.getCheckedButtonId() == R.id.buttonExpense) {
                amount = amount * -1;
            }

            String note = editTextNote.getText().toString().trim();
            Transaction newTransaction = new Transaction(selectedFromAccountId, amount, System.currentTimeMillis(), note, "General", 0.0, 0.0, true);
            viewModel.insertTransaction(newTransaction);

            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}