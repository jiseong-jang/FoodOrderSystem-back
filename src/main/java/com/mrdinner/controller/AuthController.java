package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.LoginRequest;
import com.mrdinner.dto.LoginResponse;
import com.mrdinner.dto.RegisterRequest;
import com.mrdinner.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(Authentication authentication) {
        try {
            String userId = authentication.getName();
            authService.logout(userId);
            return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("회원가입 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse> checkId(@RequestParam String id) {
        boolean exists = authService.checkIdDuplicate(id);
        if (exists) {
            return ResponseEntity.ok(ApiResponse.error("이미 존재하는 아이디입니다"));
        }
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다"));
    }

    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse> validatePassword(
            @RequestParam String password,
            @RequestParam String confirmPassword) {
        boolean isValid = authService.validatePassword(password, confirmPassword);
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 일치합니다"));
        }
        return ResponseEntity.ok(ApiResponse.error("비밀번호가 일치하지 않습니다"));
    }
}

