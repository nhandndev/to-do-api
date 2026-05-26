package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.entity.RefreshToken;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken ;

}
