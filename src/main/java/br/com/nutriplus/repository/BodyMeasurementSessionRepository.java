package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BodyMeasurementSessionRepository extends JpaRepository<BodyMeasurementSession, Long> {

    List<BodyMeasurementSession> findTop2ByUserIdOrderByMeasuredOnDescIdDesc(Long userId);

    List<BodyMeasurementSession> findTop12ByUserIdOrderByMeasuredOnAscIdAsc(Long userId);

    List<BodyMeasurementSession> findByUserIdOrderByMeasuredOnAscIdAsc(Long userId);

    Optional<BodyMeasurementSession> findFirstByUserIdOrderByMeasuredOnDescIdDesc(Long userId);

    Optional<BodyMeasurementSession> findFirstByUserIdAndMeasuredOnOrderByIdDesc(Long userId, LocalDate measuredOn);

    Optional<BodyMeasurementSession> findFirstByUserIdOrderByMeasuredOnAscIdAsc(Long userId);

    @Modifying
    @Query("""
            DELETE FROM BodyMeasurementSession b
            WHERE b.user.id = :userId AND b.measuredOn >= :fromDate
            """)
    void deleteByUserIdAndMeasuredOnGreaterThanEqual(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate);
}
