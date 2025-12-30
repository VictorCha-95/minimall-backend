package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.AbstractIntegrationTest;
import com.minimall.api.member.dto.request.MemberAddressRequest;
import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.request.MemberLoginRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.OrderRepository;
import com.minimall.service.member.dto.MemberAddressCommand;
import com.minimall.service.member.dto.MemberCreateCommand;
import com.minimall.service.member.dto.MemberSummaryResult;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.domain.product.Product;
import com.minimall.service.order.OrderService;
import com.minimall.service.product.ProductService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.member.MemberService;
import com.minimall.service.product.dto.ProductRegisterCommand;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberApiControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    OrderService orderService;

    @Autowired
    ProductService productService;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /members/login")
    class Login {
        @Test
        @DisplayName("회원 로그인 성공 -> 200 OK")
        void success() throws Exception {
            //given
            memberService.createCustomer(createCommand("member123", "손흥민"));
            MemberLoginRequest request = new MemberLoginRequest("member123", "12345");

            //when
            ResultActions result = mockMvc.perform(post("/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.loginId").value("member123"))
                    .andExpect(jsonPath("$.name").value("손흥민"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("비밀번호 오류 -> 401 isUnauthorized")
        void shouldReturn401_whenPasswordIsWrong() throws Exception {
            // given
            memberService.createCustomer(createCommand("member123", "손흥민"));
            MemberLoginRequest request = new MemberLoginRequest("member123", "wrong-password");

            // when
            ResultActions result = mockMvc.perform(post("/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("잘못된 회원아이디 -> 404 NotFound")
        void shouldReturn404_whenMemberNotFound() throws Exception {
            // given
            MemberLoginRequest request = new MemberLoginRequest("no_such_user", "12345");

            // when
            ResultActions result = mockMvc.perform(post("/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
        }
    }



    @Nested
    @DisplayName("GET /members")
    class GetAll {
        @Test
        @DisplayName("정상 -> 회원 전체 조회")
        void success() throws Exception {
            //given
            memberService.createCustomer(createCommand("member1", "손흥민"));
            memberService.createCustomer(createCommand("member2", "박지성"));
            MemberSummaryResult member1 = memberService.getSummaryByLoginId("member1");
            MemberSummaryResult member2 = memberService.getSummaryByLoginId("member2");

            //when
            ResultActions result = mockMvc.perform(get("/members"));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(member1.id()))
                    .andExpect(jsonPath("$[0].loginId").value(member1.loginId()))
                    .andExpect(jsonPath("$[0].name").value(member1.name()))
                    .andExpect(jsonPath("$[1].id").value(member2.id()))
                    .andExpect(jsonPath("$[1].loginId").value(member2.loginId()))
                    .andExpect(jsonPath("$[1].name").value(member2.name()));
        }

        @Test
        @DisplayName("저장된 회원 없음 -> 빈 회원 리스트 반환")
        void success_empty() throws Exception {
            mockMvc.perform(get("/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("알 수 없는 오류 -> 500 Internal Error")
        void failure_serverError() throws Exception {
            mockMvc.perform(get("/members/error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
        }
    }

    @Nested
    @DisplayName("GET /members/{id}")
    class GetDetail {
        @Test
        @DisplayName("정상 -> 회원 단건 상세 조회")
        void success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/" + createdMember.getId()));

            //then
            assertMemberDetail(createdMember, result);
        }

        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void failure_notFoundMember() throws Exception {
            //given
            long invalidId = 999L;

            //when
            ResultActions result = mockMvc.perform(get("/members/" + invalidId));

            //then
            assertNotFoundMemberError(result, "id", invalidId);
        }

    }

    @Nested
    @DisplayName("GET /members/{id}/summary")
    class GetSummary{
        @Test
        @DisplayName("성공 -> 회원 단건 요약 조회")
        void success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/" + createdMember.getId() + "/summary"));

            //then
            assertMemberSummary(createdMember, result);
        }

        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void failure_notFoundMember() throws Exception {
            //given
            long invalidId = 999L;

            //when
            ResultActions result = mockMvc.perform(get("/members/" + invalidId + "/summary"));

            //then
            assertNotFoundMemberError(result, "id", invalidId);
        }
    }

    @Nested
    @DisplayName("getDetailWithOrders(Long)")
    class GetDetailWithOrders {
        @Test
        void getDetailWithOrders() {
            //TODO order 개발 후 테스트
        }
    }

    @Nested
    @DisplayName("GET /members/by-email")
    class GetDetailByEmail {
        @Test
        @DisplayName("정상 -> 회원 상세 조회")
        void success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/by-email")
                    .param("email", createdMember.getLoginId() + "@example.com"));

            //then
            assertMemberDetail(createdMember, result);
        }

        @Test
        @DisplayName("회원 없음 -> 예외")
        void failure_notFoundMember() throws Exception {
            //given
            String invalidEmail = "invalid@invalid.com";

            //when
            ResultActions result = mockMvc.perform(get("/members/by-email")
                    .param("email", invalidEmail));

            //then
            assertNotFoundMemberError(result, "email", invalidEmail);
        }
    }

    @Nested
    @DisplayName("GET /members/by-email/summary")
    class GetSummaryByEmail{
        @Test
        void getSummaryByEmail_success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/by-email/summary")
                    .param("email", createdMember.getLoginId() + "@example.com"));

            //then
            assertMemberSummary(createdMember, result);
        }

        @Test
        void getSummaryByEmail_failure_notFoundMember() throws Exception {
            //given
            String invalidEmail = "invalid@invalid.com";

            //when
            ResultActions result = mockMvc.perform(get("/members/by-email/summary")
                    .param("email", invalidEmail));

            //then
            assertNotFoundMemberError(result, "email", invalidEmail);
        }
    }

    @Nested
    @DisplayName("GET /members/by-loginId")
    class GetDetailByLoginId{
        @Test
        void getDetailByLoginId_success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/by-loginId")
                    .param("loginId", createdMember.getLoginId()));

            //then
            assertMemberDetail(createdMember, result);
        }

        @Test
        void getDetailByLoginId_failure_notFoundMember() throws Exception {
            //given
            String invalidLoginId = "invalidLoginId";

            //when
            ResultActions result = mockMvc.perform(get("/members/by-loginId")
                    .param("loginId", invalidLoginId));

            //then
            assertNotFoundMemberError(result, "loginId", invalidLoginId);
        }
    }

    @Nested
    @DisplayName("GET /members/by-loginId/summary")
    class GetSummaryByLoginId{
        @Test
        void getSummaryByLoginId_success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/by-loginId/summary")
                    .param("loginId", createdMember.getLoginId()));

            //then
            assertMemberSummary(createdMember, result);

        }

        @Test
        void getSummaryByLoginId_failure_notFoundMember() throws Exception {
            //given
            String invalidLoginId = "invalidLoginId";

            //when
            ResultActions result = mockMvc.perform(get("/members/by-loginId/summary")
                    .param("loginId", invalidLoginId));

            //then
            assertNotFoundMemberError(result, "loginId", invalidLoginId);
        }
    }

    @Nested
    @DisplayName("POST /members")
    class Create{
        @Test
        void create_success() throws Exception {
            //given
            MemberCreateRequest request = createRequest("new123", "새로운 회원");

            //when
            ResultActions result = mockMvc.perform(post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.loginId").value("new123"))
                    .andExpect(jsonPath("$.name").value("새로운 회원"));
        }

        @Test
        void create_shouldFail_whenDuplicateLoginId() throws Exception {
            //given
            MemberCreateRequest request1 = createRequest("new123", "새로운 회원1");
            MemberCreateRequest request2 = createRequest("new123", "새로운 회원2");

            //when
            mockMvc.perform(post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)));

            ResultActions result = mockMvc.perform(post("/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)));

            //then
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VALUE"))
                    .andExpect(jsonPath("$.message", Matchers.containsString("loginId")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("사용 중")));
        }
    }

    @Nested
    @DisplayName("UPDATE /members/{id}")
    class Update{
        @Test
        void update_success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("new123", "새로운 회원"));
            MemberUpdateRequest updateRequest = new MemberUpdateRequest("12345", "수정된 회원", "updated@example.com", null);

            //when
            ResultActions result = mockMvc.perform(patch("/members/" + createdMember.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.loginId").value(createdMember.getLoginId()))
                    .andExpect(jsonPath("$.name").value(updateRequest.name()))
                    .andExpect(jsonPath("$.email").value(updateRequest.email()))
                    //PATCH: 주소는 수정 요청 없었으므로 기존 값 유지
                    .andExpect(jsonPath("$.addr.postcode").value("12345"))
                    .andExpect(jsonPath("$.addr.state").value("서울특별시"))
                    .andExpect(jsonPath("$.addr.city").value("강남구"))
                    .andExpect(jsonPath("$.addr.street").value("테헤란로 1"))
                    .andExpect(jsonPath("$.addr.detail").value("101동 202호"));
        }

        @Test
        void update_shouldFail_whenDuplicateEmail() throws Exception {
            //given
            memberService.createCustomer(new MemberCreateCommand("original123", "12345", "original",
                    "original123@example.com", null));

            Member otherMember = memberService.createCustomer(new MemberCreateCommand("other123", "12345", "other",
                    "other123@example.com", null));

            MemberUpdateRequest updateRequest = new MemberUpdateRequest("12345", "다른 회원",
                    "original123@example.com", null);


            //when 중복 이메일로 업데이트 시도
            ResultActions result = mockMvc.perform(patch("/members/" + otherMember.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)));

            //then
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VALUE"))
                    .andExpect(jsonPath("$.message", Matchers.containsString("email")));
        }
    }

    @Nested
    @DisplayName("DELETE /members/{id}")
    class Delete{
        @Test
        void delete_success() throws Exception {
            //given
            Member createdMember = memberService.createCustomer(createCommand("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(delete("/members/" + createdMember.getId()));

            //then
            result.andExpect(status().isNoContent());

            assertThrows(MemberNotFoundException.class, () -> memberService.getDetail(createdMember.getId()));
        }

        @Test
        void delete_shouldFail_whenNotFoundMember() throws Exception {
            //given
            long invalidId = 999L;

            //when
            ResultActions result = mockMvc.perform(delete("/members/" + invalidId));

            //then
            assertNotFoundMemberError(result, "id", invalidId);
        }
    }


    @Nested
    @DisplayName("GET /members/{id}/orders")
    class GetOrderSummaries {
        @Test
        @DisplayName("주문 목록 요약 조회 -> 200 + JSON 검증")
        void success() throws Exception {
            //given
            Product book = productService.register(new ProductRegisterCommand("도서", 10_000, 20));
            Product mouse = productService.register(new ProductRegisterCommand("마우스", 20_000, 50));

            Member member =
                    memberService.createCustomer(new MemberCreateCommand("loginId123", "12345", "박지성", "ex@ex.com", null));
            Member foundMember = memberRepository.findById(member.getId()).get();

            orderService.createOrder(new OrderCreateCommand(foundMember.getId(),
                    List.of(new OrderItemCreateCommand(book.getId(), 10))));
            orderService.createOrder(new OrderCreateCommand(foundMember.getId(),
                    List.of(new OrderItemCreateCommand(mouse.getId(), 10))));

            //when
            ResultActions result = mockMvc.perform(get("/members/" + foundMember.getId() + "/orders"));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2));

        }

        @Test
        @DisplayName("회원 주문 없음: 빈 리스트 반환")
        void returnEmpty_whenOrderIsEmpty() throws Exception {
            //given
            Member member = memberService.createCustomer(new MemberCreateCommand("loginId123", "12345", "박지성", "ex@ex.com", null));
            Member foundMember = memberRepository.findById(member.getId()).get();

            //when
            ResultActions result = mockMvc.perform(get("/members/" + foundMember.getId() + "/orders"));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }



    //== Validate Methods ==//
    private void assertMemberDetail(Member member, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(member.getLoginId()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.email").value(member.getLoginId() + "@example.com"))
                .andExpect(jsonPath("$.addr.postcode").value("12345"))
                .andExpect(jsonPath("$.addr.state").value("서울특별시"))
                .andExpect(jsonPath("$.addr.city").value("강남구"))
                .andExpect(jsonPath("$.addr.street").value("테헤란로 1"))
                .andExpect(jsonPath("$.addr.detail").value("101동 202호"));
    }

    private void assertMemberSummary(Member member, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(member.getLoginId()))
                .andExpect(jsonPath("$.name").value(member.getName()));
    }

    private static void assertNotFoundMemberError(ResultActions result, String fieldName, Object fieldValue) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(String.format("회원을(를) 찾을 수 없습니다. (%s: %s)", fieldName, fieldValue)));
    }


    //== Helper Methods==//
    private MemberCreateCommand createCommand(String loginId, String name) {
        return new MemberCreateCommand(loginId, "12345", name, loginId + "@example.com",
                new MemberAddressCommand("12345", "서울특별시", "강남구", "테헤란로 1", "101동 202호"));
    }

    private MemberCreateRequest createRequest(String loginId, String name) {
        return new MemberCreateRequest(loginId, "12345", name, loginId + "@example.com",
                new MemberAddressRequest("12345", "서울특별시", "강남구", "테헤란로 1", "101동 202호"));
    }
}