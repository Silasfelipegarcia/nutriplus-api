package br.com.nutriplus.application.shared;

import br.com.nutriplus.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilePhotoValidatorTest {

    @Test
    void acceptsNullOrBlank() {
        assertThatCode(() -> ProfilePhotoValidator.validateOptionalPhoto(null))
                .doesNotThrowAnyException();
        assertThatCode(() -> ProfilePhotoValidator.validateOptionalPhoto("  "))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNonDataUrl() {
        assertThatThrownBy(() -> ProfilePhotoValidator.validateOptionalPhoto("https://example.com/photo.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Formato de foto inválido");
    }

    @Test
    void rejectsUnsupportedImageType() {
        assertThatThrownBy(() -> ProfilePhotoValidator.validateOptionalPhoto("data:image/gif;base64,AAAA"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de imagem não permitido");
    }

    @Test
    void acceptsValidJpegDataUrl() {
        String minimalJpeg = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP";
        assertThatCode(() -> ProfilePhotoValidator.validateOptionalPhoto(minimalJpeg))
                .doesNotThrowAnyException();
    }
}
