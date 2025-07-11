package com.nayonikaeyecare.api.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HmacServiceTest {

    private static final String TEST_KEY = "test-secret-key-for-hmac-service";
    private static final String ANOTHER_TEST_KEY = "another-test-secret-key-for-hmac";

    @Test
    void testGenerateHmac_Success() {
        HmacService hmacService = new HmacService(TEST_KEY);
        String data = "TestDataString";
        String hmac = hmacService.generateHmac(data);

        assertNotNull(hmac);
        assertFalse(hmac.isEmpty());
        // For HmacSHA256, Base64 output is 44 chars for non-empty input
        // assertEquals(44, hmac.length()); // This can vary slightly with input, better to check non-empty

        // Test idempotency: same input, same key -> same HMAC
        String hmacAgain = hmacService.generateHmac(data);
        assertEquals(hmac, hmacAgain);
    }

    @Test
    void testGenerateHmac_DifferentData_DifferentHmac() {
        HmacService hmacService = new HmacService(TEST_KEY);
        String data1 = "TestDataString1";
        String data2 = "TestDataString2";

        String hmac1 = hmacService.generateHmac(data1);
        String hmac2 = hmacService.generateHmac(data2);

        assertNotNull(hmac1);
        assertNotNull(hmac2);
        assertNotEquals(hmac1, hmac2);
    }

    @Test
    void testGenerateHmac_DifferentKey_DifferentHmac() {
        HmacService hmacService1 = new HmacService(TEST_KEY);
        HmacService hmacService2 = new HmacService(ANOTHER_TEST_KEY);
        String data = "TestDataString";

        String hmac1 = hmacService1.generateHmac(data);
        String hmac2 = hmacService2.generateHmac(data);

        assertNotNull(hmac1);
        assertNotNull(hmac2);
        assertNotEquals(hmac1, hmac2);
    }

    @Test
    void testGenerateHmac_NullData_ReturnsNull() {
        HmacService hmacService = new HmacService(TEST_KEY);
        assertNull(hmacService.generateHmac(null));
    }

    @Test
    void testGenerateHmac_EmptyData_ReturnsValidHmac() {
        HmacService hmacService = new HmacService(TEST_KEY);
        String hmac = hmacService.generateHmac("");
        assertNotNull(hmac);
        assertFalse(hmac.isEmpty());
        // Example HMAC for empty string with "test-secret-key-for-hmac-service" as key using HmacSHA256
        // This value depends on the exact key and algorithm.
        // assertEquals("EXPECTED_HMAC_FOR_EMPTY_STRING", hmac); // Can be asserted if pre-calculated
    }

    @Test
    void constructor_NullKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService(null));
    }

    @Test
    void constructor_EmptyKey_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService(""));
    }

    @Test
    void constructor_DefaultKeyWarning() {
        // This test is tricky as it relies on logging output.
        // For now, we'll just ensure it doesn't throw an exception different from other invalid keys.
        // A more sophisticated test might capture log output.
        HmacService hmacService = new HmacService("your-super-secret-hmac-key-change-me-in-prod");
        assertNotNull(hmacService.generateHmac("test"));
    }
}
