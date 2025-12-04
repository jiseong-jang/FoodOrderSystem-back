package com.mrdinner.repository;

import com.mrdinner.entity.Customer;
import com.mrdinner.entity.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {
    List<CustomerCoupon> findByCustomerOrderByReceivedAtDesc(Customer customer);
    List<CustomerCoupon> findByCustomerAndIsUsedFalseOrderByReceivedAtDesc(Customer customer);
    Optional<CustomerCoupon> findByCustomerAndCouponAndIsUsedFalse(Customer customer, com.mrdinner.entity.Coupon coupon);
}

