package com.example.couponrush.common.exception;

public abstract class CouponRushException extends RuntimeException {
    public CouponRushException(String message) {
        super(message);
    }
}