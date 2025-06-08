package org.promsnmp.promsnmp.unit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.promsnmp.promsnmp.utils.Snmp4jUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Snmp4jUtils Tests")
class Snmp4jUtilsTest {

    @Test
    @DisplayName("Should resolve v2c to version 1")
    void shouldResolveV2cToVersion1() {
        // When
        int result = Snmp4jUtils.resolveSnmpVersion("v2c");
        
        // Then
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Should resolve v3 to version 3")
    void shouldResolveV3ToVersion3() {
        // When
        int result = Snmp4jUtils.resolveSnmpVersion("v3");
        
        // Then
        assertEquals(3, result);
    }

    @ParameterizedTest
    @CsvSource({
        "v2c, 1",
        "v3, 3",
        "V2C, 1",
        "V3, 3",
        "V2c, 1",
        "v2C, 1"
    })
    @DisplayName("Should handle case insensitive version strings")
    void shouldHandleCaseInsensitiveVersions(String input, int expected) {
        // When
        int result = Snmp4jUtils.resolveSnmpVersion(input);
        
        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v4", "snmpv2c", "snmpv3", "", " ", "invalid", "2c", "3"})
    @DisplayName("Should throw IllegalArgumentException for unsupported versions")
    void shouldThrowExceptionForUnsupportedVersions(String invalidVersion) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> Snmp4jUtils.resolveSnmpVersion(invalidVersion)
        );
        
        assertTrue(exception.getMessage().contains("Unsupported SNMP version"));
        assertTrue(exception.getMessage().contains(invalidVersion));
    }

    @Test
    @DisplayName("Should throw NullPointerException for null input")
    void shouldThrowNullPointerExceptionForNullInput() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            Snmp4jUtils.resolveSnmpVersion(null));
    }

    @Test
    @DisplayName("Should handle whitespace in version strings")
    void shouldHandleWhitespaceInVersionStrings() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            Snmp4jUtils.resolveSnmpVersion(" v2c "));
        assertThrows(IllegalArgumentException.class, () -> 
            Snmp4jUtils.resolveSnmpVersion(" v3 "));
    }

    @Test
    @DisplayName("Should be consistent with SNMP4J constants")
    void shouldBeConsistentWithSnmp4jConstants() {
        // This test documents the expected mapping to SNMP4J constants
        // SnmpConstants.version2c = 1, SnmpConstants.version3 = 3
        
        // When
        int v2cResult = Snmp4jUtils.resolveSnmpVersion("v2c");
        int v3Result = Snmp4jUtils.resolveSnmpVersion("v3");
        
        // Then
        assertEquals(1, v2cResult, "v2c should map to SnmpConstants.version2c (1)");
        assertEquals(3, v3Result, "v3 should map to SnmpConstants.version3 (3)");
    }

    @Test
    @DisplayName("Should handle mixed case consistently")
    void shouldHandleMixedCaseConsistently() {
        // Given
        String[] v2cVariants = {"v2c", "V2C", "V2c", "v2C"};
        String[] v3Variants = {"v3", "V3"};
        
        // When & Then
        for (String variant : v2cVariants) {
            assertEquals(1, Snmp4jUtils.resolveSnmpVersion(variant), 
                "All v2c variants should resolve to 1: " + variant);
        }
        
        for (String variant : v3Variants) {
            assertEquals(3, Snmp4jUtils.resolveSnmpVersion(variant), 
                "All v3 variants should resolve to 3: " + variant);
        }
    }

    @Test
    @DisplayName("Should validate method return type")
    void shouldValidateMethodReturnType() {
        // When
        int result = Snmp4jUtils.resolveSnmpVersion("v2c");
        
        // Then
        assertTrue(result >= 0, "Method should return a valid integer");
        assertTrue(result > 0, "SNMP version should be positive");
    }
}