package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ProductEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductEventRepository extends JpaRepository<ProductEvent, Long> {
}
