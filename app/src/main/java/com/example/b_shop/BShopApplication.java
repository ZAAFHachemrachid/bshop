package com.example.b_shop;

import android.app.Application;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.DatabaseInitializer;
import com.example.b_shop.data.repositories.*;
import com.example.b_shop.data.local.dao.*;
import com.example.b_shop.utils.UserManager;

public class BShopApplication extends Application {
    private AppDatabase database;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private ReviewRepository reviewRepository;
    private UserManager userManager;

    private static final int DEFAULT_USER_ID = 1;
    private static final String DEFAULT_USER_EMAIL = "test@example.com";
    private static final String DEFAULT_USER_NAME = "Test User";

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

        // Initialize UserManager and set default user for development
        userManager = UserManager.getInstance(this);
        if (!userManager.isUserLoggedIn()) {
            userManager.loginUser(DEFAULT_USER_ID, DEFAULT_USER_EMAIL, DEFAULT_USER_NAME);
        }

        // Initialize repositories with DAOs
        categoryRepository = new CategoryRepository(categoryDao);
        productRepository = new ProductRepository(productDao, userDao);
        userRepository = new UserRepository(userDao);
        orderRepository = new OrderRepository(orderDao);
        reviewRepository = new ReviewRepository(reviewDao, userDao, productDao);

        // Initialize database with sample data
        DatabaseInitializer.populateAsync(this);
    }

    // Getter for database instance
    public AppDatabase getDatabase() {
        return database;
    }

    // Getter for UserManager instance
    public UserManager getUserManager() {
        return userManager;
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