package org.syos.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password Value Object Tests")
class PasswordTest {

    @Test
    @DisplayName("Should create Password with valid value")
    void shouldCreatePasswordWithValidValue() {
        // Given
        String passwordValue = "password123";

        // When
        Password password = new Password(passwordValue);

        // Then
        assertThat(password.getValue()).isEqualTo(passwordValue);
        assertThat(password.getHashedValue()).isEqualTo(passwordValue);
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        // When & Then
        assertThatThrownBy(() -> new Password(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty password")
    void shouldThrowExceptionForEmptyPassword() {
        // When & Then
        assertThatThrownBy(() -> new Password(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for whitespace-only password")
    void shouldThrowExceptionForWhitespaceOnlyPassword() {
        // When & Then
        assertThatThrownBy(() -> new Password("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should trim password value")
    void shouldTrimPasswordValue() {
        // Given
        String passwordWithSpaces = "  password123  ";

        // When
        Password password = new Password(passwordWithSpaces);

        // Then
        assertThat(password.getValue()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should match correct plain text password")
    void shouldMatchCorrectPlainTextPassword() {
        // Given
        String originalPassword = "password123";
        Password password = new Password(originalPassword);

        // When & Then
        assertThat(password.matches(originalPassword)).isTrue();
        assertThat(password.matches("wrongPassword")).isFalse();
        assertThat(password.matches("PASSWORD123")).isFalse(); // Case sensitive
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        Password password1 = new Password("password123");
        Password password2 = new Password("password123");
        Password password3 = new Password("differentPassword");

        // Then
        assertThat(password1).isEqualTo(password2);
        assertThat(password1).isNotEqualTo(password3);
        assertThat(password1).isNotEqualTo(null);
        assertThat(password1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        Password password1 = new Password("password123");
        Password password2 = new Password("password123");

        // Then
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("Should hide password in toString")
    void shouldHidePasswordInToString() {
        // Given
        Password password = new Password("password123");

        // When
        String toString = password.toString();

        // Then
        assertThat(toString).isEqualTo("***HIDDEN***");
        assertThat(toString).doesNotContain("password123");
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String specialPassword = "P@ssw0rd!@#$%";

        // When
        Password password = new Password(specialPassword);

        // Then
        assertThat(password.getValue()).isEqualTo(specialPassword);
        assertThat(password.matches(specialPassword)).isTrue();
    }

    @Test
    @DisplayName("Should handle long passwords")
    void shouldHandleLongPasswords() {
        // Given
        String longPassword = "ThisIsAVeryLongPasswordWithLotsOfCharacters123456789!@#$%^&*()";

        // When
        Password password = new Password(longPassword);

        // Then
        assertThat(password.getValue()).isEqualTo(longPassword);
        assertThat(password.matches(longPassword)).isTrue();
    }

    @Test
    @DisplayName("Should handle unicode characters in password")
    void shouldHandleUnicodeCharactersInPassword() {
        // Given
        String unicodePassword = "pássword123मुकेशñ";

        // When
        Password password = new Password(unicodePassword);

        // Then
        assertThat(password.getValue()).isEqualTo(unicodePassword);
        assertThat(password.matches(unicodePassword)).isTrue();
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        String originalValue = "password123";
        Password password = new Password(originalValue);

        // When - Try to get the value and modify it (if it were mutable)
        String retrievedValue = password.getValue();

        // Then - Original password should remain unchanged
        assertThat(password.getValue()).isEqualTo(originalValue);
        assertThat(retrievedValue).isEqualTo(originalValue);

        // Verify that Password behaves as immutable value object
        Password anotherPassword = new Password(originalValue);
        assertThat(password).isEqualTo(anotherPassword);
    }
}