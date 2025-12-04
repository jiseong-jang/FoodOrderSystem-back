package com.mrdinner.controller;

import com.mrdinner.dto.*;
import com.mrdinner.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse> getCart(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            CartDto cart = cartService.getCart(customerId);
            return ResponseEntity.ok(ApiResponse.success("장바구니 조회 성공", cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {
        try {
            String customerId = authentication.getName();
            CartItemDto item = cartService.addItem(customerId, request);
            return ResponseEntity.ok(ApiResponse.success("장바구니에 추가되었습니다", item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse> updateItem(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            String customerId = authentication.getName();
            CartItemDto item = cartService.updateItem(customerId, id, quantity);
            return ResponseEntity.ok(ApiResponse.success("장바구니 아이템이 수정되었습니다", item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse> removeItem(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String customerId = authentication.getName();
            cartService.removeItem(customerId, id);
            return ResponseEntity.ok(ApiResponse.success("장바구니에서 삭제되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            cartService.clearCart(customerId);
            return ResponseEntity.ok(ApiResponse.success("장바구니가 비워졌습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

