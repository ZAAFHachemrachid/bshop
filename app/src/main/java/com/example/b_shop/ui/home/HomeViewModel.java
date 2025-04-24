package com.example.b_shop.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    private final LiveData<List<Category>> categories;
    private final LiveData<List<Product>> featuredProducts;
    private final LiveData<List<Product>> topRatedProducts;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public HomeViewModel(Application application) {
        super(application);
        
        // Initialize repositories
        categoryRepository = new CategoryRepository(application);
        productRepository = new ProductRepository(application);
        
        // Get categories
        categories = categoryRepository.getAllCategories();
        
        // Get featured products (for now, getting all available products)
        featuredProducts = productRepository.getAvailableProducts();
        
        // Get top rated products (limited to 10)
        topRatedProducts = productRepository.getTopRatedProducts(10);
        
        // Setup loading state
        MediatorLiveData<Boolean> loadingMediator = new MediatorLiveData<>();
        loadingMediator.addSource(categories, list -> checkLoading());
        loadingMediator.addSource(featuredProducts, list -> checkLoading());
        loadingMediator.addSource(topRatedProducts, list -> checkLoading());
    }
    
    private void checkLoading() {
        if (categories.getValue() != null && 
            featuredProducts.getValue() != null && 
            topRatedProducts.getValue() != null) {
            isLoading.setValue(false);
        }
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Product>> getFeaturedProducts() {
        return featuredProducts;
    }

    public LiveData<List<Product>> getTopRatedProducts() {
        return topRatedProducts;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void refreshData() {
        isLoading.setValue(true);
        // In a real app, you might want to refresh the data from a remote source here
        // For now, the LiveData will automatically update if the database changes
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        categoryRepository.cleanup();
        productRepository.cleanup();
    }
}