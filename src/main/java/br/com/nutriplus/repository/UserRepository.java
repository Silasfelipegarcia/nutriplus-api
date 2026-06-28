package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPasswordResetTokenHash(String passwordResetTokenHash);
    boolean existsByEmail(String email);
    boolean existsByCpfHash(String cpfHash);

    List<User> findByLoginEnabledFalseOrderByCreatedAtAsc();

    List<User> findByLoginEnabledTrueOrderByCreatedAtDesc();

    long countByLoginEnabledFalse();

    long countByLoginEnabledTrue();

    List<User> findByRoleOrderByCreatedAtDesc(UserRole role);

    long countByRole(UserRole role);

    @Query("""
            SELECT u FROM User u
            WHERE u.autoRenew = true
              AND u.planCancelledAt IS NULL
              AND u.planValidUntil IS NOT NULL
              AND u.planValidUntil <= :limite
              AND u.defaultCardId IS NOT NULL
              AND u.subscriptionPlan IN (br.com.nutriplus.domain.enums.SubscriptionPlan.ESSENTIAL_MONTHLY,
                                         br.com.nutriplus.domain.enums.SubscriptionPlan.ESSENTIAL_YEARLY,
                                         br.com.nutriplus.domain.enums.SubscriptionPlan.ATHLETE_MONTHLY,
                                         br.com.nutriplus.domain.enums.SubscriptionPlan.ATHLETE_YEARLY)
            """)
    List<User> findDueForRenewal(@Param("limite") Instant limite);

    @Query("""
            SELECT u FROM User u
            WHERE u.trialAte IS NOT NULL
              AND u.trialAte <= :now
              AND (u.planValidUntil IS NULL OR u.planValidUntil <= :now)
            """)
    List<User> findTrialsExpirados(@Param("now") Instant now);
}
