package com.example.b_shop.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.entities.Review;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.data.repositories.ReviewRepository;
import com.example.b_shop.data.repositories.UserRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailsViewModel extends ViewModel {
    
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    
    private final MutableLiveData<Product> product = new MutableLiveData<>();
    private final MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addToCartSuccess = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentImagePosition = new MutableLiveData<>();

    private ProductDetailsViewModel(ProductRepository productRepository, ReviewRepository reviewRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        currentImagePosition.setValue(0);
    }

    public void loadProductDetails(int productId) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // Load product details
                Product productDetails = productRepository.getProduct(productId);
                product.postValue(productDetails);
                
                // Load reviews
                List<Review> productReviews = reviewRepository.getProductReviews(productId);
                reviews.postValue(productReviews);
                
                // Check favorite status
                try {
                    boolean favoriteStatus = userRepository.isProductFavorite(productId);
                    isFavorite.postValue(favoriteStatus);
                } catch (IllegalStateException e) {
                    // User not logged in - product is not favorite
                    isFavorite.postValue(false);
                }
                
                isLoading.postValue(false);
            } catch (Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void addToCart(int quantity) {
        Product currentProduct = product.getValue();
        if (currentProduct != null) {
            executorService.execute(() -> {
                try {
                    productRepository.addToCart(currentProduct.getProductId(), quantity);
                    addToCartSuccess.postValue(true);
                } catch (Exception e) {
                    error.postValue(e.getMessage());
                    addToCartSuccess.postValue(false);
                }
            });
        }
    }

    public void toggleFavorite() {
        Product currentProduct = product.getValue();
        if (currentProduct != null) {
            executorService.execute(() -> {
                try {
                    boolean newStatus = !Boolean.TRUE.equals(isFavorite.getValue());
                    if (newStatus) {
                        userRepository.addToFavorites(currentProduct.getProductId());
                    } else {
                        userRepository.removeFromFavorites(currentProduct.getProductId());
                    }
                    isFavorite.postValue(newStatus);
                } catch (IllegalStateException e) {
                    error.postValue("Please log in to manage favorites");
                } catch (Exception e) {
                    error.postValue("Failed to update favorite status: " + e.getMessage());
                }
            });
        }
    }

    public void setCurrentImagePosition(int position) {
        currentImagePosition.setValue(position);
    }

    // Getters for LiveData
    public LiveData<Product> getProduct() {
        return product;
    }

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isFavorite() {
        return isFavorite;
    }

    public LiveData<Boolean> getAddToCartStatus() {
        return addToCartSuccess;
    }

    public LiveData<Integer> getCurrentImagePosition() {
        return currentImagePosition;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        productRepository.cleanup();
        reviewRepository.cleanup();
    }

    // ViewModel Factory
    public static class Factory implements ViewModelProvider.Factory {
        private final ProductRepository productRepository;
        private final ReviewRepository reviewRepository;
        private final UserRepository userRepository;

        public Factory(ProductRepository productRepository, ReviewRepository reviewRepository, UserRepository userRepository) {
            this.productRepository = productRepository;
            this.reviewRepository = reviewRepository;
            this.userRepository = userRepository;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProductDetailsViewModel.class)) {
                return (T) new ProductDetailsViewModel(productRepository, reviewRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}