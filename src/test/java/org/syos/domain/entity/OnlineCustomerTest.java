package org.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.syos.domain.valueobject.Password;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OnlineCustomer Entity Tests")
class OnlineCustomerTest {

    @Test
    @DisplayName("Should create online customer with required fields")
    void shouldCreateOnlineCustomerWithRequiredFields() {
        // Given
        String name = "John Doe";
        String email = "john.doe@email.com";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When
        OnlineCustomer customer = new OnlineCustomer(name, email, address, password);

        // Then
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getAddress()).isEqualTo(address);
        assertThat(customer.getPassword()).isEqualTo(password);
        assertThat(customer.getOnlineCustomerId()).isNull();
    }

    @Test
    @DisplayName("Should create online customer with ID and required fields")
    void shouldCreateOnlineCustomerWithIdAndRequiredFields() {
        // Given
        Long customerId = 1L;
        String name = "John Doe";
        String email = "john.doe@email.com";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When
        OnlineCustomer customer = new OnlineCustomer(customerId, name, email, address, password);

        // Then
        assertThat(customer.getOnlineCustomerId()).isEqualTo(customerId);
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getAddress()).isEqualTo(address);
        assertThat(customer.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        // Given
        String email = "john.doe@email.com";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(null, email, address, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty name")
    void shouldThrowExceptionForEmptyName() {
        // Given
        String email = "john.doe@email.com";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer("", email, address, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        // Given
        String name = "John Doe";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(name, null, address, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Valid email is required");
    }

    @Test
    @DisplayName("Should throw exception for invalid email")
    void shouldThrowExceptionForInvalidEmail() {
        // Given
        String name = "John Doe";
        String invalidEmail = "invalid-email";
        String address = "123 Main St";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(name, invalidEmail, address, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Valid email is required");
    }

    @Test
    @DisplayName("Should throw exception for null address")
    void shouldThrowExceptionForNullAddress() {
        // Given
        String name = "John Doe";
        String email = "john.doe@email.com";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(name, email, null, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty address")
    void shouldThrowExceptionForEmptyAddress() {
        // Given
        String name = "John Doe";
        String email = "john.doe@email.com";
        Password password = new Password("password123");

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(name, email, "", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        // Given
        String name = "John Doe";
        String email = "john.doe@email.com";
        String address = "123 Main St";

        // When & Then
        assertThatThrownBy(() -> new OnlineCustomer(name, email, address, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null");
    }

    @Test
    @DisplayName("Should update customer name")
    void shouldUpdateCustomerName() {
        // Given
        OnlineCustomer customer = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        String newName = "Jane Smith";

        // When
        customer.setName(newName);

        // Then
        assertThat(customer.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Should update customer email")
    void shouldUpdateCustomerEmail() {
        // Given
        OnlineCustomer customer = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        String newEmail = "jane.smith@email.com";

        // When
        customer.setEmail(newEmail);

        // Then
        assertThat(customer.getEmail()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("Should update customer address")
    void shouldUpdateCustomerAddress() {
        // Given
        OnlineCustomer customer = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        String newAddress = "456 Oak Ave";

        // When
        customer.setAddress(newAddress);

        // Then
        assertThat(customer.getAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("Should update customer password")
    void shouldUpdateCustomerPassword() {
        // Given
        OnlineCustomer customer = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        Password newPassword = new Password("newpassword456");

        // When
        customer.setPassword(newPassword);

        // Then
        assertThat(customer.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Long customerId = 1L;
        OnlineCustomer customer1 = new OnlineCustomer(customerId, "John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        OnlineCustomer customer2 = new OnlineCustomer(customerId, "Jane Smith", "jane.smith@email.com",
                "456 Oak Ave", new Password("password456"));
        OnlineCustomer customer3 = new OnlineCustomer(2L, "John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));

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
        OnlineCustomer customer1 = new OnlineCustomer(customerId, "John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        OnlineCustomer customer2 = new OnlineCustomer(customerId, "Jane Smith", "jane.smith@email.com",
                "456 Oak Ave", new Password("password456"));

        // Then
        assertThat(customer1.hashCode()).isEqualTo(customer2.hashCode()); // Same ID should have same hash
    }

    @Test
    @DisplayName("Should handle null ID in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        OnlineCustomer customer1 = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));
        OnlineCustomer customer2 = new OnlineCustomer("Jane Smith", "jane.smith@email.com",
                "456 Oak Ave", new Password("password456"));
        OnlineCustomer customer3 = new OnlineCustomer("John Doe", "john.doe@email.com",
                "123 Main St", new Password("password123"));

        // Then
        assertThat(customer1).isEqualTo(customer2); // Both have null IDs, equals returns true when both are null
        assertThat(customer1).isEqualTo(customer3); // Same values, null IDs
        assertThat(customer1).isNotEqualTo(null);
    }
}