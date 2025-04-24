package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "products",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "categoryId",
        childColumns = "categoryId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("categoryId")}
)
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int productId;
    private String name;
    private String description;
    private float price;
    private int categoryId;
    private String imagePath;
    private int stock;
    private float rating;

    public Product(String name, String description, float price, 
                  int categoryId, String imagePath, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.imagePath = imagePath;
        this.stock = stock;
        this.rating = 0.0f; // Default rating
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getStock() {
        return stock;
    }

    public float getRating() {
        return rating;
    }

    // Setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}