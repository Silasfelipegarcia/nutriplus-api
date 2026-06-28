package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.UserLegalAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLegalAcceptanceRepository extends JpaRepository<UserLegalAcceptance, Long> {
}
