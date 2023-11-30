package ru.astontest.bankingapi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDto {

    private BigDecimal amount;
    private String pinCode;
}
