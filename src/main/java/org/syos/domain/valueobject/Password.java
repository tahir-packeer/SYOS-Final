// File: src/main/java/org/syos/domain/valueobject/Password.java
package org.syos.domain.valueobject;

import java.util.Objects;

/**
 * Value object encapsulating hashed password.
 */
public final class Password {
    private final String hashedValue;
    
    public Password(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.hashedValue = hashedValue;
    }
    
    public String getHashedValue() {
        return hashedValue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return hashedValue.equals(password.hashedValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
    
    @Override
    public String toString() {
        return "***HIDDEN***";
    }
}