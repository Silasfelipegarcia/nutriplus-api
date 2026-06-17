package br.com.nutriplus.application.port;

import br.com.nutriplus.domain.model.User;

public interface UserUpdatePort {

    User save(User user);

    int incrementFailedLoginAttempts(Long userId);

    void resetFailedLoginAttempts(Long userId);

    void updatePassword(Long userId, String passwordHash, boolean passwordMustChange);
}
