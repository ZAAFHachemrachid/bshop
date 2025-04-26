package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "cart_items",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "userId",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Product.class,
            parentColumns = "productId",
            childColumns = "productId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("userId"),
        @Index("productId")
    }
)
public class CartItem {
    @PrimaryKey(autoGenerate = true)
    private int cartItemId;
    
    private int userId;
    private int productId;
    private int quantity;
    private float itemPrice;
    private long addedAt;  // Timestamp in milliseconds

    public CartItem(int userId, int productId, int quantity, float itemPrice) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.addedAt = System.currentTimeMillis();
    }

    // Getters
    public int getCartItemId() {
        return cartItemId;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getItemPrice() {
        return itemPrice;
    }

    public long getAddedAt() {
        return addedAt;
    }

    // Setters
    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setItemPrice(float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    // Helper method to calculate total price for this item
    public float getTotalPrice() {
        return quantity * itemPrice;
    }
}