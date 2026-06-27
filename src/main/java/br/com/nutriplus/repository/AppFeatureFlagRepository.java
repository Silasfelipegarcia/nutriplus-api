package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.AppFeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppFeatureFlagRepository extends JpaRepository<AppFeatureFlag, Long> {
    Optional<AppFeatureFlag> findByCode(String code);

    List<AppFeatureFlag> findAllByOrderByCodeAsc();
}
