package ru.test.bankingapi.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.model.CardStatus;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Вспомогательная логика для CardMapper.
 */
@Component
public class CardMapperSupport {
    private final Clock clock;

    public CardMapperSupport(Clock clock) {
        this.clock = clock;
    }

    @Named("resolveStatus")
    public CardStatus resolveStatus(Card card) {
        return card.resolveStatus(LocalDate.now(clock));
    }
}
