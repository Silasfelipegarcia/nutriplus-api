package br.com.nutriplus.application.port;

import br.com.nutriplus.domain.model.User;

public interface TokenPort {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    Long extractRefreshUserId(String refreshToken);

    long accessExpirationSeconds();

    class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
