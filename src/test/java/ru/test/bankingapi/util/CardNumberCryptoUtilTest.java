package ru.test.bankingapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты для {@link CardNumberCryptoUtil}.
 */
class CardNumberCryptoUtilTest {
    private CardNumberCryptoUtil cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CardNumberCryptoUtil(
                "YzMxNTM4ZjI2OTAxNDdkYzgyYjQ5NjRjN2YxODJjMWU=",
                "YmM3YTQ2N2M2MjA0NDBmOTlkMmQwOTFlMzRhNWEzYjY4MzQ1YjhhMjg3ZjI0YzA1OGEyYjAzODg4Mzc1M2Y4Zg=="
        );
    }

    @Test
    @DisplayName("Шифрование и расшифровка номера карты")
    void givenValidCardNumber_whenEncryptAndDecrypt_thenReturnsOriginalNumber() {
        // given
        String number = "4000001234567899";

        // when
        String encrypted = cryptoService.encrypt(number);
        String decrypted = cryptoService.decrypt(encrypted);

        // then
        assertNotEquals(number, encrypted);
        assertEquals(number, decrypted);
        assertEquals("**** **** **** 7899", CardMaskingUtils.mask(decrypted));
    }

    @Test
    @DisplayName("Маскирование короткого номера карты")
    void givenShortCardNumber_whenMask_thenThrowsIllegalArgument() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> CardMaskingUtils.mask("123"));
    }

    @Test
    @DisplayName("Расшифровка поврежденных данных карты")
    void givenBrokenPayload_whenDecrypt_thenThrowsIllegalArgument() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> cryptoService.decrypt("AQID"));
    }
}
