package com.example.couponrush.user.exception;

import com.example.couponrush.common.exception.CouponRushException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CouponRushException {
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. userId=" + userId, HttpStatus.NOT_FOUND);
    }
}
