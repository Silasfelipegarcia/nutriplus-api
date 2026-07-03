package br.com.nutriplus.application.user;

import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReactivateFrozenAccountUseCase {

    private final UserQueryPort userQueryPort;
    private final UserUpdatePort userUpdatePort;
    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;
    private final AuditLogService auditLogService;

    public ReactivateFrozenAccountUseCase(UserQueryPort userQueryPort,
                                          UserUpdatePort userUpdatePort,
                                          UserRepository userRepository,
                                          PasswordHasherPort passwordHasherPort,
                                          TokenPort tokenPort,
                                          AuditLogService auditLogService) {
        this.userQueryPort = userQueryPort;
        this.userUpdatePort = userUpdatePort;
        this.userRepository = userRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenPort = tokenPort;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public LoginUseCase.Response execute(LoginRequest request) {
        br.com.nutriplus.domain.model.User user = userQueryPort.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha incorretos."));

        if (!user.accountFrozen()) {
            throw new BusinessException("Esta conta não está congelada. Use o login normal.");
        }
        if (!passwordHasherPort.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException("E-mail ou senha incorretos.");
        }

        br.com.nutriplus.domain.model.User reactivated = userUpdatePort.reactivateFrozenAccount(user.id());
        User entity = userRepository.findById(reactivated.id()).orElseThrow();
        auditLogService.log("ACCOUNT_REACTIVATED", "USER", entity);

        return new LoginUseCase.Response(
                tokenPort.generateAccessToken(reactivated),
                tokenPort.generateRefreshToken(reactivated),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                reactivated.passwordMustChange(),
                reactivated
        );
    }
}
