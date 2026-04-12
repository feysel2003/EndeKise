package com.example.endekise.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int categoryId;

    private Integer parentCategoryId; // Uses 'Integer' instead of 'int' so it can be null for main categories
    private String name;
    private String type; // "Income" or "Expense"
    private String colorHex;
    private String iconName;
    private double budgetLimit; // --- NEW FIELD! ---
    private String budgetPeriod; // --- NEW FIELD! ("Daily" or "Monthly") ---

    // Constructor
    // Update Constructor
    public Category(Integer parentCategoryId, String name, String type, String colorHex, String iconName, double budgetLimit, String budgetPeriod) {
        this.parentCategoryId = parentCategoryId;
        this.name = name;
        this.type = type;
        this.colorHex = colorHex;
        this.iconName = iconName;
        this.budgetLimit = budgetLimit;
        this.budgetPeriod = budgetPeriod;
    }

    // Getters and Setters
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    // Add Getter and Setter
    public String getBudgetPeriod() { return budgetPeriod; }
    public void setBudgetPeriod(String budgetPeriod) { this.budgetPeriod = budgetPeriod; }
    public Integer getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }
}