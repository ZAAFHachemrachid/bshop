package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "order_items",
    foreignKeys = {
        @ForeignKey(
            entity = Order.class,
            parentColumns = "orderId",
            childColumns = "orderId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Product.class,
            parentColumns = "productId",
            childColumns = "productId",
            onDelete = ForeignKey.NO_ACTION
        )
    },
    indices = {
        @Index("orderId"),
        @Index("productId")
    }
)
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private float priceAtTime; // Price when order was placed

    public OrderItem(int orderId, int productId, int quantity, float priceAtTime) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
    }

    // Getters
    public int getOrderItemId() {
        return orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPriceAtTime() {
        return priceAtTime;
    }

    // Setters
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPriceAtTime(float priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    // Helper method to calculate total price for this item
    public float getTotalPrice() {
        return quantity * priceAtTime;
    }
}