// File: src/main/java/org/syos/domain/valueobject/Money.java
package org.syos.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable value object representing monetary amounts in Sri Lankan Rupees (LKR).
 * Follows Value Object pattern and provides precise arithmetic operations.
 * 
 * Design Principles:
 * - Immutability ensures thread safety
 * - Encapsulates monetary logic
 * - Uses BigDecimal for precision
 */
public final class Money {
    private static final String CURRENCY = "Rs";
    private static final int SCALE = 2;
    private final BigDecimal amount;
    
    // Factory method for zero
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    // Factory method with double (for convenience)
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    
    // Primary constructor with BigDecimal
    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    // Arithmetic operations return new Money instances (immutability)
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier));
    }
    
    public Money applyDiscount(BigDecimal discountPercentage) {
        BigDecimal discountAmount = this.amount.multiply(discountPercentage).divide(
            BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
        return new Money(this.amount.subtract(discountAmount));
    }
    
    // Comparison operations
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    // Getters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return CURRENCY;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return CURRENCY + " " + amount.toPlainString();
    }
    
    public String toDisplayString() {
        return String.format("%s %.2f", CURRENCY, amount.doubleValue());
    }
}
