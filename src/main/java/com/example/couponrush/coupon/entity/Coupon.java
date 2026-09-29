package com.example.couponrush.coupon.entity;

import com.example.couponrush.common.exception.CouponNotIssuablePeriodException;
import com.example.couponrush.common.exception.CouponSoldOutException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int quantity;

    // 발급 가능 기간
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    public Coupon(String name, String description, int quantity,
                  LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void issue() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startAt) || now.isAfter(endAt)) {
            throw new CouponNotIssuablePeriodException(this.id);
        }
        if (quantity <= 0) {
            throw new CouponSoldOutException(this.id);
        }
        quantity--;
    }

    public void update(String name, String description, int quantity,
                       LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
