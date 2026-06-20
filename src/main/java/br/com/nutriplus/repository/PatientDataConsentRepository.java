package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PatientDataConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDataConsentRepository extends JpaRepository<PatientDataConsent, Long> {

    Optional<PatientDataConsent> findByCareRelationshipId(Long careRelationshipId);
}
