package com.example.couponrush.coupon.exception;

import com.example.couponrush.common.exception.CouponRushException;
import org.springframework.http.HttpStatus;

public class CouponNotIssuablePeriodException extends CouponRushException {
    public CouponNotIssuablePeriodException(Long couponId) {
        super("발급 가능 기간이 아닙니다. couponId=" + couponId, HttpStatus.BAD_REQUEST);
    }
}