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
    private CartRepository cartRepository;
    private UserManager userManager;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize database
        database = AppDatabase.getInstance(this);
        
        // Initialize UserManager first
        userManager = UserManager.getInstance(this);

        // Get DAOs
        CategoryDao categoryDao = database.categoryDao();
        ProductDao productDao = database.productDao();
        UserDao userDao = database.userDao();
        OrderDao orderDao = database.orderDao();
        ReviewDao reviewDao = database.reviewDao();
        CartDao cartDao = database.cartDao();

        // Initialize repositories with correct dependencies
        cartRepository = new CartRepository(cartDao, productDao, userManager);
        categoryRepository = new CategoryRepository(categoryDao);
        productRepository = new ProductRepository(productDao, cartRepository, userManager);
        userRepository = new UserRepository(userDao);
        orderRepository = new OrderRepository(orderDao);
        reviewRepository = new ReviewRepository(
            reviewDao,
            userDao,
            productDao,
            userManager,
            cartRepository  // Added CartRepository to ReviewRepository
        );

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

    public CartRepository getCartRepository() {
        return cartRepository;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Cleanup is now handled by ViewModels
    }
}