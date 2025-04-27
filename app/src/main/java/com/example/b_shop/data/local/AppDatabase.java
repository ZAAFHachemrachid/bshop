package com.example.b_shop.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.b_shop.data.local.converters.DateConverter;
import com.example.b_shop.data.local.converters.StringListConverter;
import com.example.b_shop.data.local.converters.UserRoleConverter;
import com.example.b_shop.data.local.dao.*;
import com.example.b_shop.data.local.entities.*;

@Database(
    entities = {
        Category.class,
        Product.class,
        User.class,
        Review.class,
        Order.class,
        OrderItem.class,
        UserFavorite.class,
        CartItem.class,
        UserAuditLog.class
    },
    version = 4,
    exportSchema = false
)
@TypeConverters({
    StringListConverter.class,
    DateConverter.class,
    UserRoleConverter.class
})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "bshop_db";
    private static volatile AppDatabase instance;

    // Abstract methods for DAOs
    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract UserDao userDao();
    public abstract ReviewDao reviewDao();
    public abstract OrderDao orderDao();
    public abstract CartDao cartDao();

    // Migration from version 3 to 4 (adding admin functionality)
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns to users table
            database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USER'");
            database.execSQL("ALTER TABLE users ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))");
            database.execSQL("ALTER TABLE users ADD COLUMN last_login INTEGER");

            // Create user_audit_log table
            database.execSQL(
                "CREATE TABLE user_audit_log (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "admin_id INTEGER NOT NULL, " +
                "action TEXT NOT NULL, " +
                "details TEXT, " +
                "timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY (user_id) REFERENCES users(userId) ON DELETE CASCADE, " +
                "FOREIGN KEY (admin_id) REFERENCES users(userId) ON DELETE CASCADE)"
            );

            // Create indices for better query performance
            database.execSQL("CREATE INDEX idx_audit_user ON user_audit_log(user_id)");
            database.execSQL("CREATE INDEX idx_audit_admin ON user_audit_log(admin_id)");
            database.execSQL("CREATE INDEX idx_audit_timestamp ON user_audit_log(timestamp)");
        }
    };

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
            .addMigrations(MIGRATION_3_4) // Add migration instead of destructive fallback
            .build();
    }

    // Helper method to clear all tables
    public void clearAllTables() {
        if (instance != null) {
            instance.clearAllTables();
        }
    }

    /**
     * Creates the initial admin user if no admin exists
     * Should be called after database creation/migration
     */
    public void ensureAdminExists(String email, String password) {
        User adminUser = new User("Admin", email, password, UserRole.ADMIN);
        userDao().insertIfNotExists(adminUser);
    }
}