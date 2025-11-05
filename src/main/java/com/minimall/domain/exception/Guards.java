package com.minimall.domain.exception;

import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public final class Guards {
    private Guards(){}

    public static void requireNotNull(Object o, Supplier<? extends RuntimeException> ex) {
        if (o == null) throw ex.get();
    }

    public static void requireNotBlank(String s, Supplier<? extends RuntimeException> ex) {
        if (!StringUtils.hasText(s)) throw ex.get();
    }

    public static void requireNotNullAndNotBlank(String s,
                                                 Supplier<? extends RuntimeException> ex1,
                                                 Supplier<? extends RuntimeException> ex2) {
        if (s == null) throw ex1.get();
        if (!StringUtils.hasText(s)) throw ex2.get();
    }

    public static void requireNonNegative(int value, Supplier<? extends RuntimeException> ex) {
        if (value < 0) throw ex.get();
    }

    public static void requireNotNullAndNonNegative(Integer value,
                                                    Supplier<? extends RuntimeException> ex1,
                                                    Supplier<? extends RuntimeException> ex2) {
        if (value == null) throw ex1.get();
        if (value < 0) throw ex2.get();
    }

    public static void requirePositive(int value, Supplier<? extends RuntimeException> ex) {
        if (value <= 0) throw ex.get();
    }

    public static void requireNotNullAndPositive(Integer value,
                                                    Supplier<? extends RuntimeException> ex1,
                                                    Supplier<? extends RuntimeException> ex2) {
        if (value == null) throw ex1.get();
        if (value <= 0) throw ex2.get();
    }
}
