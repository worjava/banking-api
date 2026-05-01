package ru.test.bankingapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.test.bankingapi.dto.card.CardTransferRequest;
import ru.test.bankingapi.dto.card.CardTransferResponse;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.ForbiddenOperationException;
import ru.test.bankingapi.exception.NotFoundException;
import ru.test.bankingapi.mapper.CardTransferMapper;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.model.CardTransfer;
import ru.test.bankingapi.repository.CardRepository;
import ru.test.bankingapi.repository.CardTransferRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис переводов между собственными активными картами пользователя.
 */
@Service
@RequiredArgsConstructor
public class CardTransferService {
    private final CardRepository cardRepository;
    private final CardTransferRepository transferRepository;
    private final UserService userService;
    private final CardService cardService;
    private final CardTransferMapper cardTransferMapper;

    @Transactional
    public CardTransferResponse transfer(CardTransferRequest request) {
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new BadRequestException("Карты для перевода должны отличаться");
        }

        AppUser user = userService.currentUser();
        Map<Long, Card> cards = loadLockedCards(request.getFromCardId(), request.getToCardId());
        Card from = cards.get(request.getFromCardId());
        Card to = cards.get(request.getToCardId());

        ensureOwner(from, user);
        ensureOwner(to, user);
        cardService.ensureActive(from);
        cardService.ensureActive(to);

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Недостаточно средств для перевода");
        }

        from.debit(request.getAmount());
        to.credit(request.getAmount());

        CardTransfer transfer = CardTransfer.create(from, to, request.getAmount());
        return cardTransferMapper.toResponse(transferRepository.save(transfer));
    }

    private Map<Long, Card> loadLockedCards(Long firstId, Long secondId) {
        return List.of(firstId, secondId).stream()
                .sorted(Comparator.naturalOrder())
                .map(id -> cardRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id)))
                .collect(Collectors.toMap(Card::getId, Function.identity()));
    }

    private void ensureOwner(Card card, AppUser user) {
        if (!card.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Переводы разрешены только между собственными картами");
        }
    }

}
