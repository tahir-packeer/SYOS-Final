// File: src/main/java/org/syos/domain/enums/PaymentMethod.java
package org.syos.domain.enums;

/**
 * Represents payment methods.
 * Open for extension (new payment methods can be added).
 */
public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    PAYPAL("PayPal");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
