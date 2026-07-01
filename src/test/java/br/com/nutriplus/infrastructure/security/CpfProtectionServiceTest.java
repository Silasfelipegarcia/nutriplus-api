package br.com.nutriplus.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CpfProtectionServiceTest {

    private static final String TEST_KEY = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
    private static final String OTHER_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=";

    @Test
    void encryptDecryptRoundTrip() {
        CpfProtectionService service = new CpfProtectionService(TEST_KEY);
        String encrypted = service.encrypt("52998224725");
        assertEquals("52998224725", service.decrypt(encrypted));
    }

    @Test
    void maskFromEncrypted_returnsMaskedCpf() {
        CpfProtectionService service = new CpfProtectionService(TEST_KEY);
        String encrypted = service.encrypt("52998224725");
        assertEquals("***.982.247-**", service.maskFromEncrypted(encrypted));
    }

    @Test
    void maskFromEncrypted_returnsNullWhenKeyMismatch() {
        CpfProtectionService encryptor = new CpfProtectionService(TEST_KEY);
        CpfProtectionService decryptor = new CpfProtectionService(OTHER_KEY);
        String encrypted = encryptor.encrypt("52998224725");
        assertNull(decryptor.maskFromEncrypted(encrypted));
    }

    @Test
    void maskFromEncrypted_returnsNullForCorruptPayload() {
        CpfProtectionService service = new CpfProtectionService(TEST_KEY);
        assertNull(service.maskFromEncrypted("not-valid-base64-ciphertext"));
    }

    @Test
    void maskFromEncrypted_returnsNullForBlank() {
        CpfProtectionService service = new CpfProtectionService(TEST_KEY);
        assertNull(service.maskFromEncrypted(null));
        assertNull(service.maskFromEncrypted("   "));
    }

    @Test
    void requireValidNormalized_rejectsInvalidCpf() {
        CpfProtectionService service = new CpfProtectionService(TEST_KEY);
        assertNotNull(service.requireValidNormalized("529.982.247-25"));
    }
}
