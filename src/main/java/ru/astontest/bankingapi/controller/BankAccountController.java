package ru.astontest.bankingapi.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.astontest.bankingapi.dto.BankAccountDTO;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.service.BankAccountService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;


    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody BankAccountDTO request) {
        bankAccountService.createAccountByAccountNumber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Новый аккаунт создан");

    }

    @GetMapping("/all")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccount> getAccountByNumber(@PathVariable Long accountNumber) {
        BankAccount account = bankAccountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);

    }
}


