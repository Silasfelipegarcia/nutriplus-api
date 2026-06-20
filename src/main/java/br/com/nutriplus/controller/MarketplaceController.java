package br.com.nutriplus.controller;

import br.com.nutriplus.domain.enums.ServiceMode;
import br.com.nutriplus.dto.response.NutritionistPublicResponse;
import br.com.nutriplus.dto.response.PricingGuidelinesResponse;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.service.CareService;
import br.com.nutriplus.service.PricingGuidelineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MarketplaceController {

    private final CareService careService;
    private final ProMapper proMapper;
    private final PricingGuidelineService pricingGuidelineService;

    public MarketplaceController(CareService careService,
                                 ProMapper proMapper,
                                 PricingGuidelineService pricingGuidelineService) {
        this.careService = careService;
        this.proMapper = proMapper;
        this.pricingGuidelineService = pricingGuidelineService;
    }

    @GetMapping("/nutritionists")
    public List<NutritionistPublicResponse> list(
            @RequestParam(required = false) ServiceMode mode,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city) {
        return careService.listMarketplace(mode, state, city).stream().map(proMapper::toPublic).toList();
    }

    @GetMapping("/nutritionists/{id}")
    public NutritionistPublicResponse get(@PathVariable Long id) {
        return proMapper.toPublic(careService.getMarketplaceNutritionist(id));
    }

    @GetMapping("/pricing/guidelines")
    public PricingGuidelinesResponse guidelines() {
        return pricingGuidelineService.getGuidelines();
    }
}
