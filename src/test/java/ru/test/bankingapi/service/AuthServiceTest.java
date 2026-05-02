package ru.test.bankingapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import ru.test.bankingapi.dto.auth.JwtResponse;
import ru.test.bankingapi.dto.auth.RefreshTokenRequest;
import ru.test.bankingapi.dto.auth.SignInRequest;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.security.AuthTokens;
import ru.test.bankingapi.security.JwtTokenProvider;
import ru.test.bankingapi.security.TokenType;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;
    private AppUser user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, jwtTokenProvider, authenticationManager, refreshTokenService);

        user = new AppUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Вход выдает access и refresh token")
    void givenCredentials_whenSignIn_thenReturnsTokenPairAndStoresRefreshToken() {
        SignInRequest request = new SignInRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        AuthTokens authTokens = new AuthTokens(
                "access-token",
                "refresh-token",
                Instant.parse("2026-05-02T12:15:00Z"),
                Instant.parse("2026-05-09T12:00:00Z")
        );

        when(userService.loadUserByUsername("admin")).thenReturn(user);
        when(jwtTokenProvider.generateTokenPair(user)).thenReturn(authTokens);

        JwtResponse response = authService.signIn(request);

        assertEquals("access-token", response.token());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenService).register(user, "refresh-token", authTokens.refreshTokenExpiresAt());
        verify(authenticationManager).authenticate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Обновление access token ротирует refresh token")
    void givenValidRefreshToken_whenRefresh_thenRotatesRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh");

        AuthTokens authTokens = new AuthTokens(
                "new-access",
                "new-refresh",
                Instant.parse("2026-05-02T12:15:00Z"),
                Instant.parse("2026-05-09T12:00:00Z")
        );

        when(jwtTokenProvider.extractUsername("old-refresh", TokenType.REFRESH)).thenReturn("admin");
        when(userService.loadUserByUsername("admin")).thenReturn(user);
        when(jwtTokenProvider.isRefreshTokenValid("old-refresh", user)).thenReturn(true);
        when(jwtTokenProvider.generateTokenPair(user)).thenReturn(authTokens);

        JwtResponse response = authService.refresh(request);

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        verify(refreshTokenService).getActive("old-refresh");
        verify(refreshTokenService).rotate("old-refresh", "new-refresh", authTokens.refreshTokenExpiresAt());
    }

    @Test
    @DisplayName("Выход отзывает refresh token")
    void givenRefreshToken_whenLogout_thenRevokesIt() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(jwtTokenProvider.extractUsername("refresh-token", TokenType.REFRESH)).thenReturn("admin");
        when(userService.loadUserByUsername("admin")).thenReturn(user);
        when(jwtTokenProvider.isRefreshTokenValid("refresh-token", user)).thenReturn(true);

        authService.logout(request);

        verify(refreshTokenService).getActive("refresh-token");
        verify(refreshTokenService).revoke("refresh-token");
    }
}
