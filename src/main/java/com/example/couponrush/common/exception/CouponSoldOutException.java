package com.example.couponrush.common.exception;

public class CouponSoldOutException extends CouponRushException {
    public CouponSoldOutException(Long couponId) {
        super("쿠폰 수량이 소진되었습니다. couponId=" + couponId);
    }
}