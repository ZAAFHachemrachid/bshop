package com.example.b_shop.data.local.errors;

/**
 * Represents different types of errors that can occur during cart operations
 */
public class CartError extends Exception {
    
    private final CartErrorType type;
    private final String details;

    public CartError(CartErrorType type, String message) {
        super(message);
        this.type = type;
        this.details = message;
    }

    public CartError(CartErrorType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.details = message;
    }

    public CartErrorType getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Different types of cart errors that can occur
     */
    public enum CartErrorType {
        INSUFFICIENT_STOCK("Product is out of stock"),
        INVALID_QUANTITY("Invalid quantity specified"),
        PRODUCT_NOT_FOUND("Product not found"),
        SESSION_EXPIRED("Session has expired, please login again"),
        NETWORK_ERROR("Network connection error"),
        DATABASE_ERROR("Database error occurred"),
        UNKNOWN_ERROR("An unknown error occurred");

        private final String defaultMessage;

        CartErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    /**
     * Factory methods for creating common cart errors
     */
    public static CartError insufficientStock(int productId, int requested, int available) {
        return new CartError(
            CartErrorType.INSUFFICIENT_STOCK,
            String.format("Insufficient stock for product %d. Requested: %d, Available: %d",
                productId, requested, available)
        );
    }

    public static CartError invalidQuantity(int quantity) {
        return new CartError(
            CartErrorType.INVALID_QUANTITY,
            String.format("Invalid quantity: %d. Quantity must be greater than 0", quantity)
        );
    }

    public static CartError productNotFound(int productId) {
        return new CartError(
            CartErrorType.PRODUCT_NOT_FOUND,
            String.format("Product with id %d not found", productId)
        );
    }

    public static CartError sessionExpired() {
        return new CartError(
            CartErrorType.SESSION_EXPIRED,
            "Your session has expired. Please login again to continue."
        );
    }

    public static CartError networkError(String details, Throwable cause) {
        return new CartError(
            CartErrorType.NETWORK_ERROR,
            "Network error: " + details,
            cause
        );
    }

    public static CartError databaseError(String details, Throwable cause) {
        return new CartError(
            CartErrorType.DATABASE_ERROR,
            "Database error: " + details,
            cause
        );
    }
}