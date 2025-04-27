package com.example.b_shop.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity for tracking administrative actions in the system.
 * Maintains an audit trail of all operations performed by admin users.
 */
@Entity(
    tableName = "user_audit_log",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "userId",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "userId",
            childColumns = "adminId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("userId"),
        @Index("adminId"),
        @Index("timestamp")
    }
)
public class UserAuditLog {
    @PrimaryKey(autoGenerate = true)
    private int logId;

    private int userId;      // User affected by the action
    private int adminId;     // Admin who performed the action
    private String action;   // Type of action performed
    private String details;  // Additional details about the action
    private long timestamp;  // When the action was performed

    @androidx.room.Ignore
    public UserAuditLog(int userId, int adminId, String action, String details) {
        this.userId = userId;
        this.adminId = adminId;
        this.action = action;
        this.details = details;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    // Required by Room
    public UserAuditLog() {
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    // Getters
    public int getLogId() {
        return logId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setLogId(int logId) {
        this.logId = logId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Common admin actions that can be logged
     */
    public static class Actions {
        public static final String USER_CREATED = "USER_CREATED";
        public static final String USER_UPDATED = "USER_UPDATED";
        public static final String USER_DELETED = "USER_DELETED";
        public static final String USER_BLOCKED = "USER_BLOCKED";
        public static final String USER_UNBLOCKED = "USER_UNBLOCKED";
        public static final String ROLE_CHANGED = "ROLE_CHANGED";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String SUSPICIOUS_ACTIVITY = "SUSPICIOUS_ACTIVITY";
        public static final String ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
        public static final String SYSTEM_SETTING_CHANGED = "SYSTEM_SETTING_CHANGED";
    }
}