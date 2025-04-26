package com.example.b_shop.ui.category;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import java.util.List;

public class CategoryListViewModel extends ViewModel {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private CategoryListViewModel(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // Categories with search filtering
    public LiveData<List<Category>> getCategories() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return categoryRepository.getAllCategories();
            } else {
                return categoryRepository.searchCategories("%" + query + "%");
            }
        });
    }

    // Get products for a specific category
    public LiveData<List<Product>> getProductsForCategory(int categoryId) {
        return productRepository.getProductsByCategory(categoryId);
    }

    // Loading state
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Search functionality
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public String getCurrentSearchQuery() {
        return searchQuery.getValue();
    }

    // ViewModel Factory
    public static class Factory implements ViewModelProvider.Factory {
        private final CategoryRepository categoryRepository;
        private final ProductRepository productRepository;

        public Factory(CategoryRepository categoryRepository, ProductRepository productRepository) {
            this.categoryRepository = categoryRepository;
            this.productRepository = productRepository;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CategoryListViewModel.class)) {
                return (T) new CategoryListViewModel(categoryRepository, productRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}