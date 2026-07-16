package br.com.nutriplus.application.shared;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BioimpedanceDocumentValidatorTest {

    @Test
    void normalizesJpegAlias() {
        assertThat(BioimpedanceDocumentValidator.normalizeMime("image/jpg")).isEqualTo("image/jpeg");
        assertThat(BioimpedanceDocumentValidator.normalizeMime("application/pdf")).isEqualTo("application/pdf");
    }

    @Test
    void rejectsUnsupportedMime() {
        assertThatThrownBy(() -> BioimpedanceDocumentValidator.normalizeMime("application/msword"))
                .hasMessageContaining("não permitido");
    }

    @Test
    void acceptsValidBase64AndStripsDataUrl() {
        String payload = Base64.getEncoder().encodeToString("hello-bioimpedance-doc".getBytes());
        String dataUrl = "data:application/pdf;base64," + payload;
        assertThat(BioimpedanceDocumentValidator.cleanBase64(dataUrl)).isEqualTo(payload);
        assertThatCode(() -> BioimpedanceDocumentValidator.cleanBase64(payload)).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidBase64() {
        assertThatThrownBy(() -> BioimpedanceDocumentValidator.cleanBase64("%%%not-base64%%%"))
                .hasMessageContaining("inválido");
    }
}
