package com.example.b_shop.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> isRefreshing;

    public HomeViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        categoryRepository = new CategoryRepository(database.categoryDao());
        productRepository = new ProductRepository(database.productDao(), database.userDao());
        executorService = Executors.newSingleThreadExecutor();
        isRefreshing = new MutableLiveData<>(false);
    }

    public void refreshData() {
        isRefreshing.setValue(true);
        executorService.execute(() -> {
            try {
                // Refresh logic if needed
                isRefreshing.postValue(false);
            } catch (Exception e) {
                isRefreshing.postValue(false);
            }
        });
    }

    // Categories
    public LiveData<List<Category>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    // Featured Products
    public LiveData<List<Product>> getFeaturedProducts() {
        return productRepository.getTopRatedProducts(5); // Get top 5 products for featured
    }

    // Top Rated Products
    public LiveData<List<Product>> getTopRatedProducts() {
        return productRepository.getTopRatedProducts(10); // Get top 10 products
    }

    // Refresh state
    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        productRepository.cleanup();
        categoryRepository.cleanup();
    }
}