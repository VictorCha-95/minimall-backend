package com.minimall.domain.member;

public class Fields {
    public static final String MEMBER_LOGIN_ID = "member.loginId";
    public static final String MEMBER_PASSWORD = "member.password";
    public static final String MEMBER_NAME     = "member.name";
    public static final String MEMBER_EMAIL    = "member.email";
    public static final String MEMBER_ADDR     = "addr";

    public static String pathOf(String simple) {
        return switch (simple) {
            case "loginId" -> MEMBER_LOGIN_ID;
            case "password"-> MEMBER_PASSWORD;
            case "name"    -> MEMBER_NAME;
            case "email"   -> MEMBER_EMAIL;
            case "addr"    -> MEMBER_ADDR;
            default        -> simple;
        };
    }
}
