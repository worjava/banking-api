package ru.astontest.bankingapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.astontest.bankingapi.dto.TransactionRequestDto;
import ru.astontest.bankingapi.exception.InsufficientFundsException;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.model.Transaction;
import ru.astontest.bankingapi.model.TransactionType;
import ru.astontest.bankingapi.repository.BankAccountRepository;
import ru.astontest.bankingapi.repository.TransactionRepository;
import ru.astontest.bankingapi.service.Impl.BankTransactionServiceImpl;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankTransactionServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankTransactionServiceImpl bankTransactionService;

    @Test
    public void deposit_PerformsDepositTransaction() {
        Long accountNumber = 123L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPinCode("1234");
        requestDto.setAmount(BigDecimal.TEN);

        BankAccount account = new BankAccount();
        account.setBalance(BigDecimal.ONE);
        account.setPinCode("1234");

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        bankTransactionService.deposit(accountNumber, requestDto);

        assertEquals(BigDecimal.valueOf(11), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    public void withdraw_PerformsWithdrawTransaction() {
        Long accountNumber = 123L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPinCode("1234");
        requestDto.setAmount(BigDecimal.TEN);

        BankAccount account = new BankAccount();
        account.setBalance(BigDecimal.TEN);
        account.setPinCode("1234");

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        bankTransactionService.withdraw(accountNumber, requestDto);

        assertEquals(BigDecimal.ZERO, account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    public void transfer_PerformsTransferTransaction() {
        Long fromAccountNumber = 123L;
        Long toAccountNumber = 456L;
        String pinCode = "1234";
        BigDecimal amount = BigDecimal.TEN;

        BankAccount fromAccount = new BankAccount();
        fromAccount.setBalance(BigDecimal.TEN);
        fromAccount.setPinCode("1234");

        BankAccount toAccount = new BankAccount();
        toAccount.setBalance(BigDecimal.ONE);
        toAccount.setPinCode("1234");

        when(bankAccountRepository.findByAccountNumber(fromAccountNumber))
                .thenReturn(Optional.of(fromAccount));

        when(bankAccountRepository.findByAccountNumber(toAccountNumber))
                .thenReturn(Optional.of(toAccount));

        bankTransactionService.transfer(fromAccountNumber, toAccountNumber, pinCode, amount);

        assertEquals(BigDecimal.ZERO, fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(11), toAccount.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
    }

    @Test
    public void performTransaction_ThrowsAccountNotFoundException_WhenAccountNotFound() {
        Long accountNumber = 123L;
        String pinCode = "1234";
        BigDecimal amount = BigDecimal.TEN;
        TransactionType transactionType = TransactionType.DEPOSIT;

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankTransactionService.performTransaction(accountNumber, pinCode, amount, transactionType));
    }

    @Test
    public void performTransaction_ThrowsInsufficientFundsException_WhenInsufficientFunds() {
        Long accountNumber = 1L;
        String pinCode = "1234";
        BigDecimal amount = BigDecimal.TEN;
        TransactionType transactionType = TransactionType.WITHDRAW;

        BankAccount account = new BankAccount();
        account.setBalance(BigDecimal.ONE);
        account.setPinCode("1234");

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> bankTransactionService.performTransaction(accountNumber, pinCode, amount, transactionType));
    }
}
