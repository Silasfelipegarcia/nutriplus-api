package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @Query("SELECT mp FROM MealPlan mp WHERE mp.user.id = :userId ORDER BY mp.createdAt DESC")
    List<MealPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
