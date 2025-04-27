package com.example.b_shop.utils;

import android.util.Base64;
import androidx.annotation.NonNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for security-related operations.
 * Handles password generation, hashing, and security validations.
 */
public class SecurityUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int PASSWORD_LENGTH = 12;
    private static final long TOKEN_VALIDITY_HOURS = 24;
    private static final int SALT_LENGTH = 16;

    // Characters for password generation
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;

    /**
     * Generates a secure random password meeting complexity requirements
     */
    @NonNull
    public static String generateSecurePassword() {
        char[] password = new char[PASSWORD_LENGTH];
        
        // Ensure at least one of each required character type
        password[0] = LOWER.charAt(SECURE_RANDOM.nextInt(LOWER.length()));
        password[1] = UPPER.charAt(SECURE_RANDOM.nextInt(UPPER.length()));
        password[2] = DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length()));
        password[3] = SPECIAL.charAt(SECURE_RANDOM.nextInt(SPECIAL.length()));

        // Fill remaining characters randomly
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password[i] = ALL.charAt(SECURE_RANDOM.nextInt(ALL.length()));
        }

        // Shuffle the password
        for (int i = password.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        return new String(password);
    }

    /**
     * Hashes a password with salt using SHA-256
     */
    @NonNull
    public static String hashPassword(@NonNull String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        byte[] hash = hashWithSalt(password, salt);
        
        // Combine salt and hash
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    /**
     * Verifies a password against its hash
     */
    public static boolean verifyPassword(@NonNull String password, @NonNull String hashedPassword) {
        try {
            byte[] combined = Base64.decode(hashedPassword, Base64.NO_WRAP);
            
            // Extract salt and hash
            byte[] salt = Arrays.copyOfRange(combined, 0, SALT_LENGTH);
            byte[] hash = Arrays.copyOfRange(combined, SALT_LENGTH, combined.length);
            
            // Hash the input password with extracted salt
            byte[] newHash = hashWithSalt(password, salt);
            
            // Compare hashes
            return MessageDigest.isEqual(hash, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a secure session token
     */
    @NonNull
    public static String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.encodeToString(tokenBytes, Base64.NO_WRAP);
    }

    /**
     * Checks if a session token has expired
     */
    public static boolean isTokenExpired(long tokenTimestamp) {
        long currentTime = System.currentTimeMillis();
        long tokenAge = currentTime - tokenTimestamp;
        return tokenAge > TimeUnit.HOURS.toMillis(TOKEN_VALIDITY_HOURS);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(password.getBytes());
    }

    /**
     * Sanitizes input to prevent SQL injection and XSS
     */
    @NonNull
    public static String sanitizeInput(@NonNull String input) {
        return input.replaceAll("[<>\"';]", "");
    }

    /**
     * Validates token format
     */
    public static boolean isValidTokenFormat(@NonNull String token) {
        try {
            byte[] decoded = Base64.decode(token, Base64.NO_WRAP);
            return decoded.length == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}