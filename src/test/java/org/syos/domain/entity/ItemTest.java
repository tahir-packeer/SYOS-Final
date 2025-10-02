package org.syos.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Item entity.
 */
@DisplayName("Item Entity Tests")
class ItemTest {

    @Test
    @DisplayName("Should create Item with valid parameters")
    void shouldCreateItemWithValidParameters() {
        // Given
        String name = "Test Product";
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);
        BigDecimal discount = BigDecimal.valueOf(5.0);
        int reorderLevel = 50;

        // When
        Item item = new Item(name, code, price, discount, reorderLevel);

        // Then
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getCode()).isEqualTo(code);
        assertThat(item.getUnitPrice()).isEqualTo(price);
        assertThat(item.getDiscount()).isEqualByComparingTo(discount);
        assertThat(item.getReorderLevel()).isEqualTo(reorderLevel);
        assertThat(item.getItemId()).isNull(); // Not set in constructor
    }

    @Test
    @DisplayName("Should create Item with ID constructor")
    void shouldCreateItemWithIdConstructor() {
        // Given
        Long id = 1L;
        String name = "Test Product";
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);
        BigDecimal discount = BigDecimal.valueOf(5.0);
        int reorderLevel = 50;

        // When
        Item item = new Item(id, name, code, price, discount, reorderLevel);

        // Then
        assertThat(item.getItemId()).isEqualTo(id);
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getCode()).isEqualTo(code);
        assertThat(item.getUnitPrice()).isEqualTo(price);
        assertThat(item.getDiscount()).isEqualByComparingTo(discount);
        assertThat(item.getReorderLevel()).isEqualTo(reorderLevel);
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item(null, code, price, BigDecimal.ZERO, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty name")
    void shouldThrowExceptionForEmptyName() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item("", code, price, BigDecimal.ZERO, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null code")
    void shouldThrowExceptionForNullCode() {
        // Given
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item("Test Product", null, price, BigDecimal.ZERO, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item code cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for null price")
    void shouldThrowExceptionForNullPrice() {
        // Given
        ItemCode code = new ItemCode("TEST001");

        // When & Then
        assertThatThrownBy(() -> new Item("Test Product", code, null, BigDecimal.ZERO, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit price must be non-negative");
    }

    @Test
    @DisplayName("Should throw exception for negative discount")
    void shouldThrowExceptionForNegativeDiscount() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item("Test Product", code, price, BigDecimal.valueOf(-1), 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Discount must be between 0 and 100");
    }

    @Test
    @DisplayName("Should throw exception for discount greater than 100")
    void shouldThrowExceptionForDiscountGreaterThan100() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item("Test Product", code, price, BigDecimal.valueOf(101), 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Discount must be between 0 and 100");
    }

    @Test
    @DisplayName("Should throw exception for negative reorder level")
    void shouldThrowExceptionForNegativeReorderLevel() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Money price = Money.of(10.99);

        // When & Then
        assertThatThrownBy(() -> new Item("Test Product", code, price, BigDecimal.ZERO, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reorder level cannot be negative");
    }

    @Test
    @DisplayName("Should calculate total price correctly without discount")
    void shouldCalculateTotalPriceWithoutDiscount() {
        // Given
        Item item = new Item("Test Product", new ItemCode("TEST001"),
                Money.of(10.0), BigDecimal.ZERO, 50);
        int quantity = 5;

        // When
        Money totalPrice = item.calculateTotalPrice(quantity);

        // Then
        assertThat(totalPrice).isEqualTo(Money.of(50.0));
    }

    @Test
    @DisplayName("Should calculate total price correctly with discount")
    void shouldCalculateTotalPriceWithDiscount() {
        // Given
        Item item = new Item("Test Product", new ItemCode("TEST001"),
                Money.of(100.0), BigDecimal.valueOf(10.0), 50); // 10% discount
        int quantity = 2;

        // When
        Money totalPrice = item.calculateTotalPrice(quantity);

        // Then
        assertThat(totalPrice).isEqualTo(Money.of(180.0)); // 100 * 0.9 * 2 = 180
    }

    @Test
    @DisplayName("Should update item name")
    void shouldUpdateItemName() {
        // Given
        Item item = new Item("Old Name", new ItemCode("TEST001"),
                Money.of(10.0), BigDecimal.ZERO, 50);
        String newName = "New Name";

        // When
        item.setName(newName);

        // Then
        assertThat(item.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Should update unit price")
    void shouldUpdateUnitPrice() {
        // Given
        Item item = new Item("Test Product", new ItemCode("TEST001"),
                Money.of(10.0), BigDecimal.ZERO, 50);
        Money newPrice = Money.of(20.0);

        // When
        item.setUnitPrice(newPrice);

        // Then
        assertThat(item.getUnitPrice()).isEqualTo(newPrice);
    }

    @Test
    @DisplayName("Should update discount")
    void shouldUpdateDiscount() {
        // Given
        Item item = new Item("Test Product", new ItemCode("TEST001"),
                Money.of(10.0), BigDecimal.ZERO, 50);
        BigDecimal newDiscount = BigDecimal.valueOf(15.0);

        // When
        item.setDiscount(newDiscount);

        // Then
        assertThat(item.getDiscount()).isEqualByComparingTo(newDiscount);
    }

    @Test
    @DisplayName("Should update reorder level")
    void shouldUpdateReorderLevel() {
        // Given
        Item item = new Item("Test Product", new ItemCode("TEST001"),
                Money.of(10.0), BigDecimal.ZERO, 50);
        int newReorderLevel = 100;

        // When
        item.setReorderLevel(newReorderLevel);

        // Then
        assertThat(item.getReorderLevel()).isEqualTo(newReorderLevel);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Long id = 1L;
        ItemCode code = new ItemCode("TEST001");
        Item item1 = new Item(id, "Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item2 = new Item(id, "Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item3 = new Item(2L, "Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item4 = new Item(id, "Test Product", new ItemCode("TEST002"),
                Money.of(10.0), BigDecimal.ZERO, 50);

        // Then
        assertThat(item1).isEqualTo(item2); // Same ID and code
        assertThat(item1).isNotEqualTo(item3); // Different ID
        assertThat(item1).isNotEqualTo(item4); // Different code
        assertThat(item1).isNotEqualTo(null);
        assertThat(item1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Long id = 1L;
        ItemCode code = new ItemCode("TEST001");
        Item item1 = new Item(id, "Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item2 = new Item(id, "Different Name", code,
                Money.of(20.0), BigDecimal.valueOf(10.0), 100);

        // Then
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode()); // Same ID and code should have same hash
    }

    @Test
    @DisplayName("Should handle null ID in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        ItemCode code = new ItemCode("TEST001");
        Item item1 = new Item("Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item2 = new Item("Test Product", code,
                Money.of(10.0), BigDecimal.ZERO, 50);
        Item item3 = new Item("Test Product", new ItemCode("TEST002"),
                Money.of(10.0), BigDecimal.ZERO, 50);

        // Then
        assertThat(item1).isEqualTo(item2); // Both have null IDs but same code, should be equal
        assertThat(item1).isNotEqualTo(item3); // Different codes, should not be equal
    }
}