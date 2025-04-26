package com.example.b_shop.data.local.models;

import com.example.b_shop.data.local.errors.CartError;

/**
 * Represents the result of a cart operation, which can either be successful with data
 * or contain an error
 */
public class CartOperationResult<T> {
    private final T data;
    private final CartError error;
    private final boolean isSuccess;

    private CartOperationResult(T data, CartError error) {
        this.data = data;
        this.error = error;
        this.isSuccess = error == null;
    }

    public static <T> CartOperationResult<T> success(T data) {
        return new CartOperationResult<>(data, null);
    }

    public static <T> CartOperationResult<T> error(CartError error) {
        return new CartOperationResult<>(null, error);
    }

    public T getData() {
        if (!isSuccess) {
            throw new IllegalStateException("Cannot get data from error result");
        }
        return data;
    }

    public CartError getError() {
        if (isSuccess) {
            throw new IllegalStateException("Cannot get error from success result");
        }
        return error;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isError() {
        return !isSuccess;
    }

    /**
     * Execute different callbacks based on whether the operation was successful or not
     */
    public void handle(OnSuccess<T> onSuccess, OnError onError) {
        if (isSuccess) {
            onSuccess.execute(data);
        } else {
            onError.execute(error);
        }
    }

    /**
     * Transform the result to a different type if successful
     */
    public <R> CartOperationResult<R> map(ResultMapper<T, R> mapper) {
        if (isSuccess) {
            return CartOperationResult.success(mapper.map(data));
        }
        return CartOperationResult.error(error);
    }

    @FunctionalInterface
    public interface OnSuccess<T> {
        void execute(T data);
    }

    @FunctionalInterface
    public interface OnError {
        void execute(CartError error);
    }

    @FunctionalInterface
    public interface ResultMapper<T, R> {
        R map(T data);
    }

    /**
     * Helper method to wrap a potentially throwing operation in a CartOperationResult
     */
    public static <T> CartOperationResult<T> wrap(ThrowingSupplier<T> supplier) {
        try {
            return CartOperationResult.success(supplier.get());
        } catch (CartError e) {
            return CartOperationResult.error(e);
        } catch (Exception e) {
            return CartOperationResult.error(
                new CartError(CartError.CartErrorType.UNKNOWN_ERROR, 
                    "Unexpected error: " + e.getMessage(), e)
            );
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}