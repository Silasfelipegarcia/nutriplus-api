package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.UpdateFeatureFlagRequest;
import br.com.nutriplus.dto.response.FeatureFlagResponse;
import br.com.nutriplus.service.FeatureFlagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping("/feature-flags")
    public List<FeatureFlagResponse> listPublic() {
        return featureFlagService.listPublic();
    }

    @GetMapping("/admin/feature-flags")
    public List<FeatureFlagResponse> listForAdmin() {
        return featureFlagService.listForAdmin();
    }

    @PatchMapping("/admin/feature-flags/{code}")
    public FeatureFlagResponse update(@PathVariable String code,
                                      @Valid @RequestBody UpdateFeatureFlagRequest request) {
        return featureFlagService.update(code, request);
    }
}
