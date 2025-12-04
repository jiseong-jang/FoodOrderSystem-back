package com.mrdinner.service;

import com.mrdinner.dto.CustomerCouponDto;
import com.mrdinner.dto.CustomerProfileDto;
import com.mrdinner.dto.CouponDto;
import com.mrdinner.dto.UpdateProfileRequest;
import com.mrdinner.entity.Customer;
import com.mrdinner.entity.CustomerCoupon;
import com.mrdinner.entity.Coupon;
import com.mrdinner.entity.Order;
import com.mrdinner.entity.OrderStatus;
import com.mrdinner.repository.CustomerRepository;
import com.mrdinner.repository.CartRepository;
import com.mrdinner.repository.CustomerCouponRepository;
import com.mrdinner.repository.CouponRepository;
import com.mrdinner.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerCouponRepository customerCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    public CustomerProfileDto getProfile(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        return new CustomerProfileDto(
                customer.getId(),
                customer.getName(),
                customer.getAddress(),
                customer.getCardNumber(),
                customer.getCardExpiry(),
                customer.getCardCVC(),
                customer.getCardHolderName(),
                customer.getIsRegularCustomer()
        );
    }

    @Transactional
    public CustomerProfileDto updateProfile(String customerId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다");
        }

        // 정보 업데이트
        if (request.getName() != null && !request.getName().isEmpty()) {
            customer.setName(request.getName());
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCardNumber() != null && !request.getCardNumber().isEmpty()) {
            customer.setCardNumber(request.getCardNumber());
        }
        if (request.getCardExpiry() != null && !request.getCardExpiry().isEmpty()) {
            customer.setCardExpiry(request.getCardExpiry());
        }
        if (request.getCardCVC() != null && !request.getCardCVC().isEmpty()) {
            customer.setCardCVC(request.getCardCVC());
        }
        if (request.getCardHolderName() != null && !request.getCardHolderName().isEmpty()) {
            customer.setCardHolderName(request.getCardHolderName());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        customer = customerRepository.save(customer);

        return new CustomerProfileDto(
                customer.getId(),
                customer.getName(),
                customer.getAddress(),
                customer.getCardNumber(),
                customer.getCardExpiry(),
                customer.getCardCVC(),
                customer.getCardHolderName(),
                customer.getIsRegularCustomer()
        );
    }

    public List<CustomerCouponDto> getCustomerCoupons(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        return customerCouponRepository.findByCustomerOrderByReceivedAtDesc(customer).stream()
                .map(this::convertToCustomerCouponDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkAndUpdateRegularCustomer(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        // 이미 단골 고객이면서 쿠폰도 있는 경우 체크하지 않음
        if (Boolean.TRUE.equals(customer.getIsRegularCustomer())) {
            // 쿠폰이 있는지 확인
            Coupon regularCoupon = couponRepository.findByCode("REGULAR10000").orElse(null);
            if (regularCoupon != null) {
                boolean hasCoupon = customerCouponRepository
                        .findByCustomerAndCouponAndIsUsedFalse(customer, regularCoupon)
                        .isPresent();
                if (hasCoupon) {
                    return;
                }
            }
        }

        // 배달 완료된 주문 수 확인 (COMPLETED 상태만)
        List<Order> completedOrders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedOrders.size() >= 5) {
            customer.setIsRegularCustomer(true);
            customerRepository.save(customer);

            // 단골 고객이 되면 쿠폰 자동 지급 (예: 10,000원 할인 쿠폰)
            grantRegularCustomerCoupon(customer);
        }
    }

    @Transactional
    private void grantRegularCustomerCoupon(Customer customer) {
        // 단골 고객 쿠폰 생성 또는 조회 (예: 코드 "REGULAR10000")
        Coupon regularCoupon = couponRepository.findByCode("REGULAR10000")
                .orElseGet(() -> {
                    // 쿠폰이 없으면 자동 생성
                    Coupon newCoupon = new Coupon();
                    newCoupon.setCode("REGULAR10000");
                    newCoupon.setDiscountAmount(10000);
                    newCoupon.setIsValid(true);
                    return couponRepository.save(newCoupon);
                });

        if (Boolean.TRUE.equals(regularCoupon.getIsValid())) {
            // 이미 받은 쿠폰인지 확인
            boolean alreadyReceived = customerCouponRepository
                    .findByCustomerAndCouponAndIsUsedFalse(customer, regularCoupon)
                    .isPresent();

            if (!alreadyReceived) {
                CustomerCoupon customerCoupon = new CustomerCoupon();
                customerCoupon.setCustomer(customer);
                customerCoupon.setCoupon(regularCoupon);
                customerCoupon.setIsUsed(false);
                customerCouponRepository.save(customerCoupon);
            }
        }
    }
    
    @Transactional
    public void checkAndUpdateAllCustomers() {
        // 모든 고객에 대해 단골 고객 체크 수행 (기존 데이터 처리용)
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            checkAndUpdateRegularCustomer(customer.getId());
        }
    }
    
    @Transactional
    public void forceCheckRegularCustomer(String customerId) {
        // 강제로 단골 고객 체크 수행 (isRegularCustomer 플래그 무시)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        // 배달 완료된 주문 수 확인 (COMPLETED 상태만)
        List<Order> completedOrders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedOrders.size() >= 5) {
            customer.setIsRegularCustomer(true);
            customerRepository.save(customer);
            grantRegularCustomerCoupon(customer);
        }
    }

    private CustomerCouponDto convertToCustomerCouponDto(CustomerCoupon customerCoupon) {
        CouponDto couponDto = new CouponDto(
                customerCoupon.getCoupon().getId(),
                customerCoupon.getCoupon().getCode(),
                customerCoupon.getCoupon().getDiscountAmount(),
                customerCoupon.getCoupon().getIsValid()
        );

        return new CustomerCouponDto(
                customerCoupon.getId(),
                couponDto,
                customerCoupon.getIsUsed(),
                customerCoupon.getUsedAt(),
                customerCoupon.getReceivedAt()
        );
    }

    @Transactional
    public void deleteAccount(String customerId, String password) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // 장바구니 삭제
        cartRepository.findByCustomer(customer).ifPresent(cartRepository::delete);

        // 고객 삭제
        customerRepository.delete(customer);
    }
}

