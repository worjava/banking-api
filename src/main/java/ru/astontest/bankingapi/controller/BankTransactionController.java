package ru.astontest.bankingapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.astontest.bankingapi.dto.TransactionRequestDto;
import ru.astontest.bankingapi.model.Transaction;
import ru.astontest.bankingapi.service.BankTransactionService;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class BankTransactionController {
    private final BankTransactionService transactionService;


    @PostMapping("/deposit/{accountNumber}")
    public ResponseEntity<String> deposit(
            @PathVariable Long accountNumber,
            @RequestBody TransactionRequestDto requestDto) {
        transactionService.deposit(accountNumber, requestDto);

        return ResponseEntity.status(HttpStatus.OK).body("Пополнение на сумму " + requestDto.getAmount());
    }

    @PostMapping("/withdraw/{accountNumber}")
    public ResponseEntity<String> withdraw(
            @PathVariable Long accountNumber,
            @RequestBody TransactionRequestDto requestDto) {
        transactionService.withdraw(accountNumber, requestDto);

        return ResponseEntity.status(HttpStatus.OK).body("Снятие на сумму " + requestDto.getAmount());
    }

    @PostMapping("/transfer/{fromAccountNumber}/{toAccountNumber}")
    public ResponseEntity<String> transfer(
            @PathVariable Long fromAccountNumber,
            @PathVariable Long toAccountNumber,
            @RequestBody TransactionRequestDto requestDto) {
        transactionService.transfer(fromAccountNumber, toAccountNumber, requestDto.getPinCode(), requestDto.getAmount());

        return ResponseEntity.status(HttpStatus.OK).body("Перевод на сумму " + requestDto.getAmount() + " выполнен успешно");

    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionHistoryByAccountNumber(@PathVariable Long accountNumber) {
        List<Transaction> transactionHistory = transactionService.getTransactionHistoryByAccountNumber(accountNumber);

        return ResponseEntity.ok(transactionHistory);
    }
}