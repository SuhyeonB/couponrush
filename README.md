# couponrush

선착순 쿠폰 발급 API — 멀티 서버 환경에서의 재고 동시성 제어 (Redisson 분산락, watchdog)

서버를 2대로 띄워 같은 MySQL을 공유하는 환경에서, 한정 수량 쿠폰에 요청이 몰릴 때 생기는 동시성 문제를 직접 재현하고, 락 전략별(락 없음 / DB 비관적 락 / Redisson 분산락)로 결과를 비교한 프로젝트다.

## 결과 요약

조건: 쿠폰 수량 5, 유저 50명이 서버 2대(8080/8081)에 동시에 발급 요청

| | 성공 건수 | 최종 quantity | 비고 |
|---|---|---|---|
| 락 없음 | 9 (초과 발급) | 0 | 9번 감소 시도 중 5번만 반영(Lost Update), 500 에러 3건(MySQL 데드락) |
| DB 비관적 락 | 5 | 0 | 500 에러 없음, 성공하는 유저는 실행마다 달라짐 |
| Redisson 분산락 | 5 | 0 | 500 에러 없음 |

락 수명(leaseTime vs watchdog) 실험 결과는 [락 수명 실험](#락-수명-고정-leasetime-vs-watchdog)에 정리했다.

## 기술 스택

- Java, Spring Boot (Web, Data JPA, Validation), Lombok
- MySQL, Redis (docker compose)
- Redisson (`redisson-spring-boot-starter` 4.7.0)
- Apache JMeter 5.6.3 (부하 테스트)

## 도메인

| 엔티티 | 필드 | 설명 |
|---|---|---|
| `Coupon` | id, name(unique), description, quantity, startAt, endAt, createdAt | 쿠폰 이벤트. `startAt`~`endAt`은 **발급 가능 기간** |
| `IssuedCoupon` | id, serialCode, coupon, user, status, startAt, endAt, createdAt | 발급된 쿠폰. `startAt`~`endAt`은 **사용 가능 기간** |
| `User` | id, name | 인증 없이 최소 필드만 (동시성 실험이 목적) |

- `Coupon.issue()`: 발급 가능 기간 검증 → 수량 검증 → `quantity--`
- `IssuedCoupon.use()`: 사용 가능 기간 검증 → 중복 사용 검증 → 상태를 `USED`로 변경
- `IssuedCoupon`의 사용 가능 기간은 발급 시점에 `Coupon`의 값을 복사한다. 이후 `Coupon` 정책이 바뀌어도 이미 발급된 쿠폰의 조건은 유지된다.
- `serialCode`는 쿠폰함/결제/바코드에 노출할 외부 식별자로, PK와 분리했다 (UUID 앞 12자리, 대문자).
- `IssuedCoupon`에 `(coupon_id, user_id)` unique 제약을 걸어 한 유저의 중복 발급을 DB 레벨에서 막는다.
- 상태(`status`)는 `IssuedCoupon`에만 둔다 (`UNUSED`, `USED`). 기간 만료는 별도 상태 없이 시간 필드 비교로 판단한다.

## API

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/users` | 유저 생성 |
| GET | `/api/users`, `/api/users/{id}` | 유저 조회 |
| POST | `/api/coupons` | 쿠폰 생성 |
| GET | `/api/coupons`, `/api/coupons/{id}` | 쿠폰 조회 |
| DELETE | `/api/coupons/{id}` | 쿠폰 삭제 |
| POST | `/api/coupons/{couponId}/issued-coupons` | 쿠폰 발급 (body: `{"userId": 1}`) |
| GET | `/api/coupons/{couponId}/issued-coupons`, `.../{id}` | 발급 내역 조회 |

쿠폰 생성 요청 예시:

```json
POST /api/coupons
{
  "name": "선착순 테스트 쿠폰",
  "description": "동시성 검증용",
  "quantity": 5,
  "startAt": "2026-09-29T00:00:00",
  "endAt": "2026-12-31T23:59:59"
}
```

## 예외 처리

모든 도메인 예외는 `CouponRushException`(HttpStatus 보유)을 상속하고, `GlobalExceptionHandler`가 하나의 핸들러로 `ErrorResponse(message)` 형태로 응답한다. 새 예외를 만들 때 상태코드를 함께 정하도록 강제하는 구조다.

| 예외 | 상태코드 | 의미 |
|---|---|---|
| `CouponNotFoundException`, `UserNotFoundException`, `IssuedCouponNotFoundException` | 404 | 대상 없음 |
| `CouponNotIssuablePeriodException` | 400 | 발급 가능 기간 아님 |
| `IssuedCouponNotUsablePeriodException` | 400 | 사용 가능 기간 아님 |
| `CouponSoldOutException` | 409 | 수량 소진 |
| `IssuedCouponAlreadyUsedException` | 409 | 이미 사용된 쿠폰 |
| `LockAcquisitionException` | 503 | 락 획득 실패(대기 시간 초과 등) |

## 동시성 제어 실험

### 환경

- 같은 jar를 포트만 바꿔 2대 실행(8080, 8081), 하나의 MySQL(docker, 3306)을 공유
- 쿠폰 수량 5, 유저 50명(`data.sql`)
- JMeter: Thread Group 2개. 서버 8080에는 user 1~25, 서버 8081에는 user 26~50이 동시에 발급 요청 (유저를 겹치지 않게 나눠 unique 제약과 무관하게 함)

### 1. 락 없음

`Coupon`을 조회하고 `quantity--` 후 `IssuedCoupon`을 저장하는 하나의 트랜잭션.

- 9건 성공(수량 5 초과), 3건은 500, 나머지는 수량 소진
- 최종 `quantity`는 0. 9번의 감소 시도 중 실제 반영은 5번뿐이다. 여러 트랜잭션이 같은 값을 읽고 각자 `-1`한 값을 덮어써서 나머지 감소가 유실됐다(Lost Update).
- 500은 서버 로그 상 MySQL 데드락(ErrorCode 1213)이었다. 로그에서 `issued_coupons` INSERT 이후 `coupons` UPDATE 순서가 보여, 외래키 제약으로 인한 부모 row 공유 락(S lock) 보유 상태에서 배타 락(X lock)을 요청하다 서로 기다린 것으로 **추정**한다. `SHOW ENGINE INNODB STATUS`로는 확인하지 못했다.

### 2. DB 비관적 락

```java
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);
}
```

- 두 번 실행 모두 5건 성공, 45건 수량 소진, 500 에러 없음
- 서버가 2대여도 DB가 공유 지점이라 정합성이 지켜진다. `SELECT ... FOR UPDATE`가 배타 락을 먼저 잡으므로 락 없는 버전의 데드락도 나타나지 않았다.
- 성공하는 유저는 실행마다 달랐다. 락은 한 번에 한 트랜잭션만 통과시킬 뿐, 먼저 요청한 순서를 보장하지 않는다.

### 3. Redisson 분산락

```java
public IssuedCouponResponse issue(Long couponId, IssuedCouponRequest dto) {
    String lockKey = "lock:coupon:" + couponId;
    RLock lock = redissonClient.getLock(lockKey);

    boolean acquired = false;
    try {
        acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);   // tryLock(waitTime, leaseTime, unit)
        // acquired = lock.tryLock(40, TimeUnit.SECONDS);   // leaseTime 미지정 -> watchdog
        if (!acquired) {
            throw new LockAcquisitionException(lockKey);
        }
        return issuedCouponExecutor.issue(couponId, dto);   // @Transactional
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LockAcquisitionException(lockKey);
    } finally {
        if (acquired && lock.isHeldByCurrentThread()) {
            lock.unlock();
        } else if (acquired && !lock.isHeldByCurrentThread()) {
            log.warn("user {} 락 만료 후 종료", dto.getUserId());   // 작업 도중 락이 만료된 경우
        }
    }
}
```

- 5건 성공, 45건 수량 소진, 500 에러 없음
- 락은 트랜잭션 **바깥**에서 잡고 푼다. 트랜잭션 안에서 잡으면 `unlock()`이 커밋보다 먼저 일어나, 다른 요청이 커밋되지 않은 값을 읽을 여지가 생긴다.
- 트랜잭션 로직을 `IssuedCouponExecutor`로 분리했다. 같은 클래스 안에서 `@Transactional` 메서드를 직접 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않는 self-invocation 문제를 피하기 위해서다. 분리 전 코드에서도 테스트 결과는 정상이었는데, 락이 동시 진입을 막고 있어서 문제가 드러나지 않았을 뿐이라 잠재 문제로 판단했다.
- 발급 로직이 짧아(수십 ms) 고정 `leaseTime`(10초)을 사용했고, watchdog 방식은 주석으로 병기했다.

### 락 수명: 고정 leaseTime vs watchdog

Redisson은 `leaseTime`을 지정하면 그 시간 뒤 락이 만료되고, 지정하지 않으면 watchdog이 락을 쥔 인스턴스가 살아 있는 동안 만료 시간(기본 30초)을 주기적으로 연장한다. 이 차이를 보기 위해 임계구역 안(`coupon.issue()` 직후)에 `Thread.sleep`을 넣어 느린 작업을 흉내 냈다. 실험 후 sleep과 설정은 모두 원복했다.

| 실험 | 작업 시간 | 락 설정 | 결과 |
|---|---|---|---|
| A | 5초 | `tryLock(15, 2, SECONDS)` | 8건 성공, 최종 quantity 2, 나머지 503, "락 만료 후 종료" 로그 8건 |
| B | 5초 | `tryLock(40, SECONDS)` (watchdog) | 5건 성공, 최종 quantity 0, 나머지 409 |
| C | 40초 | `tryLock(40, SECONDS)` (watchdog) | 2건 성공, 나머지 503, 만료 로그 0건 |

- **A**: 락 획득 간격이 정확히 2초(`leaseTime`)였다. 작업이 끝나기 전에 락이 만료돼 다음 요청이 같은 구간에 진입했고, 8번 감소해야 할 수량이 3번만 반영(5→2)됐다. 락이 있어도 작업이 끝날 때까지 유지되지 않으면 정합성이 깨진다.
- **B**: 같은 5초 작업이어도 watchdog에서는 정확히 5건. 다만 5초 작업은 기본 TTL(30초) 안에 끝나므로 연장 여부를 증명하는 실험은 아니다.
- **C**: 작업이 TTL보다 긴 40초일 때 `redis-cli`로 `PTTL lock:coupon:1`을 1초 간격으로 관찰했다. 값이 30000에서 줄다가 20000 부근에서 다시 30000 근처로 올라가는 패턴이 약 10초 주기로 반복됐고(요청당 3회), 40초 동안 락이 유지됐다. 약점도 보였다. 락을 쥔 쪽이 살아 있으면 연장이 계속되므로, 느린 작업 하나가 뒤의 요청을 모두 막는다(대기 시간 초과로 503).

## 한계 / 확인하지 못한 것

- 락 없는 버전의 데드락 원인(FK 공유 락)은 추정이며 확인하지 못했다.
- 락 방식 간 응답 시간은 측정하지 않았다. 성능 우열은 주장하지 않는다.
- 이번 실험에서 비관적 락과 Redisson은 둘 다 정합성을 지켰다. 분산락을 쓰는 이유로 흔히 언급되는 점(락 대기 중 DB 커넥션 점유 회피, 락 대상이 DB row에 묶이지 않음, 락 경합을 DB 밖으로 분리)은 이 실험으로 확인하지 못했다.
- 요청 50건의 소규모 실험이고 결과가 타이밍에 의존한다. 서버별 HikariCP 기본 풀 크기(10)가 실제 동시 DB 작업의 상한이 된다.
- `leaseTime`을 30초로 고정하고 40초 작업을 돌리는 대조군은 실행하지 않았다.

## 프로젝트 구조

```
com.example.couponrush
├── common/exception          # CouponRushException, GlobalExceptionHandler, ErrorResponse, LockAcquisitionException
├── coupon
│   ├── controller
│   ├── dto
│   ├── entity                # Coupon, IssuedCoupon, IssuedCouponStatus
│   ├── exception
│   ├── repository
│   └── service               # CouponService, IssuedCouponService(락), IssuedCouponExecutor(트랜잭션)
└── user
    ├── controller
    ├── dto
    ├── entity
    ├── exception
    ├── repository
    └── service
