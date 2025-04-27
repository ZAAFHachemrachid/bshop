package com.example.b_shop.data.local;

import android.content.Context;
import android.util.Log;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.UserRole;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private final AppDatabase database;
    private final Context context;
    private final ExecutorService executorService;

    public DatabaseInitializer(Context context, AppDatabase database) {
        this.context = context;
        this.database = database;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void initialize() {
        executorService.execute(() -> {
            try {
                initializeCategories();
                initializeProducts();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database", e);
            }
        });
    }

    public void initializeAdminUser(String email, String hashedPassword) {
        executorService.execute(() -> {
            try {
                // Check if admin user already exists
                if (database.userDao().getCountByRole(UserRole.ADMIN) == 0) {
                    User adminUser = new User();
                    adminUser.setName("Admin");
                    adminUser.setEmail(email);
                    adminUser.setPassword(hashedPassword);
                    adminUser.setRole(UserRole.ADMIN);
                    adminUser.setActive(true);

                    database.userDao().insert(adminUser);
                    Log.i(TAG, "Admin user created successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating admin user", e);
            }
        });
    }

    private void initializeCategories() {
        if (database.categoryDao().getCategoryCount() > 0) {
            return;
        }

        List<Category> categories = new ArrayList<>();
        // Add default categories
        categories.add(new Category("Electronics", "Electronics and gadgets"));
        categories.add(new Category("Fashion", "Clothing and accessories"));
        categories.add(new Category("Home", "Home and furniture"));
        categories.add(new Category("Books", "Books and media"));

        database.categoryDao().insertAll(categories);
    }

    private void initializeProducts() {
        if (database.productDao().getProductCount() > 0) {
            return;
        }

        List<Product> products = new ArrayList<>();
        // Add sample products for each category
        // Electronics
        products.add(new Product(
            "Smartphone",
            "High-end smartphone with great features",
            599.99f,
            1,  // Electronics category
            null,  // imagePath can be null
            10    // stock
        ));

        // Fashion
        products.add(new Product(
            "T-Shirt",
            "Cotton t-shirt with modern design",
            29.99f,
            2,  // Fashion category
            null,  // imagePath can be null
            20    // stock
        ));

        database.productDao().insertAll(products);
    }

    public void cleanup() {
        executorService.shutdown();
    }

    /**
     * Helper method to hash passwords consistently with the rest of the application
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify if database is properly initialized
     */
    public boolean verifyInitialization() {
        try {
            boolean hasCategories = database.categoryDao().getCategoryCount() > 0;
            boolean hasProducts = database.productDao().getProductCount() > 0;
            boolean hasAdmin = database.userDao().getCountByRole(UserRole.ADMIN) > 0;

            if (!hasCategories || !hasProducts || !hasAdmin) {
                Log.w(TAG, String.format(
                    "Database initialization incomplete - Categories: %b, Products: %b, Admin: %b",
                    hasCategories, hasProducts, hasAdmin));
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error verifying database initialization", e);
            return false;
        }
    }
}