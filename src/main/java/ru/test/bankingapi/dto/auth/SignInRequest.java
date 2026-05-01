package ru.test.bankingapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Запрос на вход пользователя в систему.
 */
@Data
public class SignInRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}