package com.example.b_shop.ui.wishlist;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

import java.util.List;

public class WishListViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private LiveData<List<Product>> wishListProducts;

    public WishListViewModel(Application application) {
        super(application);
        BShopApplication app = (BShopApplication) application;
        AppDatabase database = app.getDatabase();
        UserManager userManager = app.getUserManager();
        
        userRepository = app.getUserRepository();
        
        loadWishListProducts();
    }

    private void loadWishListProducts() {
        android.util.Log.d("WishListViewModel", "Loading wish list products");
        try {
            wishListProducts = userRepository.getFavoriteProducts();
            android.util.Log.d("WishListViewModel", "Successfully loaded wish list products");
        } catch (IllegalStateException e) {
            android.util.Log.w("WishListViewModel", "User not logged in", e);
            error.setValue("Please log in to view your wish list");
            wishListProducts = new MutableLiveData<>(); // Initialize with empty LiveData
        } catch (Exception e) {
            android.util.Log.e("WishListViewModel", "Failed to load wish list", e);
            error.setValue("Failed to load wish list: " + e.getMessage());
            wishListProducts = new MutableLiveData<>(); // Initialize with empty LiveData
        }
    }

    public LiveData<List<Product>> getWishListProducts() {
        return wishListProducts;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void removeFromWishList(int productId) {
        try {
            userRepository.removeFromFavorites(productId);
        } catch (IllegalStateException e) {
            error.setValue("Please log in to manage your wish list");
        } catch (Exception e) {
            error.setValue("Failed to remove from wish list: " + e.getMessage());
        }
    }

    public void addToWishList(int productId) {
        try {
            userRepository.addToFavorites(productId);
        } catch (IllegalStateException e) {
            error.setValue("Please log in to manage your wish list");
        } catch (Exception e) {
            error.setValue("Failed to add to wish list: " + e.getMessage());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepository.cleanup();
    }
}