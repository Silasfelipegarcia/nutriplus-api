package br.com.nutriplus.service;

import br.com.nutriplus.domain.FeatureFlags;
import org.springframework.stereotype.Service;

@Service
public class BillingEnforcementService {

    private final FeatureFlagService featureFlagService;

    public BillingEnforcementService(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    public boolean isBillingEnabled() {
        return featureFlagService.isEnabled(FeatureFlags.SUBSCRIPTION_BILLING);
    }

    public void requireBillingEnabled() {
        if (!isBillingEnabled()) {
            throw new br.com.nutriplus.exception.BusinessException(
                    "A cobrança de planos ainda não está habilitada na plataforma.");
        }
    }
}
