package com.example.endekise.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int budgetId;

    private int categoryId;
    private String monthYear; // e.g., "12-2024" or "01-2025"
    private double amountLimit;

    // Constructor
    public Budget(int categoryId, String monthYear, double amountLimit) {
        this.categoryId = categoryId;
        this.monthYear = monthYear;
        this.amountLimit = amountLimit;
    }

    // Getters and Setters
    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getAmountLimit() { return amountLimit; }
    public void setAmountLimit(double amountLimit) { this.amountLimit = amountLimit; }
}