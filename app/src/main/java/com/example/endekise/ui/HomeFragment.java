package com.example.endekise.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.endekise.entity.Category;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private DashboardViewModel viewModel;
    private BarChart barChart;
    private TextView textStatFilter, textStatDateRange, textTotalBalance, textCardIncome, textCardExpense;
    private View notificationDot, iconBell;
    private ImageView btnToggleVisibility;

    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH);
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionAdapter adapter;

    private double todayIncome = 0, todayExpense = 0;

    // Privacy Mode State
    private boolean isBalanceHidden = false;
    private double currentNetWorth = 0;
    private double currentMonthlyIncome = 0;
    private double currentMonthlyExpense = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // --- 1. LOAD CUSTOM PROFILE PICTURE ---
        View profileContainer = view.findViewById(R.id.imgProfile);
        if (profileContainer instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) profileContainer;
            if (group.getChildCount() > 0 && group.getChildAt(0) instanceof ImageView) {
                ImageView iconHomeProfile = (ImageView) group.getChildAt(0);
                com.example.endekise.utils.ImageUtils.loadProfileImage(requireContext(), iconHomeProfile);
            }
        }
        profileContainer.setOnClickListener(v -> new ProfileBottomSheet().show(getParentFragmentManager(), "ProfileBottomSheet"));

        // --- 2. FIND UI ELEMENTS ---
        textTotalBalance = view.findViewById(R.id.textTotalBalance);
        textCardIncome = view.findViewById(R.id.textCardIncome);
        textCardExpense = view.findViewById(R.id.textCardExpense);
        notificationDot = view.findViewById(R.id.notificationDot);
        iconBell = view.findViewById(R.id.iconBell);
        btnToggleVisibility = view.findViewById(R.id.btnToggleVisibility);

        // --- 3. PRIVACY MODE TOGGLE (THE EYE ICON) ---
        btnToggleVisibility.setOnClickListener(v -> {
            isBalanceHidden = !isBalanceHidden;
            updateBalanceTexts();
            btnToggleVisibility.setImageResource(isBalanceHidden ? android.R.drawable.ic_menu_search : android.R.drawable.ic_menu_view);
        });

        // --- 4. FRAGMENT ROUTING (View All & Legends) ---
        view.findViewById(R.id.btnViewAll).setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new AllTransactionsFragment())
                .addToBackStack(null).commit());

        // [RESTORED] Legend Clicks to open Details Screen!
        view.findViewById(R.id.btnLegendIncome).setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new TransactionDetailFragment(true, selectedYear, selectedMonth))
                .addToBackStack(null).commit());

        view.findViewById(R.id.btnLegendExpense).setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new TransactionDetailFragment(false, selectedYear, selectedMonth))
                .addToBackStack(null).commit());

        // --- 5. NOTIFICATION BELL ---
        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            if (todayIncome == 0 && todayExpense == 0) Toast.makeText(getContext(), "No transactions today yet!", Toast.LENGTH_SHORT).show();
            else showDailySummaryPopup(true);
        });

        // --- 6. STATS DROPDOWN ---
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
                updateDashboardData();
                return true;
            });
            popup.show();
        });

        // --- 7. RECYCLER VIEW SETUP ---
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int d) {
                Transaction deletedTx = adapter.getTransactionAt(vh.getBindingAdapterPosition());
                viewModel.deleteTransaction(deletedTx);
                Snackbar.make(recyclerView, "Transaction deleted", Snackbar.LENGTH_LONG).setAction("UNDO", v -> viewModel.insertTransaction(deletedTx)).setActionTextColor(android.graphics.Color.parseColor("#03DAC5")).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // --- 8. FETCH WALLETS (NET WORTH) ---
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                adapter.setAccounts(accounts);
                currentNetWorth = 0;
                for (Account acc : accounts) currentNetWorth += acc.getCurrentBalance();
                updateBalanceTexts();
            }
        });

        // --- 9. [RESTORED] DEFAULT CATEGORIES GENERATOR ---
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && categories.isEmpty()) {
                viewModel.insertCategory(new Category(null, "Food & Drinks", "Expense", "#F44336", "ic_food", 300.0, "Daily"));
                viewModel.insertCategory(new Category(null, "Transport", "Expense", "#2196F3", "ic_transport", 100.0, "Daily"));
                viewModel.insertCategory(new Category(null, "Rent", "Expense", "#9C27B0", "ic_home", 5000.0, "Monthly"));
                viewModel.insertCategory(new Category(null, "Salary", "Income", "#4CAF50", "ic_salary", 0.0, "Monthly"));
            }
        });

        setupBarChartShell(view);

        // --- 10. FETCH TRANSACTIONS ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            if (transactions != null && transactions.size() > 3) adapter.setTransactions(transactions.subList(0, 3));
            else adapter.setTransactions(transactions);
            updateDashboardData();
        });
    }

    // --- PRIVACY MODE LOGIC ---
    private void updateBalanceTexts() {
        if (isBalanceHidden) {
            textTotalBalance.setText("ETB ***.**");
            textCardIncome.setText("ETB ***.**");
            textCardExpense.setText("ETB ***.**");
        } else {
            textTotalBalance.setText(String.format(Locale.getDefault(), "ETB %,.2f", currentNetWorth));
            textCardIncome.setText(String.format(Locale.getDefault(), "ETB %,.2f", currentMonthlyIncome));
            textCardExpense.setText(String.format(Locale.getDefault(), "ETB %,.2f", currentMonthlyExpense));
        }
    }

    private void animateBell() {
        if (iconBell != null) {
            ObjectAnimator.ofFloat(iconBell, "rotation", 0f, 20f, -20f, 20f, -20f, 0f).setDuration(600).start();
        }
    }

    private void showDailySummaryPopup(boolean isManualClick) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("EndeKisePrefs", Context.MODE_PRIVATE);
        String todayDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastShownDate = prefs.getString("last_daily_summary", "");

        if (!todayDateStr.equals(lastShownDate) || isManualClick) {

            // --- NEW: PLAY THE SOUND ALARM! ---
            if (!isManualClick) { // Don't beep if they manually clicked the bell, only beep for the automatic popup!
                com.example.endekise.utils.SoundUtils.playNotificationAlarm(requireContext());
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Today's Summary 📅")
                    .setMessage(String.format(Locale.getDefault(), "Income: ETB %,.2f\nExpenses: ETB %,.2f", todayIncome, todayExpense))
                    .setPositiveButton("Awesome", (dialog, which) -> prefs.edit().putString("last_daily_summary", todayDateStr).apply())
                    .show();
        }
    }

    private void updateDateRangeText() {
        Calendar cal = Calendar.getInstance(); cal.set(Calendar.YEAR, selectedYear); cal.set(Calendar.MONTH, selectedMonth);
        textStatDateRange.setText(String.format(Locale.getDefault(), "%s 01 - %s %02d", new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()), new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()), cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
    }

    private void setupBarChartShell(View view) {
        barChart = view.findViewById(R.id.barChart);
        if (barChart == null) return;
        barChart.getDescription().setEnabled(false); barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false); barChart.setDrawBorders(false); barChart.setScaleEnabled(false); barChart.setExtraBottomOffset(10f);

        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); xAxis.setDrawAxisLine(false); xAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E")); xAxis.setCenterAxisLabels(true); xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(new String[]{"Week 1", "Week 2", "Week 3", "Week 4", "Week 5"}));

        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false); leftAxis.setTextColor(android.graphics.Color.parseColor("#9E9E9E")); leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(15f, 10f, 0f); leftAxis.setGridColor(android.graphics.Color.parseColor("#E0E0E0"));
        leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) { return value == 0 ? "0" : (value >= 1000 ? String.format(Locale.getDefault(), "%.0fk", value / 1000) : String.valueOf((int) value)); }
        });
        barChart.getAxisRight().setEnabled(false);
    }

    private void updateDashboardData() {
        if (allTransactions == null || barChart == null) return;

        // [RESTORED] Changed to 5 weeks to prevent end-of-month data from disappearing!
        float[] weeklyIncome = new float[5], weeklyExpense = new float[5];
        currentMonthlyIncome = 0; currentMonthlyExpense = 0; todayIncome = 0; todayExpense = 0;

        Calendar cal = Calendar.getInstance();
        int currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR), currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (Transaction t : allTransactions) {
            cal.setTimeInMillis(t.getDateTimestamp());
            if (cal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear && cal.get(Calendar.YEAR) == currentYear) {
                if (t.getAmount() > 0) todayIncome += t.getAmount(); else todayExpense += Math.abs(t.getAmount());
            }
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth) {
                int weekIndex = Math.min((cal.get(Calendar.DAY_OF_MONTH) - 1) / 7, 4);
                if (t.getAmount() > 0) { weeklyIncome[weekIndex] += t.getAmount(); currentMonthlyIncome += t.getAmount(); }
                else { weeklyExpense[weekIndex] += Math.abs(t.getAmount()); currentMonthlyExpense += Math.abs(t.getAmount()); }
            }
        }

        if (notificationDot != null) notificationDot.setVisibility((todayIncome > 0 || todayExpense > 0) ? View.VISIBLE : View.GONE);
        updateBalanceTexts();

        List<com.github.mikephil.charting.data.BarEntry> entriesIncome = new ArrayList<>(), entriesExpense = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Generate 5 weeks to match the X-Axis labels!
            entriesIncome.add(new com.github.mikephil.charting.data.BarEntry(i, weeklyIncome[i]));
            entriesExpense.add(new com.github.mikephil.charting.data.BarEntry(i, weeklyExpense[i]));
        }

        com.github.mikephil.charting.data.BarDataSet setIncome = new com.github.mikephil.charting.data.BarDataSet(entriesIncome, "Income");
        setIncome.setColor(android.graphics.Color.parseColor("#7C4DFF")); setIncome.setDrawValues(false);
        com.github.mikephil.charting.data.BarDataSet setExpense = new com.github.mikephil.charting.data.BarDataSet(entriesExpense, "Expense");
        setExpense.setColor(android.graphics.Color.parseColor("#FF6D00")); setExpense.setDrawValues(false);

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(setIncome, setExpense);
        float groupSpace = 0.4f, barSpace = 0.05f, barWidth = 0.25f;

        barData.setBarWidth(barWidth); barChart.setData(barData);
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(0f + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 5); // Scale for 5 weeks!
        barChart.groupBars(0f, groupSpace, barSpace); barChart.invalidate(); barChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }
}