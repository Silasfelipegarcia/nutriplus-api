package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PlanRevision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRevisionRepository extends JpaRepository<PlanRevision, Long> {
}
