package br.com.nutriplus.application.port;

import br.com.nutriplus.domain.model.User;

import java.util.Optional;

public interface UserQueryPort {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
