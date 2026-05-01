package ru.test.bankingapi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.test.bankingapi.dto.card.BalanceResponse;
import ru.test.bankingapi.dto.card.CardCreateRequest;
import ru.test.bankingapi.dto.card.CardResponse;
import ru.test.bankingapi.exception.BadRequestException;
import ru.test.bankingapi.exception.ForbiddenOperationException;
import ru.test.bankingapi.exception.NotFoundException;
import ru.test.bankingapi.mapper.CardMapper;
import ru.test.bankingapi.model.AppUser;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.model.CardStatus;
import ru.test.bankingapi.repository.CardRepository;
import ru.test.bankingapi.util.CardNumberCryptoUtil;
import ru.test.bankingapi.util.CardNumberGenerator;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис управления картами, маскированием и проверкой доступа владельца.
 */
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardNumberCryptoUtil cryptoService;
    private final CardNumberGenerator numberGenerator;
    private final CardMapper cardMapper;
    private final Clock clock;

    @Transactional
    public CardResponse create(CardCreateRequest request) {
        AppUser owner = userService.getById(request.getOwnerId());
        String number = StringUtils.hasText(request.getCardNumber()) ? request.getCardNumber() : uniqueGeneratedNumber();
        String hash = cryptoService.hash(number);

        if (cardRepository.existsByNumberHash(hash)) {
            throw new BadRequestException("Номер карты уже существует");
        }

        Card card = Card.issue(
                cryptoService.encrypt(number),
                hash,
                number.substring(number.length() - 4),
                owner,
                request.getExpirationDate(),
                request.getBalance() == null ? BigDecimal.ZERO : request.getBalance()
        );
        return cardMapper.toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> findAll(String search, CardStatus status, Pageable pageable) {
        return cardRepository.findAll(specification(null, search, status), pageable).map(cardMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> findCurrentUserCards(String search, CardStatus status, Pageable pageable) {
        AppUser user = userService.currentUser();
        return cardRepository.findAll(specification(user, search, status), pageable).map(cardMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CardResponse getCurrentUserCard(Long id) {
        AppUser user = userService.currentUser();
        Card card = cardRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id));
        return cardMapper.toResponse(card);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getCurrentUserBalance(Long id) {
        AppUser user = userService.currentUser();
        Card card = cardRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id));
        return cardMapper.toBalanceResponse(card);
    }

    @Transactional
    public CardResponse block(Long id) {
        Card card = getCard(id);
        card.block();
        return cardMapper.toResponse(card);
    }

    @Transactional
    public CardResponse activate(Long id) {
        Card card = getCard(id);
        if (card.getExpirationDate().isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("Невозможно активировать просроченную карту");
        }

        card.activate();
        return cardMapper.toResponse(card);
    }

    @Transactional
    public CardResponse requestBlock(Long id) {
        AppUser user = userService.currentUser();
        Card card = cardRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id));
        card.block();
        return cardMapper.toResponse(card);
    }

    @Transactional
    public void delete(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new NotFoundException("Карта не найдена: " + id);
        }
        cardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Card getCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id));
    }

    public void ensureActive(Card card) {
        if (card.getExpirationDate().isBefore(LocalDate.now(clock))) {
            card.expire();
            throw new ForbiddenOperationException("Срок действия карты истек");
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ForbiddenOperationException("Карта не активна");
        }
    }

    private String uniqueGeneratedNumber() {
        for (int i = 0; i < 10; i++) {
            String number = numberGenerator.generate();
            if (!cardRepository.existsByNumberHash(cryptoService.hash(number))) {
                return number;
            }
        }
        throw new BadRequestException("Не удалось сгенерировать уникальный номер карты");
    }

    private Specification<Card> specification(AppUser owner, String search, CardStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (owner != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner"), owner));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(search)) {
                String normalized = search.trim().toLowerCase();
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("owner").get("username")), "%" + normalized + "%"),
                        criteriaBuilder.equal(root.get("lastFour"), normalized)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
