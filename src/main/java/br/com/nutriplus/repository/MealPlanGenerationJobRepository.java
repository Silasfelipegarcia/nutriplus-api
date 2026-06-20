package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.MealPlanGenerationJob;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MealPlanGenerationJobRepository extends JpaRepository<MealPlanGenerationJob, Long> {

    @Query("""
            SELECT j FROM MealPlanGenerationJob j
            WHERE j.user.id = :userId
            ORDER BY j.createdAt DESC
            """)
    List<MealPlanGenerationJob> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT j FROM MealPlanGenerationJob j
            WHERE j.user.id = :userId AND j.status IN :statuses
            ORDER BY j.createdAt DESC
            """)
    List<MealPlanGenerationJob> findByUserIdAndStatusIn(Long userId, List<MealPlanGenerationStatus> statuses);

    Optional<MealPlanGenerationJob> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
