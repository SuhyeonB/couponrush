package com.example.couponrush.common.exception;

public class IssuedCouponAlreadyUsedException extends CouponRushException {
    public IssuedCouponAlreadyUsedException(Long issuedCouponId) {
        super("이미 사용된 쿠폰입니다. issuedCouponId=" + issuedCouponId);
    }
}