package com.minimall.api.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ErrorResponseSecurityContractIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("401 UNAUTHORIZED: security error response contract")
    void unauthorized_contract() throws Exception {
        String path = "/api/admin/ping";

        ResultActions result = mockMvc.perform(get(path));

        result.andExpect(status().isUnauthorized());

        JsonNode root = readBody(result);
        assertTrue(root.has("status"), "Error response must include status.");
        assertEquals(401, root.get("status").asInt(), "Error response status must match HTTP status.");
        assertEquals(ApiErrorCode.INVALID_CREDENTIALS.name(), root.get("errorCode").asText(),
                "Error response errorCode must match ApiErrorCode.");
        assertFalse(root.get("message").asText().isBlank(), "Error response message must not be blank.");
        assertEquals(path, root.get("path").asText(), "Error response path must match request URI.");
        assertFalse(root.get("timestamp").asText().isBlank(), "Error response timestamp must not be blank.");
        assertTrue(root.get("errors").isArray(), "Error response errors must be an array.");
        assertEquals(0, root.get("errors").size(), "Error response errors must be empty for auth errors.");
    }

    private JsonNode readBody(ResultActions result) throws Exception {
        String content = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content);
    }
}
