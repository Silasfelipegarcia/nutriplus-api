package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.UserAppFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAppFeedbackRepository extends JpaRepository<UserAppFeedback, Long> {

    Optional<UserAppFeedback> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
