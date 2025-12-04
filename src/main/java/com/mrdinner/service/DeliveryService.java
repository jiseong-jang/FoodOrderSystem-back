package com.mrdinner.service;

import com.mrdinner.dto.OrderDto;
import com.mrdinner.entity.*;
import com.mrdinner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryStaffRepository deliveryStaffRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    public List<OrderDto> getReadyOrders() {
        List<Order> orders = orderRepository.findByStatusAndDeliveryStaffIsNull(OrderStatus.DELIVERING);
        return orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getMyDeliveryOrders(String deliveryStaffId) {
        DeliveryStaff staff = deliveryStaffRepository.findById(deliveryStaffId)
                .orElseThrow(() -> new RuntimeException("배달 직원을 찾을 수 없습니다"));

        List<Order> orders = orderRepository.findByStatusAndDeliveryStaff(OrderStatus.DELIVERING, staff);
        return orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto pickupOrder(String deliveryStaffId, Long orderId) {
        DeliveryStaff staff = deliveryStaffRepository.findById(deliveryStaffId)
                .orElseThrow(() -> new RuntimeException("배달 직원을 찾을 수 없습니다"));

        if (staff.getIsBusy()) {
            throw new RuntimeException("이미 배달 중인 주문이 있습니다");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new RuntimeException("배달 준비된 주문만 픽업할 수 있습니다");
        }

        if (order.getDeliveryStaff() != null) {
            throw new RuntimeException("이미 다른 직원이 픽업한 주문입니다");
        }

        staff.setIsBusy(true);
        deliveryStaffRepository.save(staff);

        order.setDeliveryStaff(staff);
        order = orderRepository.save(order);

        return orderService.convertToDto(order);
    }

    @Transactional
    public OrderDto completeDelivery(String deliveryStaffId, Long orderId) {
        DeliveryStaff staff = deliveryStaffRepository.findById(deliveryStaffId)
                .orElseThrow(() -> new RuntimeException("배달 직원을 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getDeliveryStaff() == null || !order.getDeliveryStaff().getId().equals(deliveryStaffId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new RuntimeException("배달 중인 주문만 완료할 수 있습니다");
        }

        // 배달 직원 상태 해제
        staff.setIsBusy(false);
        deliveryStaffRepository.save(staff);

        // 주문 상태를 완료로 변경
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        // 단골 고객 체크 및 업데이트
        customerService.checkAndUpdateRegularCustomer(order.getCustomer().getId());

        return orderService.convertToDto(order);
    }
}

