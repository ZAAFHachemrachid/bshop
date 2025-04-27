package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.b_shop.data.local.converters.UserRoleConverter;

@Entity(
    tableName = "users",
    indices = {@Index(value = {"email"}, unique = true)}
)
@TypeConverters(UserRoleConverter.class)
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;
    private String name;
    private String email;
    private String password;
    private String avatarUrl;
    private String phone;
    private String address;
    private UserRole role = UserRole.USER; // Default role
    private boolean isActive = true;       // Default active status
    private long createdAt;                // Unix timestamp
    private Long lastLogin;                // Nullable Unix timestamp

    @androidx.room.Ignore
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = System.currentTimeMillis() / 1000L;
    }

    // Constructor for creating user with specific ID (for testing/development)
    @androidx.room.Ignore
    public User(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = System.currentTimeMillis() / 1000L;
    }

    // Constructor for creating admin user
    @androidx.room.Ignore
    public User(String name, String email, String password, UserRole role) {
        this(name, email, password);
        this.role = role;
    }

    // Required by Room
    public User() {
        this.createdAt = System.currentTimeMillis() / 1000L;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis() / 1000L;
    }

    // Helper methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}