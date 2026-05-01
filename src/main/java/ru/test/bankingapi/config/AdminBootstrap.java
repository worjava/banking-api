package ru.test.bankingapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;
import ru.test.bankingapi.repository.UserRepository;

/**
 * Создает первого администратора для чистой базы при старте приложения.
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap-admin.username:admin}")
    private String username;

    @Value("${app.bootstrap-admin.email:admin@example.com}")
    private String email;

    @Value("${app.bootstrap-admin.password:admin123}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || userRepository.existsByRole(Role.ROLE_ADMIN)) {
            return;
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Пользователь bootstrap-администратора уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email bootstrap-администратора уже существует");
        }

        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }
}
