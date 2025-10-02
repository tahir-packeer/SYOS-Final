package org.syos.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ItemCode Value Object Tests")
class ItemCodeTest {

    @Test
    @DisplayName("Should create ItemCode with valid code")
    void shouldCreateItemCodeWithValidCode() {
        // Given
        String code = "ITEM001";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should throw exception for null code")
    void shouldThrowExceptionForNullCode() {
        // When & Then
        assertThatThrownBy(() -> new ItemCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item code cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty code")
    void shouldThrowExceptionForEmptyCode() {
        // When & Then
        assertThatThrownBy(() -> new ItemCode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item code cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for whitespace-only code")
    void shouldThrowExceptionForWhitespaceOnlyCode() {
        // When & Then
        assertThatThrownBy(() -> new ItemCode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item code cannot be null or empty");
    }

    @Test
    @DisplayName("Should trim and convert to uppercase")
    void shouldTrimAndConvertToUppercase() {
        // Given
        String code = "  item001  ";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should handle already uppercase code")
    void shouldHandleAlreadyUppercaseCode() {
        // Given
        String code = "ITEM001";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should handle mixed case code")
    void shouldHandleMixedCaseCode() {
        // Given
        String code = "Item001";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should handle numeric codes")
    void shouldHandleNumericCodes() {
        // Given
        String code = "12345";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Should handle alphanumeric codes")
    void shouldHandleAlphanumericCodes() {
        // Given
        String code = "abc123def";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ABC123DEF");
    }

    @Test
    @DisplayName("Should handle special characters")
    void shouldHandleSpecialCharacters() {
        // Given
        String code = "item-001_a";

        // When
        ItemCode itemCode = new ItemCode(code);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("ITEM-001_A");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        ItemCode itemCode1 = new ItemCode("ITEM001");
        ItemCode itemCode2 = new ItemCode("item001"); // Should be normalized to ITEM001
        ItemCode itemCode3 = new ItemCode("ITEM002");

        // Then
        assertThat(itemCode1).isEqualTo(itemCode2); // Same after normalization
        assertThat(itemCode1).isNotEqualTo(itemCode3);
        assertThat(itemCode1).isNotEqualTo(null);
        assertThat(itemCode1).isNotEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        ItemCode itemCode1 = new ItemCode("ITEM001");
        ItemCode itemCode2 = new ItemCode("item001"); // Same after normalization

        // Then
        assertThat(itemCode1.hashCode()).isEqualTo(itemCode2.hashCode());
    }

    @Test
    @DisplayName("Should generate proper toString")
    void shouldGenerateProperToString() {
        // Given
        ItemCode itemCode = new ItemCode("item001");

        // When
        String toString = itemCode.toString();

        // Then
        assertThat(toString).isEqualTo("ITEM001");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        String originalCode = "item001";
        ItemCode itemCode = new ItemCode(originalCode);

        // When - Try to get the code
        String retrievedCode = itemCode.getCode();

        // Then - Original ItemCode should remain unchanged
        assertThat(itemCode.getCode()).isEqualTo("ITEM001");
        assertThat(retrievedCode).isEqualTo("ITEM001");

        // Verify that ItemCode behaves as immutable value object
        ItemCode anotherItemCode = new ItemCode(originalCode);
        assertThat(itemCode).isEqualTo(anotherItemCode);
    }

    @Test
    @DisplayName("Should handle different input cases consistently")
    void shouldHandleDifferentInputCasesConsistently() {
        // Given
        ItemCode code1 = new ItemCode("product123");
        ItemCode code2 = new ItemCode("PRODUCT123");
        ItemCode code3 = new ItemCode("Product123");
        ItemCode code4 = new ItemCode("  product123  ");

        // Then - All should be equal after normalization
        assertThat(code1).isEqualTo(code2);
        assertThat(code1).isEqualTo(code3);
        assertThat(code1).isEqualTo(code4);
        assertThat(code2).isEqualTo(code3);
        assertThat(code2).isEqualTo(code4);
        assertThat(code3).isEqualTo(code4);
    }

    @Test
    @DisplayName("Should handle long codes")
    void shouldHandleLongCodes() {
        // Given
        String longCode = "verylongproductcode123456789abcdefghijk";

        // When
        ItemCode itemCode = new ItemCode(longCode);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("VERYLONGPRODUCTCODE123456789ABCDEFGHIJK");
    }

    @Test
    @DisplayName("Should handle single character codes")
    void shouldHandleSingleCharacterCodes() {
        // Given
        String singleCharCode = "a";

        // When
        ItemCode itemCode = new ItemCode(singleCharCode);

        // Then
        assertThat(itemCode.getCode()).isEqualTo("A");
    }
}