package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Consultation;
import br.com.nutriplus.domain.enums.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByStripePaymentIntentId(String paymentIntentId);

    @Query("""
            SELECT c FROM Consultation c
            JOIN c.careRelationship cr
            WHERE cr.nutritionist.id = :nutritionistId
              AND c.status = :status
              AND c.paidAt >= :from
              AND c.paidAt < :to
            """)
    List<Consultation> findPaidByNutritionistBetween(
            Long nutritionistId, ConsultationStatus status, LocalDateTime from, LocalDateTime to);
}
