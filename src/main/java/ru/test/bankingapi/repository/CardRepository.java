package ru.test.bankingapi.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Card;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    boolean existsByNumberHash(String numberHash);

    Optional<Card> findByIdAndOwner(Long id, AppUser owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c join fetch c.owner where c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Long id);
}
