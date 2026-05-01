package ru.test.bankingapi.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.test.bankingapi.dto.card.BalanceResponse;
import ru.test.bankingapi.dto.card.CardResponse;
import ru.test.bankingapi.model.Card;
import ru.test.bankingapi.util.CardMaskingUtils;

/**
 * MapStruct-маппер карточных DTO.
 */
@Mapper(
        componentModel = "spring",
        uses = CardMapperSupport.class,
        imports = CardMaskingUtils.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CardMapper {
    @Mapping(target = "maskedNumber", expression = "java(CardMaskingUtils.maskLastFour(card.getLastFour()))")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "status", source = "card", qualifiedByName = "resolveStatus")
    CardResponse toResponse(Card card);

    @Mapping(target = "cardId", source = "id")
    BalanceResponse toBalanceResponse(Card card);
}
