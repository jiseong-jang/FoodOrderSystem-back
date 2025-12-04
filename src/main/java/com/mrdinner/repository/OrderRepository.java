package com.mrdinner.repository;

import com.mrdinner.entity.Customer;
import com.mrdinner.entity.DeliveryStaff;
import com.mrdinner.entity.KitchenStaff;
import com.mrdinner.entity.Order;
import com.mrdinner.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(Customer customer);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusAndKitchenStaffIsNull(OrderStatus status);
    List<Order> findByStatusAndDeliveryStaffIsNull(OrderStatus status);
    List<Order> findByStatusAndKitchenStaff(OrderStatus status, KitchenStaff staff);
    List<Order> findByStatusAndDeliveryStaff(OrderStatus status, DeliveryStaff staff);
    Optional<Order> findByOrderIdAndCustomer(Long orderId, Customer customer);
    
    // orderItems를 함께 로드하는 메서드
    @EntityGraph(attributePaths = {"orderItems", "orderItems.menu"})
    Optional<Order> findById(Long id);
    
    // 예약 주문 조회: 예약 시간이 있고, 상태가 RECEIVED이고, 주방 직원이 할당되지 않은 모든 예약 주문
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.kitchenStaff IS NULL AND o.reservationTime IS NOT NULL")
    List<Order> findReservationOrders(@Param("status") OrderStatus status);
    
    // 일반 주문 조회: 예약 시간이 없는 주문
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.kitchenStaff IS NULL AND o.reservationTime IS NULL")
    List<Order> findImmediateOrders(@Param("status") OrderStatus status);
    
    // 고객의 예약 주문 조회
    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.reservationTime IS NOT NULL ORDER BY o.createdAt DESC")
    List<Order> findByCustomerAndReservationTimeIsNotNullOrderByCreatedAtDesc(@Param("customer") Customer customer);
}

