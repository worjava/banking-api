package ru.astontest.bankingapi.service;

import ru.astontest.bankingapi.dto.TransactionRequestDto;
import ru.astontest.bankingapi.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface BankTransactionService {

    List<Transaction> getTransactionHistoryByAccountNumber(Long accountNumber);

    void deposit(Long accountNumber, TransactionRequestDto requestDto);

    public void withdraw(Long accountNumber, TransactionRequestDto requestDto);

    void transfer(Long fromAccount, Long toAccount, String pinCode, BigDecimal amount);
}
