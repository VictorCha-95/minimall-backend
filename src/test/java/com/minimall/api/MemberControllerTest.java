package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.MemberService;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class MemberControllerTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {
        @Test
        @DisplayName("정상 -> 회원 전체 조회")
        void success() throws Exception {
            //given
            memberService.create(createRequestDto("member1", "손흥민"));
            memberService.create(createRequestDto("member2", "박지성"));
            MemberSummaryResponseDto member1 = memberService.getSummaryByLoginId("member1");
            MemberSummaryResponseDto member2 = memberService.getSummaryByLoginId("member2");

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
        @DisplayName("서버 에러 -> 예외")
        void failure_serverError() throws Exception {
            mockMvc.perform(get("/members/error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("getDetail(Long)")
    class GetDetail {
        @Test
        @DisplayName("정상 -> 회원 단건 상세 조회")
        void success() throws Exception {
            //given
            MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/" + createdMember.id()));

            //then
            assertMemberDetail(createdMember, result);
        }

        @Test
        @DisplayName("회원 ID 없음 -> 예외")
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
    @DisplayName("getSummary(Long)")
    class GetSummary{
        @Test
        @DisplayName("성공 -> 회원 단건 요약 조회")
        void success() throws Exception {
            //given
            MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/" + createdMember.id() + "/summary"));

            //then
            assertMemberSummary(createdMember, result);
        }

        @Test
        @DisplayName("회원 ID 없음 -> 예외")
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
    @DisplayName("getDetailByEmail(String)")
    class GetDetailByEmail {
        @Test
        @DisplayName("정상 -> 회원 상세 조회")
        void success() throws Exception {
            //given
            MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

            //when
            ResultActions result = mockMvc.perform(get("/members/by-email")
                    .param("email", createdMember.loginId() + "@example.com"));

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

    @Test
    void getDetailByEmail_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

        //when
        ResultActions result = mockMvc.perform(get("/members/by-email")
                .param("email", createdMember.loginId() + "@example.com"));

        //then
        assertMemberDetail(createdMember, result);
    }

    @Test
    void getDetailByEmail_failure_notFoundMember() throws Exception {
        //given
        String invalidEmail = "invalid@invalid.com";

        //when
        ResultActions result = mockMvc.perform(get("/members/by-email")
                .param("email", invalidEmail));

        //then
        assertNotFoundMemberError(result, "email", invalidEmail);
    }

    @Test
    void getSummaryByEmail_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

        //when
        ResultActions result = mockMvc.perform(get("/members/by-email/summary")
                .param("email", createdMember.loginId() + "@example.com"));

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

    @Test
    void getDetailByLoginId_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

        //when
        ResultActions result = mockMvc.perform(get("/members/by-loginId")
                .param("loginId", createdMember.loginId()));

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

    @Test
    void getSummaryByLoginId_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

        //when
        ResultActions result = mockMvc.perform(get("/members/by-loginId/summary")
                .param("loginId", createdMember.loginId()));

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

    @Test
    void create_success() throws Exception {
        //given
        MemberCreateRequestDto request = createRequestDto("new123", "새로운 회원");

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
        MemberCreateRequestDto request1 = createRequestDto("new123", "새로운 회원1");
        MemberCreateRequestDto request2 = createRequestDto("new123", "새로운 회원2");

        //when
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        ResultActions result = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        //then
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VALUE"))
                .andExpect(jsonPath("$.message").value("중복되는 loginId는(은) 사용할 수 없습니다. (이미 존재하는 값: new123)"));
    }

    @Test
    void update_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("new123", "새로운 회원"));
        MemberUpdateRequestDto updateRequest = new MemberUpdateRequestDto("12345", "수정된 회원", "updated@example.com", null);

        //when
        ResultActions result = mockMvc.perform(patch("/members/" + createdMember.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(createdMember.loginId()))
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
        memberService.create(new MemberCreateRequestDto("original123", "12345", "original",
                "original123@example.com", null));

        MemberSummaryResponseDto otherMember = memberService.create(new MemberCreateRequestDto("other123", "12345", "other",
                "other123@example.com", null));

        MemberUpdateRequestDto updateRequest = new MemberUpdateRequestDto("12345", "다른 회원",
                "original123@example.com", null);


        //when 중복 이메일로 업데이트 시도
        ResultActions result = mockMvc.perform(patch("/members/" + otherMember.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VALUE"))
                .andExpect(jsonPath("$.message")
                        .value("중복되는 email는(은) 사용할 수 없습니다. (이미 존재하는 값: original123@example.com)"));
    }


    @Test
    void delete_success() throws Exception {
        //given
        MemberSummaryResponseDto createdMember = memberService.create(createRequestDto("member123", "손흥민"));

        //when
        ResultActions result = mockMvc.perform(delete("/members/" + createdMember.id()));

        //then
        result.andExpect(status().isOk());

        assertThrows(MemberNotFoundException.class, () -> memberService.getDetail(createdMember.id()));
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


    //== Validate Methods ==//
    private void assertMemberDetail(MemberSummaryResponseDto member, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(member.loginId()))
                .andExpect(jsonPath("$.name").value(member.name()))
                .andExpect(jsonPath("$.email").value(member.loginId() + "@example.com"))
                .andExpect(jsonPath("$.addr.postcode").value("12345"))
                .andExpect(jsonPath("$.addr.state").value("서울특별시"))
                .andExpect(jsonPath("$.addr.city").value("강남구"))
                .andExpect(jsonPath("$.addr.street").value("테헤란로 1"))
                .andExpect(jsonPath("$.addr.detail").value("101동 202호"));
    }

    private void assertMemberSummary(MemberSummaryResponseDto member, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(member.loginId()))
                .andExpect(jsonPath("$.name").value(member.name()));
    }

    private static void assertNotFoundMemberError(ResultActions result, String fieldName, Object fieldValue) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND_MEMBER"))
                .andExpect(jsonPath("$.message").value(String.format("회원을(를) 찾을 수 없습니다. (%s: %s)", fieldName, fieldValue)));
    }


    //== Helper Methods==//
    private MemberCreateRequestDto createRequestDto(String loginId, String name) {
        return new MemberCreateRequestDto(loginId, "12345", name, loginId + "@example.com",
                Address.createAddress("12345", "서울특별시", "강남구", "테헤란로 1", "101동 202호"));
    }
}