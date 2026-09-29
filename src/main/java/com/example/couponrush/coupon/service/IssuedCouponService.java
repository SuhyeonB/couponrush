package com.example.couponrush.coupon.service;

import com.example.couponrush.coupon.dto.request.IssuedCouponRequest;
import com.example.couponrush.coupon.dto.response.IssuedCouponResponse;
import com.example.couponrush.coupon.entity.Coupon;
import com.example.couponrush.coupon.entity.IssuedCoupon;
import com.example.couponrush.coupon.exception.IssuedCouponNotFoundException;
import com.example.couponrush.coupon.repository.IssuedCouponRepository;
import com.example.couponrush.user.entity.User;
import com.example.couponrush.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssuedCouponService {

    private final IssuedCouponRepository issuedCouponRepository;
    private final UserService userService;
    private final CouponService couponService;

    @Transactional
    public IssuedCouponResponse issue(Long couponId, IssuedCouponRequest dto) {
        User user = userService.findUser(dto.getUserId());

        Coupon coupon = couponService.findCoupon(couponId);

        coupon.issue();

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(coupon)
                .user(user)
                .startAt(coupon.getStartAt())
                .endAt(coupon.getEndAt())
                .build();

        issuedCouponRepository.save(issuedCoupon);
        return IssuedCouponResponse.from(issuedCoupon);
    }

    @Transactional(readOnly = true)
    public List<IssuedCouponResponse> getAllIssuedCoupons() {
        return issuedCouponRepository.findAll()
                .stream().map(IssuedCouponResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IssuedCouponResponse getIssuedCoupon(Long id) {
        return issuedCouponRepository.findById(id)
                .map(IssuedCouponResponse::from)
                .orElseThrow(() -> new IssuedCouponNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public long countByCouponId(Long couponId) {
        return issuedCouponRepository.countByCouponId(couponId);
    }
}
