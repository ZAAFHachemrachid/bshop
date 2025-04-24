package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.OrderItem;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insertOrder(Order order);

    @Insert
    long insertOrderItem(OrderItem orderItem);

    @Update
    void updateOrder(Order order);

    @Delete
    void deleteOrder(Order order);

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    LiveData<Order> getOrderById(int orderId);

    @Transaction
    @Query("SELECT o.*, " +
           "u.name as userName, " +
           "(SELECT COUNT(*) FROM order_items WHERE orderId = o.orderId) as itemCount " +
           "FROM orders o " +
           "INNER JOIN users u ON o.userId = u.userId " +
           "WHERE o.orderId = :orderId")
    LiveData<OrderWithDetails> getOrderWithDetails(int orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    LiveData<List<OrderItem>> getOrderItems(int orderId);

    @Transaction
    @Query("SELECT oi.*, p.name as productName, p.imagePath as productImage " +
           "FROM order_items oi " +
           "INNER JOIN products p ON oi.productId = p.productId " +
           "WHERE oi.orderId = :orderId")
    LiveData<List<OrderItemWithProduct>> getOrderItemsWithProducts(int orderId);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    LiveData<List<Order>> getUserOrders(int userId);

    @Query("SELECT * FROM orders WHERE status = :status")
    LiveData<List<Order>> getOrdersByStatus(String status);

    // Cart operations
    @Query("SELECT o.orderId FROM orders o " +
           "WHERE o.userId = :userId AND o.status = 'PENDING' " +
           "ORDER BY orderDate DESC LIMIT 1")
    Integer getUserActiveCartId(int userId);

    @Transaction
    default void addToCart(int userId, int productId, int quantity, float price) {
        Integer cartId = getUserActiveCartId(userId);
        if (cartId == null) {
            // Create new cart (order with PENDING status)
            Order newCart = new Order(userId, 0);
            cartId = (int) insertOrder(newCart);
        }
        // Add item to cart
        OrderItem cartItem = new OrderItem(cartId, productId, quantity, price);
        insertOrderItem(cartItem);
        // Update order total
        updateOrderTotal(cartId);
    }

    @Query("UPDATE orders " +
           "SET totalAmount = (SELECT SUM(quantity * priceAtTime) " +
           "                   FROM order_items " +
           "                   WHERE orderId = :orderId) " +
           "WHERE orderId = :orderId")
    void updateOrderTotal(int orderId);

    // Static classes for complex queries
    static class OrderWithDetails {
        @Embedded
        public Order order;
        public String userName;
        public int itemCount;
    }

    static class OrderItemWithProduct {
        @Embedded
        public OrderItem orderItem;
        public String productName;
        public String productImage;
    }
}