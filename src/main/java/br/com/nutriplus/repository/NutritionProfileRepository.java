package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.NutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfile, Long> {
    Optional<NutritionProfile> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM NutritionProfile p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE nutrition_profiles SET created_at = :createdAt WHERE id = :id", nativeQuery = true)
    void backdateCreatedAt(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt);
}
