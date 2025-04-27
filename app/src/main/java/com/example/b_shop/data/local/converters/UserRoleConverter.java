package com.example.b_shop.data.local.converters;

import androidx.room.TypeConverter;
import com.example.b_shop.data.local.entities.UserRole;

/**
 * Room type converter for UserRole enum.
 * Handles conversion between UserRole enum and its String representation for database storage.
 */
public class UserRoleConverter {
    
    @TypeConverter
    public static String fromUserRole(UserRole role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public static UserRole toUserRole(String role) {
        return role == null ? null : UserRole.valueOf(role);
    }
}