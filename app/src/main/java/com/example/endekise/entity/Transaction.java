package com.example.endekise.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int transactionId;

    private int accountId;
    private double amount;
    private long dateTimestamp; // Saving dates as long (milliseconds) is best for SQLite
    private String note;
    private String payee;
    private double locationLat;
    private double locationLng;
    private boolean isCleared;

    // Constructor
    public Transaction(int accountId, double amount, long dateTimestamp, String note, String payee, double locationLat, double locationLng, boolean isCleared) {
        this.accountId = accountId;
        this.amount = amount;
        this.dateTimestamp = dateTimestamp;
        this.note = note;
        this.payee = payee;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.isCleared = isCleared;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public long getDateTimestamp() { return dateTimestamp; }
    public void setDateTimestamp(long dateTimestamp) { this.dateTimestamp = dateTimestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPayee() { return payee; }
    public void setPayee(String payee) { this.payee = payee; }

    public double getLocationLat() { return locationLat; }
    public void setLocationLat(double locationLat) { this.locationLat = locationLat; }

    public double getLocationLng() { return locationLng; }
    public void setLocationLng(double locationLng) { this.locationLng = locationLng; }

    public boolean isCleared() { return isCleared; }
    public void setCleared(boolean cleared) { isCleared = cleared; }
}