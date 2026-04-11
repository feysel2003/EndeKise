package com.example.endekise.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.adapter.TransactionAdapter;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionDetailFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TransactionAdapter adapter;
    private boolean isIncomeMode;
    private int selectedYear, selectedMonth;

    public TransactionDetailFragment(boolean isIncomeMode, int selectedYear, int selectedMonth) {
        this.isIncomeMode = isIncomeMode;
        this.selectedYear = selectedYear;
        this.selectedMonth = selectedMonth;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- 1. DYNAMIC GRADIENTS & THEMES ---
        View bgGradient = view.findViewById(R.id.bgHeroGradient);
        TextView textToolbarTitle = view.findViewById(R.id.textToolbarTitle);
        TextView textMonthLabel = view.findViewById(R.id.textMonthLabel);

        // Insight Card Elements
        TextView textInsightTitle = view.findViewById(R.id.textInsightTitle);
        TextView textInsightPercentage = view.findViewById(R.id.textInsightPercentage);
        MaterialCardView iconBg = view.findViewById(R.id.iconBg);
        LinearProgressIndicator insightProgressBar = view.findViewById(R.id.insightProgressBar);

        Calendar cal = Calendar.getInstance(); cal.set(Calendar.YEAR, selectedYear); cal.set(Calendar.MONTH, selectedMonth);
        String monthString = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.getTime()).toUpperCase();
        textMonthLabel.setText(monthString);

        if (isIncomeMode) {
            textToolbarTitle.setText("Income Details");
            textInsightTitle.setText("Top Income Source");

            // Create a stunning Green/Teal Gradient
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF00BFA5, 0xFF00C853});
            bgGradient.setBackground(gd);

            // Color the insight card Green
            textInsightPercentage.setTextColor(android.graphics.Color.parseColor("#00C853"));
            iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9")); // Light green bg
            insightProgressBar.setIndicatorColor(android.graphics.Color.parseColor("#00C853"));
        } else {
            textToolbarTitle.setText("Expense Details");
            textInsightTitle.setText("Top Spending Category");

            // Create a stunning Orange/Red Gradient
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFFFF6D00, 0xFFE65100});
            bgGradient.setBackground(gd);

            // Color the insight card Orange
            textInsightPercentage.setTextColor(android.graphics.Color.parseColor("#FF6D00"));
            iconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E0")); // Light orange bg
            insightProgressBar.setIndicatorColor(android.graphics.Color.parseColor("#FF6D00"));
        }

        // --- 2. RECYCLER VIEW ---
        RecyclerView recyclerView = view.findViewById(R.id.recyclerDetailTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), adapter::setAccounts);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> processTransactions(view, transactions, insightProgressBar));
    }

    private void processTransactions(View view, List<Transaction> allTransactions, LinearProgressIndicator insightProgressBar) {
        if (allTransactions == null) return;

        List<Transaction> filteredList = new ArrayList<>();
        double totalAmount = 0;
        Map<String, Double> categoryTotals = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (Transaction t : allTransactions) {
            cal.setTimeInMillis(t.getDateTimestamp());
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth) {
                boolean isTransactionIncome = t.getAmount() > 0;
                if (isTransactionIncome == isIncomeMode) {
                    filteredList.add(t);
                    double amount = Math.abs(t.getAmount());
                    totalAmount += amount;

                    String category = t.getPayee() != null ? t.getPayee() : "General";
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                }
            }
        }

        TextView textTotalAmount = view.findViewById(R.id.textTotalAmount);
        textTotalAmount.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalAmount));
        adapter.setTransactions(filteredList);

        // --- 3. CALCULATE "TOP CATEGORY" INSIGHT ---
        MaterialCardView cardInsight = view.findViewById(R.id.cardInsight);

        if (totalAmount == 0 || categoryTotals.isEmpty()) {
            cardInsight.setVisibility(View.GONE);
        } else {
            cardInsight.setVisibility(View.VISIBLE);

            String topCategory = "";
            double maxAmount = -1;
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxAmount = entry.getValue();
                    topCategory = entry.getKey();
                }
            }

            double percent = (maxAmount / totalAmount) * 100;

            TextView textInsightCategory = view.findViewById(R.id.textInsightCategory);
            TextView textInsightPercentage = view.findViewById(R.id.textInsightPercentage);

            textInsightCategory.setText(topCategory);
            textInsightPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percent));

            // Animate the mini progress bar!
            insightProgressBar.setProgressCompat((int) percent, true);
        }
    }
}