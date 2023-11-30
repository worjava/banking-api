package ru.astontest.bankingapi.service;


import ru.astontest.bankingapi.dto.BankAccountDTO;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BankAccountService {
    List<BankAccount> getAllAccounts();

    Optional<BankAccount> createAccountByAccountNumber(BankAccountDTO createAccountDTO);
    BankAccount getAccountByNumber(Long number);
}



