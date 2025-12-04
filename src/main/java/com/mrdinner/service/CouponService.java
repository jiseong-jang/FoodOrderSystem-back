package com.mrdinner.service;

import com.mrdinner.dto.CreateCouponRequest;
import com.mrdinner.dto.CouponDto;
import com.mrdinner.entity.Coupon;
import com.mrdinner.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    @Transactional
    public CouponDto createCoupon(CreateCouponRequest request) {
        // 중복 코드 확인
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("이미 존재하는 쿠폰 코드입니다");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setIsValid(true);

        coupon = couponRepository.save(coupon);
        return convertToDto(coupon);
    }

    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponDto toggleCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다"));

        coupon.setIsValid(!coupon.getIsValid());
        coupon = couponRepository.save(coupon);
        return convertToDto(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다"));

        couponRepository.delete(coupon);
    }

    private CouponDto convertToDto(Coupon coupon) {
        return new CouponDto(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountAmount(),
                coupon.getIsValid()
        );
    }
}

