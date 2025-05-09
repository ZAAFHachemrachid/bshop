package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.b_shop.data.local.converters.StringListConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private String reviewerName;
    private String reviewerAvatarUrl;
    
    @TypeConverters(StringListConverter.class)
    private List<String> images;

    public Review(int userId, int productId, int rating, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date();
        this.images = new ArrayList<>();
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

    public String getContent() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewerAvatarUrl() {
        return reviewerAvatarUrl;
    }

    public List<String> getImages() {
        return images != null ? images : new ArrayList<>();
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

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setReviewerAvatarUrl(String reviewerAvatarUrl) {
        this.reviewerAvatarUrl = reviewerAvatarUrl;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void addImage(String imagePath) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imagePath);
    }
}