package ru.astontest.bankingapi.repository;

import ru.astontest.bankingapi.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {
    Optional<BankAccount> findByAccountNumber(Long accountNumber);

    List<BankAccount> findByBeneficiaryName(String beneficiaryName);


}
