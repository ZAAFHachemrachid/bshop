package com.example.b_shop.ui.wishlist;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.ProductRepository;

import java.util.List;

public class WishListViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private LiveData<List<Product>> wishListProducts;

    public WishListViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        productRepository = new ProductRepository(database.productDao(), database.userDao());
        loadWishListProducts();
    }

    private void loadWishListProducts() {
        try {
            wishListProducts = productRepository.getFavoriteProducts();
        } catch (Exception e) {
            error.setValue("Failed to load wish list: " + e.getMessage());
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
            productRepository.setProductFavorite(productId, false);
        } catch (Exception e) {
            error.setValue("Failed to remove from wish list: " + e.getMessage());
        }
    }
}