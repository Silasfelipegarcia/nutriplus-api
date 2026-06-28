package br.com.nutriplus.service;

import br.com.nutriplus.dto.response.PlanCatalogItemResponse;
import br.com.nutriplus.dto.response.PlanCatalogResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanCatalogService {

    private final SubscriptionPlanCatalogService catalogService;
    private final BillingEnforcementService billingEnforcementService;

    public PlanCatalogService(SubscriptionPlanCatalogService catalogService,
                              BillingEnforcementService billingEnforcementService) {
        this.catalogService = catalogService;
        this.billingEnforcementService = billingEnforcementService;
    }

    public PlanCatalogResponse catalogo() {
        List<PlanCatalogItemResponse> plans = catalogService.catalogoPublico();
        return new PlanCatalogResponse(billingEnforcementService.isBillingEnabled(), plans);
    }
}
