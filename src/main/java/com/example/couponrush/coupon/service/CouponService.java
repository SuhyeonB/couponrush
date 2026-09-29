package com.example.couponrush.coupon.service;

import com.example.couponrush.coupon.exception.CouponNotFoundException;
import com.example.couponrush.coupon.dto.request.CouponRequest;
import com.example.couponrush.coupon.dto.response.CouponResponse;
import com.example.couponrush.coupon.entity.Coupon;
import com.example.couponrush.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest dto) {
        Coupon coupon = Coupon.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .quantity(dto.getQuantity())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .build();

        couponRepository.save(coupon);

        return CouponResponse.from(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream().map(CouponResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long id) {
        return couponRepository.findById(id)
                .map(CouponResponse::from)
                .orElseThrow(() -> new CouponNotFoundException(id));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new CouponNotFoundException(id);
        }
        couponRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
    }
}
