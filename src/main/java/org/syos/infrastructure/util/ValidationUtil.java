// File: src/main/java/org/syos/infrastructure/util/ValidationUtil.java
package org.syos.infrastructure.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[0-9]{10}$");
    
    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate phone number format (10 digits).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate string is not empty.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate positive number.
     */
    public static boolean isPositive(int number) {
        return number > 0;
    }
    
    /**
     * Validate non-negative number.
     */
    public static boolean isNonNegative(int number) {
        return number >= 0;
    }
}
