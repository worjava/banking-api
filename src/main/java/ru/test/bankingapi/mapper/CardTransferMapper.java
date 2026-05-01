package ru.test.bankingapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.test.bankingapi.dto.card.CardTransferResponse;
import ru.test.bankingapi.model.CardTransfer;

/**
 * MapStruct-маппер DTO перевода.
 */
@Mapper(componentModel = "spring")
public interface CardTransferMapper {
    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    CardTransferResponse toResponse(CardTransfer transfer);
}
