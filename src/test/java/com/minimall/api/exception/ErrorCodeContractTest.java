package com.minimall.api.exception;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeContractTest {

    @Test
    void apiErrorCode_hasNonEmptyUniqueCodes() {
        ApiErrorCode[] values = ApiErrorCode.values();
        assertTrue(values.length > 0, "ApiErrorCode must declare at least one value.");

        Set<String> codes = new HashSet<>();
        for (ApiErrorCode value : values) {
            String code = value.name();
            assertNotNull(code, "ApiErrorCode name must not be null.");
            assertFalse(code.isBlank(), "ApiErrorCode name must not be blank.");
            assertTrue(codes.add(code), "ApiErrorCode name must be unique: " + code);
        }
    }
}
