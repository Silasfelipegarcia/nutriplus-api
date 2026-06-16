package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {
}
