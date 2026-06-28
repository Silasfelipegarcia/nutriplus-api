package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByMpPaymentId(String mpPaymentId);

    Optional<PaymentOrder> findByMpPreferenceId(String mpPreferenceId);

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentOrder> findTop3ByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Query("""
            SELECT COALESCE(SUM(o.amountCents), 0) FROM PaymentOrder o
            WHERE o.status = 'APPROVED'
              AND o.paidAt >= :from
              AND o.paidAt < :to
            """)
    long sumApprovedAmountBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(o) FROM PaymentOrder o
            WHERE o.status = 'APPROVED'
              AND o.paidAt >= :from
              AND o.paidAt < :to
            """)
    long countApprovedBetween(@Param("from") Instant from, @Param("to") Instant to);
}
