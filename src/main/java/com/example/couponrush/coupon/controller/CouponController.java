package com.example.couponrush.coupon.controller;

import com.example.couponrush.coupon.dto.request.CouponRequest;
import com.example.couponrush.coupon.dto.response.CouponResponse;
import com.example.couponrush.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon (
            @Valid @RequestBody CouponRequest dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(dto));
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons () {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCoupon (
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(couponService.getCoupon(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon (
            @PathVariable Long id
    ) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
