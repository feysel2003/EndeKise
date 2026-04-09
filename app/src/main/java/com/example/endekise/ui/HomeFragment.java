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
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private DashboardViewModel viewModel;
    private BarChart barChart;
    private TextView textYearFilter;

    // We store the current year and the full list of transactions to easily filter them!
    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- 1. SETUP CLICKABLE UI BUTTONS ---

        MaterialCardView btnProfile = view.findViewById(R.id.imgProfile);
        btnProfile.setOnClickListener(v -> Toast.makeText(getContext(), "Profile Settings coming soon!", Toast.LENGTH_SHORT).show());

        View btnNotifications = view.findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v -> Toast.makeText(getContext(), "You have 0 new notifications", Toast.LENGTH_SHORT).show());

        View btnViewAll = view.findViewById(R.id.btnViewAll);
        btnViewAll.setOnClickListener(v -> Toast.makeText(getContext(), "Full Ledger opening soon...", Toast.LENGTH_SHORT).show());

        // --- 2. SETUP ORANGE DROPDOWN (YEAR FILTER) ---

        MaterialCardView btnYearFilter = view.findViewById(R.id.btnYearFilter);
        textYearFilter = view.findViewById(R.id.textYearFilter);
        textYearFilter.setText("Year - " + selectedYear + " ˅");

        btnYearFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnYearFilter);
            // Add the last few years dynamically
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            popup.getMenu().add(String.valueOf(currentYear));
            popup.getMenu().add(String.valueOf(currentYear - 1));
            popup.getMenu().add(String.valueOf(currentYear - 2));

            popup.setOnMenuItemClickListener(item -> {
                selectedYear = Integer.parseInt(item.getTitle().toString());
                textYearFilter.setText("Year - " + selectedYear + " ˅");

                // Recalculate the chart immediately!
                updateBarChart();
                return true;
            });
            popup.show();
        });


        // --- 3. RECYCLER VIEW SETUP ---
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        TransactionAdapter adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        // Setup defaults & swipe to delete
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), adapter::setAccounts);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Transaction deletedTransaction = adapter.getTransactionAt(viewHolder.getAdapterPosition());
                viewModel.deleteTransaction(deletedTransaction);
                Snackbar.make(recyclerView, "Transaction deleted", Snackbar.LENGTH_LONG).setAction("UNDO", v -> viewModel.insertTransaction(deletedTransaction)).setActionTextColor(android.graphics.Color.parseColor("#03DAC5")).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);


        // --- 4. CREDIT CARD BALANCES ---
        TextView textTotalBalance = view.findViewById(R.id.textTotalBalance);
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total ->
                textTotalBalance.setText(total != null ? String.format(Locale.getDefault(), "ETB %.2f", total) : "ETB 0.00"));


        // --- 5. INITIALIZE BAR CHART SHELL ---
        barChart = view.findViewById(R.id.barChart);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setScaleEnabled(false);

        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);

        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#F5F5F5"));
        leftAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        // --- 6. OBSERVE TRANSACTIONS & FEED CHART ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions); // Update list
            allTransactions = transactions; // Save locally for our filter
            updateBarChart(); // Draw chart
        });
    }

    // --- THE MAGIC FILTER LOGIC ---
    private void updateBarChart() {
        if (allTransactions == null || allTransactions.isEmpty()) return;

        float[] monthlySums = new float[12];
        Calendar cal = Calendar.getInstance();

        // Loop through all data, but ONLY add it to the chart if it matches the selected year!
        for (Transaction t : allTransactions) {
            if (t.getAmount() < 0) {
                cal.setTimeInMillis(t.getDateTimestamp());
                int transactionYear = cal.get(Calendar.YEAR);

                if (transactionYear == selectedYear) {
                    int monthIndex = cal.get(Calendar.MONTH);
                    monthlySums[monthIndex] += (float) Math.abs(t.getAmount());
                }
            }
        }

        List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < 12; i++) {
            entries.add(new com.github.mikephil.charting.data.BarEntry(i, monthlySums[i]));

            // Only highlight Purple if we are looking at the current year AND current month
            if (i == currentMonthIndex && selectedYear == currentYear) {
                colors.add(android.graphics.Color.parseColor("#7C4DFF"));
            } else {
                colors.add(android.graphics.Color.parseColor("#E0E0E0"));
            }
        }

        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries, "Expenses");
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override public String getBarLabel(com.github.mikephil.charting.data.BarEntry barEntry) {
                return barEntry.getY() == 0 ? "" : String.format(Locale.getDefault(), "%,.0f", barEntry.getY());
            }
        });

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barData.setBarWidth(0.4f);

        barChart.setData(barData);
        barChart.invalidate();
        barChart.animateY(700, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }
}