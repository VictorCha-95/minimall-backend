package com.minimall.domain.member;

import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public List<MemberSummaryResponseDto> getAll() {
        return memberService.getMembers();
    }

    @GetMapping("/{id}")
    public MemberDetailResponseDto getDetail(@PathVariable Long id) {
        return memberService.getDetail(id);
    }

    @GetMapping("/{id}/summary")
    public MemberSummaryResponseDto getSummary(@PathVariable Long id) {
        return memberService.getSummary(id);
    }

    @GetMapping("/{id}/with-orders")
    public MemberDetailWithOrdersResponseDto getDetailWithOrders(@PathVariable Long id) {
        return memberService.getDetailWithOrders(id);
    }

    @GetMapping("/{email}")
    public MemberDetailResponseDto getDetailByEmail(@PathVariable String email) {
        return memberService.getDetailByEmail(email);
    }

    @GetMapping("/{email}/summary")
    public MemberSummaryResponseDto getSummaryByEmail(@PathVariable String email) {
        return memberService.getSummaryByEmail(email);
    }

    @GetMapping("/{loginId}")
    public MemberDetailResponseDto getDetailByLoginId(@PathVariable String loginId) {
        return memberService.getDetailByLoginId(loginId);
    }

    @GetMapping("/{loginId}/summary")
    public MemberSummaryResponseDto getSummaryByLoginId(@PathVariable String loginId) {
        return memberService.getSummaryByLoginId(loginId);
    }

    @PostMapping
    public MemberSummaryResponseDto create(MemberCreateRequestDto request) {
        return memberService.create(request);
    }

    @PatchMapping("/{id}")
    public MemberDetailResponseDto update(@PathVariable Long id, MemberUpdateRequestDto request) {
        return memberService.update(id, request);
    }

    

}
