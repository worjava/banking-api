package ru.test.bankingapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.test.bankingapi.dto.card.BalanceResponse;
import ru.test.bankingapi.dto.card.CardCreateRequest;
import ru.test.bankingapi.dto.card.CardResponse;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.ForbiddenOperationException;
import ru.test.bankingapi.exception.NotFoundException;
import ru.test.bankingapi.mapper.CardMapper;
import ru.test.bankingapi.mapper.CardMapperImpl;
import ru.test.bankingapi.mapper.CardMapperSupport;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.model.CardStatus;
import ru.test.bankingapi.repository.CardRepository;
import ru.test.bankingapi.util.CardNumberCryptoUtil;
import ru.test.bankingapi.util.CardNumberGenerator;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link CardService}.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserService userService;
    @Mock
    private CardNumberCryptoUtil cryptoService;
    @Mock
    private CardNumberGenerator numberGenerator;

    private CardService cardService;
    private AppUser owner;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        CardMapper cardMapper = new CardMapperImpl(new CardMapperSupport(fixedClock));
        cardService = new CardService(cardRepository, userService, cryptoService, numberGenerator, cardMapper, fixedClock);

        owner = new AppUser();
        owner.setId(1L);
        owner.setUsername("user1");
    }

    @Test
    @DisplayName("Создание карты с генерацией номера")
    void givenMissingNumber_whenCreate_thenGeneratesAndReturnsMaskedCard() {
        // given
        CardCreateRequest request = new CardCreateRequest();
        request.setOwnerId(1L);
        request.setExpirationDate(LocalDate.of(2030, 12, 31));
        request.setBalance(new BigDecimal("1500.00"));

        when(userService.getById(1L)).thenReturn(owner);
        when(numberGenerator.generate()).thenReturn("4000001234567890");
        when(cryptoService.hash("4000001234567890")).thenReturn("hash-1");
        when(cardRepository.existsByNumberHash("hash-1")).thenReturn(false);
        when(cryptoService.encrypt("4000001234567890")).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            ReflectionTestUtils.setField(card, "id", 100L);
            return card;
        });

        // when
        CardResponse response = cardService.create(request);

        // then
        assertEquals(100L, response.id());
        assertEquals("**** **** **** 7890", response.maskedNumber());
        assertEquals("user1", response.ownerUsername());
        assertEquals(CardStatus.ACTIVE, response.status());
        assertEquals(new BigDecimal("1500.00"), response.balance());
    }

    @Test
    @DisplayName("Создание карты с занятым номером")
    void givenExistingCardHash_whenCreate_thenThrowsBadRequest() {
        // given
        CardCreateRequest request = new CardCreateRequest();
        request.setOwnerId(1L);
        request.setCardNumber("4000001234567890");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));

        when(userService.getById(1L)).thenReturn(owner);
        when(cryptoService.hash("4000001234567890")).thenReturn("hash-1");
        when(cardRepository.existsByNumberHash("hash-1")).thenReturn(true);

        // when / then
        assertThrows(BadRequestException.class, () -> cardService.create(request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Активация просроченной карты")
    void givenExpiredCard_whenActivate_thenThrowsBadRequest() {
        // given
        Card expiredCard = card(10L, owner, LocalDate.of(2026, 4, 30), CardStatus.BLOCKED, "100.00");
        when(cardRepository.findById(10L)).thenReturn(Optional.of(expiredCard));

        // when / then
        assertThrows(BadRequestException.class, () -> cardService.activate(10L));
    }

    @Test
    @DisplayName("Запрос блокировки своей карты")
    void givenOwnersCard_whenRequestBlock_thenReturnsBlockedCard() {
        // given
        Card activeCard = card(10L, owner, LocalDate.of(2030, 1, 1), CardStatus.ACTIVE, "100.00");

        when(userService.currentUser()).thenReturn(owner);
        when(cardRepository.findByIdAndOwner(10L, owner)).thenReturn(Optional.of(activeCard));

        // when
        CardResponse response = cardService.requestBlock(10L);

        // then
        assertEquals(CardStatus.BLOCKED, response.status());
    }

    @Test
    @DisplayName("Проверка просроченной карты")
    void givenExpiredCard_whenEnsureActive_thenThrowsForbiddenAndExpiresCard() {
        // given
        Card expiredCard = card(10L, owner, LocalDate.of(2026, 4, 30), CardStatus.ACTIVE, "100.00");

        // when / then
        assertThrows(ForbiddenOperationException.class, () -> cardService.ensureActive(expiredCard));
        assertEquals(CardStatus.EXPIRED, expiredCard.getStatus());
    }

    @Test
    @DisplayName("Получение баланса своей карты")
    void givenOwnersCard_whenGetBalance_thenReturnsCurrentBalance() {
        // given
        Card activeCard = card(10L, owner, LocalDate.of(2030, 1, 1), CardStatus.ACTIVE, "321.50");

        when(userService.currentUser()).thenReturn(owner);
        when(cardRepository.findByIdAndOwner(10L, owner)).thenReturn(Optional.of(activeCard));

        // when
        BalanceResponse response = cardService.getCurrentUserBalance(10L);

        // then
        assertEquals(10L, response.cardId());
        assertEquals(new BigDecimal("321.50"), response.balance());
    }

    @Test
    @DisplayName("Удаление несуществующей карты")
    void givenMissingCard_whenDelete_thenThrowsNotFound() {
        // given
        when(cardRepository.existsById(99L)).thenReturn(false);

        // when / then
        assertThrows(NotFoundException.class, () -> cardService.delete(99L));
    }

    private Card card(Long id, AppUser cardOwner, LocalDate expirationDate, CardStatus status, String balance) {
        Card card = Card.issue("encrypted", "hash-" + id, String.format("%04d", id), cardOwner, expirationDate, new BigDecimal(balance));
        ReflectionTestUtils.setField(card, "id", id);
        ReflectionTestUtils.setField(card, "status", status);
        return card;
    }
}
