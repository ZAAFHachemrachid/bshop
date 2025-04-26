package com.example.b_shop.ui.cart;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.OrderRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.domain.usecases.CartUseCase;
import com.example.b_shop.domain.usecases.CheckoutUseCase;
import com.example.b_shop.utils.UserManager;

public class CartViewModelFactory implements ViewModelProvider.Factory {
    private final CartUseCase cartUseCase;
    private final CheckoutUseCase checkoutUseCase;

    public CartViewModelFactory(BShopApplication application) {
        android.util.Log.d("CartViewModelFactory", "Using singleton repositories from BShopApplication");

        // Get singleton repositories
        CartRepository cartRepository = application.getCartRepository();
        OrderRepository orderRepository = application.getOrderRepository();
        ProductRepository productRepository = application.getProductRepository();
        UserManager userManager = application.getUserManager();

        // Create use cases
        this.cartUseCase = new CartUseCase(
            cartRepository,
            productRepository,
            userManager
        );
        
        this.checkoutUseCase = new CheckoutUseCase(
            cartRepository,
            orderRepository,
            productRepository,
            userManager
        );
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CartViewModel.class)) {
            return (T) new CartViewModel(cartUseCase, checkoutUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}