package com.example.b_shop.utils.security;

import android.util.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import com.example.b_shop.data.local.entities.UserAuditLog;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;
import com.example.b_shop.utils.annotations.RequiresAdmin;

/**
 * Aspect that intercepts method calls annotated with @RequiresAdmin
 * to enforce role-based access control and handle audit logging.
 */
@Aspect
public class SecurityInterceptor {
    private static final String TAG = "SecurityInterceptor";
    private final UserManager userManager;
    private final UserRepository userRepository;
    private final AdminRateLimiter rateLimiter;

    public SecurityInterceptor(UserManager userManager, UserRepository userRepository) {
        this.userManager = userManager;
        this.userRepository = userRepository;
        this.rateLimiter = new AdminRateLimiter();
    }

    @Around("execution(@com.example.b_shop.utils.annotations.RequiresAdmin * *(..)) && @annotation(requiresAdmin)")
    public Object validateAdminAccess(ProceedingJoinPoint joinPoint, RequiresAdmin requiresAdmin) throws Throwable {
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        Log.d(TAG, "Intercepting admin method: " + methodName);

        // Check if user is logged in and has admin role
        if (!userManager.isCurrentUserAdmin()) {
            Log.w(TAG, "Unauthorized admin access attempt: " + methodName);
            throw new SecurityException("Admin privileges required");
        }

        // Validate admin session timeout
        if (!userManager.validateAdminSession()) {
            Log.w(TAG, "Admin session expired during method call: " + methodName);
            throw new SecurityException("Admin session has expired");
        }

        // Check rate limiting
        if (!rateLimiter.checkRateLimit(userManager.getCurrentUserId())) {
            Log.w(TAG, "Rate limit exceeded for admin: " + userManager.getCurrentUserEmail());
            throw new SecurityException("Rate limit exceeded for admin operations");
        }

        // Check specific permissions if required
        if (requiresAdmin.permissions().length > 0 && !validatePermissions(requiresAdmin.permissions())) {
            Log.w(TAG, "Insufficient permissions for: " + methodName);
            throw new SecurityException("Insufficient permissions for this operation");
        }

        Object result = null;
        try {
            // Execute the intercepted method
            result = joinPoint.proceed();

            // Log the admin action if auditing is enabled
            if (requiresAdmin.audit()) {
                logAdminAction(joinPoint, requiresAdmin, null);
            }

            return result;
        } catch (Exception e) {
            // Log the failure if auditing is enabled
            if (requiresAdmin.audit()) {
                logAdminAction(joinPoint, requiresAdmin, e);
            }
            throw e;
        } finally {
            // Update last activity timestamp
            userManager.updateAdminActivity();
        }
    }

    private boolean validatePermissions(String[] requiredPermissions) {
        // TODO: Implement granular permission checking when needed
        return true;
    }

    private void logAdminAction(ProceedingJoinPoint joinPoint, RequiresAdmin annotation, Exception error) {
        try {
            String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
            String description = annotation.description().isEmpty() ? methodName : annotation.description();
            
            // Build details string including method parameters if needed
            StringBuilder details = new StringBuilder(description);
            if (error != null) {
                details.append(" [FAILED: ").append(error.getMessage()).append("]");
            }

            // Create audit log entry
            UserAuditLog log = new UserAuditLog(
                -1, // No specific target user
                userManager.getCurrentUserId(),
                methodName.toUpperCase(),
                details.toString()
            );

            // Save audit log asynchronously
            userRepository.logAdminAction(
                userManager.getCurrentUserId(),
                -1,
                methodName.toUpperCase(),
                details.toString()
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to log admin action", e);
        }
    }
}