package com.example.endekise.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {

    @PrimaryKey(autoGenerate = true)
    private int accountId;

    private String name;
    private String type; // e.g., "Cash", "Bank", "Mobile Money"
    private double currentBalance;
    private String currencyCode; // e.g., "ETB" or "USD"

    // Constructor
    public Account(String name, String type, double currentBalance, String currencyCode) {
        this.name = name;
        this.type = type;
        this.currentBalance = currentBalance;
        this.currencyCode = currencyCode;
    }

    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}