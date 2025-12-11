package com.minimall.api.member.dto;

import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.domain.member.Member;
import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.response.MemberDetailResponse;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.api.order.dto.OrderMapper;
import com.minimall.service.member.dto.MemberCreateCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MemberApiMapper {

    // ===== Request -> Command (API → Service) ===== //

    //== Create ==//
    @Mapping(target = "withEncodedPassword", ignore = true)
    MemberCreateCommand toCreateCommand(MemberCreateRequest request);

    //== Update ==//
    @Mapping(target = "orders", ignore = true)
    void updateFromRequest(@MappingTarget Member member, MemberUpdateRequest request);

    //== Response ==//
    MemberDetailResponse toDetailResponse(Member member);

    @Mapping(target = "orders", source = "orders")
    MemberDetailWithOrdersResponse toDetailWithOrdersResponse(Member member);

    MemberSummaryResponse toSummaryResponse(Member member);

    //== List 변환 ==//
    List<MemberSummaryResponse> toListResponseList(List<Member> members);

}
