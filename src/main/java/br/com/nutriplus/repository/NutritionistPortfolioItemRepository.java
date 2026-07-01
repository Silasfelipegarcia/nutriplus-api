package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.NutritionistPortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NutritionistPortfolioItemRepository extends JpaRepository<NutritionistPortfolioItem, Long> {

    List<NutritionistPortfolioItem> findByNutritionistIdOrderBySortOrderAsc(Long nutritionistId);

    List<NutritionistPortfolioItem> findByNutritionistIdInOrderByNutritionistIdAscSortOrderAsc(Collection<Long> nutritionistIds);

    void deleteByNutritionistId(Long nutritionistId);
}
