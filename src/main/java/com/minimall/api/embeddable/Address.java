package com.minimall.api.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String postcode;
    private String state;
    private String city;
    private String street;
    private String detail;

    @Builder
    public Address(String postcode, String state, String city, String street, String detail) {
        this.postcode = postcode;
        this.state = state;
        this.city = city;
        this.street = street;
        this.detail = detail;
    }
}
