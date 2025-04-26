package com.example.b_shop.data.local;

import android.content.Context;
import android.os.AsyncTask;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.entities.User;
import java.util.HashMap;
import java.util.Map;

public class DatabaseInitializer {
    // Default user credentials
    private static final int DEFAULT_USER_ID = 1;
    private static final String DEFAULT_USER_EMAIL = "test@example.com";
    private static final String DEFAULT_USER_NAME = "Test User";
    private static final String DEFAULT_USER_PASSWORD = "password123"; // For development only

    private static final Map<String, String> CATEGORIES = new HashMap<String, String>() {{
        put("Face Products", "Essential cosmetics for creating a flawless base");
        put("Eye Products", "Everything you need for stunning eye makeup");
        put("Lip Products", "Products for beautiful and lasting lip color");
        put("Skin Care", "Maintain healthy and glowing skin");
        put("Tools & Accessories", "Professional makeup application tools");
    }};

    private static final Map<String, Product[]> PRODUCTS = new HashMap<String, Product[]>() {{
        put("Face Products", new Product[]{
            new Product("Foundation", "Long-lasting liquid foundation", 29.99f, 1, null, 50),
            new Product("Concealer", "Full coverage concealer", 19.99f, 1, null, 50),
            new Product("Face Primer", "Smoothing face primer", 24.99f, 1, null, 40),
            new Product("Setting Powder", "Translucent setting powder", 22.99f, 1, null, 45),
            new Product("Blush", "Natural flush powder blush", 18.99f, 1, null, 35)
        });
        put("Eye Products", new Product[]{
            new Product("Eyeshadow Palette", "12-color neutral palette", 39.99f, 2, null, 30),
            new Product("Liquid Eyeliner", "Waterproof black eyeliner", 15.99f, 2, null, 60),
            new Product("Mascara", "Volumizing mascara", 21.99f, 2, null, 55),
            new Product("Eyebrow Pencil", "Precision brow pencil", 14.99f, 2, null, 45),
            new Product("False Eyelashes", "Natural-look lashes", 12.99f, 2, null, 40)
        });
        put("Lip Products", new Product[]{
            new Product("Matte Lipstick", "Long-wearing matte lipstick", 19.99f, 3, null, 40),
            new Product("Lip Gloss", "High-shine lip gloss", 16.99f, 3, null, 45),
            new Product("Lip Liner", "Creamy lip liner", 12.99f, 3, null, 50),
            new Product("Lip Balm", "Moisturizing lip balm", 8.99f, 3, null, 60),
            new Product("Lip Plumper", "Volumizing lip plumper", 22.99f, 3, null, 35)
        });
        put("Skin Care", new Product[]{
            new Product("Moisturizer", "Hydrating face moisturizer", 27.99f, 4, null, 40),
            new Product("Face Serum", "Anti-aging serum", 34.99f, 4, null, 35),
            new Product("Face Mask", "Purifying clay mask", 23.99f, 4, null, 45),
            new Product("Cleanser", "Gentle face cleanser", 19.99f, 4, null, 50),
            new Product("Sunscreen", "SPF 50 face sunscreen", 25.99f, 4, null, 40)
        });
        put("Tools & Accessories", new Product[]{
            new Product("Makeup Brush Set", "10-piece brush set", 49.99f, 5, null, 30),
            new Product("Beauty Sponge", "Latex-free makeup sponge", 9.99f, 5, null, 60),
            new Product("Eyelash Curler", "Professional eyelash curler", 15.99f, 5, null, 40),
            new Product("Brush Cleaner", "Makeup brush cleaning solution", 12.99f, 5, null, 45),
            new Product("Makeup Bag", "Travel makeup organizer", 24.99f, 5, null, 35)
        });
    }};

    public static void populateAsync(Context context) {
        AsyncTask.execute(() -> {
            populate(context);
        });
    }

    private static void populate(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        try {
            // Create default user if it doesn't exist
            if (db.userDao().getUserByIdSync(DEFAULT_USER_ID) == null) {
                User defaultUser = new User(DEFAULT_USER_ID, DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
                db.userDao().insert(defaultUser);
            }

            // Check if products are already populated
            if (db.categoryDao().getCategoryCount() > 0) {
                return;
            }

            // Add categories
            for (Map.Entry<String, String> entry : CATEGORIES.entrySet()) {
                Category category = new Category(entry.getKey(), entry.getValue(), null);
                int categoryId = (int) db.categoryDao().insert(category);

                // Add products for this category
                Product[] products = PRODUCTS.get(entry.getKey());
                if (products != null) {
                    for (Product product : products) {
                        product.setCategoryId(categoryId);
                        db.productDao().insert(product);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log error but don't crash the app
        }
    }
}