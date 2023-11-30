package ru.astontest.bankingapi.service;

import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Isolation;
import ru.astontest.bankingapi.dto.TransactionRequestDto;
import ru.astontest.bankingapi.exception.InsufficientFundsException;
import ru.astontest.bankingapi.exception.InvalidPinException;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.model.TransactionType;
import ru.astontest.bankingapi.model.Transaction;
import ru.astontest.bankingapi.repository.BankAccountRepository;
import ru.astontest.bankingapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankTransactionServiceImpl implements BankTransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;



    @Override
    public List<Transaction> getTransactionHistoryByAccountNumber(Long accountNumber) {
        return transactionRepository.findTransactionsByAccount_AccountNumber(accountNumber);
    }

    @Override
    @Transactional
    public void deposit(Long accountNumber, TransactionRequestDto requestDto) {
        performTransaction(accountNumber, requestDto.getPinCode(), requestDto.getAmount(), TransactionType.DEPOSIT);
    }

    @Override
    @Transactional
    public void withdraw(Long accountNumber, TransactionRequestDto requestDto) {
        performTransaction(accountNumber, requestDto.getPinCode(), requestDto.getAmount(), TransactionType.WITHDRAW);
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void transfer(Long fromAccountNumber, Long toAccountNumber, String pinCode, BigDecimal amount) {
        performTransaction(fromAccountNumber, pinCode, amount, TransactionType.TRANSFER);
        performTransaction(toAccountNumber, pinCode, amount, TransactionType.DEPOSIT);
    }

    @SneakyThrows
    public void performTransaction(Long accountNumber, String pinCode, BigDecimal amount, TransactionType transactionType) {
        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Аккаунт не найден"));

        // Проверка корректности PIN-кода
        if (!account.getPinCode().equals(pinCode)) {
            throw new InvalidPinException("Incorrect PIN code");
        }

        // Сохранение транзакции в истории
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transactionRepository.save(transaction);

        // Обновление баланса
        switch (transactionType) {
            case DEPOSIT:

                    account.setBalance(account.getBalance().add(amount));

                break;
            case WITHDRAW:
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException("Недостаточно средств на счете");
                }
                account.setBalance(account.getBalance().subtract(amount));
                break;
            case TRANSFER:
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException("Недостаточно средств на счете для перевода");
                }
                account.setBalance(account.getBalance().subtract(amount));
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type");
        }

        bankAccountRepository.save(account);
    }
}
