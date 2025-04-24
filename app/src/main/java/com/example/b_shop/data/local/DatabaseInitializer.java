package com.example.b_shop.data.local;

import android.content.Context;
import android.os.AsyncTask;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import java.util.HashMap;
import java.util.Map;

public class DatabaseInitializer {
    private static final Map<String, String> CATEGORIES = new HashMap<String, String>() {{
        put("Face Products", "Essential cosmetics for creating a flawless base");
        put("Eye Products", "Everything you need for stunning eye makeup");
        put("Lip Products", "Products for beautiful and lasting lip color");
        put("Skin Care", "Maintain healthy and glowing skin");
        put("Tools & Accessories", "Professional makeup application tools");
    }};

    private static final Map<String, Product[]> PRODUCTS = new HashMap<String, Product[]>() {{
        put("Face Products", new Product[]{
            new Product("Foundation", "Long-lasting liquid foundation", 29.99f, 1, "", 50),
            new Product("Concealer", "Full coverage concealer", 19.99f, 1, "", 50),
            new Product("Face Primer", "Smoothing face primer", 24.99f, 1, "", 40),
            new Product("Setting Powder", "Translucent setting powder", 22.99f, 1, "", 45),
            new Product("Blush", "Natural flush powder blush", 18.99f, 1, "", 35)
        });
        put("Eye Products", new Product[]{
            new Product("Eyeshadow Palette", "12-color neutral palette", 39.99f, 2, "", 30),
            new Product("Liquid Eyeliner", "Waterproof black eyeliner", 15.99f, 2, "", 60),
            new Product("Mascara", "Volumizing mascara", 21.99f, 2, "", 55),
            new Product("Eyebrow Pencil", "Precision brow pencil", 14.99f, 2, "", 45),
            new Product("False Eyelashes", "Natural-look lashes", 12.99f, 2, "", 40)
        });
        put("Lip Products", new Product[]{
            new Product("Matte Lipstick", "Long-wearing matte lipstick", 19.99f, 3, "", 40),
            new Product("Lip Gloss", "High-shine lip gloss", 16.99f, 3, "", 45),
            new Product("Lip Liner", "Creamy lip liner", 12.99f, 3, "", 50),
            new Product("Lip Balm", "Moisturizing lip balm", 8.99f, 3, "", 60),
            new Product("Lip Plumper", "Volumizing lip plumper", 22.99f, 3, "", 35)
        });
        put("Skin Care", new Product[]{
            new Product("Moisturizer", "Hydrating face moisturizer", 27.99f, 4, "", 40),
            new Product("Face Serum", "Anti-aging serum", 34.99f, 4, "", 35),
            new Product("Face Mask", "Purifying clay mask", 23.99f, 4, "", 45),
            new Product("Cleanser", "Gentle face cleanser", 19.99f, 4, "", 50),
            new Product("Sunscreen", "SPF 50 face sunscreen", 25.99f, 4, "", 40)
        });
        put("Tools & Accessories", new Product[]{
            new Product("Makeup Brush Set", "10-piece brush set", 49.99f, 5, "", 30),
            new Product("Beauty Sponge", "Latex-free makeup sponge", 9.99f, 5, "", 60),
            new Product("Eyelash Curler", "Professional eyelash curler", 15.99f, 5, "", 40),
            new Product("Brush Cleaner", "Makeup brush cleaning solution", 12.99f, 5, "", 45),
            new Product("Makeup Bag", "Travel makeup organizer", 24.99f, 5, "", 35)
        });
    }};

    public static void populateAsync(Context context) {
        AsyncTask.execute(() -> {
            populate(context);
        });
    }

    private static void populate(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        // Check if database is already populated
        if (db.categoryDao().getCategoryCount() > 0) {
            return;
        }

        // Add categories
        for (Map.Entry<String, String> entry : CATEGORIES.entrySet()) {
            Category category = new Category(entry.getKey(), entry.getValue(), "");
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
    }
}