package com.example.b_shop.utils.security;

import android.util.LruCache;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Rate limiter for admin operations to prevent abuse.
 * Implements a sliding window counter using LRU cache.
 */
public class AdminRateLimiter {
    private static final int MAX_ACTIONS_PER_MINUTE = 30;
    private static final int MAX_CACHE_SIZE = 100; // Maximum number of admin users to track
    private static final long WINDOW_MINUTES = 1;

    private final LruCache<Integer, RateWindow> adminCounters;

    public AdminRateLimiter() {
        this.adminCounters = new LruCache<Integer, RateWindow>(MAX_CACHE_SIZE) {
            @Override
            protected RateWindow create(Integer adminId) {
                return new RateWindow();
            }
        };
    }

    /**
     * Checks if the admin user has exceeded their rate limit.
     * @param adminId The ID of the admin user
     * @return true if the action is allowed, false if rate limit exceeded
     */
    public synchronized boolean checkRateLimit(int adminId) {
        RateWindow window = adminCounters.get(adminId);
        
        // Reset window if it has expired
        if (window.isExpired()) {
            window.reset();
        }

        // Check if limit is exceeded
        if (window.getCount() >= MAX_ACTIONS_PER_MINUTE) {
            return false;
        }

        // Increment counter and allow action
        window.increment();
        return true;
    }

    /**
     * Represents a sliding time window for rate limiting.
     */
    private static class RateWindow {
        private LocalDateTime windowStart;
        private AtomicInteger counter;

        public RateWindow() {
            reset();
        }

        public void reset() {
            windowStart = LocalDateTime.now();
            counter = new AtomicInteger(0);
        }

        public boolean isExpired() {
            return ChronoUnit.MINUTES.between(windowStart, LocalDateTime.now()) >= WINDOW_MINUTES;
        }

        public int getCount() {
            return counter.get();
        }

        public void increment() {
            counter.incrementAndGet();
        }
    }

    /**
     * Clears all rate limiting data.
     * Useful for testing or manual reset.
     */
    public void reset() {
        adminCounters.evictAll();
    }

    /**
     * Gets the remaining number of actions allowed for an admin user.
     * @param adminId The ID of the admin user
     * @return The number of remaining actions allowed in the current window
     */
    public int getRemainingActions(int adminId) {
        RateWindow window = adminCounters.get(adminId);
        if (window.isExpired()) {
            window.reset();
            return MAX_ACTIONS_PER_MINUTE;
        }
        return Math.max(0, MAX_ACTIONS_PER_MINUTE - window.getCount());
    }

    /**
     * Gets the time in milliseconds until the rate limit window resets.
     * @param adminId The ID of the admin user
     * @return Time in milliseconds until reset, or 0 if already reset
     */
    public long getTimeUntilReset(int adminId) {
        RateWindow window = adminCounters.get(adminId);
        if (window.isExpired()) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime = window.windowStart.plusMinutes(WINDOW_MINUTES);
        return ChronoUnit.MILLIS.between(now, resetTime);
    }
}