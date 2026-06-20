package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.PricingGuideline;
import br.com.nutriplus.dto.response.PricingGuidelinesResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.repository.PricingGuidelineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingGuidelineService {

    private final PricingGuidelineRepository repository;
    private final ProMapper proMapper;

    public PricingGuidelineService(PricingGuidelineRepository repository, ProMapper proMapper) {
        this.repository = repository;
        this.proMapper = proMapper;
    }

    public PricingGuidelinesResponse getGuidelines() {
        return proMapper.toPricing(requireGuidelines());
    }

    public PricingGuideline requireGuidelines() {
        return repository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("Diretrizes de preço não configuradas."));
    }

    public void validatePrice(int priceCents) {
        PricingGuideline g = requireGuidelines();
        if (priceCents < g.getMinConsultationPriceCents() || priceCents > g.getMaxConsultationPriceCents()) {
            throw new BusinessException(String.format(
                    "Preço deve estar entre R$ %.2f e R$ %.2f.",
                    g.getMinConsultationPriceCents() / 100.0,
                    g.getMaxConsultationPriceCents() / 100.0));
        }
    }

    public int calculatePlatformFee(int amountCents) {
        PricingGuideline g = requireGuidelines();
        BigDecimal fee = g.getPlatformFeePercent()
                .multiply(BigDecimal.valueOf(amountCents))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return fee.intValue();
    }
}
