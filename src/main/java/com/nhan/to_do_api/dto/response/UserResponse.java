package com.nhan.to_do_api.dto.response;

import com.nhan.to_do_api.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    Long id;
    String username;
    String email;
    Role role;
    Boolean enabled;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
