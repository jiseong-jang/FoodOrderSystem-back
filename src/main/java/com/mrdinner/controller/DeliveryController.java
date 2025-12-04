package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.OrderDto;
import com.mrdinner.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@PreAuthorize("hasRole('DELIVERY_STAFF')")
public class DeliveryController {
    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/orders/ready")
    public ResponseEntity<ApiResponse> getReadyOrders() {
        try {
            List<OrderDto> orders = deliveryService.getReadyOrders();
            return ResponseEntity.ok(ApiResponse.success("배달 준비된 주문 조회 성공", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/orders/my")
    public ResponseEntity<ApiResponse> getMyDeliveryOrders(Authentication authentication) {
        try {
            String deliveryStaffId = authentication.getName();
            List<OrderDto> orders = deliveryService.getMyDeliveryOrders(deliveryStaffId);
            return ResponseEntity.ok(ApiResponse.success("내 배달 중인 주문 조회 성공", orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/pickup")
    public ResponseEntity<ApiResponse> pickupOrder(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String deliveryStaffId = authentication.getName();
            OrderDto order = deliveryService.pickupOrder(deliveryStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("주문을 픽업했습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<ApiResponse> completeDelivery(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String deliveryStaffId = authentication.getName();
            OrderDto order = deliveryService.completeDelivery(deliveryStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("배달이 완료되었습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

