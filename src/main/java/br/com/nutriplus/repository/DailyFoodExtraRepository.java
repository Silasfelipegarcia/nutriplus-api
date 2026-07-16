package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.DailyFoodExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyFoodExtraRepository extends JpaRepository<DailyFoodExtra, Long> {

    List<DailyFoodExtra> findByUserIdAndEntryDateOrderByCreatedAtAsc(Long userId, LocalDate entryDate);

    List<DailyFoodExtra> findByUserIdAndEntryDateBetweenOrderByEntryDateAscCreatedAtAsc(
            Long userId, LocalDate start, LocalDate end);

    @Modifying
    @Query("""
            DELETE FROM DailyFoodExtra e
            WHERE e.user.id = :userId AND e.entryDate >= :fromDate
            """)
    void deleteByUserIdAndEntryDateGreaterThanEqual(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);

    @Modifying
    @Query("""
            DELETE FROM DailyFoodExtra e
            WHERE e.user.id = :userId AND e.entryDate = :entryDate
            """)
    void deleteByUserIdAndEntryDate(@Param("userId") Long userId, @Param("entryDate") LocalDate entryDate);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM DailyFoodExtra e
            WHERE e.id = :id AND e.user.id = :userId
            """)
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
