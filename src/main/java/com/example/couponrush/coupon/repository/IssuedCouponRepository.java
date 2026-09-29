package com.example.couponrush.coupon.repository;

import com.example.couponrush.coupon.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
}
