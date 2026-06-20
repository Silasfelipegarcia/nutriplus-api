package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.NutritionistInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionistInviteRepository extends JpaRepository<NutritionistInvite, Long> {

    Optional<NutritionistInvite> findByCode(String code);
}
