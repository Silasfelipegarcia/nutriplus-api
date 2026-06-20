package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PricingGuideline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingGuidelineRepository extends JpaRepository<PricingGuideline, Long> {
}
