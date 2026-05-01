package ru.test.bankingapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты для {@link Card}.
 */
class CardTest {
    private AppUser owner;

    @BeforeEach
    void setUp() {
        owner = new AppUser();
        owner.setId(1L);
        owner.setUsername("user1");
    }

    @Test
    @DisplayName("Статус активной карты до истечения срока")
    void givenActiveCardBeforeExpiration_whenResolveStatus_thenReturnsActive() {
        // given
        Card card = Card.issue(
                "encrypted",
                "hash",
                "1234",
                owner,
                LocalDate.of(2030, 12, 31),
                BigDecimal.TEN
        );

        // when
        CardStatus status = card.resolveStatus(LocalDate.of(2030, 1, 1));

        // then
        assertEquals(CardStatus.ACTIVE, status);
    }

    @Test
    @DisplayName("Статус карты после истечения срока")
    void givenExpiredCard_whenResolveStatus_thenReturnsExpired() {
        // given
        Card card = Card.issue(
                "encrypted",
                "hash",
                "1234",
                owner,
                LocalDate.of(2030, 1, 1),
                BigDecimal.TEN
        );

        // when
        CardStatus status = card.resolveStatus(LocalDate.of(2030, 1, 2));

        // then
        assertEquals(CardStatus.EXPIRED, status);
    }
}
