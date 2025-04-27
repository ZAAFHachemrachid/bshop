package com.example.b_shop;

import android.app.Application;
import android.util.Log;
import androidx.room.Room;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.DatabaseInitializer;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.OrderRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.data.repositories.ReviewRepository;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;
import com.example.b_shop.utils.security.SecurityInterceptor;
import java.security.SecureRandom;
import java.util.Base64;

public class BShopApplication extends Application {
    private AppDatabase database;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private CategoryRepository categoryRepository;
    private ReviewRepository reviewRepository;
    private OrderRepository orderRepository;
    private UserManager userManager;
    private SecurityInterceptor securityInterceptor;
    
    private static final String DEFAULT_ADMIN_EMAIL = "admin@bshop.com";
    private static final int GENERATED_PASSWORD_LENGTH = 12;
    private static final String TAG = "BShopApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeComponents();
        setupAdminUser();
    }

    private void initializeComponents() {
        // Initialize database
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "bshop_db")
                .addMigrations(AppDatabase.MIGRATION_3_4)
                .build();

        // Initialize repositories and managers
        // Initialize UserManager first
        userManager = UserManager.getInstance(this);

        // Initialize repositories in dependency order
        userRepository = new UserRepository(database.userDao());
        categoryRepository = new CategoryRepository(database.categoryDao());
        
        // Initialize cartRepository before productRepository since product depends on cart
        cartRepository = new CartRepository(
            database.cartDao(),
            database.productDao(),
            userManager
        );
        
        productRepository = new ProductRepository(
            database.productDao(),
            cartRepository,
            userManager
        );
        
        reviewRepository = new ReviewRepository(
            database.reviewDao(),
            database.userDao(),
            database.productDao(),
            userManager,
            cartRepository
        );
        
        orderRepository = new OrderRepository(database.orderDao());

        // Initialize security components
        securityInterceptor = new SecurityInterceptor(userManager, userRepository);
    }

    private void setupAdminUser() {
        // Run in background thread
        new Thread(() -> {
            try {
                // Check if admin user exists
                if (database.userDao().getCountByRole(UserRole.ADMIN) == 0) {
                    // Generate a secure random password
                    String adminPassword = generateSecurePassword();
                    
                    // Create admin user
                    database.ensureAdminExists(DEFAULT_ADMIN_EMAIL, adminPassword);

                    // Log the admin credentials (in production, send this securely to the system owner)
                    logAdminCredentials(DEFAULT_ADMIN_EMAIL, adminPassword);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to setup admin user", e);
            }
        }).start();
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[GENERATED_PASSWORD_LENGTH];
        random.nextBytes(bytes);
        
        // Convert to base64 and remove special characters
        String password = Base64.getEncoder().encodeToString(bytes)
                .replaceAll("[^a-zA-Z0-9]", "")
                .substring(0, GENERATED_PASSWORD_LENGTH);
        
        // Ensure password contains required character types
        password = ensurePasswordComplexity(password);
        
        return password;
    }

    private String ensurePasswordComplexity(String password) {
        // Ensure at least one uppercase, lowercase, number and special character
        StringBuilder complex = new StringBuilder(password);
        complex.setCharAt(0, Character.toUpperCase(complex.charAt(0)));
        complex.setCharAt(1, Character.toLowerCase(complex.charAt(1)));
        complex.setCharAt(2, '!');
        complex.setCharAt(3, '1');
        
        return complex.toString();
    }

    private void logAdminCredentials(String email, String password) {
        // In production, implement secure way to communicate these credentials
        // For development, just log to console
        Log.i(TAG, String.format("Initial admin credentials - Email: %s, Password: %s", 
            email, password));
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public CartRepository getCartRepository() {
        return cartRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public ReviewRepository getReviewRepository() {
        return reviewRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public SecurityInterceptor getSecurityInterceptor() {
        return securityInterceptor;
    }
}