package com.mrdinner.service;

import com.mrdinner.dto.InventoryDto;
import com.mrdinner.dto.OrderDto;
import com.mrdinner.entity.*;
import com.mrdinner.menu.MenuComposition;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.repository.InventoryRepository;
import com.mrdinner.repository.KitchenStaffRepository;
import com.mrdinner.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KitchenService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KitchenStaffRepository kitchenStaffRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderService orderService;

    public List<OrderDto> getPendingOrders() {
        List<Order> orders = orderRepository.findImmediateOrders(OrderStatus.RECEIVED);
        return orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getReservationOrders() {
        List<Order> orders = orderRepository.findReservationOrders(OrderStatus.RECEIVED);
        return orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getMyCookingOrders(String kitchenStaffId) {
        KitchenStaff staff = kitchenStaffRepository.findById(kitchenStaffId)
                .orElseThrow(() -> new RuntimeException("주방 직원을 찾을 수 없습니다"));

        List<Order> orders = orderRepository.findByStatusAndKitchenStaff(OrderStatus.COOKING, staff);
        return orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto receiveOrder(String kitchenStaffId, Long orderId) {
        KitchenStaff staff = kitchenStaffRepository.findById(kitchenStaffId)
                .orElseThrow(() -> new RuntimeException("주방 직원을 찾을 수 없습니다"));

        if (staff.getIsBusy()) {
            throw new RuntimeException("이미 조리 중인 주문이 있습니다");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new RuntimeException("접수 대기 중인 주문만 수령할 수 있습니다");
        }

        if (order.getKitchenStaff() != null) {
            throw new RuntimeException("이미 다른 직원이 수령한 주문입니다");
        }

        // 예약 주문인 경우 예약 시간 1시간 전 검증
        if (order.getReservationTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationTime = order.getReservationTime();
            LocalDateTime oneHourBefore = reservationTime.minusHours(1);
            
            if (now.isBefore(oneHourBefore)) {
                throw new RuntimeException("예약 시간 1시간 전부터 조리 가능합니다");
            }
        }

        // 재고 확인 및 차감 (orderItems는 @EntityGraph로 이미 로드됨)
        checkAndDeductInventory(order);

        staff.setIsBusy(true);
        kitchenStaffRepository.save(staff);

        order.setKitchenStaff(staff);
        order.setStatus(OrderStatus.COOKING);
        order = orderRepository.save(order);

        return orderService.convertToDto(order);
    }

    @Transactional
    public OrderDto rejectOrder(String kitchenStaffId, Long orderId) {
        // 주방 직원 존재 확인
        kitchenStaffRepository.findById(kitchenStaffId)
                .orElseThrow(() -> new RuntimeException("주방 직원을 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new RuntimeException("접수 대기 중인 주문만 거절할 수 있습니다");
        }

        // 이미 다른 이유로 취소/거절된 주문 방지
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED) {
            throw new RuntimeException("이미 취소되었거나 거절된 주문입니다");
        }

        // 재고는 아직 차감되지 않은 상태(RECEIVED)이므로 변경 없이 상태만 업데이트
        order.setStatus(OrderStatus.REJECTED);
        order = orderRepository.save(order);

        return orderService.convertToDto(order);
    }

    private void checkAndDeductInventory(Order order) {
        // 주문 아이템이 없으면 재고 차감 불필요
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new RuntimeException("주문에 아이템이 없습니다");
        }

        // 주문의 각 아이템에 대해 필요한 재료 수량 계산
        Map<String, Integer> requiredItems = new HashMap<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            MenuType menuType = orderItem.getMenu().getType();
            Map<MenuItemCode, Integer> baseComposition = MenuComposition.getComposition(menuType);
            Map<String, Integer> customized = orderItem.getCustomizedQuantities();
            int itemQuantity = orderItem.getQuantity();

            // 기본 구성 + 커스터마이징 수량 계산
            for (Map.Entry<MenuItemCode, Integer> entry : baseComposition.entrySet()) {
                MenuItemCode itemCode = entry.getKey();
                int baseQty = entry.getValue();
                int customQty = customized != null ? customized.getOrDefault(itemCode.name(), baseQty) : baseQty;
                int totalQty = customQty * itemQuantity;
                requiredItems.merge(itemCode.name(), totalQty, Integer::sum);
            }
        }

        // 재고 확인 및 차감
        for (Map.Entry<String, Integer> entry : requiredItems.entrySet()) {
            String itemCode = entry.getKey();
            int requiredQty = entry.getValue();

            Inventory inventory = inventoryRepository.findByItemName(itemCode)
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setItemName(itemCode);
                        newInv.setQuantity(0);
                        return inventoryRepository.save(newInv);
                    });

            if (inventory.getQuantity() < requiredQty) {
                MenuItemCode code = MenuItemCode.fromValue(itemCode);
                String itemName = code != null ? code.getLabelKo() : itemCode;
                throw new RuntimeException(
                    String.format("재고가 부족합니다. %s: 필요 %d개, 현재 재고 %d개", 
                        itemName, requiredQty, inventory.getQuantity())
                );
            }

            // 재고 차감
            inventory.setQuantity(inventory.getQuantity() - requiredQty);
            inventoryRepository.save(inventory);
        }
    }

    @Transactional
    public OrderDto startCooking(String kitchenStaffId, Long orderId) {
        KitchenStaff staff = kitchenStaffRepository.findById(kitchenStaffId)
                .orElseThrow(() -> new RuntimeException("주방 직원을 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getKitchenStaff() == null || !order.getKitchenStaff().getId().equals(kitchenStaffId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        if (order.getStatus() != OrderStatus.COOKING) {
            throw new RuntimeException("조리 중인 주문만 시작할 수 있습니다");
        }

        if (!Boolean.TRUE.equals(staff.getIsBusy())) {
            staff.setIsBusy(true);
            kitchenStaffRepository.save(staff);
        }

        return orderService.convertToDto(order);
    }

    @Transactional
    public OrderDto completeCooking(String kitchenStaffId, Long orderId) {
        KitchenStaff staff = kitchenStaffRepository.findById(kitchenStaffId)
                .orElseThrow(() -> new RuntimeException("주방 직원을 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getKitchenStaff() == null || !order.getKitchenStaff().getId().equals(kitchenStaffId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        if (order.getStatus() != OrderStatus.COOKING) {
            throw new RuntimeException("조리 중인 주문만 완료할 수 있습니다");
        }

        // 주방 직원 상태 해제
        staff.setIsBusy(false);
        kitchenStaffRepository.save(staff);

        // 조리 완료 후 주문을 '배달 준비(DELIVERING)' 상태로만 변경
        // 실제 배달 직원 할당은 배달 대시보드에서 픽업 시점에 수행
        order.setStatus(OrderStatus.DELIVERING);
        order = orderRepository.save(order);

        return orderService.convertToDto(order);
    }

    public List<InventoryDto> getInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDto updateStock(String itemName, Integer amount, String action) {
        Inventory inventory = inventoryRepository.findByItemName(itemName)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setItemName(itemName);
                    newInventory.setQuantity(0);
                    return inventoryRepository.save(newInventory);
                });

        if ("add".equals(action)) {
            inventory.setQuantity(inventory.getQuantity() + amount);
        } else if ("reduce".equals(action)) {
            inventory.setQuantity(Math.max(0, inventory.getQuantity() - amount));
        } else {
            throw new RuntimeException("잘못된 액션입니다. 'add' 또는 'reduce'만 가능합니다");
        }

        // 수량이 변경될 때마다 마지막 보충일을 현재 시각으로 갱신
        inventory.setLastRestocked(LocalDateTime.now());

        inventory = inventoryRepository.save(inventory);
        return convertToDto(inventory);
    }

    private InventoryDto convertToDto(Inventory inventory) {
        MenuItemCode code = MenuItemCode.fromValue(inventory.getItemName());
        String label = code != null ? code.getLabelKo() : inventory.getItemName();
        String codeValue = code != null ? code.name() : inventory.getItemName();
        return new InventoryDto(
                codeValue,
                label,
                inventory.getQuantity(),
                inventory.getLastRestocked()
        );
    }
}

