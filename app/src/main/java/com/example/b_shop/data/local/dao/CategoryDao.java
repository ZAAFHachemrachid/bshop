package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Insert
    void insertAll(List<Category> categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    LiveData<Category> getCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE name LIKE :searchQuery")
    LiveData<List<Category>> searchCategories(String searchQuery);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    @Query("SELECT c.*, COUNT(p.productId) as productCount " +
           "FROM categories c LEFT JOIN products p " +
           "ON c.categoryId = p.categoryId " +
           "GROUP BY c.categoryId")
    LiveData<List<CategoryWithProductCount>> getCategoriesWithProductCount();

    static class CategoryWithProductCount {
        @Embedded
        public Category category;
        public int productCount;
    }
}