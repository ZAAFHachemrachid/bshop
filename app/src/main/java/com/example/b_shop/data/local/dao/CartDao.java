package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.b_shop.data.local.entities.CartItem;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.relations.CartItemWithProduct;

import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    LiveData<List<CartItem>> getCartItems(int userId);

    @Transaction
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    LiveData<List<CartItemWithProduct>> getCartItemsWithProduct(int userId);

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    LiveData<Integer> getCartItemCount(int userId);

    @Query("SELECT SUM(quantity * itemPrice) FROM cart_items WHERE userId = :userId")
    LiveData<Float> getCartTotal(int userId);

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    CartItem getCartItemByProduct(int userId, int productId);

    @Query("SELECT * FROM cart_items WHERE cartItemId = :cartItemId LIMIT 1")
    CartItem getCartItemById(int cartItemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCartItem(CartItem item);

    @Update
    void updateCartItem(CartItem item);

    @Delete
    void deleteCartItem(CartItem item);

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    void clearCart(int userId);

    @Query("DELETE FROM cart_items WHERE cartItemId = :cartItemId AND userId = :userId")
    void deleteCartItemById(int cartItemId, int userId);

    @Query("UPDATE cart_items SET quantity = :quantity WHERE cartItemId = :cartItemId AND userId = :userId")
    void updateQuantity(int cartItemId, int userId, int quantity);

    // Transaction to check if adding to cart would exceed available stock
    @Transaction
    default boolean validateAndAddToCart(CartItem cartItem, Product product) {
        android.util.Log.d("CartDao", "Starting validateAndAddToCart transaction");
        
        // Get existing cart item if any
        CartItem existingItem = getCartItemByProduct(cartItem.getUserId(), cartItem.getProductId());
        int totalQuantity = cartItem.getQuantity();
        
        if (existingItem != null) {
            android.util.Log.d("CartDao", "Found existing cart item, current quantity=" + existingItem.getQuantity());
            totalQuantity += existingItem.getQuantity();
        } else {
            android.util.Log.d("CartDao", "No existing cart item found");
        }

        android.util.Log.d("CartDao", "Validating stock: requested=" + totalQuantity + ", available=" + product.getStock());
        
        // Check if total quantity would exceed available stock
        if (totalQuantity > product.getStock()) {
            android.util.Log.w("CartDao", "Stock validation failed: total quantity would exceed available stock");
            return false;
        }

        // If validation passes, insert or update the cart item
        if (existingItem != null) {
            existingItem.setQuantity(totalQuantity);
            android.util.Log.d("CartDao", "Updating existing cart item to quantity=" + totalQuantity);
            updateCartItem(existingItem);
        } else {
            android.util.Log.d("CartDao", "Inserting new cart item with quantity=" + cartItem.getQuantity());
            long insertedId = insertCartItem(cartItem);
            android.util.Log.d("CartDao", "New cart item inserted with ID=" + insertedId);
        }
        
        android.util.Log.d("CartDao", "Transaction completed successfully");
        return true;
    }
}