package com.minimall.api.domain.member;

import com.minimall.api.domain.member.dto.MemberMapper;
import com.minimall.api.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.api.domain.member.dto.request.MemberUpdatePasswordRequestDto;
import com.minimall.api.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.api.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.api.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.api.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.api.exception.DuplicateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    //== 생성 ==//
    @Transactional
    public MemberSummaryResponseDto createMember(MemberCreateRequestDto request) {
        validateDuplicateLoginId(request.loginId());
        validateDuplicateLoginId(request.email());
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = memberRepository.save(memberMapper.toEntity(request));
        return memberMapper.toSummaryResponse(member);
    }


    //== 수정 ==//
    @Transactional
    public MemberDetailResponseDto updateMember(Long memberId, MemberUpdateRequestDto request) {
        validateDuplicateLoginId(request.email());
        Member member = findMemberById(memberId);
        member.update(request.name(), request.email(), request.addr());
        return memberMapper.toDetailResponse(member);
    }

    @Transactional
    public MemberDetailResponseDto updateMemberPassword(Long memberId, MemberUpdatePasswordRequestDto request) {
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = findMemberById(memberId);
        member.changePassword(request.newPassword());
        return memberMapper.toDetailResponse(member);
    }


    //== 삭제 ==//
    //TODO Soft delete 및 deleteException 고려
    @Transactional
    public void deleteMember(Long memberId) {
        findMemberById(memberId);
        memberRepository.deleteById(memberId);
    }


    //== 단건 조회 ==//
    public MemberSummaryResponseDto getMemberSummary(Long memberId) {
        return memberMapper.toSummaryResponse(findMemberById(memberId));
    }

    public MemberDetailResponseDto getMemberDetail(Long memberId) {
        return memberMapper.toDetailResponse(findMemberById(memberId));
    }

    public MemberDetailWithOrdersResponseDto getMemberDetailWithOrders(Long memberId) {
        return memberMapper.toDetailWithOrdersResponse(findMemberById(memberId));
    }

    public MemberSummaryResponseDto getMemberSummaryByEmail(String email) {
        return memberMapper.toSummaryResponse(findMemberByEmail(email));
    }

    public MemberDetailResponseDto getMemberDetailByEmail(String email) {
        return memberMapper.toDetailResponse(findMemberByEmail(email));
    }

    public MemberSummaryResponseDto getMemberSummaryByLoginId(String loginId) {
        return memberMapper.toSummaryResponse(findMemberByLoginId(loginId));
    }


    public MemberDetailResponseDto getMemberDetailByLoginId(String loginId) {
        return memberMapper.toDetailResponse(findMemberByLoginId(loginId));
    }


    //== 목록 조회==//
    //TODO searchDto를 인자로 받아 pageable 등 구현
    public List<MemberSummaryResponseDto> getMembers() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toSummaryResponse)
                .toList();
    }


    //== 검증 로직 ==//
    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw DuplicateException.ofField("loginId", loginId);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw DuplicateException.ofField("email", email);
        }
    }


    //== 공통 조회 메서드 ==//
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("id" + memberId));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("email" + email));
    }

    private Member findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException("loginId" + loginId));
    }
}