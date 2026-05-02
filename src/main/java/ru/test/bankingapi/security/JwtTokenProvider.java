package ru.test.bankingapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.test.bankingapi.config.TokenProperties;
import ru.test.bankingapi.model.AppUser;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Провайдер выпуска и проверки access и refresh JWT-токенов.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final String AUDIENCE_CLAIM = "aud";
    private static final String TYPE_CLAIM = "type";

    private final TokenProperties tokenProperties;
    private final Clock clock;

    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    public AuthTokens generateTokenPair(UserDetails userDetails) {
        Instant issuedAt = clock.instant();
        Instant accessExpiresAt = issuedAt.plus(tokenProperties.getAccess().getExpiration());
        Instant refreshExpiresAt = issuedAt.plus(tokenProperties.getRefresh().getExpiration());

        return new AuthTokens(
                buildToken(userDetails, TokenType.ACCESS, issuedAt, accessExpiresAt),
                buildToken(userDetails, TokenType.REFRESH, issuedAt, refreshExpiresAt),
                accessExpiresAt,
                refreshExpiresAt
        );
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, TokenType.ACCESS);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, TokenType.REFRESH);
    }

    private String buildToken(UserDetails userDetails, TokenType tokenType, Instant issuedAt, Instant expiresAt) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof AppUser user) {
            claims.put("id", user.getId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(tokenProperties.getIssuer())
                .id(UUID.randomUUID().toString())
                .claim(AUDIENCE_CLAIM, tokenProperties.getAudience())
                .claim(TYPE_CLAIM, tokenType.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey(tokenType))
                .compact();
    }

    private boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        try {
            Claims claims = extractAllClaims(token, tokenType);
            return claims.getSubject().equals(userDetails.getUsername())
                    && !claims.getExpiration().before(Date.from(clock.instant()));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private <T> T extractClaim(String token, TokenType tokenType, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token, tokenType));
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        Claims claims = Jwts.parser()
                .clock(() -> Date.from(clock.instant()))
                .verifyWith(getSigningKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        validateClaims(claims, tokenType);
        return claims;
    }

    private void validateClaims(Claims claims, TokenType tokenType) {
        if (!tokenProperties.getIssuer().equals(claims.getIssuer())) {
            throw new JwtException("Некорректный issuer");
        }
        Object audienceClaim = claims.get(AUDIENCE_CLAIM);
        boolean audienceMatches = audienceClaim instanceof String audience
                ? tokenProperties.getAudience().equals(audience)
                : audienceClaim instanceof Collection<?> audiences && audiences.contains(tokenProperties.getAudience());
        if (!audienceMatches) {
            throw new JwtException("Некорректный audience");
        }
        if (!tokenType.name().equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new JwtException("Некорректный тип токена");
        }
    }

    private SecretKey getSigningKey(TokenType tokenType) {
        String signingKey = tokenType == TokenType.ACCESS
                ? tokenProperties.getAccess().getSigningKey()
                : tokenProperties.getRefresh().getSigningKey();
        byte[] keyBytes = Decoders.BASE64.decode(signingKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
