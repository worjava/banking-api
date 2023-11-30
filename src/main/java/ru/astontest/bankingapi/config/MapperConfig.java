package ru.astontest.bankingapi.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.astontest.bankingapi.mapper.BankAccountMapper;

@Configuration
public class MapperConfig {

    @Bean
    public BankAccountMapper bankAccountMapper() {
        return Mappers.getMapper(BankAccountMapper.class);
    }
}
