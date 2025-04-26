package com.example.b_shop.domain.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.OrderItem;
import com.example.b_shop.data.local.errors.CartError;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.CartRepository.CartOperationCallback;
import com.example.b_shop.data.repositories.OrderRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.utils.UserManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Use case that handles the checkout process
 */
public class CheckoutUseCase {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserManager userManager;
    private final ExecutorService executorService;

    private final MutableLiveData<CheckoutState> checkoutState;
    private final MutableLiveData<CheckoutResult> checkoutResult;

    public CheckoutUseCase(
        CartRepository cartRepository,
        OrderRepository orderRepository,
        ProductRepository productRepository,
        UserManager userManager
    ) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userManager = userManager;
        this.executorService = Executors.newSingleThreadExecutor();
        
        this.checkoutState = new MutableLiveData<>(CheckoutState.IDLE);
        this.checkoutResult = new MutableLiveData<>();
    }

    public LiveData<CheckoutState> getCheckoutState() {
        return checkoutState;
    }

    public LiveData<CheckoutResult> getCheckoutResult() {
        return checkoutResult;
    }

    public void checkout() {
        try {
            userManager.validateUserSession();
            
            executorService.execute(() -> {
                try {
                    checkoutState.postValue(CheckoutState.PROCESSING);
                    
                    // 1. Validate cart is not empty
                    List<CartItemWithProduct> cartItems = cartRepository.getCartItems().getValue();
                    if (cartItems == null || cartItems.isEmpty()) {
                        handleCheckoutError("Cart is empty");
                        return;
                    }

                    // 2. Validate stock availability
                    if (!validateStock(cartItems)) {
                        handleCheckoutError("Insufficient stock");
                        return;
                    }

                    // 3. Create order
                    Order order = createOrder(cartItems);
                    if (order == null) {
                        handleCheckoutError("Failed to create order");
                        return;
                    }

                    // 4. Update inventory
                    updateInventory(cartItems);

                    // 5. Clear cart with proper error handling
                    cartRepository.clearCart(new CartOperationCallback() {
                        @Override
                        public void onSuccess() {
                            // 6. Complete checkout
                            checkoutState.postValue(CheckoutState.COMPLETED);
                            checkoutResult.postValue(new CheckoutResult(true, order.getOrderId(), null));
                        }

                        @Override
                        public void onError(CartError error) {
                            // If cart clear fails, we should still consider the checkout successful
                            // since the order is created and inventory is updated
                            checkoutState.postValue(CheckoutState.COMPLETED);
                            checkoutResult.postValue(new CheckoutResult(
                                true,
                                order.getOrderId(),
                                "Order placed successfully but failed to clear cart: " + error.getDetails()
                            ));
                        }
                    });

                } catch (Exception e) {
                    handleCheckoutError(e.getMessage());
                }
            });
        } catch (IllegalStateException e) {
            handleCheckoutError("User not logged in");
        }
    }

    private boolean validateStock(List<CartItemWithProduct> cartItems) {
        for (CartItemWithProduct item : cartItems) {
            if (!cartRepository.validateStock(
                item.cartItem.getProductId(),
                item.cartItem.getQuantity()
            )) {
                return false;
            }
        }
        return true;
    }

    private Order createOrder(List<CartItemWithProduct> cartItems) {
        float total = 0f;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemWithProduct cartItem : cartItems) {
            // Create OrderItem with temporary orderId (will be updated after order creation)
            OrderItem orderItem = new OrderItem(
                -1, // temporary orderId
                cartItem.cartItem.getProductId(),
                cartItem.cartItem.getQuantity(),
                cartItem.cartItem.getItemPrice()
            );
            orderItems.add(orderItem);
            total += cartItem.getTotalPrice();
        }

        Order order = new Order(
            userManager.getCurrentUserId(),
            total
        );

        try {
            long orderId = orderRepository.createOrder(order, orderItems);
            if (orderId > 0) {
                order.setOrderId((int) orderId);
                return order;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateInventory(List<CartItemWithProduct> cartItems) {
        for (CartItemWithProduct item : cartItems) {
            productRepository.updateStock(
                item.cartItem.getProductId(),
                -item.cartItem.getQuantity()
            );
        }
    }

    private void handleCheckoutError(String message) {
        checkoutState.postValue(CheckoutState.ERROR);
        checkoutResult.postValue(new CheckoutResult(false, -1, message));
    }

    public void cleanup() {
        executorService.shutdown();
    }

    public enum CheckoutState {
        IDLE,
        PROCESSING,
        COMPLETED,
        ERROR
    }

    public static class CheckoutResult {
        private final boolean success;
        private final int orderId;
        private final String error;

        public CheckoutResult(boolean success, int orderId, String error) {
            this.success = success;
            this.orderId = orderId;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getOrderId() {
            return orderId;
        }

        public String getError() {
            return error;
        }
    }
}