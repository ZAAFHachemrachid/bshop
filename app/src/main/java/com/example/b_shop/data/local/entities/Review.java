package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(
    tableName = "reviews",
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
        @Index("productId"),
        @Index(value = {"userId", "productId"}, unique = true)
    }
)
public class Review {
    @PrimaryKey(autoGenerate = true)
    private int reviewId;
    private int userId;
    private int productId;
    private int rating;
    private String comment;
    private Date createdAt;

    public Review(int userId, int productId, int rating, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date();
    }

    // Getters
    public int getReviewId() {
        return reviewId;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}