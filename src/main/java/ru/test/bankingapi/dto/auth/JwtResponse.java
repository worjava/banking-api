package ru.test.bankingapi.dto.auth;

/**
 * Ответ с JWT-токеном после регистрации или входа.
 */
public record JwtResponse(String token) {
}
