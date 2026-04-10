package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.adapter.TransactionAdapter;
import com.example.endekise.entity.Account;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private DashboardViewModel viewModel;
    private BarChart barChart;
    private TextView textStatFilter, textStatDateRange, textTotalBalance, textCardIncome, textCardExpense;

    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- UI SETUP ---
        textTotalBalance = view.findViewById(R.id.textTotalBalance);
        textCardIncome = view.findViewById(R.id.textCardIncome);
        textCardExpense = view.findViewById(R.id.textCardExpense);

        view.findViewById(R.id.btnViewAll).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new AllTransactionsFragment())
                    .addToBackStack(null).commit();
        });

        // --- DROPDOWN (YEAR/MONTH FILTER) ---
        MaterialCardView btnStatFilter = view.findViewById(R.id.btnStatFilter);
        textStatFilter = view.findViewById(R.id.textStatFilter);
        textStatDateRange = view.findViewById(R.id.textStatDateRange);

        updateDateRangeText();

        btnStatFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnStatFilter);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

            popup.getMenu().add(0, 0, 0, sdf.format(cal.getTime()));
            cal.add(Calendar.MONTH, -1);
            popup.getMenu().add(0, 1, 1, sdf.format(cal.getTime()));
            cal.add(Calendar.MONTH, -1);
            popup.getMenu().add(0, 2, 2, sdf.format(cal.getTime()));

            popup.setOnMenuItemClickListener(item -> {
                Calendar clickedCal = Calendar.getInstance();
                clickedCal.add(Calendar.MONTH, -item.getItemId());
                selectedYear = clickedCal.get(Calendar.YEAR);
                selectedMonth = clickedCal.get(Calendar.MONTH);
                textStatFilter.setText(item.getTitle() + " ˅");
                updateDateRangeText();
                updateDashboardData(); // Recalculate everything!
                return true;
            });
            popup.show();
        });

        // --- TRANSACTIONS LIST ---
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Transaction deletedTransaction = adapter.getTransactionAt(viewHolder.getAdapterPosition());
                viewModel.deleteTransaction(deletedTransaction);
                Snackbar.make(recyclerView, "Transaction deleted", Snackbar.LENGTH_LONG).setAction("UNDO", v -> viewModel.insertTransaction(deletedTransaction)).setActionTextColor(android.graphics.Color.parseColor("#03DAC5")).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // --- RULE 1: TOTAL BALANCE IS SUM OF WALLETS ---
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                adapter.setAccounts(accounts);
                double trueNetWorth = 0;
                for (Account acc : accounts) {
                    trueNetWorth += acc.getCurrentBalance();
                }
                textTotalBalance.setText(String.format(Locale.getDefault(), "ETB %,.2f", trueNetWorth));
            }
        });

        //  NEW: OBSERVE CATEGORIES AND CREATE DEFAULTS ---
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && categories.isEmpty()) {
                // If the database is empty, create these default categories with dynamic budget limits!
                viewModel.insertCategory(new com.example.endekise.entity.Category(null, "Food & Drinks", "Expense", "#F44336", "ic_food", 3000.0));
                viewModel.insertCategory(new com.example.endekise.entity.Category(null, "Transport", "Expense", "#2196F3", "ic_transport", 1500.0));
                viewModel.insertCategory(new com.example.endekise.entity.Category(null, "Salary", "Income", "#4CAF50", "ic_salary", 0.0)); // Income doesn't need a limit
                viewModel.insertCategory(new com.example.endekise.entity.Category(null, "Shopping", "Expense", "#9C27B0", "ic_shopping", 2000.0));
            }
        });

        // --- INITIALIZE BAR CHART ---
        setupBarChartShell(view);

        // --- FETCH TRANSACTIONS & UPDATE DASHBOARD ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;

            // Limit list to 3 items
            if (transactions != null && transactions.size() > 3) {
                adapter.setTransactions(transactions.subList(0, 3));
            } else {
                adapter.setTransactions(transactions);
            }

            updateDashboardData();
        });
    }

    private void updateDateRangeText() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, selectedYear);
        cal.set(Calendar.MONTH, selectedMonth);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
        String monthStr = sdf.format(cal.getTime());
        textStatDateRange.setText(String.format(Locale.getDefault(), "%s 01 - %s %02d", monthStr, monthStr, lastDay));
    }

    private void setupBarChartShell(View view) {
        barChart = view.findViewById(R.id.barChart);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setScaleEnabled(false);
        barChart.setExtraBottomOffset(10f);

        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        String[] weeks = new String[]{"Week 1", "Week 2", "Week 3", "Week 4", "Week 5"};
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(weeks));

        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(15f, 10f, 0f);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#E0E0E0"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                if (value == 0) return "0";
                if (value >= 1000) return String.format(Locale.getDefault(), "%.0fk", value / 1000);
                return String.valueOf((int) value);
            }
        });
        barChart.getAxisRight().setEnabled(false);
    }

    // --- RULE 2: MONTHLY INCOME/EXPENSE MATCHES THE CHART ---
    private void updateDashboardData() {
        if (allTransactions == null || allTransactions.isEmpty()) return;

        float[] weeklyIncome = new float[5];
        float[] weeklyExpense = new float[5];
        double monthlyIncomeTotal = 0;
        double monthlyExpenseTotal = 0;

        Calendar cal = Calendar.getInstance();

        for (Transaction t : allTransactions) {
            cal.setTimeInMillis(t.getDateTimestamp());

            // Only process if it matches the selected Month and Year!
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth) {

                int weekIndex = (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7;
                if (weekIndex > 4) weekIndex = 4;

                if (t.getAmount() > 0) {
                    weeklyIncome[weekIndex] += (float) t.getAmount();
                    monthlyIncomeTotal += t.getAmount();
                } else {
                    double absExpense = Math.abs(t.getAmount());
                    weeklyExpense[weekIndex] += (float) absExpense;
                    monthlyExpenseTotal += absExpense;
                }
            }
        }

        // Update the Cards!
        textCardIncome.setText(String.format(Locale.getDefault(), "ETB %,.2f", monthlyIncomeTotal));
        textCardExpense.setText(String.format(Locale.getDefault(), "ETB %,.2f", monthlyExpenseTotal));

        // Draw the Chart!
        List<com.github.mikephil.charting.data.BarEntry> entriesIncome = new ArrayList<>();
        List<com.github.mikephil.charting.data.BarEntry> entriesExpense = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            entriesIncome.add(new com.github.mikephil.charting.data.BarEntry(i, weeklyIncome[i]));
            entriesExpense.add(new com.github.mikephil.charting.data.BarEntry(i, weeklyExpense[i]));
        }

        com.github.mikephil.charting.data.BarDataSet setIncome = new com.github.mikephil.charting.data.BarDataSet(entriesIncome, "Income");
        setIncome.setColor(android.graphics.Color.parseColor("#7C4DFF"));
        setIncome.setDrawValues(false);

        com.github.mikephil.charting.data.BarDataSet setExpense = new com.github.mikephil.charting.data.BarDataSet(entriesExpense, "Expense");
        setExpense.setColor(android.graphics.Color.parseColor("#FF6D00"));
        setExpense.setDrawValues(false);

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(setIncome, setExpense);
        float groupSpace = 0.4f, barSpace = 0.05f, barWidth = 0.25f;

        barData.setBarWidth(barWidth);
        barChart.setData(barData);
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(0f + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 4);
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.invalidate();
        barChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }
}