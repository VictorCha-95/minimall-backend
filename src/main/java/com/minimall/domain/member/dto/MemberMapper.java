package com.minimall.domain.member.dto;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    //== Create ==//
    Member toEntity(MemberCreateRequestDto request);

    //== Update ==//
    void updateMemberFromRequest(@MappingTarget Member member, MemberUpdateRequestDto request);

    //== Response ==//
    MemberDetailResponseDto toDetailResponse(Member member);
    MemberDetailWithOrdersResponseDto toDetailWithOrdersResponse(Member member);
    MemberSummaryResponseDto toSummaryResponse(Member member);

    //== List 변환 ==//
    List<MemberSummaryResponseDto> toListResponseList(List<Member> members);

}
