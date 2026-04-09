package com.example.endekise.ui;

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
import com.example.endekise.entity.Category;
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
    private List<Category> allCategories = new ArrayList<>();

    private TextView textIncome, textExpense, textMonthYear;

    // Calendar & Filter Logic
    private List<Calendar> calendarDays = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance(); // Default to Today
    private CalendarAdapter calendarAdapter;
    private CategoryBudgetAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        textIncome = view.findViewById(R.id.textBudgetIncome);
        textExpense = view.findViewById(R.id.textBudgetExpense);
        textMonthYear = view.findViewById(R.id.textMonthYear);

        // --- 1. SETUP SLIDING CALENDAR ---
        textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.getTime()));

        // Generate past 14 days and next 14 days
        for (int i = -14; i <= 14; i++) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, i);
            calendarDays.add(day);
        }

        RecyclerView recyclerCalendar = view.findViewById(R.id.recyclerViewCalendar);
        calendarAdapter = new CalendarAdapter();
        recyclerCalendar.setAdapter(calendarAdapter);
        recyclerCalendar.scrollToPosition(14); // Scroll to center (Today)

        // --- 2. SETUP MODERN CATEGORY LIST ---
        RecyclerView recyclerCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryBudgetAdapter();
        recyclerCategories.setAdapter(categoryAdapter);

        // --- 3. THE "VIEW ALL" REDIRECT ---
        TextView btnViewAll = view.findViewById(R.id.btnViewAll);
        btnViewAll.setOnClickListener(v -> {
            // Redirects to the 3rd screen (TotalExpenseFragment)!
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new TotalExpenseFragment())
                    .addToBackStack(null) // Allows user to hit 'back' to return here
                    .commit();
        });

        // --- 4. FETCH DATA ---
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategories = categories;
            calculateAndDisplayData();
        });

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            calculateAndDisplayData();
        });
    }

    private void calculateAndDisplayData() {
        if (allTransactions == null || allCategories == null) return;

        double totalIncome = 0;
        double totalExpense = 0;
        double[] categorySpends = new double[allCategories.size()];

        for (Transaction t : allTransactions) {
            Calendar txCal = Calendar.getInstance();
            txCal.setTimeInMillis(t.getDateTimestamp());

            // Filter by the selected sliding calendar date
            if (txCal.get(Calendar.DAY_OF_YEAR) != selectedDate.get(Calendar.DAY_OF_YEAR) ||
                    txCal.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR)) {
                continue;
            }

            if (t.getAmount() > 0) {
                totalIncome += t.getAmount();
            } else {
                double absoluteExpense = Math.abs(t.getAmount());
                totalExpense += absoluteExpense;

                for (int i = 0; i < allCategories.size(); i++) {
                    if (allCategories.get(i).getName().equalsIgnoreCase(t.getPayee())) {
                        categorySpends[i] += absoluteExpense;
                        break;
                    }
                }
            }
        }

        textIncome.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalIncome));
        textExpense.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalExpense));

        // Pass calculated data to Category Adapter
        categoryAdapter.setData(allCategories, categorySpends);
    }

    // --- CALENDAR SLIDER ADAPTER ---
    class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
        @NonNull @Override public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            Calendar day = calendarDays.get(position);
            holder.textDayName.setText(new SimpleDateFormat("E", Locale.getDefault()).format(day.getTime()).substring(0, 1));
            holder.textDayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

            boolean isSelected = (day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR));

            if (isSelected) {
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.parseColor("#FF6D00"));
                holder.textDayName.setTextColor(android.graphics.Color.WHITE);
                holder.textDayNumber.setTextColor(android.graphics.Color.WHITE);
            } else {
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.textDayName.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                holder.textDayNumber.setTextColor(android.graphics.Color.parseColor("#212121"));
            }

            holder.itemView.setOnClickListener(v -> {
                selectedDate = day;
                textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.getTime()));
                notifyDataSetChanged();
                calculateAndDisplayData(); // Recalculate!
            });
        }
        @Override public int getItemCount() { return calendarDays.size(); }
        class DayViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardDay; TextView textDayName, textDayNumber;
            public DayViewHolder(@NonNull View itemView) {
                super(itemView);
                cardDay = itemView.findViewById(R.id.cardDay);
                textDayName = itemView.findViewById(R.id.textDayName);
                textDayNumber = itemView.findViewById(R.id.textDayNumber);
            }
        }
    }

    // --- MODERN CATEGORY ADAPTER ---
    class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CatViewHolder> {
        private List<Category> categories = new ArrayList<>();
        private double[] spends = new double[0];

        public void setData(List<Category> cats, double[] sp) {
            this.categories = new ArrayList<>();
            List<Double> validSpends = new ArrayList<>();
            // Only show Expenses!
            for (int i = 0; i < cats.size(); i++) {
                if (!cats.get(i).getType().equalsIgnoreCase("Income")) {
                    this.categories.add(cats.get(i));
                    validSpends.add(sp[i]);
                }
            }
            this.spends = new double[validSpends.size()];
            for(int i=0; i<validSpends.size(); i++) this.spends[i] = validSpends.get(i);
            notifyDataSetChanged();
        }

        @NonNull @Override public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_category, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
            Category cat = categories.get(position);
            double spent = spends[position];
            double limit = 5000.0; // Dynamic later

            holder.textCategoryName.setText(cat.getName());
            holder.textSpend.setText(String.format(Locale.getDefault(), "ETB %,.0f", spent));
            holder.textLimit.setText(String.format(Locale.getDefault(), "ETB %,.0f", limit));

            // Dynamic Date!
            holder.textCategoryDate.setText(new SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(selectedDate.getTime()));

            holder.progress.setMax((int) limit);
            holder.progress.setProgressCompat((int) spent, true);

            double percent = (spent / limit) * 100;
            if (percent > 100) percent = 100;
            holder.textPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percent));

            if (percent >= 100) {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#D32F2F"));
            } else if (percent > 75) {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#FF6D00"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#FF6D00"));
            } else {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#7C4DFF"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#7C4DFF"));
            }
        }
        @Override public int getItemCount() { return categories.size(); }
        class CatViewHolder extends RecyclerView.ViewHolder {
            TextView textCategoryName, textCategoryDate, textSpend, textLimit, textPercentage;
            LinearProgressIndicator progress;
            public CatViewHolder(@NonNull View itemView) {
                super(itemView);
                textCategoryName = itemView.findViewById(R.id.textCategoryName);
                textCategoryDate = itemView.findViewById(R.id.textCategoryDate);
                textSpend = itemView.findViewById(R.id.textSpend);
                textLimit = itemView.findViewById(R.id.textLimit);
                textPercentage = itemView.findViewById(R.id.textPercentage);
                progress = itemView.findViewById(R.id.budgetProgressBar);
            }
        }
    }
}