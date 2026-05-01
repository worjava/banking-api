package ru.test.bankingapi.util;

/**
 * Утилиты маскирования карточных номеров для внешнего API.
 */
public final class CardMaskingUtils {
    private CardMaskingUtils() {
    }

    public static String mask(String plainNumber) {
        if (plainNumber == null || plainNumber.length() < 4) {
            throw new IllegalArgumentException("Номер карты должен содержать минимум 4 цифры");
        }
        return maskLastFour(plainNumber.substring(plainNumber.length() - 4));
    }

    public static String maskLastFour(String lastFour) {
        if (lastFour == null || !lastFour.matches("\\d{4}")) {
            throw new IllegalArgumentException("Последние 4 цифры карты должны содержать ровно 4 цифры");
        }
        return "**** **** **** " + lastFour;
    }
}