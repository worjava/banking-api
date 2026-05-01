package ru.test.bankingapi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.test.bankingapi.config.CardNumberProperties;

import java.security.SecureRandom;

/**
 * Генератор номеров карт с контрольной цифрой по алгоритму Луна.
 */
@Component
@RequiredArgsConstructor
public class CardNumberGenerator {
    private final CardNumberProperties properties;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder number = new StringBuilder(properties.getBin());
        for (int i = 0; i < properties.getRandomDigitsCount(); i++) {
            number.append(random.nextInt(10));
        }
        number.append(luhnCheckDigit(number.toString()));
        return number.toString();
    }

    private int luhnCheckDigit(String partialNumber) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = partialNumber.length() - 1; i >= 0; i--) {
            int digit = partialNumber.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}