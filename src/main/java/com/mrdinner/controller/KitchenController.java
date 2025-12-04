package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.InventoryDto;
import com.mrdinner.dto.OrderDto;
import com.mrdinner.service.KitchenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kitchen")
@PreAuthorize("hasRole('KITCHEN_STAFF')")
public class KitchenController {
    @Autowired
    private KitchenService kitchenService;

    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse> getPendingOrders() {
        try {
            List<OrderDto> orders = kitchenService.getPendingOrders();
            return ResponseEntity.ok(ApiResponse.success("대기 중인 주문 조회 성공", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/orders/reservations")
    public ResponseEntity<ApiResponse> getReservationOrders() {
        try {
            List<OrderDto> orders = kitchenService.getReservationOrders();
            return ResponseEntity.ok(ApiResponse.success("예약 주문 조회 성공", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/orders/my")
    public ResponseEntity<ApiResponse> getMyCookingOrders(Authentication authentication) {
        try {
            String kitchenStaffId = authentication.getName();
            List<OrderDto> orders = kitchenService.getMyCookingOrders(kitchenStaffId);
            return ResponseEntity.ok(ApiResponse.success("내 조리 중인 주문 조회 성공", orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/receive")
    public ResponseEntity<ApiResponse> receiveOrder(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String kitchenStaffId = authentication.getName();
            OrderDto order = kitchenService.receiveOrder(kitchenStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("주문을 수령했습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/reject")
    public ResponseEntity<ApiResponse> rejectOrder(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String kitchenStaffId = authentication.getName();
            OrderDto order = kitchenService.rejectOrder(kitchenStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("주문을 거절했습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/start")
    public ResponseEntity<ApiResponse> startCooking(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String kitchenStaffId = authentication.getName();
            OrderDto order = kitchenService.startCooking(kitchenStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("조리를 시작했습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<ApiResponse> completeCooking(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            String kitchenStaffId = authentication.getName();
            OrderDto order = kitchenService.completeCooking(kitchenStaffId, id);
            return ResponseEntity.ok(ApiResponse.success("조리가 완료되었습니다", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse> getInventory() {
        try {
            List<InventoryDto> inventory = kitchenService.getInventory();
            return ResponseEntity.ok(ApiResponse.success("재고 조회 성공", inventory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/inventory/{itemName}")
    public ResponseEntity<ApiResponse> updateStock(
            @PathVariable String itemName,
            @RequestParam Integer amount,
            @RequestParam String action) {
        try {
            InventoryDto inventory = kitchenService.updateStock(itemName, amount, action);
            return ResponseEntity.ok(ApiResponse.success("재고가 수정되었습니다", inventory));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/inventory/{itemName}/restock-request")
    public ResponseEntity<ApiResponse> requestRestock(@PathVariable String itemName) {
        try {
            // 발주 요청 로직 (현재는 단순히 메시지만 반환)
            return ResponseEntity.ok(ApiResponse.success("발주 요청이 전송되었습니다: " + itemName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}

