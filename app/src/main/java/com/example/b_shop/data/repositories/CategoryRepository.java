package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.dao.CategoryDao;
import com.example.b_shop.data.local.dao.CategoryDao.CategoryWithProductCount;
import com.example.b_shop.data.local.entities.Category;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final ExecutorService executorService;

    public CategoryRepository(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<Category> getCategoryById(int categoryId) {
        return categoryDao.getCategoryById(categoryId);
    }

    public LiveData<List<Category>> searchCategories(String query) {
        return categoryDao.searchCategories(query);
    }

    public LiveData<List<CategoryWithProductCount>> getCategoriesWithProductCount() {
        return categoryDao.getCategoriesWithProductCount();
    }

    public void cleanup() {
        executorService.shutdown();
    }
}