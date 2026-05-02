package ru.test.bankingapi.security;

import java.time.Instant;

/**
 * Пара access и refresh token с временем их истечения.
 */
public record AuthTokens(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
