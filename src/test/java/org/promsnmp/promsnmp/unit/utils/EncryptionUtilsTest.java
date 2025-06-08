package org.promsnmp.promsnmp.unit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.promsnmp.promsnmp.utils.EncryptionUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EncryptionUtils Tests")
class EncryptionUtilsTest {

    private static final String VALID_16_BYTE_KEY = "1234567890123456"; // 16 bytes for AES-128
    private static final String VALID_24_BYTE_KEY = "123456789012345678901234"; // 24 bytes for AES-192
    private static final String VALID_32_BYTE_KEY = "12345678901234567890123456789012"; // 32 bytes for AES-256

    @Test
    @DisplayName("Should encrypt and decrypt successfully with 16-byte key")
    void shouldEncryptAndDecryptWith16ByteKey() throws Exception {
        // Given
        String originalText = "Hello, World!";
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, VALID_16_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_16_BYTE_KEY);
        
        // Then
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    @DisplayName("Should encrypt and decrypt successfully with 24-byte key")
    void shouldEncryptAndDecryptWith24ByteKey() throws Exception {
        // Given
        String originalText = "Test message for AES-192";
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, VALID_24_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_24_BYTE_KEY);
        
        // Then
        assertEquals(originalText, decrypted);
    }

    @Test
    @DisplayName("Should encrypt and decrypt successfully with 32-byte key")
    void shouldEncryptAndDecryptWith32ByteKey() throws Exception {
        // Given
        String originalText = "Test message for AES-256";
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, VALID_32_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_32_BYTE_KEY);
        
        // Then
        assertEquals(originalText, decrypted);
    }

    @Test
    @DisplayName("Should produce different encrypted values for same input")
    void shouldProduceDifferentEncryptedValuesForSameInput() throws Exception {
        // Given
        String originalText = "Same input text";
        
        // When
        String encrypted1 = EncryptionUtils.encrypt(originalText, VALID_16_BYTE_KEY);
        String encrypted2 = EncryptionUtils.encrypt(originalText, VALID_16_BYTE_KEY);
        
        // Then
        assertNotEquals(encrypted1, encrypted2, "Each encryption should produce different results due to random IV");
        
        // But both should decrypt to the same original text
        assertEquals(originalText, EncryptionUtils.decrypt(encrypted1, VALID_16_BYTE_KEY));
        assertEquals(originalText, EncryptionUtils.decrypt(encrypted2, VALID_16_BYTE_KEY));
    }

    @Test
    @DisplayName("Should handle empty string encryption")
    void shouldHandleEmptyStringEncryption() throws Exception {
        // Given
        String emptyText = "";
        
        // When
        String encrypted = EncryptionUtils.encrypt(emptyText, VALID_16_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_16_BYTE_KEY);
        
        // Then
        assertEquals(emptyText, decrypted);
    }

    @Test
    @DisplayName("Should handle special characters and Unicode")
    void shouldHandleSpecialCharactersAndUnicode() throws Exception {
        // Given
        String specialText = "Special chars: !@#$%^&*()_+{}|:<>?[] and Unicode: 你好世界 🚀";
        
        // When
        String encrypted = EncryptionUtils.encrypt(specialText, VALID_16_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_16_BYTE_KEY);
        
        // Then
        assertEquals(specialText, decrypted);
    }

    @Test
    @DisplayName("Should return Base64 encoded string")
    void shouldReturnBase64EncodedString() throws Exception {
        // Given
        String originalText = "Test Base64 encoding";
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, VALID_16_BYTE_KEY);
        
        // Then
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encrypted), 
            "Encrypted string should be valid Base64");
        assertTrue(encrypted.matches("[A-Za-z0-9+/]*={0,2}"), 
            "Encrypted string should contain only Base64 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "short", "1234567890123", "12345678901234567890123456789012345"})
    @DisplayName("Should handle invalid key lengths gracefully")
    void shouldHandleInvalidKeyLengths(String invalidKey) {
        // Given
        String testText = "Test message";
        
        // When & Then - Current implementation doesn't validate key length
        // This test documents the current behavior and should be updated when key validation is added
        if (invalidKey.isEmpty()) {
            assertThrows(Exception.class, () -> 
                EncryptionUtils.encrypt(testText, invalidKey));
        } else {
            // AES encryption throws InvalidKeyException for invalid key lengths
            assertThrows(Exception.class, () -> EncryptionUtils.encrypt(testText, invalidKey));
        }
    }

    @Test
    @DisplayName("Should fail decryption with wrong key")
    void shouldFailDecryptionWithWrongKey() throws Exception {
        // Given
        String originalText = "Secret message";
        String correctKey = VALID_16_BYTE_KEY;
        String wrongKey = "wrong_key_123456";
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, correctKey);
        
        // Then
        assertThrows(Exception.class, () -> 
            EncryptionUtils.decrypt(encrypted, wrongKey));
    }

    @Test
    @DisplayName("Should fail decryption with corrupted encrypted data")
    void shouldFailDecryptionWithCorruptedData() {
        // Given
        String corruptedData = "CorruptedBase64Data==";
        
        // When & Then
        assertThrows(Exception.class, () -> 
            EncryptionUtils.decrypt(corruptedData, VALID_16_BYTE_KEY));
    }

    @Test
    @DisplayName("Should fail decryption with invalid Base64")
    void shouldFailDecryptionWithInvalidBase64() {
        // Given
        String invalidBase64 = "This is not Base64!";
        
        // When & Then
        assertThrows(Exception.class, () -> 
            EncryptionUtils.decrypt(invalidBase64, VALID_16_BYTE_KEY));
    }

    @Test
    @DisplayName("Should handle null inputs appropriately")
    void shouldHandleNullInputs() {
        // When & Then
        assertThrows(Exception.class, () -> 
            EncryptionUtils.encrypt(null, VALID_16_BYTE_KEY));
        assertThrows(Exception.class, () -> 
            EncryptionUtils.encrypt("test", null));
        assertThrows(Exception.class, () -> 
            EncryptionUtils.decrypt(null, VALID_16_BYTE_KEY));
        assertThrows(Exception.class, () -> 
            EncryptionUtils.decrypt("dGVzdA==", null));
    }

    @Test
    @DisplayName("Should handle large text encryption")
    void shouldHandleLargeTextEncryption() throws Exception {
        // Given
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("This is a large text for testing encryption performance and correctness. ");
        }
        String originalText = largeText.toString();
        
        // When
        String encrypted = EncryptionUtils.encrypt(originalText, VALID_32_BYTE_KEY);
        String decrypted = EncryptionUtils.decrypt(encrypted, VALID_32_BYTE_KEY);
        
        // Then
        assertEquals(originalText, decrypted);
        assertTrue(encrypted.length() > originalText.length(), 
            "Encrypted data should be larger due to IV and authentication tag");
    }
}