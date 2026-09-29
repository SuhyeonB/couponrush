package com.example.couponrush.common.exception;

public class IssuedCouponNotUsablePeriodException extends CouponRushException {
    public IssuedCouponNotUsablePeriodException(Long issuedCouponId) {
        super("사용 가능 기간이 아닙니다. issuedCouponId=" + issuedCouponId);
    }
}