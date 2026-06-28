package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.CareRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CareRatingRepository extends JpaRepository<CareRating, Long> {

    Optional<CareRating> findByCareRelationshipId(Long careRelationshipId);

    List<CareRating> findTop10ByNutritionistIdOrderByCreatedAtDesc(Long nutritionistId);

    @Query("SELECT AVG(r.stars) FROM CareRating r WHERE r.nutritionist.id = :nutritionistId")
    Double averageStarsByNutritionistId(Long nutritionistId);

    long countByNutritionistId(Long nutritionistId);

    @Query("""
            SELECT r.nutritionist.id, AVG(r.stars), COUNT(r)
            FROM CareRating r
            WHERE r.nutritionist.id IN :nutritionistIds
            GROUP BY r.nutritionist.id
            """)
    List<Object[]> avgStarsAndCountByNutritionistIds(@Param("nutritionistIds") Collection<Long> nutritionistIds);
}
