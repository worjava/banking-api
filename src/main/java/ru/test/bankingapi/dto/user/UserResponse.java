package ru.test.bankingapi.dto.user;

import ru.test.bankingapi.model.Role;

/**
 * Ответ с публичными данными пользователя.
 */
public record UserResponse(Long id, String username, String email, Role role) {
}