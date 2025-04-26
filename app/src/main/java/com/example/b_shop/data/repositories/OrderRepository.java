package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.dao.OrderDao;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.OrderItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OrderRepository {
    private final OrderDao orderDao;
    private final ExecutorService executorService;

    public OrderRepository(OrderDao orderDao) {
        this.orderDao = orderDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Cart operations
    public void addToCart(int userId, int productId, int quantity, float price) {
        executorService.execute(() -> {
            orderDao.addToCart(userId, productId, quantity, price);
        });
    }

    public Future<Integer> getActiveCartId(int userId) {
        return executorService.submit(() -> orderDao.getUserActiveCartId(userId));
    }

    public LiveData<OrderDao.OrderWithDetails> getCartWithDetails(int cartId) {
        return orderDao.getOrderWithDetails(cartId);
    }

    public LiveData<List<OrderDao.OrderItemWithProduct>> getCartItems(int cartId) {
        return orderDao.getOrderItemsWithProducts(cartId);
    }

    // Order management
    public long createOrder(Order order, List<OrderItem> items) throws Exception {
        try {
            return executorService.submit(() -> {
                // Insert order first to get the orderId
                long orderId = orderDao.insertOrder(order);
                
                // Update orderId in all items
                for (OrderItem item : items) {
                    item.setOrderId((int) orderId);
                    orderDao.insertOrderItem(item);
                }
                
                return orderId;
            }).get();
        } catch (Exception e) {
            throw new Exception("Failed to create order: " + e.getMessage(), e);
        }
    }

    public void updateOrder(Order order) {
        executorService.execute(() -> {
            orderDao.updateOrder(order);
        });
    }

    public void deleteOrder(Order order) {
        executorService.execute(() -> {
            orderDao.deleteOrder(order);
        });
    }

    // Order queries
    public LiveData<Order> getOrderById(int orderId) {
        return orderDao.getOrderById(orderId);
    }

    public LiveData<OrderDao.OrderWithDetails> getOrderWithDetails(int orderId) {
        return orderDao.getOrderWithDetails(orderId);
    }

    public LiveData<List<OrderItem>> getOrderItems(int orderId) {
        return orderDao.getOrderItems(orderId);
    }

    public LiveData<List<OrderDao.OrderItemWithProduct>> getOrderItemsWithProducts(int orderId) {
        return orderDao.getOrderItemsWithProducts(orderId);
    }

    public LiveData<List<Order>> getUserOrders(int userId) {
        return orderDao.getUserOrders(userId);
    }

    public LiveData<List<Order>> getOrdersByStatus(String status) {
        return orderDao.getOrdersByStatus(status);
    }

    // Order processing
    public void updateOrderStatus(int orderId, String newStatus) {
        executorService.execute(() -> {
            Order order = orderDao.getOrderById(orderId).getValue();
            if (order != null) {
                order.setStatus(newStatus);
                orderDao.updateOrder(order);
            }
        });
    }

    public void updateOrderTotal(int orderId) {
        executorService.execute(() -> {
            orderDao.updateOrderTotal(orderId);
        });
    }

    // Checkout process
    public Future<Boolean> processCheckout(int cartId) {
        return executorService.submit(() -> {
            Order cart = orderDao.getOrderById(cartId).getValue();
            if (cart != null && "PENDING".equals(cart.getStatus())) {
                cart.setStatus("CONFIRMED");
                orderDao.updateOrder(cart);
                return true;
            }
            return false;
        });
    }

    // Cleanup
    public void cleanup() {
        executorService.shutdown();
    }
}