package com.example.endekise.entity; // Keep your package name here

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_splits")
public class TransactionSplit {

    @PrimaryKey(autoGenerate = true)
    private int splitId;

    private int transactionId;
    private int categoryId;
    private double amount;

    // Constructor
    public TransactionSplit(int transactionId, int categoryId, double amount) {
        this.transactionId = transactionId;
        this.categoryId = categoryId;
        this.amount = amount;
    }

    // Getters and Setters
    public int getSplitId() { return splitId; }
    public void setSplitId(int splitId) { this.splitId = splitId; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}