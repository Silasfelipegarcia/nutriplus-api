package br.com.nutriplus.application.shared;

import br.com.nutriplus.exception.BusinessException;

import java.util.Base64;

public final class ProfilePhotoValidator {

    private static final int MAX_DATA_URL_CHAR_LENGTH = 4_000_000;
    private static final int MAX_DECODED_IMAGE_BYTES = 2_000_000;

    private ProfilePhotoValidator() {
    }

    public static void validateOptionalPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }
        String u = photoUrl.strip();
        if (u.length() > MAX_DATA_URL_CHAR_LENGTH) {
            throw new BusinessException("Foto excede o tamanho máximo permitido.");
        }
        if (!u.regionMatches(true, 0, "data:image", 0, "data:image".length())) {
            throw new BusinessException("Formato de foto inválido. Use data:image (JPEG/PNG/WebP).");
        }
        String lower = u.substring(0, Math.min(u.length(), 40)).toLowerCase();
        if (!lower.startsWith("data:image/jpeg")
                && !lower.startsWith("data:image/jpg")
                && !lower.startsWith("data:image/png")
                && !lower.startsWith("data:image/webp")) {
            throw new BusinessException("Tipo de imagem não permitido. Use JPEG, PNG ou WebP.");
        }
        int comma = u.indexOf(',');
        if (comma < 0 || comma >= u.length() - 1) {
            throw new BusinessException("Data URL de imagem inválida.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(u.substring(comma + 1).replaceAll("\\s", ""));
            if (decoded.length > MAX_DECODED_IMAGE_BYTES) {
                throw new BusinessException("Imagem decodificada excede o tamanho máximo permitido.");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Base64 da imagem inválido.");
        }
    }
}
