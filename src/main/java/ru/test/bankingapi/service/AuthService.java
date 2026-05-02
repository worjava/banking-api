package ru.test.bankingapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.test.bankingapi.dto.auth.JwtResponse;
import ru.test.bankingapi.dto.auth.RefreshTokenRequest;
import ru.test.bankingapi.dto.auth.SignInRequest;
import ru.test.bankingapi.dto.auth.SignUpRequest;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.security.AuthTokens;
import ru.test.bankingapi.security.JwtTokenProvider;
import ru.test.bankingapi.security.TokenType;

/**
 * Сервис входа, регистрации и ротации JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public JwtResponse signUp(SignUpRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.ROLE_USER);
        return issueTokens(userService.loadUserByUsername(request.getUsername()));
    }

    public JwtResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        return issueTokens(userService.loadUserByUsername(request.getUsername()));
    }

    public JwtResponse refresh(RefreshTokenRequest request) {
        String rawRefreshToken = request.getRefreshToken();
        AppUser user = validateRefreshToken(rawRefreshToken);

        AuthTokens nextTokens = jwtTokenProvider.generateTokenPair(user);
        refreshTokenService.rotate(rawRefreshToken, nextTokens.refreshToken(), nextTokens.refreshTokenExpiresAt());

        return JwtResponse.from(nextTokens);
    }

    public void logout(RefreshTokenRequest request) {
        validateRefreshToken(request.getRefreshToken());
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private JwtResponse issueTokens(AppUser user) {
        AuthTokens authTokens = jwtTokenProvider.generateTokenPair(user);
        refreshTokenService.register(user, authTokens.refreshToken(), authTokens.refreshTokenExpiresAt());
        return JwtResponse.from(authTokens);
    }

    private AppUser validateRefreshToken(String rawRefreshToken) {
        try {
            String username = jwtTokenProvider.extractUsername(rawRefreshToken, TokenType.REFRESH);
            AppUser user = userService.loadUserByUsername(username);
            if (!jwtTokenProvider.isRefreshTokenValid(rawRefreshToken, user)) {
                throw new BadRequestException("Некорректный refresh token");
            }
            refreshTokenService.getActive(rawRefreshToken);
            return user;
        } catch (RuntimeException ex) {
            if (ex instanceof BadRequestException badRequestException) {
                throw badRequestException;
            }
            throw new BadRequestException("Некорректный refresh token");
        }
    }
}
