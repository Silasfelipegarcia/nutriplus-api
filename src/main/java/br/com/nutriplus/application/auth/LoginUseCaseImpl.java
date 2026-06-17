package br.com.nutriplus.application.auth;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.AccountLockedException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;

    private static final String LOCKED_MESSAGE =
            "Conta bloqueada após várias tentativas falhadas. Entre em contato com o suporte ou aguarde.";

    private final UserQueryPort userQueryPort;
    private final UserUpdatePort userUpdatePort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;

    public LoginUseCaseImpl(
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

    @Override
    @Transactional
    public Response execute(Request request) {
        User user = userQueryPort.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (user.failedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            throw new AccountLockedException(LOCKED_MESSAGE);
        }

        if (!passwordHasherPort.matches(request.password(), user.passwordHash())) {
            int attempts = userUpdatePort.incrementFailedLoginAttempts(user.id());
            if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                throw new AccountLockedException(LOCKED_MESSAGE);
            }
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        userUpdatePort.resetFailedLoginAttempts(user.id());
        User refreshed = userQueryPort.findById(user.id()).orElse(user);
        return new Response(
                tokenPort.generateAccessToken(refreshed),
                tokenPort.generateRefreshToken(refreshed),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                refreshed.passwordMustChange(),
                refreshed
        );
    }
}
