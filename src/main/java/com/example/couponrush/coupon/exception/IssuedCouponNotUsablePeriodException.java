package com.example.couponrush.coupon.exception;

import com.example.couponrush.common.exception.CouponRushException;
import org.springframework.http.HttpStatus;

public class IssuedCouponNotUsablePeriodException extends CouponRushException {
    public IssuedCouponNotUsablePeriodException(Long issuedCouponId) {
        super("사용 가능 기간이 아닙니다. issuedCouponId=" + issuedCouponId, HttpStatus.BAD_REQUEST);
    }
}