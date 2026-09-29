package com.example.couponrush.coupon.exception;

import com.example.couponrush.common.exception.CouponRushException;
import org.springframework.http.HttpStatus;

public class CouponNotFoundException extends CouponRushException {
    public CouponNotFoundException(Long id) {
        super("쿠폰을 찾을 수 없습니다. couponId=" + id, HttpStatus.NOT_FOUND);
    }
}
