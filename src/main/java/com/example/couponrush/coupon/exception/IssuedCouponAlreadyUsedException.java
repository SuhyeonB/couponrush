package com.example.couponrush.coupon.exception;

import com.example.couponrush.common.exception.CouponRushException;
import org.springframework.http.HttpStatus;

public class IssuedCouponAlreadyUsedException extends CouponRushException {
    public IssuedCouponAlreadyUsedException(Long issuedCouponId) {
        super("이미 사용된 쿠폰입니다. issuedCouponId=" + issuedCouponId, HttpStatus.CONFLICT);
    }
}