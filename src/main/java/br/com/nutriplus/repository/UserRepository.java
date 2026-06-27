package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpfHash(String cpfHash);

    List<User> findByLoginEnabledFalseOrderByCreatedAtAsc();

    List<User> findByLoginEnabledTrueOrderByCreatedAtDesc();

    long countByLoginEnabledFalse();

    long countByLoginEnabledTrue();

    List<User> findByRoleOrderByCreatedAtDesc(UserRole role);

    long countByRole(UserRole role);
}
