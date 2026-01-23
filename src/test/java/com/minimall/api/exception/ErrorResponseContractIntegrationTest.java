package com.minimall.api.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.AbstractIntegrationTest;
import com.minimall.api.member.dto.request.MemberRegisterRequest;
import com.minimall.domain.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ErrorResponseContractIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("404 NOT_FOUND: missing resource")
    void notFound_contract() throws Exception {
        String path = "/api/members/999999";

        ResultActions result = mockMvc.perform(get(path));

        assertBaseErrorContract(result, 404, ApiErrorCode.NOT_FOUND, path, false);
    }

    @Test
    @DisplayName("400 VALIDATION: @Valid failure")
    void validationError_contract() throws Exception {
        String path = "/api/members/customers";
        String body = """
                {
                  "loginId": "",
                  "password": "",
                  "name": "",
                  "email": "not-an-email",
                  "addr": null
                }
                """;

        ResultActions result = mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        assertBaseErrorContract(result, 400, ApiErrorCode.VALIDATION_ERROR, path, true);
        JsonNode errors = readBody(result).get("errors");
        assertTrue(errors.isArray(), "errors must be an array.");
        assertTrue(errors.size() > 0, "errors must contain items for validation failures.");
        assertFalse(errors.get(0).get("field").asText().isBlank(), "errors[0].field must not be blank.");
        assertFalse(errors.get(0).get("message").asText().isBlank(), "errors[0].message must not be blank.");
    }

    @Test
    @DisplayName("409 CONFLICT: duplicate registration")
    void duplicateMember_contract() throws Exception {
        String path = "/api/members/customers";
        MemberRegisterRequest request = new MemberRegisterRequest(
                "dup123",
                "pass1234!",
                "dupMember",
                "dup123@example.com",
                null
        );

        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ResultActions result = mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        assertBaseErrorContract(result, 409, ApiErrorCode.DUPLICATE_VALUE, path, false);
    }

    @Test
    @DisplayName("422 DOMAIN_RULE_VIOLATION: product rule violation")
    void domainRuleViolation_contract() throws Exception {
        String path = "/api/products";
        String body = """
                {
                  "name": "",
                  "price": -1000,
                  "stockQuantity": -1
                }
                """;

        ResultActions result = mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        assertBaseErrorContract(result, 422, ApiErrorCode.DOMAIN_RULE_VIOLATION, path, false);
    }

    @Test
    @DisplayName("405 METHOD_NOT_ALLOWED: unsupported method")
    void methodNotAllowed_contract() throws Exception {
        String path = "/api/products";

        ResultActions result = mockMvc.perform(put(path));

        assertBaseErrorContract(result, 405, ApiErrorCode.METHOD_NOT_ALLOWED, path, false);
    }

    private void assertBaseErrorContract(ResultActions result,
                                         int expectedStatus,
                                         ApiErrorCode expectedErrorCode,
                                         String expectedPath,
                                         boolean expectsErrors) throws Exception {
        assertTrue(expectedStatus >= 400 && expectedStatus <= 599, "Expected HTTP status must be 4xx/5xx.");

        result.andExpect(status().is(expectedStatus));

        JsonNode root = readBody(result);
        assertTrue(root.has("status"), "Error response must include status.");
        assertEquals(expectedStatus, root.get("status").asInt(), "Error response status must match HTTP status.");
        assertEquals(expectedErrorCode.name(), root.get("errorCode").asText(), "Error response errorCode must match ApiErrorCode.");
        assertFalse(root.get("message").asText().isBlank(), "Error response message must not be blank.");
        assertEquals(expectedPath, root.get("path").asText(), "Error response path must match request URI.");
        assertFalse(root.get("timestamp").asText().isBlank(), "Error response timestamp must not be blank.");
        assertTrue(root.get("errors").isArray(), "Error response errors must be an array.");

        if (expectsErrors) {
            assertTrue(root.get("errors").size() > 0, "Error response errors must not be empty for validation failures.");
        } else {
            assertEquals(0, root.get("errors").size(), "Error response errors must be empty for non-validation errors.");
        }
    }

    private JsonNode readBody(ResultActions result) throws Exception {
        String content = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content);
    }
}
