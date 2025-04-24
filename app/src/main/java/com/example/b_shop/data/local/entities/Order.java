package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(
    tableName = "orders",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "userId",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("userId")}
)
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int orderId;
    private int userId;
    private Date orderDate;
    private String status; // PENDING, CONFIRMED, DELIVERED
    private float totalAmount;

    public Order(int userId, float totalAmount) {
        this.userId = userId;
        this.orderDate = new Date();
        this.status = "PENDING";
        this.totalAmount = totalAmount;
    }

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    // Setters
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }
}