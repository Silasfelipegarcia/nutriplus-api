package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgressReviewRepository extends JpaRepository<ProgressReview, Long> {

    Optional<ProgressReview> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProgressReview> findFirstByUserIdAndStatusOrderByCompletedAtDesc(
            Long userId, ProgressReviewStatus status);

    /**
     * Loads measurement sessions eagerly — required with {@code spring.jpa.open-in-view=false}.
     * Pass {@code PageRequest.of(0, 1)} for the latest review.
     */
    @Query("""
            SELECT r FROM ProgressReview r
            JOIN FETCH r.currentSession
            LEFT JOIN FETCH r.previousSession
            WHERE r.user.id = :userId AND r.status = :status
            ORDER BY r.completedAt DESC
            """)
    List<ProgressReview> findCompletedWithSessionsOrderByCompletedAtDesc(
            @Param("userId") Long userId,
            @Param("status") ProgressReviewStatus status,
            Pageable pageable);

    Optional<ProgressReview> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("""
            DELETE FROM ProgressReview r
            WHERE r.user.id = :userId AND r.createdAt >= :fromDateTime
            """)
    void deleteByUserIdAndCreatedAtGreaterThanEqual(
            @Param("userId") Long userId,
            @Param("fromDateTime") LocalDateTime fromDateTime);
}
