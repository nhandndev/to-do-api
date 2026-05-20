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
    @Size(min = 1, max = 50)
    String username;
    @Size(min = 6 )
    @NotBlank
    @Email
    String email;
    @NotBlank
    String password;
}
