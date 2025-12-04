package com.mrdinner.controller;

import com.mrdinner.dto.*;
import com.mrdinner.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            String customerId = authentication.getName();
            OrderDto order = orderService.createOrder(customerId, request);
            return ResponseEntity.ok(ApiResponse.success("주문이 생성되었습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/coupon")
    public ResponseEntity<ApiResponse> applyCoupon(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) Long customerCouponId) {
        try {
            String customerId = authentication.getName();
            OrderDto order;
            if (customerCouponId != null) {
                // CustomerCoupon ID로 쿠폰 적용
                order = orderService.applyCouponByCustomerCouponId(customerId, id, customerCouponId);
            } else if (couponCode != null) {
                // 쿠폰 코드로 쿠폰 적용 (기존 방식 유지)
                order = orderService.applyCoupon(customerId, id, couponCode);
            } else {
                throw new RuntimeException("쿠폰 코드 또는 쿠폰 ID를 제공해야 합니다");
            }
            return ResponseEntity.ok(ApiResponse.success("쿠폰이 적용되었습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String customerId = authentication.getName();
            OrderDto order = orderService.cancelOrder(customerId, id);
            return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getOrderHistory(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            List<OrderDto> orders = orderService.getOrderHistory(customerId);
            return ResponseEntity.ok(ApiResponse.success("주문 내역 조회 성공", orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String customerId = authentication.getName();
            OrderDto order = orderService.getOrderById(customerId, id);
            return ResponseEntity.ok(ApiResponse.success("주문 조회 성공", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse> getCurrentOrder(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            OrderDto order = orderService.getCurrentOrder(customerId);
            return ResponseEntity.ok(ApiResponse.success("현재 주문 조회 성공", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse> getReservationOrders(Authentication authentication) {
        try {
            String customerId = authentication.getName();
            List<OrderDto> orders = orderService.getReservationOrders(customerId);
            return ResponseEntity.ok(ApiResponse.success("예약 주문 조회 성공", orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateOrder(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        try {
            String customerId = authentication.getName();
            OrderDto order = orderService.updateOrder(customerId, id, request);
            return ResponseEntity.ok(ApiResponse.success("주문이 수정되었습니다 (환불 후 재결제 완료)", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/coupons/{code}")
    public ResponseEntity<ApiResponse> getCouponByCode(
            Authentication authentication,
            @PathVariable String code) {
        try {
            CouponDto coupon = orderService.getCouponByCode(code);
            return ResponseEntity.ok(ApiResponse.success("쿠폰 조회 성공", coupon));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/modification-logs")
    public ResponseEntity<ApiResponse> getOrderModificationLogs(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String customerId = authentication.getName();
            List<OrderModificationLogDto> logs = orderService.getOrderModificationLogs(customerId, id);
            return ResponseEntity.ok(ApiResponse.success("주문 수정 로그 조회 성공", logs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

