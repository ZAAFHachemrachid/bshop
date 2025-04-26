package com.example.b_shop.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.b_shop.domain.usecases.CartUseCase;
import com.example.b_shop.domain.usecases.CartUseCase.CartState;
import com.example.b_shop.domain.usecases.CheckoutUseCase;
import com.example.b_shop.domain.usecases.CheckoutUseCase.CheckoutState;
import com.example.b_shop.domain.usecases.CheckoutUseCase.CheckoutResult;
import com.example.b_shop.utils.UserManager;

/**
 * ViewModel for the cart screen that handles cart-related operations and state management
 */
public class CartViewModel extends ViewModel {
    private final CartUseCase cartUseCase;
    private final CheckoutUseCase checkoutUseCase;
    private final UserManager userManager;

    private final MutableLiveData<CartUIState> _uiState;
    private final LiveData<CartUIState> uiState;

    public CartViewModel(
        CartUseCase cartUseCase,
        CheckoutUseCase checkoutUseCase,
        UserManager userManager
    ) {
        this.cartUseCase = cartUseCase;
        this.checkoutUseCase = checkoutUseCase;
        this.userManager = userManager;

        this._uiState = new MutableLiveData<>(CartUIState.loading());
        this.uiState = setupUiState();
    }

    private LiveData<CartUIState> setupUiState() {
        MediatorLiveData<CartUIState> mediator = new MediatorLiveData<>();

        // Observe cart state
        mediator.addSource(cartUseCase.getCartState(), cartState -> {
            if (cartState == null) {
                mediator.setValue(CartUIState.loading());
                return;
            }

            CartUIState currentState = _uiState.getValue();
            if (currentState == null) {
                currentState = CartUIState.loading();
            }

            mediator.setValue(new CartUIState(
                cartState,
                currentState.isLoading(),
                currentState.getError(),
                currentState.getCheckoutState(),
                currentState.getCheckoutError()
            ));
        });

        // Observe checkout state
        mediator.addSource(checkoutUseCase.getCheckoutState(), checkoutState -> {
            CartUIState currentState = _uiState.getValue();
            if (currentState == null) return;

            boolean isLoading = checkoutState == CheckoutState.PROCESSING;
            mediator.setValue(currentState.copy(
                isLoading,
                null,
                checkoutState,
                currentState.getCheckoutError()
            ));
        });

        // Observe checkout result
        mediator.addSource(checkoutUseCase.getCheckoutResult(), result -> {
            CartUIState currentState = _uiState.getValue();
            if (currentState == null || result == null) return;

            mediator.setValue(currentState.copy(
                false,
                null,
                currentState.getCheckoutState(),
                result.isSuccess() ? null : result.getError()
            ));
        });

        return mediator;
    }

    public LiveData<CartUIState> getUiState() {
        return uiState;
    }

    public void updateQuantity(int cartItemId, int quantity) {
        cartUseCase.updateQuantity(cartItemId, quantity);
    }

    public void removeItem(int cartItemId) {
        cartUseCase.removeItem(cartItemId);
    }

    public void clearCart() {
        cartUseCase.clearCart();
    }

    public void checkout() {
        if (!userManager.isUserLoggedIn()) {
            _uiState.setValue(_uiState.getValue().copy(
                false,
                "Please login to checkout",
                null,
                null
            ));
            return;
        }
        checkoutUseCase.checkout();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        checkoutUseCase.cleanup();
    }

    /**
     * Represents the UI state for the cart screen
     */
    public static class CartUIState {
        private final CartState cartState;
        private final boolean isLoading;
        private final String error;
        private final CheckoutState checkoutState;
        private final String checkoutError;

        private CartUIState(
            CartState cartState,
            boolean isLoading,
            String error,
            CheckoutState checkoutState,
            String checkoutError
        ) {
            this.cartState = cartState;
            this.isLoading = isLoading;
            this.error = error;
            this.checkoutState = checkoutState;
            this.checkoutError = checkoutError;
        }

        public static CartUIState loading() {
            return new CartUIState(null, true, null, null, null);
        }

        public CartUIState copy(
            boolean isLoading,
            String error,
            CheckoutState checkoutState,
            String checkoutError
        ) {
            return new CartUIState(
                this.cartState,
                isLoading,
                error,
                checkoutState,
                checkoutError
            );
        }

        public CartState getCartState() {
            return cartState;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public String getError() {
            return error;
        }

        public CheckoutState getCheckoutState() {
            return checkoutState;
        }

        public String getCheckoutError() {
            return checkoutError;
        }

        public boolean isEmpty() {
            return cartState == null || cartState.isEmpty();
        }

        public float getTotal() {
            return cartState != null ? cartState.getTotal() : 0f;
        }

        public int getItemCount() {
            return cartState != null ? cartState.getItemCount() : 0;
        }
    }
}