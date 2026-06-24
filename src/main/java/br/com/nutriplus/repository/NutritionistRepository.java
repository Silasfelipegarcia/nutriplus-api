package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Nutritionist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {

    Optional<Nutritionist> findByUserId(Long userId);

    List<Nutritionist> findByMarketplaceVisibleTrueAndCrnVerifiedTrueOrderByCreatedAtDesc();

    List<Nutritionist> findByCrnVerifiedFalseOrderByCreatedAtAsc();
}
