package ru.test.bankingapi.mapper;

import org.mapstruct.Mapper;
import ru.test.bankingapi.dto.user.UserResponse;
import ru.test.bankingapi.model.AppUser;

/**
 * MapStruct-маппер пользовательских DTO.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(AppUser user);
}
