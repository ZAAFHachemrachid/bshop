package com.example.b_shop.ui.category;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.repositories.CategoryRepository;

import java.util.List;

public class CategoryListViewModel extends AndroidViewModel {
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public CategoryListViewModel(Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        refreshCategories();
    }

    private void refreshCategories() {
        isLoading.setValue(true);
        categoryRepository.refreshCategories();
        isLoading.setValue(false);
    }

    public LiveData<List<Category>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        categoryRepository.cleanup();
    }
}