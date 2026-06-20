package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.LegalDocumentResponse;
import br.com.nutriplus.service.LegalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal")
public class LegalController {

    private final LegalService legalService;

    public LegalController(LegalService legalService) {
        this.legalService = legalService;
    }

    @GetMapping("/terms")
    public LegalDocumentResponse terms() {
        return legalService.terms();
    }

    @GetMapping("/privacy")
    public LegalDocumentResponse privacy() {
        return legalService.privacy();
    }

    @GetMapping("/ai-disclosure")
    public LegalDocumentResponse aiDisclosure() {
        return legalService.aiDisclosure();
    }

    @GetMapping("/data-sharing-consent")
    public LegalDocumentResponse dataSharingConsent() {
        return legalService.dataSharingConsent();
    }

    @GetMapping("/nutritionist-terms")
    public LegalDocumentResponse nutritionistTerms() {
        return legalService.nutritionistTerms();
    }
}
