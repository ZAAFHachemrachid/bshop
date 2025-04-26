package com.example.b_shop;

import android.app.Application;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.DatabaseInitializer;
import com.example.b_shop.data.repositories.*;
import com.example.b_shop.data.local.dao.*;

public class BShopApplication extends Application {
    private AppDatabase database;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private ReviewRepository reviewRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize database
        database = AppDatabase.getInstance(this);
        
        // Get DAOs
        CategoryDao categoryDao = database.categoryDao();
        ProductDao productDao = database.productDao();
        UserDao userDao = database.userDao();
        OrderDao orderDao = database.orderDao();
        ReviewDao reviewDao = database.reviewDao();

        // Initialize repositories with DAOs
        categoryRepository = new CategoryRepository(categoryDao);
        productRepository = new ProductRepository(productDao, userDao);
        userRepository = new UserRepository(userDao);
        orderRepository = new OrderRepository(orderDao);
        reviewRepository = new ReviewRepository(reviewDao, userDao, productDao);

        // Initialize database with sample data
        DatabaseInitializer.populateAsync(this);
    }

    // Repository getters
    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public ReviewRepository getReviewRepository() {
        return reviewRepository;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Cleanup is now handled by ViewModels
    }
}