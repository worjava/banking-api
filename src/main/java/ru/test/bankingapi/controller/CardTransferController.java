package ru.test.bankingapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.test.bankingapi.config.ApiPaths;
import ru.test.bankingapi.dto.card.CardTransferRequest;
import ru.test.bankingapi.dto.card.CardTransferResponse;
import ru.test.bankingapi.service.CardTransferService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1 + "/transfers")
public class CardTransferController {
    private final CardTransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardTransferResponse transfer(@RequestBody @Valid CardTransferRequest request) {
        return transferService.transfer(request);
    }
}
