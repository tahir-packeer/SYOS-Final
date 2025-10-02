// File: src/main/java/org/syos/domain/valueobject/ItemCode.java
package org.syos.domain.valueobject;

import java.util.Objects;

/**
 * Immutable value object representing unique item code.
 */
public final class ItemCode {
    private final String code;
    
    public ItemCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        this.code = code.trim().toUpperCase();
    }
    
    public String getCode() {
        return code;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCode itemCode = (ItemCode) o;
        return code.equals(itemCode.code);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
