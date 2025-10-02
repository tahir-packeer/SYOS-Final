package org.syos.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with BigDecimal amount")
    void shouldCreateMoneyWithBigDecimalAmount() {
        // Given
        BigDecimal amount = new BigDecimal("10.99");

        // When
        Money money = new Money(amount);

        // Then
        assertThat(money.getAmount()).isEqualTo(new BigDecimal("10.99"));
        assertThat(money.getCurrency()).isEqualTo("Rs");
    }

    @Test
    @DisplayName("Should create Money using factory method with double")
    void shouldCreateMoneyUsingFactoryMethodWithDouble() {
        // When
        Money money = Money.of(10.99);

        // Then
        assertThat(money.getAmount()).isEqualTo(new BigDecimal("10.99"));
    }

    @Test
    @DisplayName("Should create zero Money using factory method")
    void shouldCreateZeroMoneyUsingFactoryMethod() {
        // When
        Money money = Money.zero();

        // Then
        assertThat(money.getAmount()).isEqualTo(new BigDecimal("0.00"));
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for null amount")
    void shouldThrowExceptionForNullAmount() {
        // When & Then
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    @DisplayName("Should round amount to two decimal places")
    void shouldRoundAmountToTwoDecimalPlaces() {
        // Given
        BigDecimal amount = new BigDecimal("10.999");

        // When
        Money money = new Money(amount);

        // Then
        assertThat(money.getAmount()).isEqualTo(new BigDecimal("11.00"));
    }

    @Test
    @DisplayName("Should add two Money instances")
    void shouldAddTwoMoneyInstances() {
        // Given
        Money money1 = Money.of(10.00);
        Money money2 = Money.of(5.50);

        // When
        Money result = money1.add(money2);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("15.50"));
        assertThat(money1.getAmount()).isEqualTo(new BigDecimal("10.00")); // Original unchanged
        assertThat(money2.getAmount()).isEqualTo(new BigDecimal("5.50")); // Original unchanged
    }

    @Test
    @DisplayName("Should subtract two Money instances")
    void shouldSubtractTwoMoneyInstances() {
        // Given
        Money money1 = Money.of(15.50);
        Money money2 = Money.of(5.25);

        // When
        Money result = money1.subtract(money2);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.25"));
    }

    @Test
    @DisplayName("Should multiply Money by integer quantity")
    void shouldMultiplyMoneyByIntegerQuantity() {
        // Given
        Money money = Money.of(10.50);

        // When
        Money result = money.multiply(3);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("31.50"));
    }

    @Test
    @DisplayName("Should multiply Money by BigDecimal multiplier")
    void shouldMultiplyMoneyByBigDecimalMultiplier() {
        // Given
        Money money = Money.of(100.00);
        BigDecimal multiplier = new BigDecimal("1.25");

        // When
        Money result = money.multiply(multiplier);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Should apply discount percentage")
    void shouldApplyDiscountPercentage() {
        // Given
        Money money = Money.of(100.00);
        BigDecimal discountPercentage = new BigDecimal("20");

        // When
        Money discountedMoney = money.applyDiscount(discountPercentage);

        // Then
        assertThat(discountedMoney.getAmount()).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("Should apply zero discount")
    void shouldApplyZeroDiscount() {
        // Given
        Money money = Money.of(50.00);
        BigDecimal discountPercentage = BigDecimal.ZERO;

        // When
        Money discountedMoney = money.applyDiscount(discountPercentage);

        // Then
        assertThat(discountedMoney.getAmount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should check if Money is greater than another")
    void shouldCheckIfMoneyIsGreaterThanAnother() {
        // Given
        Money money1 = Money.of(15.00);
        Money money2 = Money.of(10.00);

        // Then
        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
    }

    @Test
    @DisplayName("Should check if Money is less than another")
    void shouldCheckIfMoneyIsLessThanAnother() {
        // Given
        Money money1 = Money.of(10.00);
        Money money2 = Money.of(15.00);

        // Then
        assertThat(money1.isLessThan(money2)).isTrue();
        assertThat(money2.isLessThan(money1)).isFalse();
    }

    @Test
    @DisplayName("Should check if Money is zero")
    void shouldCheckIfMoneyIsZero() {
        // Given
        Money zeroMoney = Money.zero();
        Money nonZeroMoney = Money.of(1.00);

        // Then
        assertThat(zeroMoney.isZero()).isTrue();
        assertThat(nonZeroMoney.isZero()).isFalse();
    }

    @Test
    @DisplayName("Should check if Money is negative")
    void shouldCheckIfMoneyIsNegative() {
        // Given
        Money positiveMoney = Money.of(10.00);
        Money negativeMoney = Money.of(-5.00);

        // Then
        assertThat(positiveMoney.isNegative()).isFalse();
        assertThat(negativeMoney.isNegative()).isTrue();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Money money1 = Money.of(10.50);
        Money money2 = Money.of(10.50);
        Money money3 = Money.of(15.00);

        // Then
        assertThat(money1).isEqualTo(money2);
        assertThat(money1).isNotEqualTo(money3);
        assertThat(money1).isNotEqualTo(null);
        assertThat(money1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Money money1 = Money.of(10.50);
        Money money2 = Money.of(10.50);

        // Then
        assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
    }

    @Test
    @DisplayName("Should generate proper toString")
    void shouldGenerateProperToString() {
        // Given
        Money money = Money.of(10.50);

        // When
        String toString = money.toString();

        // Then
        assertThat(toString).isEqualTo("Rs 10.50");
    }

    @Test
    @DisplayName("Should generate proper display string")
    void shouldGenerateProperDisplayString() {
        // Given
        Money money = Money.of(10.50);

        // When
        String displayString = money.toDisplayString();

        // Then
        assertThat(displayString).isEqualTo("Rs 10.50");
    }

    @Test
    @DisplayName("Should handle precision in arithmetic operations")
    void shouldHandlePrecisionInArithmeticOperations() {
        // Given
        Money money1 = Money.of(0.1);
        Money money2 = Money.of(0.2);

        // When
        Money result = money1.add(money2);

        // Then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("0.30"));
    }

    @Test
    @DisplayName("Should handle complex discount calculations")
    void shouldHandleComplexDiscountCalculations() {
        // Given
        Money originalPrice = Money.of(99.99);
        BigDecimal discount = new BigDecimal("15.75");

        // When
        Money discountedPrice = originalPrice.applyDiscount(discount);

        // Then
        // 99.99 - (99.99 * 0.1575) = 99.99 - 15.74841 = 84.24 (rounded)
        assertThat(discountedPrice.getAmount()).isEqualTo(new BigDecimal("84.24"));
    }

    @Test
    @DisplayName("Should compare Money with different scales correctly")
    void shouldCompareMoneyWithDifferentScalesCorrectly() {
        // Given
        Money money1 = new Money(new BigDecimal("10.5"));
        Money money2 = new Money(new BigDecimal("10.50"));

        // Then
        assertThat(money1).isEqualTo(money2);
        assertThat(money1.isGreaterThan(money2)).isFalse();
        assertThat(money1.isLessThan(money2)).isFalse();
    }
}