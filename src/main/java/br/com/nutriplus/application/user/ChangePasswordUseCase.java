package br.com.nutriplus.application.user;

import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordUseCase {

    private final UserQueryPort userQueryPort;
    private final UserUpdatePort userUpdatePort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;

    public ChangePasswordUseCase(
            UserQueryPort userQueryPort,
            UserUpdatePort userUpdatePort,
            PasswordHasherPort passwordHasherPort,
            TokenPort tokenPort
    ) {
        this.userQueryPort = userQueryPort;
        this.userUpdatePort = userUpdatePort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenPort = tokenPort;
    }

    @Transactional
    public LoginUseCase.Response execute(Long userId, ChangePasswordRequest request) {
        User user = userQueryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!passwordHasherPort.matches(request.currentPassword(), user.passwordHash())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessException("A nova senha deve ser diferente da atual.");
        }

        userUpdatePort.updatePassword(userId, passwordHasherPort.encode(request.newPassword()), false);
        User refreshed = userQueryPort.findById(userId).orElseThrow();

        return new LoginUseCase.Response(
                tokenPort.generateAccessToken(refreshed),
                tokenPort.generateRefreshToken(refreshed),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                false,
                refreshed
        );
    }
}
