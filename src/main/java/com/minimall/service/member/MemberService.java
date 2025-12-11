package com.minimall.service.member;

import com.minimall.api.member.dto.response.MemberDetailResponse;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.member.dto.MemberCreateCommand;
import com.minimall.service.member.dto.MemberLoginCommand;
import com.minimall.service.member.dto.MemberServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberServiceMapper memberServiceMapper;
    private final PasswordEncoder passwordEncoder;

    //== 로그인 ==//
    @Transactional
    public Member login(MemberLoginCommand command) {
        Member member = findMemberByLoginId(command.loginId());
        if (!passwordEncoder.matches(command.password(), member.getPassword())){
            throw new InvalidCredentialException("password not matched");
        }

        return member;
    }

    //== 생성 ==//
    @Transactional
    public Member create(MemberCreateCommand command) {
        validateDuplicateLoginId(command.loginId());
        validateDuplicateEmail(command.email());

        String encodedPassword = passwordEncoder.encode(command.password());
        MemberCreateCommand encodedCommand = command.withEncodedPassword(encodedPassword);

        Member member = memberServiceMapper.toEntity(encodedCommand);
        return memberRepository.save(member);
    }

    //== 수정 ==//
    @Transactional
    public MemberDetailResponse update(Long memberId, MemberUpdateRequest request) {
        validateDuplicateEmailForUpdate(memberId, request.email());
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = findMemberById(memberId);
        member.update(request.password(), request.name(), request.email(), request.addr());
        return memberServiceMapper.toDetailResponse(member);
    }


    //== 삭제 ==//
    //TODO Soft delete 및 deleteException 고려
    @Transactional
    public void delete(Long memberId) {
        findMemberById(memberId);
        memberRepository.deleteById(memberId);
    }

    //== 단건 조회 ==//
    public MemberSummaryResponse getSummary(Long memberId) {
        return memberServiceMapper.toSummaryResponse(findMemberById(memberId));
    }
    public MemberDetailResponse getDetail(Long memberId) {
        return memberServiceMapper.toDetailResponse(findMemberById(memberId));
    }

    public MemberDetailWithOrdersResponse getDetailWithOrders(Long memberId) {
        return memberServiceMapper.toDetailWithOrdersResponse(findMemberById(memberId));
    }

    public MemberSummaryResponse getSummaryByEmail(String email) {
        return memberServiceMapper.toSummaryResponse(findMemberByEmail(email));
    }

    public MemberDetailResponse getDetailByEmail(String email) {
        return memberServiceMapper.toDetailResponse(findMemberByEmail(email));
    }

    public MemberSummaryResponse getSummaryByLoginId(String loginId) {
        return memberServiceMapper.toSummaryResponse(findMemberByLoginId(loginId));
    }


    public MemberDetailResponse getDetailByLoginId(String loginId) {
        return memberServiceMapper.toDetailResponse(findMemberByLoginId(loginId));
    }


    //== 목록 조회==//
    public List<MemberSummaryResponse> getMembers() {
        //TODO searchDto를 인자로 받아 pageable 등 구현
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(memberServiceMapper::toSummaryResponse)
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