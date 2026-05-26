package com.nhan.to_do_api.controller;

import com.nhan.to_do_api.dto.request.AuthenticationRequest;
import com.nhan.to_do_api.dto.request.LogoutRequest;
import com.nhan.to_do_api.dto.request.RefreshTokenRequest;
import com.nhan.to_do_api.dto.request.RegisterRequest;
import com.nhan.to_do_api.dto.response.ApiResponse;
import com.nhan.to_do_api.dto.response.AuthenticationResponse;
import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.entity.RefreshToken;
import com.nhan.to_do_api.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationService authenticationService;
    @PostMapping("/register")
    public ApiResponse<UserResponse> register (@RequestBody  @Valid RegisterRequest registerRequest) {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Register successfully!")
                .result(authenticationService.register(registerRequest))
                .build();
    }
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login (@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Login successfully!")
                .result(authenticationService.login(authenticationRequest))
                .build();
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request){
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout succcessful")
                .result(null)
                .build();
    }
    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return  ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("refresh-token successfull")
                .result(authenticationService.refreshToken(refreshTokenRequest))
                .build();
    }

}
