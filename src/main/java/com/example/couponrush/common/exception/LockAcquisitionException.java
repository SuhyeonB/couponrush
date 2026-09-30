package com.example.couponrush.common.exception;

import org.springframework.http.HttpStatus;

public class LockAcquisitionException extends CouponRushException {
    public LockAcquisitionException(String lockKey) {
        super("락 획득에 실패했습니다. key=" + lockKey, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