```

## 실행 방법

1. MySQL, Redis 실행

   ```bash
   docker compose up -d
   ```

2. 서버 2대 실행 (같은 DB를 바라봄)

   ```bash
   ./gradlew bootRun --args='--server.port=8080'
   ./gradlew bootRun --args='--server.port=8081'
   ```

   IntelliJ에서는 Run Configuration을 복제해 VM options에 `-Dserver.port=8081`을 추가한다.

3. 초기 데이터
   - `src/main/resources/data.sql`로 유저 50명을 넣는다. 서버를 시작할 때마다 실행되므로 `users` 테이블을 비우고 한 번만 실행하거나, 재시작 시 중복 삽입에 주의한다.

     ```yaml
     spring:
       jpa:
         defer-datasource-initialization: true
       sql:
         init:
           mode: always
     ```

   - 수량 5인 쿠폰은 `POST /api/coupons`로 생성한다.

4. JMeter 설정
   - Thread Group 2개(서버 8080/8081), 각 25 스레드, Ramp-up 1초, Loop 1
   - HTTP Request: `POST /api/coupons/1/issued-coupons`, Body `{"userId": ${userId}}`
   - HTTP Header Manager: `Content-Type: application/json` (없으면 415)
   - CSV Data Set Config로 서버별 userId 범위를 분리 (1~25 / 26~50)

5. 결과 확인

   ```sql
   SELECT COUNT(*) FROM issued_coupons WHERE coupon_id = 1;
   SELECT quantity FROM coupons WHERE id = 1;
   ```

   재실행 전에는 초기화한다.

   ```sql
   DELETE FROM issued_coupons WHERE coupon_id = 1;
   UPDATE coupons SET quantity = 5 WHERE id = 1;
   ```

## 관련 글

- [동시성 제어 시리즈 (velog)](https://velog.io/@siha_014/series/동시성-제어)
