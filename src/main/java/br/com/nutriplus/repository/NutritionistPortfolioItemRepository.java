package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.NutritionistPortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NutritionistPortfolioItemRepository extends JpaRepository<NutritionistPortfolioItem, Long> {

    List<NutritionistPortfolioItem> findByNutritionistIdOrderBySortOrderAsc(Long nutritionistId);

    @Query("""
            SELECT p FROM NutritionistPortfolioItem p
            JOIN FETCH p.nutritionist n
            WHERE n.id IN :nutritionistIds
            ORDER BY n.id ASC, p.sortOrder ASC
            """)
    List<NutritionistPortfolioItem> findByNutritionistIdInWithNutritionist(
            @Param("nutritionistIds") Collection<Long> nutritionistIds);

    void deleteByNutritionistId(Long nutritionistId);
}
