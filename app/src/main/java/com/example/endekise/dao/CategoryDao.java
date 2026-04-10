package com.example.endekise.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.endekise.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insertCategory(Category category);

    // Get all categories so we can show them in the dropdown
    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @androidx.room.Delete
    void deleteCategory(com.example.endekise.entity.Category category);
    @androidx.room.Update
    void updateCategory(com.example.endekise.entity.Category category);
}