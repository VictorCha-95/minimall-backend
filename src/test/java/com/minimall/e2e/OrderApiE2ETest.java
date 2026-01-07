package com.minimall.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.product.ProductRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("e2e")
@Tag("e2e")
class OrderApiE2ETest {

    private static final String BASE_URL = "http://localhost:8080";

    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: 회원가입 -> 로그인 -> 상품 등록 -> 주문 -> 결제 -> 배송 흐름")
    void orderFlow_success() throws Exception {
        registerSeller("seller-e2e");
        String sellerToken = login("seller-e2e", "pass1234!");

        Long productId = registerProduct(sellerToken, "e2e-product", 15000, 10);

        Long customerId = registerCustomer("customer-e2e", true);
        String customerToken = login("customer-e2e", "pass1234!");

        JsonNode order = createOrder(customerToken, customerId, productId, 2);
        long orderId = order.get("id").asLong();
        int finalAmount = order.get("finalAmount").asInt();

        processPayment(customerToken, orderId, finalAmount);
        prepareDelivery(customerToken, orderId, Map.of(
                "postcode", "12345",
                "state", "Seoul",
                "city", "Gangnam",
                "street", "Teheran-ro",
                "detail", "101"
        ));
        startDelivery(customerToken, orderId);
        completeDelivery(customerToken, orderId);
    }

    @Test
    @DisplayName("E2E 실패: 결제 금액 불일치 -> 422")
    void payment_amount_mismatch_returns_422() throws Exception {
        registerSeller("seller-pay-fail");
        String sellerToken = login("seller-pay-fail", "pass1234!");
        Long productId = registerProduct(sellerToken, "e2e-product", 20000, 5);

        Long customerId = registerCustomer("customer-pay-fail", true);
        String customerToken = login("customer-pay-fail", "pass1234!");

        JsonNode order = createOrder(customerToken, customerId, productId, 1);
        long orderId = order.get("id").asLong();

        HttpStatusCodeException ex = catchThrowableOfType(
                () -> processPayment(customerToken, orderId, 9999),
                HttpStatusCodeException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("E2E 실패: 배송 주소 미입력 + 회원 주소 없음 -> 422")
    void prepare_delivery_without_address_returns_422() throws Exception {
        registerSeller("seller-addr-fail");
        String sellerToken = login("seller-addr-fail", "pass1234!");
        Long productId = registerProduct(sellerToken, "e2e-product", 12000, 5);

        Long customerId = registerCustomer("customer-addr-fail", false);
        String customerToken = login("customer-addr-fail", "pass1234!");

        JsonNode order = createOrder(customerToken, customerId, productId, 1);
        long orderId = order.get("id").asLong();

        HttpStatusCodeException ex = catchThrowableOfType(
                () -> prepareDelivery(customerToken, orderId, null),
                HttpStatusCodeException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("E2E 실패: 고객 토큰으로 상품 등록 시 403")
    void create_product_with_customer_token_returns_403() throws Exception {
        registerCustomer("customer-product-fail", true);
        String customerToken = login("customer-product-fail", "pass1234!");

        HttpStatusCodeException ex = catchThrowableOfType(
                () -> registerProduct(customerToken, "blocked-product", 10000, 1),
                HttpStatusCodeException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private Long registerCustomer(String loginId, boolean includeAddress) throws Exception {
        Map<String, Object> payload = Map.of(
                "loginId", loginId,
                "password", "pass1234!",
                "name", "CUSTOMER",
                "email", loginId + "@example.com",
                "addr", includeAddress ? Map.of(
                        "postcode", "12345",
                        "state", "Seoul",
                        "city", "Gangnam",
                        "street", "Teheran-ro",
                        "detail", "101"
                ) : null
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/members/customers",
                payload,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get("id").asLong();
    }

    private Long registerSeller(String loginId) throws Exception {
        Map<String, Object> payload = Map.of(
                "loginId", loginId,
                "password", "pass1234!",
                "name", "SELLER",
                "email", loginId + "@example.com",
                "storeName", "SELLER-STORE",
                "businessNumber", "123-45-67890",
                "account", "account-should-not-leak"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/members/sellers",
                payload,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get("id").asLong();
    }

    private String login(String loginId, String password) throws Exception {
        Map<String, Object> payload = Map.of(
                "loginId", loginId,
                "password", password
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/auth/login",
                payload,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get("accessToken").asText();
    }

    private Long registerProduct(String token, String name, int price, int stock) throws Exception {
        Map<String, Object> payload = Map.of(
                "name", name,
                "price", price,
                "stock", stock
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                BASE_URL + "/api/products",
                request,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return extractId(response.getHeaders().getLocation());
    }

    private JsonNode createOrder(String token, Long memberId, Long productId, int quantity) throws Exception {
        Map<String, Object> payload = Map.of(
                "memberId", memberId,
                "items", List.of(
                        Map.of("productId", productId, "quantity", quantity)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/orders",
                request,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return objectMapper.readTree(response.getBody());
    }

    private void processPayment(String token, long orderId, int payAmount) {
        Map<String, Object> payload = Map.of(
                "payMethod", "CARD",
                "payAmount", payAmount
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/orders/" + orderId + "/payment",
                request,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void prepareDelivery(String token, long orderId, Map<String, Object> address) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(address, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/api/orders/" + orderId + "/delivery",
                request,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private void startDelivery(String token, long orderId) {
        Map<String, Object> payload = Map.of(
                "trackingNo", "TRACK-1234",
                "shippedAt", LocalDateTime.now().minusMinutes(10).withNano(0).toString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/api/orders/" + orderId + "/delivery",
                HttpMethod.PATCH,
                request,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private void completeDelivery(String token, long orderId) {
        Map<String, Object> payload = Map.of(
                "arrivedAt", LocalDateTime.now().withNano(0).toString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/api/orders/" + orderId + "/delivery/complete",
                HttpMethod.PATCH,
                request,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private Long extractId(URI location) {
        assertThat(location).isNotNull();
        String path = location.getPath();
        String[] segments = path.split("/");
        return Long.parseLong(segments[segments.length - 1]);
    }
}
