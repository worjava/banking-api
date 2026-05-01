package ru.test.bankingapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.test.bankingapi.dto.user.UserCreateRequest;
import ru.test.bankingapi.dto.user.UserResponse;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.NotFoundException;
import ru.test.bankingapi.mapper.UserMapper;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.repository.UserRepository;

/**
 * Сервис управления пользователями и загрузки данных для Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    @Transactional
    public UserResponse registerUser(String username, String email, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Пользователь с такой электронной почтой уже существует");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        return registerUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        AppUser user = getById(id);
        user.setRole(role);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AppUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public AppUser currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AppUser user) {
            return user;
        }
        throw new UsernameNotFoundException("Текущий пользователь не аутентифицирован");
    }

}
