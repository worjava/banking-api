package ru.test.bankingapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.test.bankingapi.dto.card.CardTransferRequest;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.ForbiddenOperationException;
import ru.test.bankingapi.mapper.CardTransferMapper;
import ru.test.bankingapi.mapper.CardTransferMapperImpl;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.repository.CardRepository;
import ru.test.bankingapi.repository.CardTransferRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link CardTransferService}.
 */
@ExtendWith(MockitoExtension.class)
class CardTransferServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardTransferRepository transferRepository;
    @Mock
    private UserService userService;
    @Mock
    private CardService cardService;

    private CardTransferService transferService;
    private AppUser currentUser;
    private Card fromCard;
    private Card toCard;
    private CardTransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        CardTransferMapper cardTransferMapper = new CardTransferMapperImpl();
        transferService = new CardTransferService(
                cardRepository,
                transferRepository,
                userService,
                cardService,
                cardTransferMapper
        );
        currentUser = user(1L);
        fromCard = card(10L, currentUser, "100.00");
        toCard = card(20L, currentUser, "15.00");
        transferRequest = request(10L, 20L, "30.00");
    }

    @Test
    @DisplayName("Перевод между своими картами")
    void givenOwnCards_whenTransfer_thenMovesMoneyBetweenThem() {
        // given
        when(userService.currentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        transferService.transfer(transferRequest);

        // then
        assertEquals(new BigDecimal("70.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("45.00"), toCard.getBalance());
        verify(cardService).ensureActive(fromCard);
        verify(cardService).ensureActive(toCard);
    }

    @Test
    @DisplayName("Перевод на чужую карту запрещен")
    void givenAnotherOwnersCard_whenTransfer_thenThrowsForbidden() {
        // given
        toCard = card(20L, user(2L), "15.00");

        when(userService.currentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(toCard));

        // when / then
        assertThrows(ForbiddenOperationException.class, () -> transferService.transfer(transferRequest));
    }

    @Test
    @DisplayName("Перевод при недостаточном балансе")
    void givenInsufficientFunds_whenTransfer_thenThrowsBadRequest() {
        // given
        fromCard = card(10L, currentUser, "10.00");

        when(userService.currentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(toCard));

        // when / then
        assertThrows(BadRequestException.class, () -> transferService.transfer(transferRequest));
    }

    private CardTransferRequest request(Long fromId, Long toId, String amount) {
        CardTransferRequest request = new CardTransferRequest();
        request.setFromCardId(fromId);
        request.setToCardId(toId);
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        return user;
    }

    private Card card(Long id, AppUser owner, String balance) {
        Card card = Card.issue("enc", "hash-" + id, String.format("%04d", id), owner, LocalDate.of(2030, 1, 1), new BigDecimal(balance));
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }
}
