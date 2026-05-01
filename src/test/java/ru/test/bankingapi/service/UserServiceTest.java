package ru.test.bankingapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.test.bankingapi.dto.user.UserCreateRequest;
import ru.test.bankingapi.dto.user.UserResponse;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.NotFoundException;
import ru.test.bankingapi.mapper.UserMapper;
import ru.test.bankingapi.mapper.UserMapperImpl;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = new UserMapperImpl();
        userService = new UserService(userRepository, passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    void givenUniqueCredentials_whenRegisterUser_thenEncodesPasswordAndSavesUser() {
        // given
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        // when
        UserResponse response = userService.registerUser("user1", "user1@example.com", "secret123", Role.ROLE_USER);

        // then
        assertEquals(1L, response.id());
        assertEquals("user1", response.username());
        assertEquals(Role.ROLE_USER, response.role());
    }

    @Test
    @DisplayName("Регистрация с занятым логином")
    void givenDuplicateUsername_whenRegisterUser_thenThrowsBadRequest() {
        // given
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        // when / then
        assertThrows(BadRequestException.class,
                () -> userService.registerUser("user1", "user1@example.com", "secret123", Role.ROLE_USER));

        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Создание пользователя администратором")
    void givenCreateRequest_whenCreate_thenReturnsCreatedUser() {
        // given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin2");
        request.setEmail("admin2@example.com");
        request.setPassword("secret123");
        request.setRole(Role.ROLE_ADMIN);

        when(userRepository.existsByUsername("admin2")).thenReturn(false);
        when(userRepository.existsByEmail("admin2@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 5L);
            return user;
        });

        // when
        UserResponse response = userService.create(request);

        // then
        assertEquals(5L, response.id());
        assertEquals(Role.ROLE_ADMIN, response.role());
    }

    @Test
    @DisplayName("Изменение роли пользователя")
    void givenExistingUser_whenUpdateRole_thenChangesRole() {
        // given
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setUsername("user7");
        user.setEmail("user7@example.com");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateRole(7L, Role.ROLE_ADMIN);

        // then
        assertEquals(Role.ROLE_ADMIN, response.role());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void givenMissingUser_whenDelete_thenThrowsNotFound() {
        // given
        when(userRepository.existsById(99L)).thenReturn(false);

        // when / then
        assertThrows(NotFoundException.class, () -> userService.delete(99L));
    }
}
