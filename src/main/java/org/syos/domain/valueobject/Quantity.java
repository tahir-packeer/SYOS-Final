// File: src/main/java/org/syos/domain/valueobject/Quantity.java
package org.syos.domain.valueobject;

import java.util.Objects;

/**
 * Immutable value object representing item quantity.
 */
public final class Quantity {
    private final int value;
    
    public Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.value = value;
    }
    
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
    
    public Quantity subtract(Quantity other) {
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        return new Quantity(result);
    }
    
    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }
    
    public boolean isZero() {
        return this.value == 0;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}