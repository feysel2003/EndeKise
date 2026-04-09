package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.endekise.R;
import com.example.endekise.entity.CategorySum;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TotalExpenseFragment extends Fragment {

    private DashboardViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();

    // Monthly State (No longer filtering by Day!)
    private Calendar selectedMonth = Calendar.getInstance();

    // UI Elements
    private TextView textMonthYear, textSpendSentence, textSpendMonth, textExpensePercent, textRemainingPercent;
    private MaterialCardView cardExpenseRatio;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_total_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- 1. BACK BUTTON ---
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- 2. FIND UI ELEMENTS ---
        textMonthYear = view.findViewById(R.id.textMonthYear);
        textSpendSentence = view.findViewById(R.id.textSpendSentence);
        textSpendMonth = view.findViewById(R.id.textSpendMonth);
        textExpensePercent = view.findViewById(R.id.textExpensePercent);
        textRemainingPercent = view.findViewById(R.id.textRemainingPercent);
        cardExpenseRatio = view.findViewById(R.id.cardExpenseRatio);
        pieChart = view.findViewById(R.id.advancedPieChart);

        // --- 3. SETUP MONTHLY SELECTOR ---
        updateMonthText();

        // 1. Find the actual Arrow Buttons from the XML
        View btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        View btnNextMonth = view.findViewById(R.id.btnNextMonth);

        // 2. Remove the click listener from the middle text (it shouldn't do anything)
        textMonthYear.setOnClickListener(null);
        textMonthYear.setClickable(false);

        // 3. Make the LEFT Arrow go back one month
        btnPrevMonth.setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, -1);
            updateMonthText();
            calculateAndDisplayData(); // Redraw chart for the previous month!
        });

        // 4. Make the RIGHT Arrow go forward one month
        btnNextMonth.setOnClickListener(v -> {
            selectedMonth.add(Calendar.MONTH, 1);
            updateMonthText();
            calculateAndDisplayData(); // Redraw chart for the next month!
        });

        // Hide the daily RecyclerView because this screen is for MONTHLY analytics!
        View recyclerCal = view.findViewById(R.id.recyclerViewCalendar);
        if (recyclerCal != null) recyclerCal.setVisibility(View.GONE);
        // --- 4. ADVANCED PIE CHART CONFIGURATION ---
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false); // Solid pie
        pieChart.getLegend().setEnabled(false); // Hide default legend
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(30f, 20f, 30f, 20f); // Add extra padding so outside text isn't cut off!

        // --- 5. OBSERVE DATA ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            calculateAndDisplayData();
        });
    }

    private void updateMonthText() {
        String formattedMonth = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.getTime());
        textMonthYear.setText(formattedMonth);
        textSpendMonth.setText(formattedMonth);
    }

    private void calculateAndDisplayData() {
        if (allTransactions == null) return;

        double totalExpense = 0;

        List<CategorySum> filteredSums = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();

        for (Transaction t : allTransactions) {
            if (t.getAmount() >= 0) continue; // Only process Expenses

            Calendar txCal = Calendar.getInstance();
            txCal.setTimeInMillis(t.getDateTimestamp());

            // --- MONTHLY FILTERING MAGIC ---
            // Check if the transaction happened in the currently selected Month & Year
            if (txCal.get(Calendar.MONTH) != selectedMonth.get(Calendar.MONTH) ||
                    txCal.get(Calendar.YEAR) != selectedMonth.get(Calendar.YEAR)) {
                continue;
            }

            double expenseAmount = Math.abs(t.getAmount());
            totalExpense += expenseAmount;

            String catName = t.getPayee() != null ? t.getPayee() : "General";
            int index = categoryNames.indexOf(catName);
            if (index == -1) {
                categoryNames.add(catName);
                CategorySum newSum = new CategorySum();
                newSum.categoryName = catName;
                newSum.totalAmount = expenseAmount;
                filteredSums.add(newSum);
            } else {
                filteredSums.get(index).totalAmount += expenseAmount;
            }
        }

        // 1. Update Summary Text
        String formattedSpend = String.format(Locale.getDefault(), "ETB %,.0f", totalExpense);
        textSpendSentence.setText("You have Spent\n" + formattedSpend + " this month.");

        // 2. Update the Purple Ratio Bar
        double budgetLimit = 10000.0; // Hardcoded monthly budget for demo
        double percent = (totalExpense / budgetLimit) * 100;
        if (percent > 100) percent = 100;
        if (percent == 0) percent = 1; // Keep bar visible

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardExpenseRatio.getLayoutParams();
        params.weight = (float) percent;
        cardExpenseRatio.setLayoutParams(params);

        textExpensePercent.setText(String.format(Locale.getDefault(), "%.1f%%", percent));
        textRemainingPercent.setText(String.format(Locale.getDefault(), "%.1f%%", 100 - percent));

        // 3. Update Pie Chart
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySum sum : filteredSums) {
            entries.add(new PieEntry((float) sum.totalAmount, sum.categoryName));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // --- DYNAMIC MULTI-COLOR GENERATOR ---
        // This will automatically generate a beautiful, unique color for ANY number of categories!
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : com.github.mikephil.charting.utils.ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(android.graphics.Color.parseColor("#2A2D43")); // Deep Blue from Dribbble
        dataSet.setColors(colors);

        dataSet.setSliceSpace(2f); // Tiny gap between slices looks cleaner

        // --- THE SPIDER WEB LINES (Fixed Text Color!) ---
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.6f);

        // Make the lines Black so they are visible on a white background
        dataSet.setValueLineColor(android.graphics.Color.parseColor("#424242"));

        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE); // Number inside
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Text outside

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));
        data.setValueTextSize(14f);

        // IMPORTANT: Change the text inside the slice to White, but the label text outside to Dark Gray!
        data.setValueTextColor(android.graphics.Color.WHITE);
        pieChart.setEntryLabelColor(android.graphics.Color.parseColor("#424242")); // Dark Gray outside text
        pieChart.setEntryLabelTextSize(12f);

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }
}