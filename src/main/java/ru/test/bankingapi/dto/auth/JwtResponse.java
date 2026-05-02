package ru.test.bankingapi.dto.auth;

import ru.test.bankingapi.security.AuthTokens;

import java.time.Instant;

public record JwtResponse(
        String token,
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
    public static JwtResponse from(AuthTokens authTokens) {
        return new JwtResponse(
                authTokens.accessToken(),
                authTokens.accessToken(),
                authTokens.refreshToken(),
                "Bearer",
                authTokens.accessTokenExpiresAt(),
                authTokens.refreshTokenExpiresAt()
        );
    }
}
