// File: src/main/java/org/syos/domain/enums/TransactionType.java
package org.syos.domain.enums;

/**
 * Represents transaction types: counter or online sales.
 */
public enum TransactionType {
    COUNTER("Counter"),
    ONLINE("Online");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}