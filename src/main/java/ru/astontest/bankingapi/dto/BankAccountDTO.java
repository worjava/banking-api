package ru.astontest.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class BankAccountDTO {
    private String beneficiaryName;
    private String pin;

}
