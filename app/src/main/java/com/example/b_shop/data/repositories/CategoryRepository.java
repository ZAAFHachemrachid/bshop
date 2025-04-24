package com.example.b_shop.data.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.dao.CategoryDao;
import com.example.b_shop.data.local.entities.Category;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final ExecutorService executorService;

    public CategoryRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<Category> getCategoryById(int categoryId) {
        return categoryDao.getCategoryById(categoryId);
    }

    public LiveData<List<Category>> searchCategories(String query) {
        return categoryDao.searchCategories("%" + query + "%");
    }

    public LiveData<List<CategoryDao.CategoryWithProductCount>> getCategoriesWithProductCount() {
        return categoryDao.getCategoriesWithProductCount();
    }

    public void insert(Category category) {
        executorService.execute(() -> {
            categoryDao.insert(category);
        });
    }

    public void update(Category category) {
        executorService.execute(() -> {
            categoryDao.update(category);
        });
    }

    public void delete(Category category) {
        executorService.execute(() -> {
            categoryDao.delete(category);
        });
    }

    // Cleanup method to be called when repository is no longer needed
    public void cleanup() {
        executorService.shutdown();
    }
}