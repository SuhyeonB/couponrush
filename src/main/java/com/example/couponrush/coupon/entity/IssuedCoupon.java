package com.example.couponrush.coupon.entity;

import com.example.couponrush.coupon.exception.IssuedCouponAlreadyUsedException;
import com.example.couponrush.coupon.exception.IssuedCouponNotUsablePeriodException;
import com.example.couponrush.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "issued_coupons")
public class IssuedCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String serialCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private IssuedCouponStatus status;

    // 사용 가능 기간
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.serialCode = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        this.status = IssuedCouponStatus.UNUSED;
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public IssuedCoupon(Coupon coupon, User user, LocalDateTime startAt, LocalDateTime endAt) {
        this.coupon = coupon;
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void use() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startAt) || now.isAfter(endAt)) {
            throw new IssuedCouponNotUsablePeriodException(this.id);
        }
        if (this.status == IssuedCouponStatus.USED) {
            throw new IssuedCouponAlreadyUsedException(this.id);
        }
        this.status = IssuedCouponStatus.USED;
    }
}
