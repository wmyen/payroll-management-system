package com.payroll.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil util;

    @BeforeEach
    void setUp() {
        String testKey = java.util.Base64.getEncoder().encodeToString(new byte[16]);
        util = new EncryptionUtil(testKey);
    }

    @Test
    void encrypt_and_decrypt_roundtrip() {
        String original = "A123456789";
        String encrypted = util.encrypt(original);
        assertNotEquals(original, encrypted);
        assertEquals(original, util.decrypt(encrypted));
    }

    @Test
    void mask_returns_correct_format() {
        assertEquals("A12***89", EncryptionUtil.mask("A123456789", 3, 2));
    }

    @Test
    void mask_handles_short_string() {
        assertEquals("***", EncryptionUtil.mask("AB", 2, 2));
    }

    @Test
    void encrypt_produces_different_ciphertext_each_time() {
        String original = "A123456789";
        String encrypted1 = util.encrypt(original);
        String encrypted2 = util.encrypt(original);
        assertNotEquals(encrypted1, encrypted2);
    }
}
