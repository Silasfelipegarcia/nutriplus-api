package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProgressReviewRepository extends JpaRepository<ProgressReview, Long> {

    Optional<ProgressReview> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProgressReview> findFirstByUserIdAndStatusOrderByCompletedAtDesc(
            Long userId, ProgressReviewStatus status);

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
