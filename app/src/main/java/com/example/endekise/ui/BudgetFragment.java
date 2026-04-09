package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.R;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private DashboardViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();

    // UI Elements
    private TextView textIncome, textExpense, textSpend, textPercentage, textLimit;
    private LinearProgressIndicator progressBar;

    // Budget Configuration (Specifically for Food & Drinks)
    private final String TARGET_CATEGORY = "Food & Drinks";
    private final double CATEGORY_BUDGET_LIMIT = 5000.0;

    private boolean isFilteredByDay = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- 1. SETUP DYNAMIC REAL-TIME CALENDAR ---
        TextView textMonthYear = view.findViewById(R.id.textMonthYear);
        TextView textActiveDayName = view.findViewById(R.id.textActiveDayName);
        TextView textActiveDayNumber = view.findViewById(R.id.textActiveDayNumber);

        Calendar today = Calendar.getInstance();

        // Format: "April 2024"
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        textMonthYear.setText(monthYearFormat.format(today.getTime()));

        // Format: "M", "T", "W"
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("E", Locale.getDefault());
        textActiveDayName.setText(dayNameFormat.format(today.getTime()).substring(0, 1));

        // Format: "24"
        textActiveDayNumber.setText(String.valueOf(today.get(Calendar.DAY_OF_MONTH)));

        // --- 2. FIND BUDGET UI ELEMENTS ---
        textIncome = view.findViewById(R.id.textBudgetIncome);
        textExpense = view.findViewById(R.id.textBudgetExpense);
        textSpend = view.findViewById(R.id.textSpend);
        textPercentage = view.findViewById(R.id.textPercentage);
        textLimit = view.findViewById(R.id.textLimit);
        progressBar = view.findViewById(R.id.budgetProgressBar);

        progressBar.setMax((int) CATEGORY_BUDGET_LIMIT);
        textLimit.setText(String.format(Locale.getDefault(), "ETB %,.0f", CATEGORY_BUDGET_LIMIT));

        // --- 3. FILTER BY DAY CLICK LISTENER ---
        MaterialCardView btnActiveDay = view.findViewById(R.id.btnActiveDay);
        btnActiveDay.setOnClickListener(v -> {
            isFilteredByDay = !isFilteredByDay;
            Toast.makeText(getContext(), isFilteredByDay ? "Filtering by Today" : "Showing All-Time", Toast.LENGTH_SHORT).show();
            calculateAndDisplayData();
        });

        // --- 4. FETCH DATA FROM DATABASE ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            calculateAndDisplayData();
        });
    }

    // --- THE CORE LOGIC: Category & Time Filtering ---
    private void calculateAndDisplayData() {
        if (allTransactions == null) return;

        double totalIncome = 0;
        double totalExpense = 0;
        double foodAndDrinksExpense = 0; // NEW: Track specific category!

        Calendar cal = Calendar.getInstance();
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        for (Transaction t : allTransactions) {
            cal.setTimeInMillis(t.getDateTimestamp());
            int transactionDay = cal.get(Calendar.DAY_OF_YEAR);

            // Filter out transactions that aren't from today (if filter is ON)
            if (isFilteredByDay && transactionDay != currentDayOfYear) {
                continue;
            }

            if (t.getAmount() > 0) {
                totalIncome += t.getAmount();
            } else {
                double absoluteExpense = Math.abs(t.getAmount());
                totalExpense += absoluteExpense;

                // --- CATEGORY FILTERING MAGIC ---
                // Remember we temporarily saved the category name in the 'payee' column!
                if (t.getPayee() != null && t.getPayee().equals(TARGET_CATEGORY)) {
                    foodAndDrinksExpense += absoluteExpense;
                }
            }
        }

        // 1. Update Global Cards
        textIncome.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalIncome));
        textExpense.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalExpense));

        // 2. Update Specific "Food & Drinks" Budget Card
        textSpend.setText(String.format(Locale.getDefault(), "ETB %,.0f", foodAndDrinksExpense));
        progressBar.setProgressCompat((int) foodAndDrinksExpense, true);

        // 3. Calculate Percentage for Food & Drinks
        double percent = (foodAndDrinksExpense / CATEGORY_BUDGET_LIMIT) * 100;
        if (percent > 100) percent = 100;

        textPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percent));

        // Dynamic Color Coding!
        if (percent >= 100) {
            textPercentage.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Red
            progressBar.setIndicatorColor(android.graphics.Color.parseColor("#D32F2F"));
        } else if (percent > 80) {
            textPercentage.setTextColor(android.graphics.Color.parseColor("#FF6D00")); // Orange
            progressBar.setIndicatorColor(android.graphics.Color.parseColor("#FF6D00"));
        } else {
            textPercentage.setTextColor(android.graphics.Color.parseColor("#7C4DFF")); // Purple
            progressBar.setIndicatorColor(android.graphics.Color.parseColor("#7C4DFF"));
        }
    }
}