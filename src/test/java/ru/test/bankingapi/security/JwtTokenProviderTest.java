package ru.test.bankingapi.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.test.bankingapi.config.TokenProperties;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {
    @Test
    @DisplayName("Провайдер выпускает отдельные access и refresh токены")
    void givenUser_whenGenerateTokenPair_thenAccessAndRefreshHaveDifferentTypes() {
        TokenProperties tokenProperties = tokenProperties();
        Clock clock = Clock.fixed(Instant.parse("2026-05-02T12:00:00Z"), ZoneOffset.UTC);
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(tokenProperties, clock);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.ROLE_ADMIN);

        AuthTokens authTokens = jwtTokenProvider.generateTokenPair(user);

        assertEquals("admin", jwtTokenProvider.extractUsername(authTokens.accessToken(), TokenType.ACCESS));
        assertEquals("admin", jwtTokenProvider.extractUsername(authTokens.refreshToken(), TokenType.REFRESH));
        assertTrue(jwtTokenProvider.isAccessTokenValid(authTokens.accessToken(), user));
        assertTrue(jwtTokenProvider.isRefreshTokenValid(authTokens.refreshToken(), user));
        assertThrows(JwtException.class,
                () -> jwtTokenProvider.extractUsername(authTokens.refreshToken(), TokenType.ACCESS));
    }

    @Test
    @DisplayName("Access token истекает по короткому TTL")
    void givenExpiredClock_whenValidateAccessToken_thenReturnsFalse() {
        TokenProperties tokenProperties = tokenProperties();
        Clock issueClock = Clock.fixed(Instant.parse("2026-05-02T12:00:00Z"), ZoneOffset.UTC);
        JwtTokenProvider issuer = new JwtTokenProvider(tokenProperties, issueClock);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.ROLE_ADMIN);

        String accessToken = issuer.generateTokenPair(user).accessToken();

        Clock expiredClock = Clock.fixed(Instant.parse("2026-05-02T12:16:00Z"), ZoneOffset.UTC);
        JwtTokenProvider validator = new JwtTokenProvider(tokenProperties, expiredClock);

        assertTrue(!validator.isAccessTokenValid(accessToken, user));
    }

    private TokenProperties tokenProperties() {
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setIssuer("banking-api");
        tokenProperties.setAudience("banking-api-clients");
        tokenProperties.getAccess().setSigningKey("MzM5Y2FjNmQ5ZGIwNGM1OTljNzE1OGQzYWZkYmFmNWE0NmZiMDA0Y2Q1MjFhZmQ1NjA0ZGE4YjcxNmIyNTRiZQ==");
        tokenProperties.getAccess().setExpiration(Duration.ofMinutes(15));
        tokenProperties.getRefresh().setSigningKey("YTQ2N2MzZTM4N2EzYzY0MGY4NzBjMDE0MDE0YjhhMzMzMDRmMmVmNGZkY2E2OTZkMzk2NGRlZGE4MmYxNDAwNQ==");
        tokenProperties.getRefresh().setExpiration(Duration.ofDays(7));
        return tokenProperties;
    }
}
