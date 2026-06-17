package br.com.nutriplus.dto.response;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
}
