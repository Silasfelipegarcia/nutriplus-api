package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BodyMeasurementSessionRepository extends JpaRepository<BodyMeasurementSession, Long> {

    List<BodyMeasurementSession> findTop2ByUserIdOrderByMeasuredOnDescIdDesc(Long userId);

    List<BodyMeasurementSession> findTop12ByUserIdOrderByMeasuredOnAscIdAsc(Long userId);

    Optional<BodyMeasurementSession> findFirstByUserIdOrderByMeasuredOnDescIdDesc(Long userId);

    Optional<BodyMeasurementSession> findFirstByUserIdOrderByMeasuredOnAscIdAsc(Long userId);
}
