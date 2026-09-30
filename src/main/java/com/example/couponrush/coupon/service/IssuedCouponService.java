package com.example.couponrush.coupon.service;

import com.example.couponrush.common.exception.LockAcquisitionException;
import com.example.couponrush.coupon.dto.request.IssuedCouponRequest;
import com.example.couponrush.coupon.dto.response.IssuedCouponResponse;
import com.example.couponrush.coupon.exception.IssuedCouponNotFoundException;
import com.example.couponrush.coupon.repository.CouponRepository;
import com.example.couponrush.coupon.repository.IssuedCouponRepository;
import com.example.couponrush.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IssuedCouponService {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;
    private final UserService userService;
    // private final CouponService couponService;
    private final IssuedCouponExecutor issuedCouponExecutor;
    private final RedissonClient redissonClient;

    /*
    @Transactional
    public IssuedCouponResponse issue(Long couponId, IssuedCouponRequest dto) {
        User user = userService.findUser(dto.getUserId());

        // Coupon coupon = couponService.findCoupon(couponId); // without Lock version
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                        .orElseThrow(() -> new CouponNotFoundException(couponId));

        coupon.issue();

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(coupon)
                .user(user)
                .startAt(coupon.getStartAt())
                .endAt(coupon.getEndAt())
                .build();

        issuedCouponRepository.save(issuedCoupon);
        return IssuedCouponResponse.from(issuedCoupon);
    }
     */

    public IssuedCouponResponse issue(Long couponId, IssuedCouponRequest dto) {
        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;

        try {
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new LockAcquisitionException(lockKey);
            }
            return issuedCouponExecutor.issue(couponId, dto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException(lockKey);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<IssuedCouponResponse> getAllIssuedCoupons() {
        return issuedCouponRepository.findAll()
                .stream().map(IssuedCouponResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IssuedCouponResponse getIssuedCoupon(Long id) {
        return issuedCouponRepository.findById(id)
                .map(IssuedCouponResponse::from)
                .orElseThrow(() -> new IssuedCouponNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public long countByCouponId(Long couponId) {
        return issuedCouponRepository.countByCouponId(couponId);
    }
}
