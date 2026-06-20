package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.DailyMealCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
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
}
