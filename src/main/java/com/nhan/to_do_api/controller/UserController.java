package com.nhan.to_do_api.controller;

import com.nhan.to_do_api.dto.response.ApiResponse;
import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(){
       return ApiResponse.<UserResponse>builder()
               .code(1000)
               .message("getMyInfo successfully!")
               .result(userService.getMyInfo())
               .build();
    }
}
