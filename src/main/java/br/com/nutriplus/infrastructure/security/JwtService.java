package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService implements TokenPort {

    static final String TOKEN_TYPE_CLAIM = "typ";
    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.expirationSeconds());

        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim(TOKEN_TYPE_CLAIM, TYPE_ACCESS)
                .claim("email", user.email())
                .claim("roles", List.of("USER"))
                .claim("passwordMustChange", user.passwordMustChange())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSecretKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.refreshExpirationSeconds());

        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim(TOKEN_TYPE_CLAIM, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSecretKey())
                .compact();
    }

    @Override
    public Long extractRefreshUserId(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token ausente.");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(refreshToken.strip())
                    .getPayload();
            if (!TYPE_REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new InvalidTokenException("Token informado não é um refresh token.");
            }
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new InvalidTokenException("Refresh token sem usuário.");
            }
            return Long.parseLong(subject);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Refresh token inválido ou expirado.");
        }
    }

    public SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public long accessExpirationSeconds() {
        return jwtProperties.expirationSeconds();
    }
}
