package com.minimall.domain.member;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.dto.MemberMapper;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberMapper memberMapper;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    private Member member;
    private MemberCreateRequestDto createRequest;
    private MemberUpdateRequestDto updateRequest;
    private MemberDetailResponseDto detailResponse;
    private MemberSummaryResponseDto summaryResponse;


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

        //== Response DTOs ==//
        summaryResponse = new MemberSummaryResponseDto(member.getId(), member.getLoginId(), member.getName());

        detailResponse = new MemberDetailResponseDto(member.getId(), member.getLoginId(), member.getName(), member.getEmail(), member.getGrade(), member.getAddr());
    }


    //== create ==//
    @Test
    void createMember_success() {
        //given
        when(memberRepository.existsByLoginId(createRequest.loginId())).thenReturn(false);
        when(memberRepository.existsByEmail(createRequest.email())).thenReturn(false);
        when(memberMapper.toEntity(createRequest)).thenReturn(member);
        when(memberMapper.toSummaryResponse(member)).thenReturn(summaryResponse);
        when(memberRepository.save(member)).thenReturn(member);

        //when
        MemberSummaryResponseDto result = memberService.create(createRequest);

        //then
        assertThat(result).isEqualTo(summaryResponse);
        verify(memberRepository).existsByLoginId(createRequest.loginId());
        verify(memberRepository).existsByEmail(createRequest.email());
        verify(memberMapper).toEntity(createRequest);
        verify(memberMapper).toSummaryResponse(member);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("회원 등록 시 로그인 아이디 중복일 경우 예외 발생")
    void createMember_duplicateLoginId_shouldFail() {
        //given
        when(memberRepository.existsByLoginId(createRequest.loginId())).thenReturn(true);

        //then
        DuplicateException duplicateException =
                assertThrows(DuplicateException.class, () -> memberService.create(createRequest));
        assertThat(duplicateException.getMessage())
                .isEqualTo(String.format("중복되는 %s은 사용할 수 없습니다. (이미 존재하는 값: %s)",
                        "loginId", createRequest.loginId()));
        verify(memberRepository).existsByLoginId(createRequest.loginId());
    }

    @Test
    @DisplayName("회원 등록 시 이메일 중복일 경우 예외 발생")
    void createMember_duplicateEmail_shouldFail() {
        //given
        when(memberRepository.existsByLoginId(createRequest.loginId())).thenReturn(false);
        when(memberRepository.existsByEmail(createRequest.email())).thenReturn(true);

        //then
        DuplicateException duplicateException =
                assertThrows(DuplicateException.class, () -> memberService.create(createRequest));
        assertThat(duplicateException.getMessage())
                .isEqualTo(String.format("중복되는 %s은 사용할 수 없습니다. (이미 존재하는 값: %s)",
                        "email", createRequest.email()));
        verify(memberRepository).existsByLoginId(createRequest.loginId());
        verify(memberRepository).existsByEmail(createRequest.email());
    }

    //== update ==//
    @Test
    void updateMember_success() {
        //given
        when(memberRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMapper.toDetailResponse(member)).thenReturn(detailResponse);

        //when
        MemberDetailResponseDto result = memberService.update(1L, updateRequest);

        //then
        assertThat(result).isEqualTo(detailResponse);
        verify(memberRepository).existsByEmail(updateRequest.email());
        verify(memberRepository).findById(1L);
        verify(memberMapper).toDetailResponse(member);
    }

    @Test
    @DisplayName("회원 수정 시 기존 회원의 이메일과 중복된 이메일로 수정하면 예외 발생")
    void updateMember_duplicateEmail_shouldFail() {
        //given
        when(memberRepository.existsByEmail(updateRequest.email())).thenReturn(true);

        //then
        DuplicateException duplicateException =
                assertThrows(DuplicateException.class, () -> memberService.update(1L, updateRequest));
        assertThat(duplicateException.getMessage())
                .isEqualTo(String.format("중복되는 %s은 사용할 수 없습니다. (이미 존재하는 값: %s)",
                        "email", updateRequest.email()));
        verify(memberRepository).existsByEmail(updateRequest.email());
    }

    @Test
    @DisplayName("회원 수정 시 회원id로 조회 불가능하면 예외 발생")
    void updateMember_notFindById_shouldFail() {
        //given
        when(memberRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        //then
        MemberNotFoundException memberNotFoundException =
                assertThrows(MemberNotFoundException.class, () -> memberService.update(1L, updateRequest));
        assertThat(memberNotFoundException.getMessage())
                .isEqualTo(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)",
                        DomainType.MEMBER.getDisPlayName(),"id", 1L));
        verify(memberRepository).existsByEmail(updateRequest.email());
        verify(memberRepository).findById(1L);
    }

    //== delete ==//
    @Test
    void deleteMember_success() {
        //given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        //when
        memberService.delete(1L);

        //then
        verify(memberRepository).findById(1L);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    void deleteMember_notFindById_shouldFail() {
        //given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        //then
        MemberNotFoundException memberNotFoundException =
                assertThrows(MemberNotFoundException.class, () -> memberService.delete(1L));
        assertThat(memberNotFoundException.getMessage())
                .isEqualTo(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)",
                        DomainType.MEMBER.getDisPlayName(),"id", 1L));
        verify(memberRepository).findById(1L);
    }

    //== read ==//
    @Test
    void getMembers() {
        //given
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(memberMapper.toSummaryResponse(member)).thenReturn(summaryResponse);

        //when
        List<MemberSummaryResponseDto> result = memberService.getMembers();

        //then
        assertThat(result).containsExactly(summaryResponse);
        verify(memberRepository).findAll();
        verify(memberMapper).toSummaryResponse(member);
    }

}