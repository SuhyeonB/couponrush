package com.example.couponrush.coupon.dto.response;

import com.example.couponrush.coupon.entity.IssuedCoupon;
import com.example.couponrush.coupon.entity.IssuedCouponStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record IssuedCouponResponse(Long id, String serialCode, String couponName, String username,
                                   IssuedCouponStatus status, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt) {

    public static IssuedCouponResponse from(IssuedCoupon issuedCoupon) {
        return IssuedCouponResponse.builder()
                .id(issuedCoupon.getId())
                .serialCode(issuedCoupon.getSerialCode())
                .couponName(issuedCoupon.getCoupon().getName())
                .username(issuedCoupon.getUser().getName())
                .status(issuedCoupon.getStatus())
                .startAt(issuedCoupon.getStartAt())
                .endAt(issuedCoupon.getEndAt())
                .createdAt(issuedCoupon.getCreatedAt())
                .build();
    }
}
