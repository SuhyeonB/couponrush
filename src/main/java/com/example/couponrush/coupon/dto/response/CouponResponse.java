package com.example.couponrush.coupon.dto.response;

import com.example.couponrush.coupon.entity.Coupon;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CouponResponse(Long id, String name, String description, int quantity,
                             LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt) {
    public static CouponResponse from (Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .quantity(coupon.getQuantity())
                .startAt(coupon.getStartAt())
                .endAt(coupon.getEndAt())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
