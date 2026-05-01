package ru.test.bankingapi.dto.card;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Ответ с результатом перевода между картами.
 */
public record CardTransferResponse(
        Long id,
        Long fromCardId,
        Long toCardId,
        BigDecimal amount,
        Instant createdAt
) {
}
