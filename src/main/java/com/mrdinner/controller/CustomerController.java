package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.CustomerCouponDto;
import com.mrdinner.dto.CustomerProfileDto;
import com.mrdinner.dto.UpdateProfileRequest;
import com.mrdinner.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            CustomerProfileDto profile = customerService.getProfile(customerId);
            return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            String customerId = authentication.getName();
            CustomerProfileDto profile = customerService.updateProfile(customerId, request);
            return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse> deleteAccount(
            Authentication authentication,
            @RequestParam String password) {
        try {
            String customerId = authentication.getName();
            customerService.deleteAccount(customerId, password);
            return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse> getCustomerCoupons(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            // 쿠폰함 조회 전에 단골 고객 체크 수행
            customerService.checkAndUpdateRegularCustomer(customerId);
            List<CustomerCouponDto> coupons = customerService.getCustomerCoupons(customerId);
            return ResponseEntity.ok(ApiResponse.success("쿠폰함 조회 성공", coupons));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/check-regular")
    public ResponseEntity<ApiResponse> forceCheckRegularCustomer(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            customerService.forceCheckRegularCustomer(customerId);
            return ResponseEntity.ok(ApiResponse.success("단골 고객 체크 완료"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

