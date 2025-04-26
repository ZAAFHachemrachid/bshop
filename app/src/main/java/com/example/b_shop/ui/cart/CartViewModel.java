package com.example.b_shop.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.b_shop.data.local.errors.CartError;
import com.example.b_shop.data.local.errors.CartError.CartErrorType;
import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.domain.usecases.CartUseCase;
import com.example.b_shop.domain.usecases.CartUseCase.CartState;
import com.example.b_shop.domain.usecases.CheckoutUseCase;
import com.example.b_shop.domain.usecases.CheckoutUseCase.CheckoutState;

import java.util.List;

public class CartViewModel extends ViewModel {
    private final CartUseCase cartUseCase;
    private final CheckoutUseCase checkoutUseCase;
    
    private final MediatorLiveData<CartUIState> uiState;
    private CartError lastError;
    
    public CartViewModel(CartUseCase cartUseCase, CheckoutUseCase checkoutUseCase) {
        this.cartUseCase = cartUseCase;
        this.checkoutUseCase = checkoutUseCase;
        this.uiState = new MediatorLiveData<>();
        
        initializeState();
    }

    private void initializeState() {
        // Observe cart state changes
        uiState.addSource(cartUseCase.getCartState(), this::handleCartStateChange);
        
        // Observe operation errors
        uiState.addSource(cartUseCase.getOperationError(), this::handleError);
        
        // Initialize with loading state
        uiState.setValue(new CartUIState(true));
    }

    private void handleCartStateChange(CartState cartState) {
        if (cartState == null) {
            uiState.setValue(new CartUIState(true));
            return;
        }

        uiState.setValue(new CartUIState(
            cartState.getItems(),
            cartState.getTotal(),
            false,
            lastError != null ? lastError.getDetails() : null,
            cartState.isEmpty(),
            checkoutUseCase.getCheckoutState().getValue(),
            null
        ));
    }

    private void handleError(CartError error) {
        if (error == null) {
            lastError = null;
            return;
        }

        lastError = error;
        
        // Handle session expiration differently
        if (error.getType() == CartErrorType.SESSION_EXPIRED) {
            // Trigger navigation to login
            uiState.setValue(new CartUIState(
                false,
                error.getDetails(),
                true  // requiresLogin
            ));
            return;
        }

        // Update UI state with error
        CartUIState currentState = uiState.getValue();
        if (currentState != null) {
            uiState.setValue(new CartUIState(
                currentState.getItems(),
                currentState.getTotal(),
                false,
                error.getDetails(),
                currentState.isEmpty(),
                currentState.getCheckoutState(),
                null
            ));
        }
    }

    public LiveData<CartUIState> getUiState() {
        return uiState;
    }

    public void updateQuantity(int cartItemId, int quantity) {
        clearError();
        cartUseCase.updateQuantity(cartItemId, quantity);
    }

    public void removeItem(int cartItemId) {
        clearError();
        cartUseCase.removeItem(cartItemId);
    }

    public void clearCart() {
        clearError();
        cartUseCase.clearCart();
    }

    public void checkout() {
        clearError();
        checkoutUseCase.checkout();
    }

    public void clearError() {
        cartUseCase.clearError();
        lastError = null;
        
        // Clear error from current UI state
        CartUIState currentState = uiState.getValue();
        if (currentState != null && currentState.getError() != null) {
            uiState.setValue(new CartUIState(
                currentState.getItems(),
                currentState.getTotal(),
                false,
                null,
                currentState.isEmpty(),
                currentState.getCheckoutState(),
                null
            ));
        }
    }

    /**
     * Represents the UI state of the cart screen
     */
    public static class CartUIState {
        private final List<CartItemWithProduct> items;
        private final float total;
        private final boolean isLoading;
        private final String error;
        private final boolean isEmpty;
        private final CheckoutState checkoutState;
        private final String checkoutError;
        private final boolean requiresLogin;

        // Constructor for loading state
        public CartUIState(boolean isLoading) {
            this.isLoading = isLoading;
            this.items = null;
            this.total = 0;
            this.error = null;
            this.isEmpty = true;
            this.checkoutState = null;
            this.checkoutError = null;
            this.requiresLogin = false;
        }

        // Constructor for session expired state
        public CartUIState(boolean isLoading, String error, boolean requiresLogin) {
            this.isLoading = isLoading;
            this.items = null;
            this.total = 0;
            this.error = error;
            this.isEmpty = true;
            this.checkoutState = null;
            this.checkoutError = null;
            this.requiresLogin = requiresLogin;
        }

        // Constructor for normal state
        public CartUIState(
            List<CartItemWithProduct> items,
            float total,
            boolean isLoading,
            String error,
            boolean isEmpty,
            CheckoutState checkoutState,
            String checkoutError
        ) {
            this.items = items;
            this.total = total;
            this.isLoading = isLoading;
            this.error = error;
            this.isEmpty = isEmpty;
            this.checkoutState = checkoutState;
            this.checkoutError = checkoutError;
            this.requiresLogin = false;
        }

        public List<CartItemWithProduct> getItems() {
            return items;
        }

        public float getTotal() {
            return total;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public String getError() {
            return error;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public CheckoutState getCheckoutState() {
            return checkoutState;
        }

        public String getCheckoutError() {
            return checkoutError;
        }

        public boolean requiresLogin() {
            return requiresLogin;
        }
    }
}