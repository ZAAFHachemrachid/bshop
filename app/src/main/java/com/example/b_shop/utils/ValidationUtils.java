package com.example.b_shop.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_PHONE_LENGTH = 8;
    private static final int MAX_PHONE_LENGTH = 15;

    public static class ValidationResult {
        private final boolean valid;
        private final String error;

        private ValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.error = error;
        }

        public boolean isValid() {
            return valid;
        }

        public String getError() {
            return error;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return ValidationResult.error("Email is required");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult.error("Invalid email format");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return ValidationResult.error("Password is required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!containsLetterAndDigit(password)) {
            return ValidationResult.error("Password must contain both letters and numbers");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return ValidationResult.error("Please confirm your password");
        }
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("Passwords do not match");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return ValidationResult.error("Name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return ValidationResult.error("Name is too long (maximum " + MAX_NAME_LENGTH + " characters)");
        }
        if (!name.matches("^[a-zA-Z\\s'-]+$")) {
            return ValidationResult.error("Name contains invalid characters");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return ValidationResult.error("Phone number is required");
        }
        if (phone.length() < MIN_PHONE_LENGTH || phone.length() > MAX_PHONE_LENGTH) {
            return ValidationResult.error("Invalid phone number length");
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            return ValidationResult.error("Invalid phone number format");
        }
        return ValidationResult.success();
    }

    private static boolean containsLetterAndDigit(String str) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }

    public static String formatPhoneNumber(String phone) {
        // Remove all non-digit characters
        String digits = phone.replaceAll("\\D+", "");
        
        // Format based on length
        if (digits.length() >= 10) {
            return String.format("+%s %s %s %s",
                    digits.substring(0, 2),
                    digits.substring(2, 5),
                    digits.substring(5, 8),
                    digits.substring(8));
        }
        return phone;
    }
}