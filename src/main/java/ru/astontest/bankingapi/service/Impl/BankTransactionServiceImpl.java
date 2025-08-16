package ru.astontest.bankingapi.service.Impl;

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
import ru.astontest.bankingapi.service.BankTransactionService;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class BankTransactionServiceImpl implements BankTransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    private static final Map<TransactionType, BiConsumer<BankAccount, BigDecimal>> BALANCE_HANDLERS = createBalanceHandlers();

    private static Map<TransactionType, BiConsumer<BankAccount, BigDecimal>> createBalanceHandlers() {
        EnumMap<TransactionType, BiConsumer<BankAccount, BigDecimal>> map = new EnumMap<>(TransactionType.class);
        map.put(TransactionType.DEPOSIT, (account, amount) -> {
            account.setBalance(account.getBalance().add(amount));
        });
        map.put(TransactionType.WITHDRAW, (account, amount) -> {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Недостаточно средств на счете");
            }
            account.setBalance(account.getBalance().subtract(amount));
        });
        map.put(TransactionType.TRANSFER, (account, amount) -> {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Недостаточно средств на счете для перевода");
            }
            account.setBalance(account.getBalance().subtract(amount));
        });
        return map;
    }



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
    @Transactional
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
        BiConsumer<BankAccount, BigDecimal> handler = BALANCE_HANDLERS.get(transactionType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported transaction type");
        }
        handler.accept(account, amount);

        bankAccountRepository.save(account);
    }
}
