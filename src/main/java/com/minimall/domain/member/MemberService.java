package com.minimall.domain.member;

import com.minimall.domain.member.dto.MemberMapper;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.exception.DuplicateException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    //== 생성 ==//
    @Transactional
    public MemberSummaryResponseDto create(MemberCreateRequestDto request) {
        validateDuplicateLoginId(request.loginId());
        validateDuplicateEmail(request.email());
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = memberRepository.save(memberMapper.toEntity(request));
        return memberMapper.toSummaryResponse(member);
    }


    //== 수정 ==//
    @Transactional
    public MemberDetailResponseDto update(Long memberId, MemberUpdateRequestDto request) {
        validateDuplicateEmailForUpdate(memberId, request.email());
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = findMemberById(memberId);
        member.update(request.password(), request.name(), request.email(), request.addr());
        return memberMapper.toDetailResponse(member);
    }


    //== 삭제 ==//
    //TODO Soft delete 및 deleteException 고려
    @Transactional
    public void delete(Long memberId) {
        findMemberById(memberId);
        memberRepository.deleteById(memberId);
    }

    //== 단건 조회 ==//
    public MemberSummaryResponseDto getSummary(Long memberId) {
        return memberMapper.toSummaryResponse(findMemberById(memberId));
    }
    public MemberDetailResponseDto getDetail(Long memberId) {
        return memberMapper.toDetailResponse(findMemberById(memberId));
    }

    public MemberDetailWithOrdersResponseDto getDetailWithOrders(Long memberId) {
        return memberMapper.toDetailWithOrdersResponse(findMemberById(memberId));
    }

    public MemberSummaryResponseDto getSummaryByEmail(String email) {
        return memberMapper.toSummaryResponse(findMemberByEmail(email));
    }

    public MemberDetailResponseDto getDetailByEmail(String email) {
        return memberMapper.toDetailResponse(findMemberByEmail(email));
    }

    public MemberSummaryResponseDto getSummaryByLoginId(String loginId) {
        return memberMapper.toSummaryResponse(findMemberByLoginId(loginId));
    }


    public MemberDetailResponseDto getDetailByLoginId(String loginId) {
        return memberMapper.toDetailResponse(findMemberByLoginId(loginId));
    }


    //== 목록 조회==//
    public List<MemberSummaryResponseDto> getMembers() {
        //TODO searchDto를 인자로 받아 pageable 등 구현
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(memberMapper::toSummaryResponse)
                .toList();
    }

    //== 검증 로직 ==//
    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            DuplicateException.validateField("loginId", loginId);
        }
    }
    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            DuplicateException.validateField("email", email);
        }
    }

    private void validateDuplicateEmailForUpdate(Long memberId, String email) {
        memberRepository.findByEmail(email)
                .filter(m -> !m.getId().equals(memberId))
                .ifPresent(m -> DuplicateException.validateField("email", email));
    }


    //== 공통 조회 메서드 ==//
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("id", memberId));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("email", email));
    }

    private Member findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException("loginId", loginId));
    }
}