package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.SubscriptionPlanCatalog;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanCatalogRepository extends JpaRepository<SubscriptionPlanCatalog, Long> {

    Optional<SubscriptionPlanCatalog> findByPlanCode(SubscriptionPlan planCode);

    List<SubscriptionPlanCatalog> findAllByOrderBySortOrderAscPlanCodeAsc();

    List<SubscriptionPlanCatalog> findByEnabledTrueAndVisibleInCatalogTrueOrderBySortOrderAscPlanCodeAsc();
}
