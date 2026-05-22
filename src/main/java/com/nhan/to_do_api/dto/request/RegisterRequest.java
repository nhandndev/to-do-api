package com.nhan.to_do_api.dto.request;

import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank
    @Size(min = 4, max = 50)
    String username;
    @Size(min = 6 )
    @NotBlank
    @Email
    String email;
    @NotBlank
    @Size(min = 6 , message = "Password must be at least 6 characters")
    String password;
}
