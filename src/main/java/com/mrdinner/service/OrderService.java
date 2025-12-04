package com.mrdinner.service;

import com.mrdinner.dto.*;
import com.mrdinner.entity.*;
import com.mrdinner.repository.CartItemRepository;
import com.mrdinner.repository.CartRepository;
import com.mrdinner.repository.CouponRepository;
import com.mrdinner.repository.CustomerRepository;
import com.mrdinner.repository.MenuRepository;
import com.mrdinner.repository.OrderRepository;
import com.mrdinner.repository.OrderModificationLogRepository;
import com.mrdinner.repository.CustomerCouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.menu.MenuComposition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PriceCalculationService priceCalculationService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderModificationLogRepository orderModificationLogRepository;

    @Autowired
    private CustomerCouponRepository customerCouponRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public OrderDto createOrder(String customerId, CreateOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("장바구니가 비어있습니다"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("장바구니가 비어있습니다");
        }

        // 주문 생성
        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryType(request.getDeliveryType());
        order.setReservationTime(request.getReservationTime());
        order.setStatus(OrderStatus.RECEIVED);

        // 장바구니 아이템을 주문 아이템으로 변환
        int totalPrice = 0;
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenu(cartItem.getSelectedMenu());
            orderItem.setStyleType(cartItem.getSelectedStyle());
            orderItem.setCustomizedQuantities(cartItem.getCustomizedQuantities());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubTotal(cartItem.getSubTotal());
            order.getOrderItems().add(orderItem);
            totalPrice += cartItem.getSubTotal();
        }

        order.setFinalPrice(totalPrice);
        order = orderRepository.save(order);

        // 장바구니 비우기 - DB에서 완전히 삭제
        // Cart의 items 리스트를 먼저 가져온 후 삭제
        List<CartItem> itemsToDelete = new ArrayList<>(cart.getItems());
        cart.getItems().clear(); // Cart에서 연결 제거
        cartItemRepository.deleteAll(itemsToDelete); // DB에서 삭제
        cartRepository.save(cart); // 변경사항 반영

        // 주방 직원 자동 할당은 사용하지 않고,
        // 주방 대시보드에서 '주문 수령'을 통해 직원이 직접 할당하도록 한다.

        return convertToDto(order);
    }

    @Transactional
    public OrderDto applyCoupon(String customerId, Long orderId, String couponCode) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.RECEIVED && order.getStatus() != OrderStatus.COOKING) {
            throw new RuntimeException("쿠폰은 주문 접수 또는 조리 중일 때만 적용 가능합니다");
        }

        Coupon coupon = couponRepository.findByCodeAndIsValidTrue(couponCode)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 쿠폰입니다"));

        if (order.getCoupon() != null) {
            throw new RuntimeException("이미 쿠폰이 적용된 주문입니다");
        }

        // CustomerCoupon 찾아서 사용 처리
        CustomerCoupon customerCoupon = customerCouponRepository
                .findByCustomerAndCouponAndIsUsedFalse(customer, coupon)
                .orElse(null);

        if (customerCoupon != null) {
            customerCoupon.setIsUsed(true);
            customerCoupon.setUsedAt(LocalDateTime.now());
            customerCouponRepository.save(customerCoupon);
        }

        order.setCoupon(coupon);
        int discountedPrice = order.getFinalPrice() - coupon.getDiscountAmount();
        order.setFinalPrice(Math.max(0, discountedPrice)); // 음수 방지
        order = orderRepository.save(order);

        return convertToDto(order);
    }

    @Transactional
    public OrderDto applyCouponByCustomerCouponId(String customerId, Long orderId, Long customerCouponId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.RECEIVED && order.getStatus() != OrderStatus.COOKING) {
            throw new RuntimeException("쿠폰은 주문 접수 또는 조리 중일 때만 적용 가능합니다");
        }

        CustomerCoupon customerCoupon = customerCouponRepository.findById(customerCouponId)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다"));

        // 고객 소유 쿠폰인지 확인
        if (!customerCoupon.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("본인의 쿠폰만 사용할 수 있습니다");
        }

        // 이미 사용된 쿠폰인지 확인
        if (Boolean.TRUE.equals(customerCoupon.getIsUsed())) {
            throw new RuntimeException("이미 사용된 쿠폰입니다");
        }

        // 쿠폰이 유효한지 확인
        if (!Boolean.TRUE.equals(customerCoupon.getCoupon().getIsValid())) {
            throw new RuntimeException("유효하지 않은 쿠폰입니다");
        }

        if (order.getCoupon() != null) {
            throw new RuntimeException("이미 쿠폰이 적용된 주문입니다");
        }

        Coupon coupon = customerCoupon.getCoupon();

        // CustomerCoupon 사용 처리
        customerCoupon.setIsUsed(true);
        customerCoupon.setUsedAt(LocalDateTime.now());
        customerCouponRepository.save(customerCoupon);

        order.setCoupon(coupon);
        int discountedPrice = order.getFinalPrice() - coupon.getDiscountAmount();
        order.setFinalPrice(Math.max(0, discountedPrice)); // 음수 방지
        order = orderRepository.save(order);

        return convertToDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(String customerId, Long orderId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new RuntimeException("조리 시작 전(접수 완료 상태) 주문만 취소할 수 있습니다");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return convertToDto(order);
    }

    @Transactional
    public OrderDto updateOrder(String customerId, Long orderId, UpdateOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        // 주문 상태가 RECEIVED인지 확인
        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new RuntimeException("주문 수정은 접수 완료 상태에서만 가능합니다");
        }

        // 주문이 이미 주방 직원에게 할당되었는지 확인
        if (order.getKitchenStaff() != null) {
            throw new RuntimeException("이미 주방에서 수령한 주문은 수정할 수 없습니다");
        }

        // 수정 전 데이터 저장 (로그용)
        String previousOrderItemsJson = convertOrderItemsToJson(order.getOrderItems());
        int previousPrice = order.getFinalPrice();

        // 기존 주문 아이템 삭제
        order.getOrderItems().clear();

        // 새로운 주문 아이템 생성 및 가격 계산
        int totalPrice = 0;
        for (UpdateOrderItemRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));

            // 스타일 검증
            priceCalculationService.validateStyleForMenu(menu.getType(), itemRequest.getStyleType());

            // customizedQuantities 정규화
            Map<String, Integer> normalizedQuantities = normalizeQuantities(itemRequest.getCustomizedQuantities(), menu.getType());

            // 가격 계산
            Integer subTotal = priceCalculationService.calculateSubTotal(
                    itemRequest.getMenuId(),
                    itemRequest.getStyleType(),
                    normalizedQuantities,
                    itemRequest.getQuantity()
            );

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenu(menu);
            orderItem.setStyleType(itemRequest.getStyleType());
            orderItem.setCustomizedQuantities(normalizedQuantities);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubTotal(subTotal);

            order.getOrderItems().add(orderItem);
            totalPrice += subTotal;
        }

        // 쿠폰이 적용되어 있었다면 할인 금액 재계산
        if (order.getCoupon() != null) {
            int discountedPrice = totalPrice - order.getCoupon().getDiscountAmount();
            order.setFinalPrice(Math.max(0, discountedPrice));
        } else {
            order.setFinalPrice(totalPrice);
        }

        order = orderRepository.save(order);

        // 수정 후 데이터 저장 (로그용)
        String newOrderItemsJson = convertOrderItemsToJson(order.getOrderItems());
        int priceDifference = order.getFinalPrice() - previousPrice;

        // 주문 수정 로그 저장
        OrderModificationLog log = new OrderModificationLog();
        log.setOrder(order);
        log.setPreviousOrderItems(previousOrderItemsJson);
        log.setNewOrderItems(newOrderItemsJson);
        log.setPriceDifference(priceDifference);
        orderModificationLogRepository.save(log);

        return convertToDto(order);
    }

    private String convertOrderItemsToJson(List<OrderItem> orderItems) {
        try {
            List<Map<String, Object>> items = orderItems.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("menuId", item.getMenu().getId());
                itemMap.put("menuType", item.getMenu().getType().name());
                itemMap.put("styleType", item.getStyleType().name());
                itemMap.put("customizedQuantities", item.getCustomizedQuantities());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("subTotal", item.getSubTotal());
                return itemMap;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }

    private Map<String, Integer> normalizeQuantities(Map<String, Integer> original, com.mrdinner.entity.MenuType menuType) {
        Map<String, Integer> normalized = new HashMap<>();
        if (original == null) {
            // 기본 구성으로 설정
            Map<MenuItemCode, Integer> baseComposition = MenuComposition.getComposition(menuType);
            for (Map.Entry<MenuItemCode, Integer> entry : baseComposition.entrySet()) {
                normalized.put(entry.getKey().name(), entry.getValue());
            }
            return normalized;
        }

        original.forEach((key, value) -> {
            MenuItemCode code = MenuItemCode.fromValue(key);
            if (code != null) {
                normalized.put(code.name(), value);
            }
        });

        // 기본 구성에 있는 항목이 누락되었으면 기본값으로 추가
        Map<MenuItemCode, Integer> baseComposition = MenuComposition.getComposition(menuType);
        for (Map.Entry<MenuItemCode, Integer> entry : baseComposition.entrySet()) {
            normalized.putIfAbsent(entry.getKey().name(), entry.getValue());
        }

        return normalized;
    }

    public List<OrderDto> getOrderHistory(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getReservationOrders(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        return orderRepository.findByCustomerAndReservationTimeIsNotNullOrderByCreatedAtDesc(customer).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(String customerId, Long orderId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        return convertToDto(order);
    }

    public OrderDto getCurrentOrder(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        Order currentOrder = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.COMPLETED
                        && o.getStatus() != OrderStatus.CANCELLED
                        && o.getStatus() != OrderStatus.REJECTED)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("진행 중인 주문이 없습니다"));

        return convertToDto(currentOrder);
    }

    public CouponDto getCouponByCode(String couponCode) {
        Coupon coupon = couponRepository.findByCodeAndIsValidTrue(couponCode)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 쿠폰입니다"));
        
        return new CouponDto(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountAmount(),
                coupon.getIsValid()
        );
    }

    public List<OrderModificationLogDto> getOrderModificationLogs(String customerId, Long orderId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        return orderModificationLogRepository.findByOrderOrderByModifiedAtDesc(order).stream()
                .map(this::convertToModificationLogDto)
                .collect(Collectors.toList());
    }

    private OrderModificationLogDto convertToModificationLogDto(OrderModificationLog log) {
        return new OrderModificationLogDto(
                log.getId(),
                log.getOrder().getOrderId(),
                log.getModifiedAt(),
                log.getPreviousOrderItems(),
                log.getNewOrderItems(),
                log.getPriceDifference()
        );
    }

    public OrderDto convertToDto(Order order) {
        List<OrderItemDto> orderItems = order.getOrderItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CouponDto couponDto = null;
        if (order.getCoupon() != null) {
            couponDto = new CouponDto(
                    order.getCoupon().getId(),
                    order.getCoupon().getCode(),
                    order.getCoupon().getDiscountAmount(),
                    order.getCoupon().getIsValid()
            );
        }

        return new OrderDto(
                order.getOrderId(),
                order.getStatus(),
                order.getDeliveryType(),
                order.getReservationTime(),
                order.getFinalPrice(),
                orderItems,
                order.getCreatedAt(),
                order.getKitchenStaff() != null ? order.getKitchenStaff().getEmployeeId() : null,
                order.getDeliveryStaff() != null ? order.getDeliveryStaff().getEmployeeId() : null,
                couponDto
        );
    }

    private OrderItemDto convertToDto(OrderItem orderItem) {
        MenuDto menuDto = new MenuDto();
        menuDto.setId(orderItem.getMenu().getId());
        menuDto.setType(orderItem.getMenu().getType());
        menuDto.setBasePrice(orderItem.getMenu().getBasePrice());

        return new OrderItemDto(
                orderItem.getId(),
                menuDto,
                orderItem.getStyleType(),
                orderItem.getCustomizedQuantities(),
                orderItem.getQuantity(),
                orderItem.getSubTotal()
        );
    }
}

