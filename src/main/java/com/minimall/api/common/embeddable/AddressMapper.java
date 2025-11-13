package com.minimall.api.common.embeddable;

import com.minimall.domain.embeddable.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDto toDto(Address address);

    Address toEntity(AddressDto dto);
}
