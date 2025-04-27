# Admin Implementation Documentation

## Overview
This document outlines the implementation details, security considerations, and usage guidelines for the admin functionality in the B-Shop Android application.

## Core Components

### 1. User Roles
- `UserRole` enum defines two roles: `USER` and `ADMIN`
- Role information is stored in the User entity
- Role-based access control is enforced through security interceptors

### 2. Security Components
- `SecurityUtils`: Handles password hashing, token generation, and input sanitization
- `AdminUtils`: Provides admin-specific validation and utility functions
- `AdminSessionManager`: Manages admin sessions with timeout functionality
- `SecurityInterceptor`: Enforces role-based access control using AspectJ

### 3. Authentication Flow
1. Admin users must use email addresses ending with `@bshop.com`
2. Enhanced password requirements for admin accounts
3. Two-step validation process:
   - Credential verification
   - Session validation

## Security Measures

### Password Requirements
- Minimum 8 characters
- Must contain:
  - Uppercase letters
  - Lowercase letters
  - Numbers
  - Special characters

### Session Management
- 30-minute session timeout
- Secure token generation
- Activity-based session extension
- Automatic logout on timeout
- Rate limiting for admin operations

### Access Control
- Method-level security using `@RequiresAdmin` annotation
- Role validation before each admin operation
- Audit logging for all admin actions
- IP-based access restrictions (optional)

## Database Schema Updates

### User Table
Added columns:
- `role` (TEXT): User role (USER/ADMIN)
- `is_active` (BOOLEAN): Account status
- `created_at` (INTEGER): Unix timestamp
- `last_login` (INTEGER): Last login timestamp

### UserAuditLog Table
New table for tracking admin actions:
- `log_id` (INTEGER): Primary key
- `user_id` (INTEGER): Target user
- `admin_id` (INTEGER): Acting admin
- `action` (TEXT): Action performed
- `details` (TEXT): Additional information
- `timestamp` (INTEGER): Action timestamp

## Usage Guidelines

### Creating Admin Users
```java
User adminUser = new User(name, email, password, UserRole.ADMIN);
userRepository.createAdminUser(email, name, password);
```

### Securing Admin Operations
```java
@RequiresAdmin(audit = true, description = "Block user operation")
public void blockUser(int userId) {
    // Implementation
}
```

### Handling Admin Authentication
```java
if (!userManager.isCurrentUserAdmin()) {
    AdminRequiredDialog.show(fragment, "Operation requires admin access");
    return;
}
```

### Audit Logging
```java
userRepository.logAdminAction(
    adminId,
    targetUserId,
    UserAuditLog.Actions.USER_BLOCKED,
    "User blocked for violation"
);
```

## Session Management

### Session Timeout
- Default timeout: 30 minutes
- Timer displayed in UI when admin is active
- Warning notification before timeout
- Automatic logout on timeout

### Session Extension
- Active operations reset timer
- Maximum session duration enforced
- Rate limiting prevents abuse

### Session Security
- Secure token generation using `SecurityUtils`
- Token rotation on sensitive operations
- Session invalidation on security events

## Rate Limiting

### Configuration
- Default: 30 operations per minute
- Separate limits for different operation types
- Exponential backoff for repeated violations

### Monitoring
- Rate limit status in admin dashboard
- Alerts for repeated violations
- Audit logs for blocked attempts

## Best Practices

### Code Organization
1. Use dedicated admin packages
2. Separate admin UI components
3. Centralize security logic
4. Follow consistent naming conventions

### Security Guidelines
1. Always use `@RequiresAdmin` for admin operations
2. Never bypass session validation
3. Log all sensitive operations
4. Validate all admin input
5. Use strong password requirements
6. Implement proper session management
7. Follow least privilege principle

### Error Handling
1. Clear error messages for admins
2. Generic errors for users
3. Proper exception handling
4. Audit logging for errors

## Testing

### Test Cases
1. Admin authentication
2. Session management
3. Rate limiting
4. Access control
5. Audit logging
6. Error handling

### Security Testing
1. Session timeout verification
2. Token validation
3. Input validation
4. Access control bypass attempts
5. Rate limit effectiveness

## Monitoring

### Audit Logs
- All admin actions logged
- Timestamp and admin ID recorded
- Detailed operation information
- Success/failure status

### Security Alerts
- Failed login attempts
- Rate limit violations
- Suspicious activities
- Session anomalies

### Performance Monitoring
- Session count tracking
- Operation response times
- Resource utilization
- Error rates

## Maintenance

### Regular Tasks
1. Review audit logs
2. Update security configurations
3. Monitor rate limits
4. Review access patterns
5. Update documentation

### Security Updates
1. Regular security assessments
2. Vulnerability scanning
3. Dependency updates
4. Configuration reviews

## Support

### Documentation
- Implementation details
- Security guidelines
- Troubleshooting guides
- API reference

### Tools
- Admin dashboard
- Log viewers
- Monitoring tools
- Debug utilities