package ru.test.bankingapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.RefreshToken;
import ru.test.bankingapi.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-05-02T12:00:00Z"), ZoneOffset.UTC);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, clock);
    }

    @Test
    @DisplayName("Ротация refresh token отзывает старый и сохраняет новый")
    void givenActiveToken_whenRotate_thenRevokesCurrentAndStoresNext() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        RefreshToken current = RefreshToken.issue(hash("old-refresh"), user, Instant.parse("2026-05-09T12:00:00Z"));

        when(refreshTokenRepository.findByTokenHash(hash("old-refresh"))).thenReturn(Optional.of(current));

        refreshTokenService.rotate("old-refresh", "new-refresh", Instant.parse("2026-05-10T12:00:00Z"));

        assertNotNull(current.getRevokedAt());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Просроченный refresh token отклоняется")
    void givenExpiredToken_whenGetActive_thenThrowsBadRequest() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        RefreshToken expired = RefreshToken.issue(hash("expired-refresh"), user, Instant.parse("2026-05-01T12:00:00Z"));

        when(refreshTokenRepository.findByTokenHash(hash("expired-refresh"))).thenReturn(Optional.of(expired));

        assertThrows(BadRequestException.class, () -> refreshTokenService.getActive("expired-refresh"));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
