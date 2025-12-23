package com.minimall.domain.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequiredArgsConstructor
public class Address {

    @Column(length = 20)
    private final String postcode;

    @Column(length = 50)
    private final String state;

    @Column(length = 50)
    private final String city;

    private final String street;

    private final String detail;

    public static Address createAddress(String postcode, String state, String city, String street, String detail) {
        if (postcode == null || state == null || city == null || street == null) {
            throw InvalidAddressException.missingRequiredFields();
        }

        return new Address(postcode, state, city, street, detail);
    }

    //TODO 이메일 형식 검증 추가
}
