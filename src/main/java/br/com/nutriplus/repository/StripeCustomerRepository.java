package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.StripeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeCustomerRepository extends JpaRepository<StripeCustomer, Long> {

    Optional<StripeCustomer> findByUserId(Long userId);
}
