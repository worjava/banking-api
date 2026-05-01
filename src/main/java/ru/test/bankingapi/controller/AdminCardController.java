package ru.test.bankingapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.test.bankingapi.config.ApiPaths;
import ru.test.bankingapi.dto.card.CardCreateRequest;
import ru.test.bankingapi.dto.card.CardResponse;
import ru.test.bankingapi.model.CardStatus;
import ru.test.bankingapi.service.CardService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1 + "/admin/cards")
public class AdminCardController {
    private final CardService cardService;

    @GetMapping
    public Page<CardResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable
    ) {
        return cardService.findAll(search, status, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@RequestBody @Valid CardCreateRequest request) {
        return cardService.create(request);
    }

    @PatchMapping("/{id}/block")
    public CardResponse block(@PathVariable Long id) {
        return cardService.block(id);
    }

    @PatchMapping("/{id}/activate")
    public CardResponse activate(@PathVariable Long id) {
        return cardService.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardService.delete(id);
    }
}