package br.com.nutriplus.dto.response;

public record LegalDocumentResponse(
        String version,
        String title,
        String bodyMarkdown
) {
}
