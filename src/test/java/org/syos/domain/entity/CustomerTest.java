package org.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Customer Entity Tests")
class CustomerTest {

    @Test
    @DisplayName("Should create customer with name and phone")
    void shouldCreateCustomerWithNameAndPhone() {
        // Given
        String name = "John Doe";
        String phone = "123-456-7890";

        // When
        Customer customer = new Customer(name, phone);

        // Then
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getPhone()).isEqualTo(phone);
        assertThat(customer.getCustomerId()).isNull();
    }

    @Test
    @DisplayName("Should create customer with ID, name and phone")
    void shouldCreateCustomerWithIdNameAndPhone() {
        // Given
        Long customerId = 1L;
        String name = "John Doe";
        String phone = "123-456-7890";

        // When
        Customer customer = new Customer(customerId, name, phone);

        // Then
        assertThat(customer.getCustomerId()).isEqualTo(customerId);
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getPhone()).isEqualTo(phone);
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        // Given
        String phone = "123-456-7890";

        // When & Then
        assertThatThrownBy(() -> new Customer(null, phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty name")
    void shouldThrowExceptionForEmptyName() {
        // Given
        String phone = "123-456-7890";

        // When & Then
        assertThatThrownBy(() -> new Customer("", phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for whitespace-only name")
    void shouldThrowExceptionForWhitespaceOnlyName() {
        // Given
        String phone = "123-456-7890";

        // When & Then
        assertThatThrownBy(() -> new Customer("   ", phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null phone")
    void shouldThrowExceptionForNullPhone() {
        // Given
        String name = "John Doe";

        // When & Then
        assertThatThrownBy(() -> new Customer(name, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer phone cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty phone")
    void shouldThrowExceptionForEmptyPhone() {
        // Given
        String name = "John Doe";

        // When & Then
        assertThatThrownBy(() -> new Customer(name, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer phone cannot be null or empty");
    }

    @Test
    @DisplayName("Should update customer name")
    void shouldUpdateCustomerName() {
        // Given
        Customer customer = new Customer("John Doe", "123-456-7890");
        String newName = "Jane Smith";

        // When
        customer.setName(newName);

        // Then
        assertThat(customer.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Should update customer phone")
    void shouldUpdateCustomerPhone() {
        // Given
        Customer customer = new Customer("John Doe", "123-456-7890");
        String newPhone = "098-765-4321";

        // When
        customer.setPhone(newPhone);

        // Then
        assertThat(customer.getPhone()).isEqualTo(newPhone);
    }

    @Test
    @DisplayName("Should update customer ID")
    void shouldUpdateCustomerId() {
        // Given
        Customer customer = new Customer("John Doe", "123-456-7890");
        Long customerId = 1L;

        // When
        customer.setCustomerId(customerId);

        // Then
        assertThat(customer.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should validate name when updating")
    void shouldValidateNameWhenUpdating() {
        // Given
        Customer customer = new Customer("John Doe", "123-456-7890");

        // When & Then
        assertThatThrownBy(() -> customer.setName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name cannot be null or empty");
    }

    @Test
    @DisplayName("Should validate phone when updating")
    void shouldValidatePhoneWhenUpdating() {
        // Given
        Customer customer = new Customer("John Doe", "123-456-7890");

        // When & Then
        assertThatThrownBy(() -> customer.setPhone(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer phone cannot be null or empty");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Long customerId = 1L;
        Customer customer1 = new Customer(customerId, "John Doe", "123-456-7890");
        Customer customer2 = new Customer(customerId, "Jane Smith", "098-765-4321");
        Customer customer3 = new Customer(2L, "John Doe", "123-456-7890");

        // Then
        assertThat(customer1).isEqualTo(customer2); // Same ID
        assertThat(customer1).isNotEqualTo(customer3); // Different ID
        assertThat(customer1).isNotEqualTo(null);
        assertThat(customer1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Long customerId = 1L;
        Customer customer1 = new Customer(customerId, "John Doe", "123-456-7890");
        Customer customer2 = new Customer(customerId, "Jane Smith", "098-765-4321");

        // Then
        assertThat(customer1.hashCode()).isEqualTo(customer2.hashCode()); // Same ID should have same hash
    }

    @Test
    @DisplayName("Should handle null ID in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        Customer customer1 = new Customer("John Doe", "123-456-7890");
        Customer customer2 = new Customer("Jane Smith", "098-765-4321");
        Customer customer3 = new Customer("John Doe", "123-456-7890");

        // Then
        assertThat(customer1).isEqualTo(customer2); // Both have null IDs, equals returns true when both are null
        assertThat(customer1).isEqualTo(customer3); // Same values, null IDs
        assertThat(customer1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should generate proper toString")
    void shouldGenerateProperToString() {
        // Given
        Customer customer = new Customer(1L, "John Doe", "123-456-7890");

        // When
        String toString = customer.toString();

        // Then
        assertThat(toString).contains("Customer");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=John Doe");
        assertThat(toString).contains("phone=123-456-7890");
    }
}