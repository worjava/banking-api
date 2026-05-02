package ru.test.bankingapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.RefreshToken;
import ru.test.bankingapi.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Сервис хранения, проверки и отзыва refresh token.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    @Transactional
    public void register(AppUser user, String rawRefreshToken, Instant expiresAt) {
        refreshTokenRepository.save(RefreshToken.issue(hash(rawRefreshToken), user, expiresAt));
    }

    @Transactional
    public void rotate(String currentRawToken, String nextRawToken, Instant nextExpiresAt) {
        RefreshToken current = getActive(currentRawToken);
        current.revoke(clock.instant());
        refreshTokenRepository.save(RefreshToken.issue(hash(nextRawToken), current.getUser(), nextExpiresAt));
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        RefreshToken refreshToken = getActive(rawRefreshToken);
        refreshToken.revoke(clock.instant());
    }

    @Transactional(readOnly = true)
    public RefreshToken getActive(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new BadRequestException("Refresh token не найден"));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token уже отозван");
        }
        if (refreshToken.isExpired(clock.instant())) {
            throw new BadRequestException("Refresh token истек");
        }

        return refreshToken;
    }

    private String hash(String rawRefreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 недоступен", ex);
        }
    }
}
