package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ProgressReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressReviewRepository extends JpaRepository<ProgressReview, Long> {

    Optional<ProgressReview> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProgressReview> findByIdAndUserId(Long id, Long userId);
}
