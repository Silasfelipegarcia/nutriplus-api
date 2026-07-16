package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.MealItemSwapEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MealItemSwapEventRepository extends JpaRepository<MealItemSwapEvent, Long> {

    @Query("""
            SELECT e FROM MealItemSwapEvent e
            WHERE e.user.id = :userId AND e.createdAt >= :from
            ORDER BY e.createdAt DESC
            """)
    List<MealItemSwapEvent> findByUserIdAndCreatedAtGreaterThanEqual(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from);
}
