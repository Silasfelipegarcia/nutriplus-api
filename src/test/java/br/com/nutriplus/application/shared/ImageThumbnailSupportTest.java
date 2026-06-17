package br.com.nutriplus.application.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageThumbnailSupportTest {

    @Test
    void returnsEmptyForNullOrExternalUrl() {
        assertThat(ImageThumbnailSupport.thumbnailFromPhoto(null)).isEmpty();
        assertThat(ImageThumbnailSupport.thumbnailFromPhoto("https://example.com/photo.jpg")).isEmpty();
    }

    @Test
    void returnsEmptyForInvalidDataUrl() {
        assertThat(ImageThumbnailSupport.thumbnailFromPhoto("data:image/jpeg;base64,!!!")).isEmpty();
    }
}
