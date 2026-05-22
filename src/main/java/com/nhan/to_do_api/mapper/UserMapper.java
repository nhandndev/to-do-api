package com.nhan.to_do_api.mapper;

import com.nhan.to_do_api.dto.request.RegisterRequest;
import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.entity.User;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterRequest registerRequest);
    UserResponse toUserResponse(User user);
}
