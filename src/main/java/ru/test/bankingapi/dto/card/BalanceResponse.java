package ru.test.bankingapi.dto.card;

import java.math.BigDecimal;

/**
 * Ответ с текущим балансом карты.
 */
public record BalanceResponse(Long cardId, BigDecimal balance) {
}
