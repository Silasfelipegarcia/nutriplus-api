package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.DailyMealCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DailyMealCheckinRepository extends JpaRepository<DailyMealCheckin, Long> {

    List<DailyMealCheckin> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    @Query("""
            SELECT c FROM DailyMealCheckin c
            WHERE c.user.id = :userId AND c.checkinDate BETWEEN :start AND :end
            """)
    List<DailyMealCheckin> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);

    Optional<DailyMealCheckin> findByUserIdAndCheckinDateAndMealId(Long userId, LocalDate checkinDate, Long mealId);

    void deleteByUserIdAndCheckinDateAndMealId(Long userId, LocalDate checkinDate, Long mealId);

    @Query("""
            SELECT COUNT(c) FROM DailyMealCheckin c
            WHERE c.user.id = :userId AND c.meal.id IN :mealIds
            """)
    long countByUserIdAndMealIdIn(@Param("userId") Long userId, @Param("mealIds") Collection<Long> mealIds);

    @Modifying
    @Query("""
            DELETE FROM DailyMealCheckin c
            WHERE c.user.id = :userId AND c.meal.id IN :mealIds
            """)
    void deleteByUserIdAndMealIdIn(@Param("userId") Long userId, @Param("mealIds") Collection<Long> mealIds);
}
