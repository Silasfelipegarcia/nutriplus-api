package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Consultation;
import br.com.nutriplus.domain.enums.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT COALESCE(SUM(c.platformFeeCents), 0) FROM Consultation c
            WHERE c.status = :status
              AND c.paidAt >= :from
              AND c.paidAt < :to
            """)
    long sumPlatformFeeBetween(
            @Param("status") ConsultationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(c) FROM Consultation c
            WHERE c.status = :status
              AND c.paidAt >= :from
              AND c.paidAt < :to
            """)
    long countPaidBetween(
            @Param("status") ConsultationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
