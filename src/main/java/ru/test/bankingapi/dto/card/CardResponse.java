package ru.test.bankingapi.dto.card;

import ru.test.bankingapi.model.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ответ с публичными данными карты и замаскированным номером.
 */
public record CardResponse(
        Long id,
        String maskedNumber,
        Long ownerId,
        String ownerUsername,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {
}
