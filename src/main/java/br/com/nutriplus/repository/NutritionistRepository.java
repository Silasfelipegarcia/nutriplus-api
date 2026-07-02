package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Nutritionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {

    Optional<Nutritionist> findByUserId(Long userId);

    @Query("""
            SELECT n FROM Nutritionist n
            JOIN FETCH n.user
            WHERE n.marketplaceVisible = true AND n.crnVerified = true
            ORDER BY n.createdAt DESC
            """)
    List<Nutritionist> findMarketplaceListedWithUser();

    @Query("SELECT n FROM Nutritionist n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Nutritionist> findByIdWithUser(@Param("id") Long id);

    List<Nutritionist> findByMarketplaceVisibleTrueAndCrnVerifiedTrueOrderByCreatedAtDesc();

    List<Nutritionist> findByCrnVerifiedFalseOrderByCreatedAtAsc();

    long countByCrnVerifiedFalse();
}
