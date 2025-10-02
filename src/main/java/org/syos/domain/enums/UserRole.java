// File: src/main/java/org/syos/domain/enums/UserRole.java
package org.syos.domain.enums;

/**
 * Represents user roles in the system.
 * Follows Single Responsibility Principle.
 */
public enum UserRole {
    CASHIER("Cashier"),
    MANAGER("Manager"),
    ADMIN("Admin"),
    ONLINE_CUSTOMER("Online Customer");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
