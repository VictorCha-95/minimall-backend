package com.minimall.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final String ADMIN_ID = "admin";
    private static final String CUSTOMER_ID = "customer";
    private static final String PW = "pass1234!";

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        // 아래는 네 도메인에 맞게 필드/팩토리만 맞춰라.
        // 핵심은 (loginId, passwordHash, role, status=ACTIVE) 저장이야.
        memberRepository.save(Member.registerAdmin(
                ADMIN_ID,
                passwordEncoder.encode(PW),
                "ADMIN_ID",
                "ADMIN_ID@naver.com",
                null
        ));

        memberRepository.save(Member.registerCustomer(
                CUSTOMER_ID,
                passwordEncoder.encode(PW),
                "CUSTOMER_ID",
                "CUSTOMER_ID@naver.com",
                null
        ));
    }

    @Test
    void adminPing_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPing_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/admin/ping")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPing_withCustomerToken_returns403() throws Exception {
        String token = loginAndGetAccessToken(CUSTOMER_ID, PW);

        mockMvc.perform(get("/admin/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPing_withAdminToken_returns200() throws Exception {
        String token = loginAndGetAccessToken(ADMIN_ID, PW);

        mockMvc.perform(get("/admin/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    private String loginAndGetAccessToken(String loginId, String password) throws Exception {
        String body = """
                {"loginId":"%s","password":"%s"}
                """.formatted(loginId, password);

        String json = mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        return root.get("accessToken").asText();
    }
}
