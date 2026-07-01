package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.domain.util.CpfUtil;
import br.com.nutriplus.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CpfProtectionService {

    private static final Logger log = LoggerFactory.getLogger(CpfProtectionService.class);

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final byte[] encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CpfProtectionService(@Value("${nutriplus.cpf.encryption-key:}") String encryptionKeyBase64) {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank()) {
            throw new IllegalStateException("nutriplus.cpf.encryption-key (CPF_ENCRYPTION_KEY) é obrigatório");
        }
        byte[] key = Base64.getDecoder().decode(encryptionKeyBase64.trim());
        if (key.length != 32) {
            throw new IllegalStateException("CPF_ENCRYPTION_KEY deve decodificar para 32 bytes (AES-256)");
        }
        this.encryptionKey = key;
    }

    public String requireValidNormalized(String cpf) {
        String normalized = CpfUtil.normalize(cpf);
        if (!CpfUtil.isValid(normalized)) {
            throw new BusinessException("CPF inválido");
        }
        return normalized;
    }

    public String hash(String normalizedCpf) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedCpf.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash do CPF", e);
        }
    }

    public String encrypt(String normalizedCpf) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(normalizedCpf.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criptografar CPF", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao descriptografar CPF", e);
        }
    }

    public String maskFromEncrypted(String encryptedCpf) {
        if (encryptedCpf == null || encryptedCpf.isBlank()) {
            return null;
        }
        try {
            return CpfUtil.mask(decrypt(encryptedCpf));
        } catch (RuntimeException e) {
            log.warn("Não foi possível descriptografar CPF para exibição: {}", e.getMessage());
            return null;
        }
    }
}
