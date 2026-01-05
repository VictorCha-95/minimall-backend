package com.minimall.api.auth.dto;

import com.minimall.service.auth.dto.LoginCommand;
import com.minimall.service.auth.dto.LoginResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthApiMapper {

    LoginResponse toLoginResponse(LoginResult result);

    LoginCommand toLoginCommand(LoginRequest request);
}
