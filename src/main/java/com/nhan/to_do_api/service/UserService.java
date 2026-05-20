package com.nhan.to_do_api.service;

import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.entity.User;
import com.nhan.to_do_api.exception.AppException;
import com.nhan.to_do_api.exception.ErrorCode;
import com.nhan.to_do_api.mapper.UserMapper;
import com.nhan.to_do_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    UserMapper userMapper;
    public UserResponse getMyInfo(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return  userMapper.toUserResponse(user);
    }
}
