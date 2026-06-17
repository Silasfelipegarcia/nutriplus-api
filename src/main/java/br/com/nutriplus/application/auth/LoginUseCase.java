package br.com.nutriplus.application.auth;

import br.com.nutriplus.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface LoginUseCase {

    Response execute(Request request);

    record Request(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    record Response(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            boolean passwordMustChange,
            User user
    ) {
    }
}
