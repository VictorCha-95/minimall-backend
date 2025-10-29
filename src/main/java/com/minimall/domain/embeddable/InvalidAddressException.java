package com.minimall.domain.embeddable;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException(String message) {
        super(message);
    }

    public static InvalidAddressException empty() {
        return new InvalidAddressException("주소는 필수 입력 값입니다.");
    }

    public static InvalidAddressException missingRequiredFields() {
        return new InvalidAddressException("주소의 필수 항목이 누락되었습니다.");
    }


}
