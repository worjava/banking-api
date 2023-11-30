package ru.astontest.bankingapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "accounts")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountNumber;

    @NotNull(message = "Имя не может быть null")
    private String beneficiaryName;

    @Size(min = 4, max = 4, message = "Пин-код должен содержать ровно 4 цифры")
    private String pinCode;

    private BigDecimal balance;

    @OneToMany(mappedBy = "account")
    private Set<Transaction> transactions;

    public BankAccount(String beneficiaryName, String pinCode) {
        this.beneficiaryName = beneficiaryName;
        this.pinCode = pinCode;
        this.balance = BigDecimal.ZERO;
    }
}
