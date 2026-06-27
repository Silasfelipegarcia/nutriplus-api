package br.com.nutriplus.application.auth;

import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenUseCase {

    private final UserQueryPort userQueryPort;
    private final TokenPort tokenPort;

    public RefreshTokenUseCase(UserQueryPort userQueryPort, TokenPort tokenPort) {
        this.userQueryPort = userQueryPort;
        this.tokenPort = tokenPort;
    }

    public LoginUseCase.Response execute(String refreshToken) {
        Long userId = tokenPort.extractRefreshUserId(refreshToken);
        User user = userQueryPort.findById(userId)
                .orElseThrow(() -> new TokenPort.InvalidTokenException("Usuário do refresh token não existe mais."));
        LoginAccessPolicy.ensureCanLogin(user);
        return new LoginUseCase.Response(
                tokenPort.generateAccessToken(user),
                tokenPort.generateRefreshToken(user),
                "Bearer",
                tokenPort.accessExpirationSeconds(),
                user.passwordMustChange(),
                user
        );
    }
}
