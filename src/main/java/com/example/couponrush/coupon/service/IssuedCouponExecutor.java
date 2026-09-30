package com.example.couponrush.coupon.service;

import com.example.couponrush.coupon.dto.request.IssuedCouponRequest;
import com.example.couponrush.coupon.dto.response.IssuedCouponResponse;
import com.example.couponrush.coupon.entity.Coupon;
import com.example.couponrush.coupon.entity.IssuedCoupon;
import com.example.couponrush.coupon.exception.CouponNotFoundException;
import com.example.couponrush.coupon.repository.CouponRepository;
import com.example.couponrush.coupon.repository.IssuedCouponRepository;
import com.example.couponrush.user.entity.User;
import com.example.couponrush.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssuedCouponExecutor {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;
    private final UserService userService;

    @Transactional
    public IssuedCouponResponse issue(
            Long couponId,
            IssuedCouponRequest dto
    ) {
        User user = userService.findUser(dto.getUserId());

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

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
}




