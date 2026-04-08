package com.example.endekise.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.R;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ViewModel from the main activity so we can save to the database
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        TextInputEditText editTextAmount = view.findViewById(R.id.editTextAmount);
        TextInputEditText editTextNote = view.findViewById(R.id.editTextNote);
        Button buttonSave = view.findViewById(R.id.buttonSaveTransaction);

        // --- FRICTIONLESS ENTRY: Auto-focus and pop up keyboard ---
        editTextAmount.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        buttonSave.setOnClickListener(v -> {
            String amountStr = editTextAmount.getText().toString().trim();
            String note = editTextNote.getText().toString().trim();

            if (amountStr.isEmpty()) {
                editTextAmount.setError("Please enter an amount");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long currentTime = System.currentTimeMillis();

            // Create a new Transaction!
            // (Note: accountId is hardcoded to 1 for now. We will add a wallet selector later)
            Transaction newTransaction = new Transaction(1, amount, currentTime, note, "General", 0.0, 0.0, true);

            // Save it to the SQLite database via our ViewModel
            viewModel.insertTransaction(newTransaction);

            Toast.makeText(getContext(), "Expense saved!", Toast.LENGTH_SHORT).show();
            dismiss(); // Close the bottom sheet
        });
    }
}