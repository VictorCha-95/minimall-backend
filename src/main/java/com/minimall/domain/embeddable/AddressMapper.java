package com.minimall.domain.embeddable;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDto toDto(Address address);

    Address toEntity(AddressDto dto);
}
