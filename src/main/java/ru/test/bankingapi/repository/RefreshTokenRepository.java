package ru.test.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.test.bankingapi.model.RefreshToken;

import java.util.Optional;

/**
 * Репозиторий сохраненных refresh token.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("select rt from RefreshToken rt join fetch rt.user where rt.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);
}
