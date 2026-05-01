package ru.test.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.test.bankingapi.model.CardTransfer;

public interface CardTransferRepository extends JpaRepository<CardTransfer, Long> {
}
