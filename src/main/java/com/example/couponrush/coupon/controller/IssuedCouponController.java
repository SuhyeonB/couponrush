package com.example.couponrush.coupon.controller;

import com.example.couponrush.coupon.dto.request.IssuedCouponRequest;
import com.example.couponrush.coupon.dto.response.IssuedCouponResponse;
import com.example.couponrush.coupon.service.IssuedCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons/{couponId}/issued-coupons")
public class IssuedCouponController {

    private final IssuedCouponService issuedCouponService;

    @PostMapping
    public ResponseEntity<IssuedCouponResponse> issue(
            @PathVariable Long couponId,
            @Valid @RequestBody IssuedCouponRequest dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issuedCouponService.issue(couponId, dto));
    }

    @GetMapping
    public ResponseEntity<List<IssuedCouponResponse>> getAllIssuedCoupons() {
        return ResponseEntity.ok(issuedCouponService.getAllIssuedCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssuedCouponResponse> getIssuedCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(issuedCouponService.getIssuedCoupon(id));
    }
}
