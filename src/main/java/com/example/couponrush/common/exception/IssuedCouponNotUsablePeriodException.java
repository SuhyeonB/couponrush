package com.example.couponrush.common.exception;

import org.springframework.http.HttpStatus;

public class IssuedCouponNotUsablePeriodException extends CouponRushException {
    public IssuedCouponNotUsablePeriodException(Long issuedCouponId) {
        super("사용 가능 기간이 아닙니다. issuedCouponId=" + issuedCouponId, HttpStatus.BAD_REQUEST);
    }
}