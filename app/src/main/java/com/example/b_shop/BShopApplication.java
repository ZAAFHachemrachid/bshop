package com.example.b_shop;

import android.app.Application;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.DatabaseInitializer;
import com.example.b_shop.data.repositories.*;

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
        
        // Initialize repositories
        categoryRepository = new CategoryRepository(this);
        productRepository = new ProductRepository(this);
        userRepository = new UserRepository(this);
        orderRepository = new OrderRepository(this);
        reviewRepository = new ReviewRepository(this);

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
        
        // Cleanup repositories
        if (categoryRepository != null) categoryRepository.cleanup();
        if (productRepository != null) productRepository.cleanup();
        if (userRepository != null) userRepository.cleanup();
        if (orderRepository != null) orderRepository.cleanup();
        if (reviewRepository != null) reviewRepository.cleanup();
    }
}