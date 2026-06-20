package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.DailyFoodExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyFoodExtraRepository extends JpaRepository<DailyFoodExtra, Long> {

    List<DailyFoodExtra> findByUserIdAndEntryDateOrderByCreatedAtAsc(Long userId, LocalDate entryDate);
}
