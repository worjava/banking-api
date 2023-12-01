package ru.astontest.bankingapi.service.Impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import ru.astontest.bankingapi.dto.BankAccountDTO;
import ru.astontest.bankingapi.exception.EmptyNameException;
import ru.astontest.bankingapi.exception.InvalidPinException;
import ru.astontest.bankingapi.mapper.BankAccountMapper;
import ru.astontest.bankingapi.model.BankAccount;
import ru.astontest.bankingapi.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.astontest.bankingapi.service.BankAccountService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {


    private final BankAccountRepository bankAccountRepository;

    private final BankAccountMapper bankAccountMapper;

    @Override
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();

    }

    public Optional<BankAccount> createAccountByAccountNumber(BankAccountDTO createAccountDTO) {
        if (!StringUtils.hasText(createAccountDTO.getBeneficiaryName())) {
            throw new EmptyNameException("Имя не может быть пустым");
        }
        if (createAccountDTO.getPin().length() != 4) {
            throw new InvalidPinException("Некорректная длина пин-кода");
        }

        BankAccount account = bankAccountMapper.createAccountDTOToBankAccount(createAccountDTO);
        bankAccountRepository.save(account);

        return Optional.of(account);
    }


    @Override
    public BankAccount getAccountByNumber(Long number) {
        return bankAccountRepository.findByAccountNumber(number)
                .orElseThrow(() -> new NoSuchElementException("Акаунт с номером " + number + " не найден"));
    }

}





