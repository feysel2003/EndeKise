package com.example.endekise.entity;

import androidx.room.Entity;

// This table uses a composite primary key because it maps two IDs together
@Entity(tableName = "transaction_tags", primaryKeys = {"transactionId", "tagId"})
public class TransactionTag {

    private int transactionId;
    private int tagId;

    // Constructor
    public TransactionTag(int transactionId, int tagId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }
}