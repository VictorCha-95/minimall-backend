package com.minimall.service.member;

import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.api.member.dto.response.MemberDetailResponse;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.domain.order.OrderRepository;
import com.minimall.service.exception.NotFoundException;
import com.minimall.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Transactional
public class MemberServiceIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    private Member member;
    private MemberCreateRequest createRequest;
    private MemberUpdateRequest updateRequest;


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

        //== CreateRequest DTO ==//
        createRequest = new MemberCreateRequest(member.getLoginId(), member.getPassword(), member.getName(), member.getEmail(), member.getAddr());

        //== UpdateRequest DTO ==//
        updateRequest = new MemberUpdateRequest(
                "newPassword456",
                "차태승2",
                "cts9458+update@naver.com",
                member.getAddr()
        );

        orderRepository.deleteAll();
        memberRepository.deleteAll();
    }

    //== create ==//
    @Test
    void createMember_success() {
        //when
        MemberSummaryResponse created = memberService.create(createRequest);

        //then
        MemberSummaryResponse found = memberService.getSummary(created.id());
        assertThat(found.name()).isEqualTo(created.name());
    }

    @Test
    void createMember_duplicateLoginId_shouldFail() {
        //given
        memberService.create(createRequest);
        MemberCreateRequest duplicateLoginIdRequest =
                new MemberCreateRequest(member.getLoginId(), member.getPassword(), "차태승", "example@naver.com", member.getAddr());

        //then
        assertThrows(DuplicateException.class, () -> memberService.create(duplicateLoginIdRequest));
    }

    @Test
    void createMember_duplicateEmail_shouldFail() {
        //given
        memberService.create(createRequest);
        MemberCreateRequest duplicateEmailRequest =
                new MemberCreateRequest("exampleLoginId", member.getPassword(), "차태승", member.getEmail(), member.getAddr());

        //then
        assertThrows(DuplicateException.class, () -> memberService.create(duplicateEmailRequest));
    }

    //== update ==//
    @Test
    void updateMember_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberDetailResponse updated = memberService.update(created.id(), updateRequest);
        MemberDetailResponse found = memberService.getDetail(updated.id());

        //then
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo(updateRequest.name());
        assertThat(found.email()).isEqualTo(updateRequest.email());
    }

    @Test
    void updateMember_duplicateEmail_shouldFail() {
        //given
        MemberSummaryResponse original = memberService.create(createRequest);
        MemberDetailResponse foundOriginal = memberService.getDetail(original.id());
        MemberSummaryResponse newCreated = memberService.create(new MemberCreateRequest(
                "example123", "12345", "이름", "example123@naver.com", null));

        //then
        assertThrows(DuplicateException.class,
                () -> memberService.update(newCreated.id(),
                        new MemberUpdateRequest(null, null, foundOriginal.email(), null)));
    }

    //== delete ==//
    @Test
    void deleteMember_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //then
        memberService.delete(created.id());
    }

    @Test
    void deleteMember_deletedMemberFind_shouldFail() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        memberService.delete(created.id());

        //then
        assertThrows(NotFoundException.class,
                () -> memberService.getSummary(created.id()));
    }

    @Test
    void getMemberSummary_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberSummaryResponse memberSummary = memberService.getSummary(created.id());

        //then
        assertThat(memberSummary.id()).isEqualTo(created.id());
        assertThat(memberSummary.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummary.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetail_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberDetailResponse memberDetail = memberService.getDetail(created.id());

        //then
        assertThat(memberDetail.id()).isEqualTo(created.id());
        assertThat(memberDetail.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailWithOrders_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberDetailWithOrdersResponse memberDetailWithOrders = memberService.getDetailWithOrders(created.id());

        //then
        assertThat(memberDetailWithOrders.id()).isEqualTo(created.id());
        assertThat(memberDetailWithOrders.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailWithOrders.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberSummaryByEmail_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberSummaryResponse memberSummaryByEmail = memberService.getSummaryByEmail(createRequest.email());

        //then
        assertThat(memberSummaryByEmail.id()).isEqualTo(created.id());
        assertThat(memberSummaryByEmail.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummaryByEmail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailByEmail_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberDetailResponse memberDetailByEmail = memberService.getDetailByEmail(createRequest.email());

        //then
        assertThat(memberDetailByEmail.id()).isEqualTo(created.id());
        assertThat(memberDetailByEmail.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailByEmail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberSummaryByLoginId_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberSummaryResponse memberSummaryByLoginId = memberService.getSummaryByLoginId(created.loginId());

        //then
        assertThat(memberSummaryByLoginId.id()).isEqualTo(created.id());
        assertThat(memberSummaryByLoginId.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummaryByLoginId.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailByLoginId_success() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        MemberDetailResponse memberDetailByLoginId = memberService.getDetailByLoginId(created.loginId());

        //then
        assertThat(memberDetailByLoginId.id()).isEqualTo(created.id());
        assertThat(memberDetailByLoginId.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailByLoginId.name()).isEqualTo(created.name());
    }

    @Test
    void getMembers() {
        //given
        MemberSummaryResponse created = memberService.create(createRequest);

        //when
        List<MemberSummaryResponse> result = memberService.getMembers();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsExactly(created);
    }
}
