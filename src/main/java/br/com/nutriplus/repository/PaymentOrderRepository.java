package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByMpPaymentId(String mpPaymentId);

    Optional<PaymentOrder> findByMpPreferenceId(String mpPreferenceId);

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentOrder> findTop3ByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
