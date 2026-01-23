package com.minimall.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.AbstractIntegrationTest;
import com.minimall.api.product.dto.request.ProductRegisterRequest;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.fixture.ProductFixture;
import com.minimall.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;

    @Nested
    @DisplayName("GET /api/products")
    class ListProducts {

        @BeforeEach
        void setup() {
            for (int i = 0; i < 25; i++) {
                ProductFixture.createProductSaved(
                        productRepository,
                        "상품-" + i,
                        10_000 + i,
                        10
                );
            }
        }

        @Test
        @DisplayName("기본 페이징 -> 200 + 20개 이하")
        void success_defaultPaging() throws Exception {
            ResultActions result = mockMvc.perform(get("/api/products")
                    .param("page", "0")
                    .param("size", "20"));

            result.andExpect(status().isOk());

            String body = result.andReturn().getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(body);
            assertThat(root.has("items")).isTrue();
            assertThat(root.has("page")).isTrue();
            assertThat(root.has("size")).isTrue();
            assertThat(root.has("hasNext")).isTrue();
            assertThat(root.get("items").isArray()).isTrue();
            assertThat(root.get("items").size()).isLessThanOrEqualTo(20);
            JsonNode first = root.get("items").get(0);
            assertThat(first.has("productId")).isTrue();
            assertThat(first.has("productName")).isTrue();
            assertThat(first.has("productPrice")).isTrue();
            assertThat(first.has("stockQuantity")).isTrue();
            assertThat(first.has("createdAt")).isTrue();
            assertThat(first.has("updatedAt")).isTrue();
        }

        @Test
        @DisplayName("size 상한 -> 100 제한")
        void shouldLimitPageSizeToMax() throws Exception {
            for (int i = 25; i < 120; i++) {
                ProductFixture.createProductSaved(
                        productRepository,
                        "상품-" + i,
                        10_000 + i,
                        10
                );
            }

            ResultActions result = mockMvc.perform(get("/api/products")
                    .param("page", "0")
                    .param("size", "200"));

            result.andExpect(status().isOk());

            String body = result.andReturn().getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(body);
            assertThat(root.get("size").asInt()).isEqualTo(100);
            assertThat(root.get("items").size()).isLessThanOrEqualTo(100);
        }
    }


    @Nested
    @DisplayName("POST /api/products")
    class RegisterProduct {
        @Test
        @DisplayName("상품 등록 -> 201 검증")
        void success() throws Exception {
            //given
            ProductRegisterRequest request = ProductFixture.createProductRegisterRequest("상품이름", 150_000, 50);

            //when
            ResultActions result = mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            MvcResult mvcResult = result.andExpect(status().isCreated()).andReturn();

            String location = mvcResult.getResponse().getHeader("Location");
            assertThat(location).startsWith("/api/products/");
        }

        @Test
        @DisplayName("상품명 공백 -> 422 에러")
        void shouldFail_whenNameIsBlank() throws Exception {
            //given
            ProductRegisterRequest request = ProductFixture.createProductRegisterRequest("", 150_000, 50);

            //when
            ResultActions result = mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            result.andExpect(status().isUnprocessableEntity());
        }

    }

    @Nested
    @DisplayName("POST /api/products/{id}/stock")
    class Stock {

        static final int ORIGINAL_STOCK = 50;
        static final int REQUESTED_QUANTITY = 20;

        Long id;

        @BeforeEach
        void setup() {
            Product savedProduct = ProductFixture.createProductSaved(
                    productRepository,
                    "마우스",
                    150_000,
                    ORIGINAL_STOCK
            );
            id = savedProduct.getId();
        }

        @Nested
        @DisplayName("/add")
        class Add {
            @Test
            @DisplayName("재고 증가 -> 204 + 재고 수량 검증")
            void success() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/add", id)
                        .param("requestedQuantity", String.valueOf(REQUESTED_QUANTITY)));

                //then
                result.andExpect(status().isNoContent());

                Product found = productRepository.findById(id).orElseThrow();
                assertThat(found.getStockQuantity()).isEqualTo(ORIGINAL_STOCK + REQUESTED_QUANTITY);

            }

            @Test
            @DisplayName("0 요청 -> 422 에러")
            void shouldFail_whenRequestZero() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/add", id)
                        .param("requestedQuantity", String.valueOf(0)));

                //then
                result.andExpect(status().isUnprocessableEntity());
            }

            @Test
            @DisplayName("음수 요청 -> 422 에러")
            void shouldFail_whenRequestNegative() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/add", id)
                        .param("requestedQuantity", String.valueOf(-1)));

                //then
                result.andExpect(status().isUnprocessableEntity());
            }
        }

        @Nested
        @DisplayName("/reduce")
        class Reduce {
            @Test
            @DisplayName("재고 감소 -> 204 + 재고 수량 검증")
            void success() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/reduce", id)
                        .param("requestedQuantity", String.valueOf(REQUESTED_QUANTITY)));

                //then
                result.andExpect(status().isNoContent());

                Product found = productRepository.findById(id).orElseThrow();
                assertThat(found.getStockQuantity()).isEqualTo(ORIGINAL_STOCK - REQUESTED_QUANTITY);

            }

            @Test
            @DisplayName("재고 부족 -> 422 에러")
            void shouldFail_whenStockIsInsufficient() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/reduce", id)
                        .param("requestedQuantity", String.valueOf(ORIGINAL_STOCK + 1)));

                //then
                result.andExpect(status().isUnprocessableEntity());

                Product found = productRepository.findById(id).orElseThrow();
                assertThat(found.getStockQuantity()).isEqualTo(ORIGINAL_STOCK);
            }
        }

        @Nested
        @DisplayName("/clear")
        class Clear {
            @Test
            @DisplayName("재고 초기화 -> 204 + 재고 수량 검증")
            void success() throws Exception {
                //when
                ResultActions result = mockMvc.perform(post("/api/products/{id}/stock/clear", id));

                //then
                result.andExpect(status().isNoContent());

                Product found = productRepository.findById(id).orElseThrow();
                assertThat(found.getStockQuantity()).isZero();
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{id}/name")
    class ChangeName {

        static final String ORIGINAL_NAME = "기존 상품명";
        static final String NEW_NAME = "새로운 상품명";


        Long id;

        @BeforeEach
        void setup() {
            Product savedProduct = ProductFixture.createProductSaved(
                    productRepository,
                    ORIGINAL_NAME,
                    150_000,
                    50
            );
            id = savedProduct.getId();
        }

        @Test
        @DisplayName("상품명 변경 -> 204 + 변경된 상품명 검증")
        void success() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/api/products/{id}/name", id)
                    .param("name", NEW_NAME));

            //then
            result.andExpect(status().isNoContent());

            Product found = productRepository.findById(id).orElseThrow();
            assertThat(found.getName()).isEqualTo(NEW_NAME);
        }

        @Test
        @DisplayName("상품명 공백 요청 -> 422 에러")
        void shouldFail_whenNameIsBlank() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/api/products/{id}/name", id)
                    .param("name", "  "));

            //then
            result.andExpect(status().isUnprocessableEntity());

            Product found = productRepository.findById(id).orElseThrow();
            assertThat(found.getName()).isEqualTo(ORIGINAL_NAME);
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{id}/price")
    class ChangePrice {

        static final int ORIGINAL_PRICE = 100_000;
        static final int NEW_PRICE = 200_000;


        Long id;

        @BeforeEach
        void setup() {
            Product savedProduct = ProductFixture.createProductSaved(
                    productRepository,
                    "상품명",
                    ORIGINAL_PRICE,
                    50
            );
            id = savedProduct.getId();
        }

        @Test
        @DisplayName("가격 변경 -> 204 + 변경된 가격 검증")
        void success() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/api/products/{id}/price", id)
                    .param("price", String.valueOf(NEW_PRICE)));

            //then
            result.andExpect(status().isNoContent());

            Product found = productRepository.findById(id).orElseThrow();
            assertThat(found.getPrice()).isEqualTo(NEW_PRICE);
        }

        @Test
        @DisplayName("음수 요청 -> 422 에러")
        void shouldFail_whenNameIsBlank() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/api/products/{id}/price", id)
                    .param("price", String.valueOf(-1)));

            //then
            result.andExpect(status().isUnprocessableEntity());

            Product found = productRepository.findById(id).orElseThrow();
            assertThat(found.getPrice()).isEqualTo(ORIGINAL_PRICE);
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class Delete {
        Long id;
        static final int NOT_EXIST_ID = 999_999;

        @BeforeEach
        void setup() {
            Product savedProduct = ProductFixture.createProductSaved(
                    productRepository,
                    "상품명",
                    150_000,
                    50
            );
            id = savedProduct.getId();
        }

        @Test
        @DisplayName("상품 삭제 -> 204 + DB 검증")
        void success() throws Exception {
            //when
            ResultActions result = mockMvc.perform(delete("/api/products/{id}", id));

            //then
            result.andExpect(status().isNoContent());

            assertThat(productRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("미존재하는 상품 삭제 -> 404 에러")
        void shouldFail_whenProductIsNotFound() throws Exception {
            //when
            ResultActions result = mockMvc.perform(delete("/api/products/{id}", NOT_EXIST_ID));

            //then
            result.andExpect(status().isNotFound());
        }
    }

}
