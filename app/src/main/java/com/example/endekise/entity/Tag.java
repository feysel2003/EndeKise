package com.example.endekise.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tags")
public class Tag {

    @PrimaryKey(autoGenerate = true)
    private int tagId;

    private String name; // e.g., "#Vacation2025"

    // Constructor
    public Tag(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}