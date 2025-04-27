package com.example.b_shop.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.example.b_shop.data.local.entities.UserRole;

/**
 * Annotation to mark methods that require admin privileges.
 * Used in combination with the SecurityInterceptor to enforce role-based access control.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresAdmin {
    /**
     * Specific admin permissions required for the operation.
     * If empty, only basic admin role check is performed.
     */
    String[] permissions() default {};

    /**
     * Whether to log this admin action.
     * Set to false for read-only operations that don't need auditing.
     */
    boolean audit() default true;

    /**
     * Description of the admin action for audit logs.
     * Required if audit is true.
     */
    String description() default "";
}