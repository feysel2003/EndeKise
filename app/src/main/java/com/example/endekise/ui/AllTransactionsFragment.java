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
import com.example.endekise.adapter.TransactionAdapter;
import com.example.endekise.entity.Transaction;
import com.example.endekise.viewmodel.DashboardViewModel;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AllTransactionsFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> fullTransactionList = new ArrayList<>();

    // Calendar & Filter State
    private List<Calendar> calendarDays = new ArrayList<>();
    private Calendar selectedDate = null; // null = Show All Time
    private CalendarAdapter calendarAdapter;

    private TextView textMonthYear, textFilterDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        textMonthYear = view.findViewById(R.id.textMonthYear);
        textFilterDescription = view.findViewById(R.id.textFilterDescription);

        // --- 1. BACK BUTTON ---
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- 2. SETUP SLIDING CALENDAR (Rolling 14 days) ---
        textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        for (int i = -14; i <= 0; i++) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, i);
            calendarDays.add(day);
        }

        RecyclerView recyclerCalendar = view.findViewById(R.id.recyclerViewCalendar);
        calendarAdapter = new CalendarAdapter();
        recyclerCalendar.setAdapter(calendarAdapter);
        recyclerCalendar.scrollToPosition(14); // Scroll to today

        // --- 3. SETUP TRANSACTIONS LIST ---
        RecyclerView recyclerTransactions = view.findViewById(R.id.recyclerAllTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTransactions.setHasFixedSize(true);
        transactionAdapter = new TransactionAdapter();
        recyclerTransactions.setAdapter(transactionAdapter);

        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), transactionAdapter::setAccounts);

        // --- 4. OBSERVE & FILTER TRANSACTIONS ---
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            fullTransactionList = transactions;
            applyDateFilter();
        });
    }

    private void applyDateFilter() {
        if (fullTransactionList == null) return;

        if (selectedDate == null) {
            transactionAdapter.setTransactions(fullTransactionList);
            textFilterDescription.setText("Showing all-time records");
            return;
        }

        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : fullTransactionList) {
            Calendar txCal = Calendar.getInstance();
            txCal.setTimeInMillis(t.getDateTimestamp());

            if (txCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                    txCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)) {
                filteredList.add(t);
            }
        }

        transactionAdapter.setTransactions(filteredList);
        String formattedDate = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(selectedDate.getTime());
        textFilterDescription.setText("Showing records for " + formattedDate);
    }

    // --- INNER CLASS: CALENDAR ADAPTER ---
    class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            Calendar day = calendarDays.get(position);
            holder.textDayName.setText(new SimpleDateFormat("E", Locale.getDefault()).format(day.getTime()).substring(0, 1));
            holder.textDayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

            boolean isSelected = (selectedDate != null && day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR));

            if (isSelected) {
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.parseColor("#FF6D00")); // Orange
                holder.textDayName.setTextColor(android.graphics.Color.WHITE);
                holder.textDayNumber.setTextColor(android.graphics.Color.WHITE);
            } else {
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.textDayName.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                holder.textDayNumber.setTextColor(android.graphics.Color.parseColor("#212121"));
            }

            holder.itemView.setOnClickListener(v -> {
                if (isSelected) {
                    selectedDate = null; // Unselect to show all time
                } else {
                    selectedDate = day;
                }
                notifyDataSetChanged();
                applyDateFilter();
            });
        }

        @Override
        public int getItemCount() {
            return calendarDays.size();
        }

        class DayViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardDay;
            TextView textDayName, textDayNumber;

            public DayViewHolder(@NonNull View itemView) {
                super(itemView);
                cardDay = itemView.findViewById(R.id.cardDay);
                textDayName = itemView.findViewById(R.id.textDayName);
                textDayNumber = itemView.findViewById(R.id.textDayNumber);
            }
        }
    }
}