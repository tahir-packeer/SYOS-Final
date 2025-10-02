package org.syos.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quantity Value Object Tests")
class QuantityTest {

    @Test
    @DisplayName("Should create Quantity with valid value")
    void shouldCreateQuantityWithValidValue() {
        // Given
        int value = 10;

        // When
        Quantity quantity = new Quantity(value);

        // Then
        assertThat(quantity.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should create Quantity with zero value")
    void shouldCreateQuantityWithZeroValue() {
        // When
        Quantity quantity = new Quantity(0);

        // Then
        assertThat(quantity.getValue()).isEqualTo(0);
        assertThat(quantity.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for negative value")
    void shouldThrowExceptionForNegativeValue() {
        // When & Then
        assertThatThrownBy(() -> new Quantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should add two Quantity instances")
    void shouldAddTwoQuantityInstances() {
        // Given
        Quantity quantity1 = new Quantity(10);
        Quantity quantity2 = new Quantity(5);

        // When
        Quantity result = quantity1.add(quantity2);

        // Then
        assertThat(result.getValue()).isEqualTo(15);
        assertThat(quantity1.getValue()).isEqualTo(10); // Original unchanged
        assertThat(quantity2.getValue()).isEqualTo(5); // Original unchanged
    }

    @Test
    @DisplayName("Should subtract two Quantity instances")
    void shouldSubtractTwoQuantityInstances() {
        // Given
        Quantity quantity1 = new Quantity(15);
        Quantity quantity2 = new Quantity(5);

        // When
        Quantity result = quantity1.subtract(quantity2);

        // Then
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should subtract to zero")
    void shouldSubtractToZero() {
        // Given
        Quantity quantity1 = new Quantity(10);
        Quantity quantity2 = new Quantity(10);

        // When
        Quantity result = quantity1.subtract(quantity2);

        // Then
        assertThat(result.getValue()).isEqualTo(0);
        assertThat(result.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when subtraction results in negative")
    void shouldThrowExceptionWhenSubtractionResultsInNegative() {
        // Given
        Quantity quantity1 = new Quantity(5);
        Quantity quantity2 = new Quantity(10);

        // When & Then
        assertThatThrownBy(() -> quantity1.subtract(quantity2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resulting quantity cannot be negative");
    }

    @Test
    @DisplayName("Should check if Quantity is less than another")
    void shouldCheckIfQuantityIsLessThanAnother() {
        // Given
        Quantity quantity1 = new Quantity(5);
        Quantity quantity2 = new Quantity(10);

        // Then
        assertThat(quantity1.isLessThan(quantity2)).isTrue();
        assertThat(quantity2.isLessThan(quantity1)).isFalse();
    }

    @Test
    @DisplayName("Should check if equal quantities are not less than each other")
    void shouldCheckIfEqualQuantitiesAreNotLessThanEachOther() {
        // Given
        Quantity quantity1 = new Quantity(10);
        Quantity quantity2 = new Quantity(10);

        // Then
        assertThat(quantity1.isLessThan(quantity2)).isFalse();
        assertThat(quantity2.isLessThan(quantity1)).isFalse();
    }

    @Test
    @DisplayName("Should check if Quantity is zero")
    void shouldCheckIfQuantityIsZero() {
        // Given
        Quantity zeroQuantity = new Quantity(0);
        Quantity nonZeroQuantity = new Quantity(1);

        // Then
        assertThat(zeroQuantity.isZero()).isTrue();
        assertThat(nonZeroQuantity.isZero()).isFalse();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Quantity quantity1 = new Quantity(10);
        Quantity quantity2 = new Quantity(10);
        Quantity quantity3 = new Quantity(15);

        // Then
        assertThat(quantity1).isEqualTo(quantity2);
        assertThat(quantity1).isNotEqualTo(quantity3);
        assertThat(quantity1).isNotEqualTo(null);
        assertThat(quantity1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Quantity quantity1 = new Quantity(10);
        Quantity quantity2 = new Quantity(10);

        // Then
        assertThat(quantity1.hashCode()).isEqualTo(quantity2.hashCode());
    }

    @Test
    @DisplayName("Should generate proper toString")
    void shouldGenerateProperToString() {
        // Given
        Quantity quantity = new Quantity(25);

        // When
        String toString = quantity.toString();

        // Then
        assertThat(toString).isEqualTo("25");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        int originalValue = 10;
        Quantity quantity = new Quantity(originalValue);

        // When - Perform operations
        Quantity added = quantity.add(new Quantity(5));
        Quantity subtracted = quantity.subtract(new Quantity(3));

        // Then - Original quantity should remain unchanged
        assertThat(quantity.getValue()).isEqualTo(originalValue);
        assertThat(added.getValue()).isEqualTo(15);
        assertThat(subtracted.getValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should handle large quantities")
    void shouldHandleLargeQuantities() {
        // Given
        int largeValue = 1000000;

        // When
        Quantity quantity = new Quantity(largeValue);

        // Then
        assertThat(quantity.getValue()).isEqualTo(largeValue);
    }

    @Test
    @DisplayName("Should handle arithmetic operations with zero")
    void shouldHandleArithmeticOperationsWithZero() {
        // Given
        Quantity quantity = new Quantity(10);
        Quantity zero = new Quantity(0);

        // When & Then
        assertThat(quantity.add(zero)).isEqualTo(quantity);
        assertThat(quantity.subtract(zero)).isEqualTo(quantity);
        assertThat(zero.add(quantity)).isEqualTo(quantity);
    }

    @Test
    @DisplayName("Should handle complex arithmetic chains")
    void shouldHandleComplexArithmeticChains() {
        // Given
        Quantity initial = new Quantity(100);
        Quantity toAdd1 = new Quantity(50);
        Quantity toAdd2 = new Quantity(25);
        Quantity toSubtract1 = new Quantity(30);
        Quantity toSubtract2 = new Quantity(20);

        // When
        Quantity result = initial
                .add(toAdd1) // 100 + 50 = 150
                .add(toAdd2) // 150 + 25 = 175
                .subtract(toSubtract1) // 175 - 30 = 145
                .subtract(toSubtract2); // 145 - 20 = 125

        // Then
        assertThat(result.getValue()).isEqualTo(125);
        assertThat(initial.getValue()).isEqualTo(100); // Original unchanged
    }
}