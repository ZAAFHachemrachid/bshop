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

public class CategoryProductsViewModel extends ViewModel {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<Integer> categoryId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    private final LiveData<List<Product>> products;
    private final LiveData<String> categoryName;

    private CategoryProductsViewModel(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;

        // Transform categoryId into products list
        products = Transformations.switchMap(categoryId, 
            id -> productRepository.getProductsByCategory(id));

        // Transform categoryId into category name
        categoryName = Transformations.switchMap(categoryId,
            id -> Transformations.map(categoryRepository.getCategoryById(id),
                category -> category != null ? category.getName() : null));
    }

    public void setCategoryId(int id) {
        if (categoryId.getValue() == null || categoryId.getValue() != id) {
            isLoading.setValue(true);
            categoryId.setValue(id);
            isLoading.setValue(false);
        }
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<String> getCategoryName() {
        return categoryName;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        productRepository.cleanup();
        categoryRepository.cleanup();
    }

    // ViewModel Factory
    public static class Factory implements ViewModelProvider.Factory {
        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;

        public Factory(ProductRepository productRepository, CategoryRepository categoryRepository) {
            this.productRepository = productRepository;
            this.categoryRepository = categoryRepository;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CategoryProductsViewModel.class)) {
                return (T) new CategoryProductsViewModel(productRepository, categoryRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}