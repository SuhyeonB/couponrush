package com.example.couponrush.coupon.service;

import com.example.couponrush.common.exception.LockAcquisitionException;
import com.example.couponrush.coupon.dto.request.IssuedCouponRequest;
import com.example.couponrush.coupon.dto.response.IssuedCouponResponse;
import com.example.couponrush.coupon.exception.IssuedCouponNotFoundException;
import com.example.couponrush.coupon.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssuedCouponService {

    private final IssuedCouponRepository issuedCouponRepository;
    private final IssuedCouponExecutor issuedCouponExecutor;
    private final RedissonClient redissonClient;

    public IssuedCouponResponse issue(Long couponId, IssuedCouponRequest dto) {
        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;

        try {
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);   // tryLock(waitTime, leaseTime, Timeunit unit)
            // acquired = lock.tryLock(40, TimeUnit.SECONDS);   // tryLock(waitTime, unit) : leaseTime 명시 X -> watchdog
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
            } else if (acquired && !lock.isHeldByCurrentThread()) {
                log.warn("user {} 락 만료 후 종료", dto.getUserId());
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
