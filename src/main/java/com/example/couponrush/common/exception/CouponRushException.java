package com.example.couponrush.common.exception;

import org.springframework.http.HttpStatus;

public abstract class CouponRushException extends RuntimeException {

    private final HttpStatus status;

    protected CouponRushException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}