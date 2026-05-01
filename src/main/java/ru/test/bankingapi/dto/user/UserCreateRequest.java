package ru.test.bankingapi.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.test.bankingapi.model.Role;

/**
 * Запрос администратора на создание пользователя.
 */
@Data
public class UserCreateRequest {
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

    @NotNull
    private Role role;
}