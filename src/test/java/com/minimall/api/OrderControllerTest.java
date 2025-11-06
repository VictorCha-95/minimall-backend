package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class OrderControllerTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    private OrderCreateRequestDto createRequest;

    private Member member;
    private Product book;
    private Product keyboard;


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();
        Member savedMember = memberRepository.save(member);

        //== Product Entity ==//
        book = new Product("도서", 20000, 50);
        keyboard = new Product("키보드", 100000, 20);
        Product savedBook = productRepository.save(book);
        Product savedKeyboard = productRepository.save(keyboard);

        //== OrderItemRequestList ==//
        List<OrderItemCreateDto> orderItems = new ArrayList<>();

        OrderItemCreateDto orderItemCreateRequest1 = new OrderItemCreateDto(savedBook.getId(), 30);
        OrderItemCreateDto orderItemCreateRequest2 = new OrderItemCreateDto(savedKeyboard.getId(), 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        createRequest = new OrderCreateRequestDto(savedMember.getId(), orderItems);
    }

    @Nested
    @DisplayName("create(OrderCreateRequest)")
    class Create {
        @Test
        @DisplayName("정상 -> 주문 생성")
        void success() throws Exception {
            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));

            //then
            MvcResult mvcResult = result.andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.orderedAt").exists())
                    .andExpect(jsonPath("$.orderStatus").value("ORDERED"))
                    .andExpect(jsonPath("$.itemCount").value(createRequest.items().size()))
                    .andReturn();

            String json = mvcResult.getResponse().getContentAsString();
            OrderCreateResponseDto body = objectMapper.readValue(json, OrderCreateResponseDto.class);

            String location = mvcResult.getResponse().getHeader("Location");
            assertThat(location).endsWith("/orders/" + body.id());

            int expectedAmount = createRequest.items().stream()
                    .mapToInt(i -> {
                        int price = Objects.equals(i.productId(), keyboard.getId()) ? keyboard.getPrice() : book.getPrice();
                        return price * i.quantity();
                    })
                    .sum();

            assertSoftly(softly -> {
                softly.assertThat(body.originalAmount()).isEqualTo(expectedAmount);
                softly.assertThat(body.finalAmount()).isEqualTo(expectedAmount);
                softly.assertThat(body.discountAmount()).isZero();
            });
        }
    }
}