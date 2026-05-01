package ru.test.bankingapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Запрос на регистрацию нового пользователя.
 */
@Data
public class SignUpRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String username;

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 6, max = 255)
    private String password;
}