package com.minimall.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRefreshTokenStoreConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jwt.issuer=minimall",
        "jwt.access-ttl-seconds=600",
        "jwt.refresh-ttl-seconds=1209600",
        "jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class AuthControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final String CUSTOMER_ID = "customer";
    private static final String SELLER_ID = "seller";
    private static final String PW = "pass1234!";

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        memberRepository.save(Member.registerCustomer(
                CUSTOMER_ID,
                passwordEncoder.encode(PW),
                "CUSTOMER_ID",
                "CUSTOMER_ID@naver.com",
                new Address("12345", "Seoul", "Gangnam", "Teheran-ro", "101")
        ));

        memberRepository.save(Member.registerSeller(
                SELLER_ID,
                passwordEncoder.encode(PW),
                "SELLER_ID",
                "SELLER_ID@naver.com",
                null,
                "스토어",
                "123-45-67890",
                "account-should-not-leak"
        ));
    }

    @Test
    void me_returns_member_info() throws Exception {
        String accessToken = loginAndGetAccessToken(CUSTOMER_ID, PW);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(CUSTOMER_ID))
                .andExpect(jsonPath("$.email").value("CUSTOMER_ID@naver.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.grade").value("BRONZE"))
                .andExpect(jsonPath("$.addr.postcode").value("12345"))
                .andExpect(jsonPath("$.addr.state").value("Seoul"))
                .andExpect(jsonPath("$.addr.city").value("Gangnam"))
                .andExpect(jsonPath("$.addr.street").value("Teheran-ro"))
                .andExpect(jsonPath("$.addr.detail").value("101"));
    }

    @Test
    void refresh_rotates_token_and_reuse_fails() throws Exception {
        MvcResult loginResult = login(CUSTOMER_ID, PW);
        String refreshToken = extractRefreshToken(loginResult);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        String newRefreshToken = extractRefreshToken(refreshResult);
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_for_seller_does_not_expose_account() throws Exception {
        String accessToken = loginAndGetAccessToken(SELLER_ID, PW);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(SELLER_ID))
                .andExpect(jsonPath("$.storeName").value("스토어"))
                .andExpect(jsonPath("$.businessNumber").value("123-45-67890"))
                .andExpect(jsonPath("$.account").doesNotExist());
    }

    private MvcResult login(String loginId, String password) throws Exception {
        String body = """
                {"loginId":"%s","password":"%s"}
                """.formatted(loginId, password);

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String loginAndGetAccessToken(String loginId, String password) throws Exception {
        MvcResult result = login(loginId, password);
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        return root.get("accessToken").asText();
    }

    private String extractRefreshToken(MvcResult result) {
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotBlank();

        String prefix = "refreshToken=";
        int start = setCookie.indexOf(prefix);
        int end = setCookie.indexOf(';', start);
        if (start < 0) {
            throw new IllegalStateException("refreshToken cookie missing");
        }
        if (end < 0) {
            end = setCookie.length();
        }
        return setCookie.substring(start + prefix.length(), end);
    }
}
