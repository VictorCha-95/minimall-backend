package com.minimall.domain.embeddable;

public record AddressDto(
    String postcode,
    String state,
    String city,
    String street,
    String detail
) {}
