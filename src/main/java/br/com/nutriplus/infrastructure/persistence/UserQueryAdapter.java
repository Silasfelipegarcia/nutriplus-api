package br.com.nutriplus.infrastructure.persistence;

import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    public UserQueryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
