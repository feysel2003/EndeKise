package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

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
    private View notificationDot;
    private List<String> alertMessages = new ArrayList<>(); // Stores the 90% budget alerts!

    private List<Calendar> calendarDays = new ArrayList<>();
    private Calendar selectedDate = null; // Default is null (shows full month)
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

        // --- 1. PROFILE PICTURE LOGIC (Crash-Proof) ---
        View profileContainer = view.findViewById(R.id.imgProfile);
        if (profileContainer instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) profileContainer;
            if (group.getChildCount() > 0 && group.getChildAt(0) instanceof android.widget.ImageView) {
                android.widget.ImageView iconProfile = (android.widget.ImageView) group.getChildAt(0);
                com.example.endekise.utils.ImageUtils.loadProfileImage(requireContext(), iconProfile);
            }
        }

        profileContainer.setOnClickListener(v -> {
            ProfileBottomSheet profileSheet = new ProfileBottomSheet();
            profileSheet.show(getParentFragmentManager(), "ProfileBottomSheet");
        });

        // --- 2. NOTIFICATION BELL LOGIC ---
        notificationDot = view.findViewById(R.id.notificationDot);
        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            if (alertMessages.isEmpty()) {
                Toast.makeText(getContext(), "You are within all your budgets. Great job!", Toast.LENGTH_SHORT).show();
            } else {
                String alerts = String.join("\n\n", alertMessages);
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Budget Alerts ⚠️")
                        .setMessage(alerts)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        // --- 3. UI ELEMENTS SETUP ---
        textIncome = view.findViewById(R.id.textBudgetIncome);
        textExpense = view.findViewById(R.id.textBudgetExpense);
        textMonthYear = view.findViewById(R.id.textMonthYear);

        // --- 4. CALENDAR SETUP (Rolling 14 Days) ---
        textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        for (int i = -14; i <= 14; i++) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, i);
            calendarDays.add(day);
        }
        RecyclerView recyclerCalendar = view.findViewById(R.id.recyclerViewCalendar);
        calendarAdapter = new CalendarAdapter();
        recyclerCalendar.setAdapter(calendarAdapter);
        recyclerCalendar.scrollToPosition(14); // Scroll to center (Today)

        // --- 5. CATEGORY LIST SETUP ---
        RecyclerView recyclerCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryBudgetAdapter();
        recyclerCategories.setAdapter(categoryAdapter);

        // --- 6. BUTTON CLICKS ---
        view.findViewById(R.id.btnViewAll).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new TotalExpenseFragment())
                    .addToBackStack(null).commit();
        });

        view.findViewById(R.id.btnAddCategory).setOnClickListener(v -> showAddEditCategoryDialog(null));

        // --- 7. OBSERVE DATA ---
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategories = categories;
            calculateAndDisplayData();
        });

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions;
            calculateAndDisplayData();
        });
    }

    // --- THE ADD/EDIT CATEGORY DIALOG ---
    private void showAddEditCategoryDialog(Category existingCategory) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        dialog.setContentView(dialogView);

        TextView textTitle = dialogView.findViewById(R.id.textDialogTitle);
        TextInputEditText editName = dialogView.findViewById(R.id.editCategoryName);
        TextInputEditText editLimit = dialogView.findViewById(R.id.editBudgetLimit);
        MaterialAutoCompleteTextView dropdownType = dialogView.findViewById(R.id.dropdownCategoryType);
        MaterialAutoCompleteTextView dropdownPeriod = dialogView.findViewById(R.id.dropdownBudgetPeriod);
        View btnSave = dialogView.findViewById(R.id.btnSaveCategory);

        // Populate Dropdowns
        String[] types = new String[]{"Expense", "Income"};
        dropdownType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types));

        String[] periods = new String[]{"Daily", "Monthly"};
        dropdownPeriod.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, periods));

        if (existingCategory == null) {
            // NEW CATEGORY MODE
            textTitle.setText("New Category");
            dropdownType.setText("Expense", false);
            dropdownPeriod.setText("Monthly", false); // Default to Monthly
        } else {
            // EDIT CATEGORY MODE
            textTitle.setText("Edit Budget Limit");
            editName.setText(existingCategory.getName());
            editName.setEnabled(false); // Lock name
            dropdownType.setText(existingCategory.getType(), false);
            dropdownType.setEnabled(false); // Lock type

            editLimit.setText(String.valueOf(existingCategory.getBudgetLimit()));
            String period = existingCategory.getBudgetPeriod() != null ? existingCategory.getBudgetPeriod() : "Monthly";
            dropdownPeriod.setText(period, false);
        }

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String limitStr = editLimit.getText().toString().trim();
            String type = dropdownType.getText().toString().trim();
            String period = dropdownPeriod.getText().toString().trim();

            if (name.isEmpty()) {
                editName.setError("Required");
                return;
            }

            double limit = limitStr.isEmpty() ? 0.0 : Double.parseDouble(limitStr);

            if (existingCategory == null) {
                // Pass PERIOD to the new Category object
                Category newCat = new Category(null, name, type, "#7C4DFF", "ic_general", limit, period);
                viewModel.insertCategory(newCat);
                Toast.makeText(getContext(), "Category created!", Toast.LENGTH_SHORT).show();
            } else {
                existingCategory.setBudgetLimit(limit);
                existingCategory.setBudgetPeriod(period);
                viewModel.updateCategory(existingCategory);
                Toast.makeText(getContext(), "Budget updated!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    // --- SMART CALCULATOR (DAILY VS MONTHLY MATH) ---
    private void calculateAndDisplayData() {
        if (allTransactions == null || allCategories == null) return;

        double totalIncome = 0;
        double totalExpense = 0;

        double[] categorySpendsDaily = new double[allCategories.size()];
        double[] categorySpendsMonthly = new double[allCategories.size()];

        alertMessages.clear();

        Calendar currentMonthCal = Calendar.getInstance();
        int todayDayOfYear = currentMonthCal.get(Calendar.DAY_OF_YEAR);

        for (Transaction t : allTransactions) {
            Calendar txCal = Calendar.getInstance();
            txCal.setTimeInMillis(t.getDateTimestamp());

            boolean isThisMonth = (txCal.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH) && txCal.get(Calendar.YEAR) == currentMonthCal.get(Calendar.YEAR));
            boolean isToday = (txCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear && txCal.get(Calendar.YEAR) == currentMonthCal.get(Calendar.YEAR));
            boolean isSelectedDay = selectedDate != null && (txCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) && txCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR));

            // Global Cards Logic
            boolean includeInGlobalTotals = (selectedDate != null) ? isSelectedDay : isThisMonth;

            if (t.getAmount() > 0) {
                if (includeInGlobalTotals) totalIncome += t.getAmount();
            } else {
                double absoluteExpense = Math.abs(t.getAmount());
                if (includeInGlobalTotals) totalExpense += absoluteExpense;

                // Category Specific Logic
                for (int i = 0; i < allCategories.size(); i++) {
                    if (allCategories.get(i).getName().equalsIgnoreCase(t.getPayee())) {

                        if (isThisMonth) categorySpendsMonthly[i] += absoluteExpense;

                        if (selectedDate != null && isSelectedDay) {
                            categorySpendsDaily[i] += absoluteExpense;
                        } else if (selectedDate == null && isToday) {
                            categorySpendsDaily[i] += absoluteExpense;
                        }
                        break;
                    }
                }
            }
        }

        // Update Top Purple/Orange Cards
        textIncome.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalIncome));
        textExpense.setText(String.format(Locale.getDefault(), "ETB %,.2f", totalExpense));

        // Format data for the Adapter
        double[] finalSpends = new double[allCategories.size()];

        for (int i = 0; i < allCategories.size(); i++) {
            Category cat = allCategories.get(i);
            String period = cat.getBudgetPeriod() != null ? cat.getBudgetPeriod() : "Monthly";

            // If it's a Daily budget, use Daily spend. If Monthly, use Monthly!
            if ("Daily".equalsIgnoreCase(period)) {
                finalSpends[i] = categorySpendsDaily[i];
            } else {
                finalSpends[i] = categorySpendsMonthly[i];
            }

            // 90% Alert Logic & Sound Alarm!
            // --- FIXED 90% ALERT ALARM LOGIC ---
            if (cat.getType().equalsIgnoreCase("Expense") && cat.getBudgetLimit() > 0) {
                double percent = (finalSpends[i] / cat.getBudgetLimit()) * 100;

                android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("EndeKisePrefs", android.content.Context.MODE_PRIVATE);
                String warningKey = "warned_90_" + cat.getName() + "_" + period; // E.g., "warned_90_Food_Daily"

                if (percent >= 90.0) {
                    String alertMsg = "🚨 " + cat.getName() + " (" + period + ") is at " + String.format(Locale.getDefault(), "%.1f%%", percent) + " of its budget!";
                    alertMessages.add(alertMsg);

                    // Did we ALREADY warn them today/this month?
                    boolean alreadyWarned = prefs.getBoolean(warningKey, false);

                    if (!alreadyWarned) {
                        // 1. PLAY SOUND (Only once!)
                        com.example.endekise.utils.SoundUtils.playNotificationAlarm(requireContext());

                        // 2. SAVE FLAG IMMEDIATELY SO IT NEVER PLAYS AGAIN FOR THIS CATEGORY
                        prefs.edit().putBoolean(warningKey, true).apply();

                        // 3. SHOW POPUP
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Budget Alert! ⚠️")
                                .setMessage(alertMsg)
                                .setPositiveButton("I'll be careful", null)
                                .show();
                    }
                } else if (percent < 90.0) {
                    // IF they delete an expense and drop below 90%, we reset the flag so it can warn them again in the future!
                    prefs.edit().putBoolean(warningKey, false).apply();
                }
            }
        }

        notificationDot.setVisibility(alertMessages.isEmpty() ? View.GONE : View.VISIBLE);

        // Pass calculated data to Adapter
        categoryAdapter.setData(allCategories, finalSpends);
    }

    // --- HORIZONTAL CALENDAR SLIDER ADAPTER ---
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
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.parseColor("#FF6D00"));
                holder.textDayName.setTextColor(android.graphics.Color.WHITE);
                holder.textDayNumber.setTextColor(android.graphics.Color.WHITE);
            } else {
                holder.cardDay.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.textDayName.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                holder.textDayNumber.setTextColor(android.graphics.Color.parseColor("#212121"));
            }

            holder.itemView.setOnClickListener(v -> {
                selectedDate = isSelected ? null : day;
                textMonthYear.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate == null ? Calendar.getInstance().getTime() : selectedDate.getTime()));
                notifyDataSetChanged();
                calculateAndDisplayData();
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

    // --- CATEGORY BUDGET LIST ADAPTER ---
    class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CatViewHolder> {

        private List<Category> categories = new ArrayList<>();
        private double[] spends = new double[0];

        public void setData(List<Category> cats, double[] sp) {
            this.categories = new ArrayList<>();
            List<Double> validSpends = new ArrayList<>();

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

        @NonNull
        @Override
        public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_category, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
            Category cat = categories.get(position);
            double spent = spends[position];
            double limit = cat.getBudgetLimit();
            if (limit <= 0) limit = 1;

            // Safe period getter
            String period = cat.getBudgetPeriod() != null ? cat.getBudgetPeriod() : "Monthly";

            holder.textCategoryName.setText(cat.getName());
            holder.textSpend.setText(String.format(Locale.getDefault(), "ETB %,.0f", spent));
            holder.textLimit.setText(String.format(Locale.getDefault(), "ETB %,.0f (%s)", limit, period));

            holder.textCategoryDate.setText(new SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(selectedDate == null ? Calendar.getInstance().getTime() : selectedDate.getTime()));

            holder.progress.setMax((int) limit);
            holder.progress.setProgressCompat((int) spent, true);

            double percent = (spent / limit) * 100;
            if (percent > 100) percent = 100;
            holder.textPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percent));

            // Dynamic colors
            if (percent >= 100) {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#D32F2F"));
            } else if (percent >= 90) {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#FF6D00"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#FF6D00"));
            } else {
                holder.textPercentage.setTextColor(android.graphics.Color.parseColor("#7C4DFF"));
                holder.progress.setIndicatorColor(android.graphics.Color.parseColor("#7C4DFF"));
            }

            // CLICK TO EDIT, LONG CLICK TO DELETE!
            holder.itemView.setOnClickListener(v -> showAddEditCategoryDialog(cat));

            holder.itemView.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Category?")
                        .setMessage("Remove '" + cat.getName() + "'?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.deleteCategory(cat);
                            Toast.makeText(getContext(), cat.getName() + " deleted", Toast.LENGTH_SHORT).show();
                        }).show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

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