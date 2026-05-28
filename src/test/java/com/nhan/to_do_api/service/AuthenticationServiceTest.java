package com.nhan.to_do_api.service;

import com.nhan.to_do_api.dto.request.AuthenticationRequest;
import com.nhan.to_do_api.dto.request.RegisterRequest;
import com.nhan.to_do_api.dto.response.AuthenticationResponse;
import com.nhan.to_do_api.dto.response.UserResponse;
import com.nhan.to_do_api.entity.RefreshToken;
import com.nhan.to_do_api.entity.User;
import com.nhan.to_do_api.exception.AppException;
import com.nhan.to_do_api.exception.ErrorCode;
import com.nhan.to_do_api.mapper.UserMapper;
import com.nhan.to_do_api.repository.InvalidTokenRepository;
import com.nhan.to_do_api.repository.RefreshTokenRepository;
import com.nhan.to_do_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvalidTokenRepository invalidTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        // Set up the SIGN_KEY which is read from @Value in the service
        ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "bXlTZWNyZXRLZXlUaGF0SXNMZWFzdDMyQnl0ZXNMb25n");
    }

    @Test
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        UserResponse response = UserResponse.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(any(RegisterRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(response);

        // When
        UserResponse result = authenticationService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

        // When
        AuthenticationResponse result = authenticationService.login(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
    }

    @Test
    void login_InvalidUsername_ThrowsException() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("wronguser");
        request.setPassword("password");

        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.INVALID_USERNAME_OR_PASSWORD, exception.getErrorCode());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.INVALID_USERNAME_OR_PASSWORD, exception.getErrorCode());
    }

    @Test
    void login_UserDisabled_ThrowsException() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(false); // Disabled user

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.USER_DISABLED, exception.getErrorCode());
    }
}
