package org.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.syos.domain.enums.UserRole;
import org.syos.domain.valueobject.Password;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with required fields")
    void shouldCreateUserWithRequiredFields() {
        // Given
        String username = "john.doe";
        Password password = new Password("password123");
        String fullName = "John Doe";
        UserRole role = UserRole.CASHIER;

        // When
        User user = new User(username, password, fullName, role);

        // Then
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getFullName()).isEqualTo(fullName);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should create user with ID and all fields")
    void shouldCreateUserWithIdAndAllFields() {
        // Given
        Long userId = 1L;
        String username = "john.doe";
        Password password = new Password("password123");
        String fullName = "John Doe";
        UserRole role = UserRole.MANAGER;
        boolean active = false;

        // When
        User user = new User(userId, username, password, fullName, role, active);

        // Then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getFullName()).isEqualTo(fullName);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for null username")
    void shouldThrowExceptionForNullUsername() {
        // Given
        Password password = new Password("password123");
        String fullName = "John Doe";
        UserRole role = UserRole.CASHIER;

        // When & Then
        assertThatThrownBy(() -> new User(null, password, fullName, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty username")
    void shouldThrowExceptionForEmptyUsername() {
        // Given
        Password password = new Password("password123");
        String fullName = "John Doe";
        UserRole role = UserRole.CASHIER;

        // When & Then
        assertThatThrownBy(() -> new User("", password, fullName, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        // Given
        String username = "john.doe";
        String fullName = "John Doe";
        UserRole role = UserRole.CASHIER;

        // When & Then
        assertThatThrownBy(() -> new User(username, null, fullName, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for null full name")
    void shouldThrowExceptionForNullFullName() {
        // Given
        String username = "john.doe";
        Password password = new Password("password123");
        UserRole role = UserRole.CASHIER;

        // When & Then
        assertThatThrownBy(() -> new User(username, password, null, role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty full name")
    void shouldThrowExceptionForEmptyFullName() {
        // Given
        String username = "john.doe";
        Password password = new Password("password123");
        UserRole role = UserRole.CASHIER;

        // When & Then
        assertThatThrownBy(() -> new User(username, password, "", role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null role")
    void shouldThrowExceptionForNullRole() {
        // Given
        String username = "john.doe";
        Password password = new Password("password123");
        String fullName = "John Doe";

        // When & Then
        assertThatThrownBy(() -> new User(username, password, fullName, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User role cannot be null");
    }

    @Test
    @DisplayName("Should check if user has specific role")
    void shouldCheckIfUserHasSpecificRole() {
        // Given
        User cashier = new User("cashier", new Password("password123"), "John Cashier", UserRole.CASHIER);
        User manager = new User("manager", new Password("password123"), "Jane Manager", UserRole.MANAGER);

        // Then
        assertThat(cashier.hasRole(UserRole.CASHIER)).isTrue();
        assertThat(cashier.hasRole(UserRole.MANAGER)).isFalse();
        assertThat(manager.hasRole(UserRole.MANAGER)).isTrue();
        assertThat(manager.hasRole(UserRole.CASHIER)).isFalse();
    }

    @Test
    @DisplayName("Should update user password")
    void shouldUpdateUserPassword() {
        // Given
        User user = new User("john.doe", new Password("password123"), "John Doe", UserRole.CASHIER);
        Password newPassword = new Password("newpassword456");

        // When
        user.setPassword(newPassword);

        // Then
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("Should update user active status")
    void shouldUpdateUserActiveStatus() {
        // Given
        User user = new User("john.doe", new Password("password123"), "John Doe", UserRole.CASHIER);

        // When
        user.setActive(false);

        // Then
        assertThat(user.isActive()).isFalse();

        // When
        user.setActive(true);

        // Then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Long userId = 1L;
        User user1 = new User(userId, "john.doe", new Password("password123"),
                "John Doe", UserRole.CASHIER, true);
        User user2 = new User(userId, "jane.smith", new Password("password456"),
                "Jane Smith", UserRole.MANAGER, false);
        User user3 = new User(2L, "john.doe", new Password("password123"),
                "John Doe", UserRole.CASHIER, true);

        // Then
        assertThat(user1).isEqualTo(user2); // Same ID
        assertThat(user1).isNotEqualTo(user3); // Different ID
        assertThat(user1).isNotEqualTo(null);
        assertThat(user1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Long userId = 1L;
        User user1 = new User(userId, "john.doe", new Password("password123"),
                "John Doe", UserRole.CASHIER, true);
        User user2 = new User(userId, "jane.smith", new Password("password456"),
                "Jane Smith", UserRole.MANAGER, false);

        // Then
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode()); // Same ID should have same hash
    }

    @Test
    @DisplayName("Should handle null ID in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        User user1 = new User("john.doe", new Password("password123"), "John Doe", UserRole.CASHIER);
        User user2 = new User("jane.smith", new Password("password456"), "Jane Smith", UserRole.MANAGER);
        User user3 = new User("john.doe", new Password("password123"), "John Doe", UserRole.CASHIER);

        // Then
        assertThat(user1).isEqualTo(user2); // Both have null IDs, equals returns true when both are null
        assertThat(user1).isEqualTo(user3); // Same values, null IDs
        assertThat(user1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should test all user roles")
    void shouldTestAllUserRoles() {
        // Given & When & Then
        User cashier = new User("cashier", new Password("password123"), "Cashier User", UserRole.CASHIER);
        assertThat(cashier.getRole()).isEqualTo(UserRole.CASHIER);
        assertThat(cashier.hasRole(UserRole.CASHIER)).isTrue();

        User manager = new User("manager", new Password("password123"), "Manager User", UserRole.MANAGER);
        assertThat(manager.getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(manager.hasRole(UserRole.MANAGER)).isTrue();

        User admin = new User("admin", new Password("password123"), "Admin User", UserRole.ADMIN);
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.hasRole(UserRole.ADMIN)).isTrue();

        User onlineCustomer = new User("customer", new Password("password123"), "Online Customer",
                UserRole.ONLINE_CUSTOMER);
        assertThat(onlineCustomer.getRole()).isEqualTo(UserRole.ONLINE_CUSTOMER);
        assertThat(onlineCustomer.hasRole(UserRole.ONLINE_CUSTOMER)).isTrue();
    }
}