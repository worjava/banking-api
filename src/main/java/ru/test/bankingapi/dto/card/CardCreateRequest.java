package ru.test.bankingapi.dto.card;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запрос администратора на выпуск банковской карты.
 */
@Data
public class CardCreateRequest {
    @NotNull
    private Long ownerId;

    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать ровно 16 цифр")
    private String cardNumber;

    @NotNull
    @Future
    private LocalDate expirationDate;

    @DecimalMin(value = "0.00")
    private BigDecimal balance;
}
