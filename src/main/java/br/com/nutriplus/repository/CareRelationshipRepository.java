package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.CareRelationship;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CareRelationshipRepository extends JpaRepository<CareRelationship, Long> {

    Optional<CareRelationship> findByPatientIdAndNutritionistId(Long patientId, Long nutritionistId);

    List<CareRelationship> findByNutritionistIdOrderByUpdatedAtDesc(Long nutritionistId);

    List<CareRelationship> findByNutritionistIdAndStatusInOrderByUpdatedAtDesc(
            Long nutritionistId, List<CareRelationshipStatus> statuses);

    List<CareRelationship> findByPatientIdOrderByUpdatedAtDesc(Long patientId);

    @Query("SELECT cr FROM CareRelationship cr WHERE cr.patient.id = :patientId AND cr.status = 'ACTIVE'")
    List<CareRelationship> findActiveByPatientId(Long patientId);

    List<CareRelationship> findByStatusAndExpiresAtBefore(CareRelationshipStatus status, java.time.LocalDateTime expiresAt);
}
