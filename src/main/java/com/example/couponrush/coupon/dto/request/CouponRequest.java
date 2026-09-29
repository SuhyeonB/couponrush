package com.example.couponrush.coupon.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CouponRequest {

    @NotBlank
    private String name;

    private String description;

    @Min(0)
    private int quantity;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
