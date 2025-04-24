package com.example.b_shop.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.b_shop.data.local.converters.DateConverter;
import com.example.b_shop.data.local.dao.*;
import com.example.b_shop.data.local.entities.*;

@Database(
    entities = {
        Category.class,
        Product.class,
        User.class,
        Review.class,
        Order.class,
        OrderItem.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "bshop_db";
    private static volatile AppDatabase instance;

    // Abstract methods for DAOs
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract UserDao userDao();
    public abstract ReviewDao reviewDao();
    public abstract OrderDao orderDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static AppDatabase create(Context context) {
        return Room.databaseBuilder(
            context.getApplicationContext(),
            AppDatabase.class,
            DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build();
    }

    // Helper method to clear all tables
    public void clearAllTables() {
        if (instance != null) {
            instance.clearAllTables();
        }
    }
}