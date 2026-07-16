package br.com.nutriplus.application.shared;

import br.com.nutriplus.exception.BusinessException;

import java.util.Base64;
import java.util.Locale;
import java.util.Set;

public final class BioimpedanceDocumentValidator {

    private static final int MAX_BASE64_CHAR_LENGTH = 12_000_000;
    private static final int MAX_DECODED_BYTES = 6_000_000;
    private static final Set<String> ALLOWED_MIME = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private BioimpedanceDocumentValidator() {
    }

    public static String normalizeMime(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new BusinessException("Informe o tipo do arquivo (PDF ou imagem).");
        }
        String mime = mimeType.strip().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(mime)) {
            mime = "image/jpeg";
        }
        if (!ALLOWED_MIME.contains(mime)) {
            throw new BusinessException("Tipo de arquivo não permitido. Use PDF, JPEG, PNG ou WebP.");
        }
        return mime;
    }

    public static String cleanBase64(String contentBase64) {
        if (contentBase64 == null || contentBase64.isBlank()) {
            throw new BusinessException("Arquivo vazio.");
        }
        String raw = contentBase64.strip();
        if (raw.length() > MAX_BASE64_CHAR_LENGTH) {
            throw new BusinessException("Arquivo excede o tamanho máximo permitido.");
        }
        int comma = raw.indexOf(',');
        if (raw.regionMatches(true, 0, "data:", 0, 5) && comma > 0) {
            raw = raw.substring(comma + 1);
        }
        raw = raw.replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(raw);
            if (decoded.length == 0) {
                throw new BusinessException("Arquivo vazio.");
            }
            if (decoded.length > MAX_DECODED_BYTES) {
                throw new BusinessException("Arquivo decodificado excede o tamanho máximo permitido.");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Base64 do arquivo inválido.");
        }
        return raw;
    }
}
