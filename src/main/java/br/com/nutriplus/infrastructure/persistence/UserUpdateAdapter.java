package br.com.nutriplus.infrastructure.persistence;

import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserUpdateAdapter implements UserUpdatePort {

    private final UserRepository userRepository;

    public UserUpdateAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        br.com.nutriplus.domain.entity.User entity;
        if (user.id() != null) {
            entity = userRepository.findById(user.id())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            UserPersistenceMapper.applyDomain(user, entity);
        } else {
            entity = UserPersistenceMapper.toNewEntity(user);
        }
        return UserPersistenceMapper.toDomain(userRepository.save(entity));
    }

    @Override
    public int incrementFailedLoginAttempts(Long userId) {
        br.com.nutriplus.domain.entity.User entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        int next = entity.getFailedLoginAttempts() + 1;
        entity.setFailedLoginAttempts(next);
        userRepository.save(entity);
        return next;
    }

    @Override
    public void resetFailedLoginAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(entity -> {
            if (entity.getFailedLoginAttempts() != 0) {
                entity.setFailedLoginAttempts(0);
                userRepository.save(entity);
            }
        });
    }

    @Override
    public void updatePassword(Long userId, String passwordHash, boolean passwordMustChange) {
        br.com.nutriplus.domain.entity.User entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        entity.setPasswordHash(passwordHash);
        entity.setPasswordMustChange(passwordMustChange);
        entity.setFailedLoginAttempts(0);
        userRepository.save(entity);
    }

    @Override
    public User reactivateFrozenAccount(Long userId) {
        br.com.nutriplus.domain.entity.User entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        entity.setAccountFrozenAt(null);
        entity.setLoginEnabled(true);
        entity.setLoginEnabledAt(LocalDateTime.now());
        entity.setFailedLoginAttempts(0);
        return UserPersistenceMapper.toDomain(userRepository.save(entity));
    }
}
