package ru.test.bankingapi.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки генерации номера банковской карты.
 */
@Validated
@Component
@ConfigurationProperties(prefix = "card.number")
public class CardNumberProperties {
    @Pattern(regexp = "\\d{6,8}", message = "BIN карты должен содержать от 6 до 8 цифр")
    private String bin = "400000";

    @Min(value = 1, message = "Количество случайных цифр должно быть не меньше 1")
    @Max(value = 12, message = "Количество случайных цифр должно быть не больше 12")
    private int randomDigitsCount = 9;

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public int getRandomDigitsCount() {
        return randomDigitsCount;
    }

    public void setRandomDigitsCount(int randomDigitsCount) {
        this.randomDigitsCount = randomDigitsCount;
    }
}
