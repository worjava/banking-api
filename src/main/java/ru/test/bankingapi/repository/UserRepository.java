package ru.test.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);
}
