package com.dlms.auth.constants;

public final class AuthConstants {

    private AuthConstants() {}

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Error messages (domain-level, not HTTP-level)
    public static final String EMAIL_ALREADY_EXISTS = "Email already in use";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String ROLE_NOT_FOUND = "Default role not found";

    // public static final String SAVINGS = "SAVINGS";
    // public static final String ADDRESS = "ADDRESS";

    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "Account created successfully";

    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "Request processed successfully";

    public static final String STATUS_500 = "500";
    public static final String MESSAGE_500 = "An error occurred. Please try again or contact Dev team";

}
