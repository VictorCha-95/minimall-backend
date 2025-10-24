package com.minimall.domain.member;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.dto.MemberMapper;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.exception.DuplicateException;
import com.minimall.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberServiceIntegrationTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberMapper memberMapper;


    private Member member;
    private MemberCreateRequestDto createRequest;
    private MemberUpdateRequestDto updateRequest;


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
        createRequest = new MemberCreateRequestDto(member.getLoginId(), member.getPassword(), member.getName(), member.getEmail(), member.getAddr());

        //== UpdateRequest DTO ==//
        updateRequest = new MemberUpdateRequestDto(
                "newPassword456",
                "차태승2",
                "cts9458+update@naver.com",
                member.getAddr()
        );
    }

    //== create ==//
    @Test
    void createMember_success() {
        //when
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //then
        MemberSummaryResponseDto found = memberService.getSummary(created.id());
        assertThat(found.name()).isEqualTo(created.name());
    }

    @Test
    void createMember_duplicateLoginId_shouldFail() {
        //given
        memberService.create(createRequest);
        MemberCreateRequestDto duplicateLoginIdRequest =
                new MemberCreateRequestDto(member.getLoginId(), member.getPassword(), "차태승", "example@naver.com", member.getAddr());

        //then
        assertThrows(DuplicateException.class, () -> memberService.create(duplicateLoginIdRequest));
    }

    @Test
    void createMember_duplicateEmail_shouldFail() {
        //given
        memberService.create(createRequest);
        MemberCreateRequestDto duplicateEmailRequest =
                new MemberCreateRequestDto("exampleLoginId", member.getPassword(), "차태승", member.getEmail(), member.getAddr());

        //then
        assertThrows(DuplicateException.class, () -> memberService.create(duplicateEmailRequest));
    }

    //== update ==//
    @Test
    void updateMember_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberDetailResponseDto updated = memberService.update(created.id(), updateRequest);
        MemberDetailResponseDto found = memberService.getDetail(updated.id());

        //then
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo(updateRequest.name());
        assertThat(found.email()).isEqualTo(updateRequest.email());
    }

    @Test
    void updateMember_duplicateEmail_shouldFail() {
        //given
        MemberSummaryResponseDto original = memberService.create(createRequest);
        MemberDetailResponseDto foundOriginal = memberService.getDetail(original.id());
        MemberSummaryResponseDto newCreated = memberService.create(new MemberCreateRequestDto(
                "example123", "12345", "이름", "example123@naver.com", null));

        //then
        assertThrows(DuplicateException.class,
                () -> memberService.update(newCreated.id(),
                        new MemberUpdateRequestDto(null, null, foundOriginal.email(), null)));
    }

    //== delete ==//
    @Test
    void deleteMember_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //then
        memberService.delete(created.id());
    }

    @Test
    void deleteMember_deletedMemberFind_shouldFail() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        memberService.delete(created.id());

        //then
        assertThrows(NotFoundException.class,
                () -> memberService.getSummary(created.id()));
    }

    @Test
    void getMemberSummary_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberSummaryResponseDto memberSummary = memberService.getSummary(created.id());

        //then
        assertThat(memberSummary.id()).isEqualTo(created.id());
        assertThat(memberSummary.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummary.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetail_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberDetailResponseDto memberDetail = memberService.getDetail(created.id());

        //then
        assertThat(memberDetail.id()).isEqualTo(created.id());
        assertThat(memberDetail.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailWithOrders_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberDetailWithOrdersResponseDto memberDetailWithOrders = memberService.getDetailWithOrders(created.id());

        //then
        assertThat(memberDetailWithOrders.id()).isEqualTo(created.id());
        assertThat(memberDetailWithOrders.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailWithOrders.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberSummaryByEmail_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberSummaryResponseDto memberSummaryByEmail = memberService.getSummaryByEmail(createRequest.email());

        //then
        assertThat(memberSummaryByEmail.id()).isEqualTo(created.id());
        assertThat(memberSummaryByEmail.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummaryByEmail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailByEmail_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberDetailResponseDto memberDetailByEmail = memberService.getDetailByEmail(createRequest.email());

        //then
        assertThat(memberDetailByEmail.id()).isEqualTo(created.id());
        assertThat(memberDetailByEmail.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailByEmail.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberSummaryByLoginId_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberSummaryResponseDto memberSummaryByLoginId = memberService.getSummaryByLoginId(created.loginId());

        //then
        assertThat(memberSummaryByLoginId.id()).isEqualTo(created.id());
        assertThat(memberSummaryByLoginId.loginId()).isEqualTo(created.loginId());
        assertThat(memberSummaryByLoginId.name()).isEqualTo(created.name());
    }

    @Test
    void getMemberDetailByLoginId_success() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        MemberDetailResponseDto memberDetailByLoginId = memberService.getDetailByLoginId(created.loginId());

        //then
        assertThat(memberDetailByLoginId.id()).isEqualTo(created.id());
        assertThat(memberDetailByLoginId.loginId()).isEqualTo(created.loginId());
        assertThat(memberDetailByLoginId.name()).isEqualTo(created.name());
    }

    @Test
    void getMembers() {
        //given
        MemberSummaryResponseDto created = memberService.create(createRequest);

        //when
        List<MemberSummaryResponseDto> result = memberService.getMembers();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsExactly(created);
    }
}
