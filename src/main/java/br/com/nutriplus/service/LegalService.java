package br.com.nutriplus.service;

import br.com.nutriplus.dto.response.LegalDocumentResponse;
import br.com.nutriplus.infrastructure.config.LegalProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class LegalService {

    private final LegalProperties legalProperties;

    public LegalService(LegalProperties legalProperties) {
        this.legalProperties = legalProperties;
    }

    public LegalDocumentResponse terms() {
        return document(
                legalProperties.version(),
                legalProperties.termsTitle(),
                "legal/TERMS_OF_USE.md"
        );
    }

    public LegalDocumentResponse privacy() {
        return document(
                legalProperties.privacyVersion(),
                legalProperties.privacyTitle(),
                "legal/PRIVACY_POLICY.md"
        );
    }

    public LegalDocumentResponse aiDisclosure() {
        return document(
                legalProperties.version(),
                "Sobre a inteligência artificial",
                "legal/AI_DISCLOSURE.md"
        );
    }

    public LegalDocumentResponse dataSharingConsent() {
        return document(
                "2026-06-pro-1",
                "Consentimento de compartilhamento de dados",
                "legal/DATA_SHARING_CONSENT.md"
        );
    }

    public LegalDocumentResponse nutritionistTerms() {
        return document(
                "2026-06-pro-1",
                "Termos do Nutricionista",
                "legal/NUTRITIONIST_TERMS.md"
        );
    }

    private LegalDocumentResponse document(String version, String title, String classpath) {
        try {
            ClassPathResource resource = new ClassPathResource(classpath);
            String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return new LegalDocumentResponse(version, title, body);
        } catch (IOException e) {
            throw new IllegalStateException("Documento legal não encontrado: " + classpath, e);
        }
    }
}
