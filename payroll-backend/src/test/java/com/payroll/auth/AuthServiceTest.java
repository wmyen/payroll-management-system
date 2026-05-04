package com.payroll.auth;

import com.payroll.auth.domain.Role;
import com.payroll.auth.domain.User;
import com.payroll.auth.dto.LoginRequest;
import com.payroll.auth.dto.LoginResponse;
import com.payroll.auth.repository.UserRepository;
import com.payroll.auth.security.JwtUtil;
import com.payroll.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returns_tokens() {
        User user = User.builder()
                .username("admin")
                .password("encoded_password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateAccessToken("admin", "ADMIN")).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("refresh_token");

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
    }

    @Test
    void login_wrong_password_throws() {
        User user = User.builder()
                .username("admin")
                .password("encoded_password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("admin", "wrong")));
    }

    @Test
    void login_nonexistent_user_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("unknown", "password")));
    }
}
