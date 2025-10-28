package com.minimall.domain.member.dto;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import com.minimall.domain.order.dto.OrderMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import java.util.List;

@Mapper(componentModel = "spring", uses = OrderMapper.class)
public interface MemberMapper {

    //== Create ==//
    Member toEntity(MemberCreateRequestDto request);

    //== Update ==//
    @Mapping(target = "orders", ignore = true)
    void updateFromRequest(@MappingTarget Member member, MemberUpdateRequestDto request);

    //== Response ==//
    MemberDetailResponseDto toDetailResponse(Member member);

    @Mapping(target = "orders", source = "orders")
    MemberDetailWithOrdersResponseDto toDetailWithOrdersResponse(Member member);

    MemberSummaryResponseDto toSummaryResponse(Member member);

    //== List 변환 ==//
    List<MemberSummaryResponseDto> toListResponseList(List<Member> members);



}
