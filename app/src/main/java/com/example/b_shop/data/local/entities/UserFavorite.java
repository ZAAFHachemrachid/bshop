package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "user_favorites",
    primaryKeys = {"userId", "productId"},
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
public class UserFavorite {
    private int userId;
    private int productId;

    public UserFavorite(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}