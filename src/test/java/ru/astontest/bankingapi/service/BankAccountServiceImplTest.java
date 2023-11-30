package ru.astontest.bankingapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.astontest.bankingapi.dto.BankAccountDTO;
import ru.astontest.bankingapi.exception.EmptyNameException;
import ru.astontest.bankingapi.exception.InvalidPinException;
import ru.astontest.bankingapi.mapper.BankAccountMapper;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.repository.BankAccountRepository;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private BankAccountMapper bankAccountMapper;
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Test
    void getAllAccounts_ReturnsListOfAccounts() {
        // Arrange
        when(bankAccountRepository.findAll()).thenReturn(Collections.singletonList(new BankAccount()));
        // Act
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        // Assert
        assertEquals(1, accounts.size());
        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void createAccountByAccountNumber_WithValidDTO_CreatesAccount() {
        // Arrange
        BankAccountDTO accountDTO = new BankAccountDTO("John Doe", "1234");
        BankAccount createdAccount = new BankAccount();
        when(bankAccountMapper.createAccountDTOToBankAccount(any())).thenReturn(createdAccount);
        // Act
        Optional<BankAccount> result = bankAccountService.createAccountByAccountNumber(accountDTO);
        // Assert
        assertTrue(result.isPresent());
        assertSame(createdAccount, result.get());
        verify(bankAccountRepository, times(1)).save(createdAccount);
    }

    @Test
    void createAccountByAccountNumber_WithEmptyName_ThrowsEmptyNameException() {
        // Arrange
        BankAccountDTO accountDTO = new BankAccountDTO("", "1234");
        // Act & Assert
        assertThrows(EmptyNameException.class, () -> bankAccountService.createAccountByAccountNumber(accountDTO));
        // Verify
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void createAccountByAccountNumber_WithInvalidPin_ThrowsInvalidPinException() {
        // Arrange
        BankAccountDTO accountDTO = new BankAccountDTO("John Doe", "123");
        // Act & Assert
        assertThrows(InvalidPinException.class, () -> bankAccountService.createAccountByAccountNumber(accountDTO));
        // Verify
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void getAccountByNumber_WithValidNumber_ReturnsAccount() {
        // Arrange
        Long accountNumber = 123456L;
        BankAccount account = new BankAccount();
        when(bankAccountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        // Act
        BankAccount result = bankAccountService.getAccountByNumber(accountNumber);
        // Assert
        assertSame(account, result);
        verify(bankAccountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void getAccountByNumber_WithInvalidNumber_ThrowsNoSuchElementException() {
        // Arrange
        Long accountNumber = 123456L;
        when(bankAccountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> bankAccountService.getAccountByNumber(accountNumber));
        // Verify
        verify(bankAccountRepository, times(1)).findByAccountNumber(accountNumber);
    }
}
