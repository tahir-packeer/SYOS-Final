// File: src/main/java/org/syos/domain/valueobject/Password.java
package org.syos.domain.valueobject;

import java.util.Objects;

/**
 * Simple password value object with plain text storage.
 */
public final class Password {
    private final String value;

    public Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    // Keep this for backward compatibility
    public String getHashedValue() {
        return value;
    }

    public boolean matches(String plainTextPassword) {
        return this.value.equals(plainTextPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Password password = (Password) o;
        return value.equals(password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "***HIDDEN***";
    }
}
