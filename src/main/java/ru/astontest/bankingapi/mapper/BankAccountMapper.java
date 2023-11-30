package ru.astontest.bankingapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import ru.astontest.bankingapi.dto.BankAccountDTO;
import ru.astontest.bankingapi.model.BankAccount;

@Mapper
@Component
public interface BankAccountMapper {


    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", constant = "0")
    @Mapping(target = "beneficiaryName", source = "beneficiaryName")
    @Mapping(target = "pinCode", source = "pin")
    BankAccount createAccountDTOToBankAccount(BankAccountDTO createAccountDTO);
}
