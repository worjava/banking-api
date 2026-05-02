package ru.test.bankingapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.test.bankingapi.config.ApiPaths;
import ru.test.bankingapi.dto.auth.JwtResponse;
import ru.test.bankingapi.dto.auth.RefreshTokenRequest;
import ru.test.bankingapi.dto.auth.SignInRequest;
import ru.test.bankingapi.dto.auth.SignUpRequest;
import ru.test.bankingapi.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1 + "/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public JwtResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authService.signIn(request);
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request);
    }
}
