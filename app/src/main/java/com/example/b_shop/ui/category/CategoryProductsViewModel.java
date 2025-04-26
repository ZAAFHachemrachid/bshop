package com.example.b_shop.ui.category;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.ProductRepository;

import java.util.List;

public class CategoryProductsViewModel extends AndroidViewModel {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<Integer> categoryId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    private final LiveData<List<Product>> products;
    private final LiveData<String> categoryName;

    public CategoryProductsViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);

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
}