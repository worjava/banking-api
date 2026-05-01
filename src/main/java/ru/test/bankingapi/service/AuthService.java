package ru.test.bankingapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.test.bankingapi.dto.auth.JwtResponse;
import ru.test.bankingapi.dto.auth.SignInRequest;
import ru.test.bankingapi.dto.auth.SignUpRequest;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.security.JwtTokenProvider;

/**
 * Сервис регистрации, входа и выпуска JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public JwtResponse signUp(SignUpRequest request) {
        userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword(), Role.ROLE_USER);
        AppUser user = userService.loadUserByUsername(request.getUsername());
        return new JwtResponse(jwtTokenProvider.generateToken(user));
    }

    public JwtResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        return new JwtResponse(jwtTokenProvider.generateToken(userService.loadUserByUsername(request.getUsername())));
    }
}
