package com.example.couponrush.common.exception;

public class CouponNotIssuablePeriodException extends CouponRushException {
    public CouponNotIssuablePeriodException(Long couponId) {
        super("발급 가능 기간이 아닙니다. couponId=" + couponId);
    }
}