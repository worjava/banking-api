package ru.test.bankingapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.test.bankingapi.config.ApiPaths;
import ru.test.bankingapi.dto.card.BalanceResponse;
import ru.test.bankingapi.dto.card.CardResponse;
import ru.test.bankingapi.model.CardStatus;
import ru.test.bankingapi.service.CardService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1 + "/cards")
public class CardController {
    private final CardService cardService;

    @GetMapping
    public Page<CardResponse> findCurrentUserCards(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable
    ) {
        return cardService.findCurrentUserCards(search, status, pageable);
    }

    @GetMapping("/{id}")
    public CardResponse get(@PathVariable Long id) {
        return cardService.getCurrentUserCard(id);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable Long id) {
        return cardService.getCurrentUserBalance(id);
    }

    @PostMapping("/{id}/block-request")
    public CardResponse requestBlock(@PathVariable Long id) {
        return cardService.requestBlock(id);
    }
}
