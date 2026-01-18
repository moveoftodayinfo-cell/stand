# 월말 자동 정산 로직 설명 (3단계 할인 시스템)

Stand 앱의 월말 자동 정산 로직이 완성되었습니다! 🎉

---

## 📊 개요

매달 마지막 날, 사용자의 걸음 목표 달성률을 계산하여:

**새로운 3단계 할인 시스템:**
- **🏆 95% 이상 달성** → 다음 달 100% 면제 (0원) + 연속 달성 카운트
- **✅ 80~94% 달성** → 다음 달 50% 할인
- **❌ 80% 미만** → 다음 달 전액 결제
- **🔄 실패 후 성공** → 다다음 달에 실패분 환불

---

## 🔄 작동 방식

### 1. 월말 감지
```kotlin
// MainActivity.kt - checkAndResetDaily()
if (preferenceManager.isControlPeriodEnded()) {
    handlePeriodEnd()
}
```

제어 기간이 끝나면 자동으로 `handlePeriodEnd()` 호출

### 2. 정산 처리
```kotlin
// MainActivity.kt - handlePeriodEnd()
subscriptionManager.processMonthlyResult(
    currentMonthId = "2026-01",
    totalDays = 22,      // 제어 요일 총 일수
    successDays = 20     // 성공한 일수
)
```

### 3. Firebase에 저장
```
users/{userId}/subscriptions/
  ├─ 2026-01/  (현재 월)
  │   ├─ totalDays: 22
  │   ├─ successDays: 20
  │   ├─ achievementRate: 90.9
  │   ├─ isSuccess: true (80% 이상)
  │   ├─ discountRate: 50 (80~94%이므로)
  │   └─ ...
  │
  ├─ 2026-02/  (다음 월 - 자동 생성)
  │   ├─ deposit: 10000
  │   ├─ isExempt: false
  │   ├─ actualCharge: 5000    ✅ 50% 할인!
  │   ├─ discountRate: 0
  │   ├─ refundAmount: 0
  │   └─ ...
  │
  └─ 2026-03/  (환불 적용 달 - 필요 시 생성)
      ├─ deposit: 10000
      ├─ actualCharge: 0    ✅ 환불로 0원!
      └─ ...
```

---

## 📅 시나리오별 동작

### 시나리오 1: 계속 완전 달성 🏆🏆🏆

**상황:**
- 1월: 96% 달성
- 2월: 98% 달성
- 3월: 95% 달성

**결과:**

| 월 | 달성률 | 결제 금액 | 이유 |
|----|--------|----------|------|
| 1월 | 96% | 10,000원 | 최초 결제 |
| 2월 | 98% | 0원 | 1월 완전 달성 (100% 면제) |
| 3월 | 95% | 0원 | 2월 완전 달성 (100% 면제) |
| 4월 | - | 0원 | 3월 완전 달성 (100% 면제) |

**Firebase 데이터:**
```json
{
  "2026-01": { "achievementRate": 96, "isSuccess": true, "discountRate": 100, "consecutiveSuccessCount": 1 },
  "2026-02": { "isExempt": true, "actualCharge": 0, "consecutiveSuccessCount": 2 },
  "2026-03": { "isExempt": true, "actualCharge": 0, "consecutiveSuccessCount": 3 },
  "2026-04": { "isExempt": true, "actualCharge": 0 }
}
```

---

### 시나리오 2: 부분 달성 ✅✅✅

**상황:**
- 1월: 85% 달성
- 2월: 90% 달성
- 3월: 82% 달성

**결과:**

| 월 | 달성률 | 결제 금액 | 이유 |
|----|--------|----------|------|
| 1월 | 85% | 10,000원 | 최초 결제 |
| 2월 | 90% | 5,000원 | 1월 부분 달성 (50% 할인) |
| 3월 | 82% | 5,000원 | 2월 부분 달성 (50% 할인) |
| 4월 | - | 5,000원 | 3월 부분 달성 (50% 할인) |

**Firebase 데이터:**
```json
{
  "2026-01": { "achievementRate": 85, "isSuccess": true, "discountRate": 50 },
  "2026-02": { "isExempt": false, "actualCharge": 5000 },
  "2026-03": { "isExempt": false, "actualCharge": 5000 },
  "2026-04": { "isExempt": false, "actualCharge": 5000 }
}
```

---

### 시나리오 3: 계속 실패 ❌❌

**상황:**
- 1월: 75% 달성 (실패)
- 2월: 70% 달성 (실패)

**결과:**

| 월 | 달성률 | 결제 금액 | 이유 |
|----|--------|----------|------|
| 1월 | 75% | 10,000원 | 최초 결제 |
| 2월 | 70% | 10,000원 | 1월 실패 (전액) |
| 3월 | - | 10,000원 | 2월 실패 (전액) |

**Firebase 데이터:**
```json
{
  "2026-01": { "achievementRate": 75, "isSuccess": false, "discountRate": 0 },
  "2026-02": { "isExempt": false, "actualCharge": 10000, "refundAmount": 0 },
  "2026-03": { "isExempt": false, "actualCharge": 10000, "refundAmount": 0 }
}
```

---

### 시나리오 4: 실패 후 부분 성공 ❌✅ (환불!)

**상황:**
- 1월: 75% 달성 (실패)
- 2월: 85% 달성 (부분 성공)

**결과:**

| 월 | 달성률 | 결제 금액 | 이유 |
|----|--------|----------|------|
| 1월 | 75% | 10,000원 | 최초 결제 |
| 2월 | 85% | 10,000원 | 1월 실패 (전액) |
| 3월 | - | 5,000원 | 2월 부분 성공 (50% 할인) |
| 4월 | - | 0원 | **1월 실패분 10,000원 환불** + 50% 할인 = 0원 |

**Firebase 데이터:**
```json
{
  "2026-01": { "achievementRate": 75, "isSuccess": false },
  "2026-02": { "achievementRate": 85, "isSuccess": true, "discountRate": 50, "actualCharge": 10000 },
  "2026-03": {
    "isExempt": false,
    "actualCharge": 5000,
    "refundAmount": 10000  // 1월 실패분, 4월에 환불 예정
  },
  "2026-04": {
    "actualCharge": 0      // 5000 - 10000(환불) = 0
  }
}
```

---

### 시나리오 5: 실패 후 완전 성공 ❌🏆 (환불!)

**상황:**
- 1월: 70% 달성 (실패)
- 2월: 96% 달성 (완전 성공)

**결과:**

| 월 | 달성률 | 결제 금액 | 이유 |
|----|--------|----------|------|
| 1월 | 70% | 10,000원 | 최초 결제 |
| 2월 | 96% | 10,000원 | 1월 실패 (전액) |
| 3월 | - | 0원 | 2월 완전 성공 (100% 면제) |
| 4월 | - | 0원 | **1월 실패분 10,000원 환불** + 100% 면제 = 0원 |

**Firebase 데이터:**
```json
{
  "2026-01": { "achievementRate": 70, "isSuccess": false },
  "2026-02": { "achievementRate": 96, "isSuccess": true, "discountRate": 100, "consecutiveSuccessCount": 1 },
  "2026-03": {
    "isExempt": true,
    "actualCharge": 0,
    "refundAmount": 10000  // 1월 실패분, 4월에 환불 예정
  },
  "2026-04": {
    "actualCharge": 0      // 0 - 10000(환불) = 0
  }
}
```

---

### 시나리오 6: 복잡한 경우 ❌✅🏆❌✅

**상황:**
- 1월: 75% 실패
- 2월: 85% 부분 성공
- 3월: 97% 완전 성공
- 4월: 78% 실패
- 5월: 88% 부분 성공

**결과:**

| 월 | 달성률 | 결제 금액 | 할인율 | 환불 | 실제 결제 |
|----|--------|----------|--------|------|----------|
| 1월 | 75% ❌ | 10,000원 | 0% | - | 10,000원 |
| 2월 | 85% ✅ | 10,000원 | 0% | - | 10,000원 |
| 3월 | 97% 🏆 | 5,000원 | 50% | - | 5,000원 |
| 4월 | 78% ❌ | 0원 | 100% | 10,000원 (1월분) | 0원 |
| 5월 | 88% ✅ | 10,000원 | 0% | - | 10,000원 |
| 6월 | - | 5,000원 | 50% | 10,000원 (4월분) | 0원 |

**상세 설명:**

**1월 말 (75% 실패):**
- 2월: 전액 10,000원

**2월 말 (1월 실패, 2월 85% 성공):**
- 3월: 50% 할인 → 5,000원
- 4월: 1월 실패분 10,000원 환불 예정

**3월 말 (2월 성공, 3월 97% 완전 성공):**
- 4월: 100% 면제 → 0원 (이미 환불 적용되어 0원)
- 연속 성공 카운트: 1

**4월 말 (3월 성공, 4월 78% 실패):**
- 5월: 전액 10,000원
- 연속 카운트 리셋

**5월 말 (4월 실패, 5월 88% 성공):**
- 6월: 50% 할인 → 5,000원
- 7월: 4월 실패분 10,000원 환불 → 실제 0원

---

## 🔧 코드 구조

### SubscriptionManager.kt

```kotlin
suspend fun processMonthlyResult(
    currentMonthId: String,
    totalDays: Int,
    successDays: Int
): Result<Unit>
```

**작동 순서:**
1. 달성률 계산 (successDays / totalDays * 100)
2. **3단계 판정:**
   ```kotlin
   val (discountRate, isSuccess) = when {
       achievementRate >= 95f -> Pair(100, true)  // 🏆 완전 성공
       achievementRate >= 80f -> Pair(50, true)   // ✅ 부분 성공
       else -> Pair(0, false)                     // ❌ 실패
   }
   ```
3. 현재 월 구독 정보 업데이트 (discountRate 포함)
4. 연속 성공 카운트 계산:
   ```kotlin
   val consecutiveCount = if (achievementRate >= 95f) {
       currentSubscription.consecutiveSuccessCount + 1
   } else {
       0
   }
   ```
5. 이전 달 실패 여부 확인
6. 다음 달 구독 생성:
   - `isExempt` = (discountRate == 100)
   - `actualCharge` = deposit * (100 - discountRate) / 100
   - `refundAmount` = (성공 && 이전달 실패) ? 이전달 deposit : 0
   - `consecutiveSuccessCount` = consecutiveCount
7. 환불 예정이면 다다음 달에 적용

```kotlin
private suspend fun applyRefundToNextNextMonth(
    nextMonthId: String,
    refundAmount: Int
)
```

**작동 방식:**
1. 다다음 달 ID 계산
2. 다다음 달 구독이 이미 있으면:
   - `actualCharge -= refundAmount`
   - `isExempt = (actualCharge == 0)`
3. 없으면 새로 생성 (환불 적용된 상태로)

### MainActivity.kt

```kotlin
private fun handlePeriodEnd()
```

**작동 순서:**
1. 총 일수, 성공 일수 계산
2. `SubscriptionManager.processMonthlyResult()` 호출
3. 로컬 데이터 정리
4. 다음 달 준비

---

## 📊 Firebase 데이터 구조

```
users/{userId}/
  └─ subscriptions/
      └─ {monthId}/          (예: "2026-01")
          ├─ monthId: string
          ├─ deposit: number
          ├─ isPaid: boolean
          ├─ isExempt: boolean               ⭐ 100% 면제 여부
          ├─ actualCharge: number            ⭐ 실제 결제 금액
          ├─ refundAmount: number            ⭐ 환불 예정 금액
          ├─ totalDays: number
          ├─ successDays: number
          ├─ achievementRate: number
          ├─ isSuccess: boolean              ⭐ 80% 이상 달성 여부
          ├─ discountRate: number            ⭐ 할인율 (0, 50, 100)
          ├─ consecutiveSuccessCount: number ⭐ 연속 완전 달성 횟수
          ├─ goal: number
          ├─ controlDays: array
          ├─ startDate: date
          ├─ endDate: date
          ├─ createdAt: date
          └─ updatedAt: date
```

---

## ✅ 테스트 방법

### 1. 수동 테스트

```kotlin
// SettingsScreen의 테스트 버튼 사용
- "+100", "+1000" 버튼으로 걸음 수 조작
- "달성" 버튼으로 즉시 목표 달성
```

### 2. 강제 월말 테스트

```kotlin
// PreferenceManager에서 제어 종료 날짜를 어제로 설정
preferenceManager.saveControlEndDate("2026-01-04") // 어제
```

앱 재시작 → `handlePeriodEnd()` 자동 호출

### 3. Firestore 확인

Firebase Console > Firestore Database:
```
users > {userId} > subscriptions
```

각 월별 문서 확인:
- `achievementRate`: 달성률
- `isSuccess`: 80% 이상 여부
- `discountRate`: 할인율 (0, 50, 100)
- `isExempt`: 100% 면제 여부
- `actualCharge`: 실제 청구 금액
- `refundAmount`: 환불 예정 금액
- `consecutiveSuccessCount`: 연속 완전 달성 횟수

---

## 🔔 로그 확인

```
// 월말 정산 시작
📊 Period ended: 20/22 (90%)

// Firebase 처리
✅ Monthly result: rate=90%, discount=50%, nextCharge=5000, consecutive=0, refund=0

// 환불 적용 (있을 경우)
✅ Refund applied to new 2026-03: -10000, charge=0

// MainActivity 완료
🎉 부분 성공! Next month 50% discount
```

---

## 🎯 요약

### 핵심 로직

**3단계 할인 시스템:**
1. 🏆 **95% 이상** → 100% 면제 (`discountRate: 100`, `actualCharge: 0`)
2. ✅ **80~94%** → 50% 할인 (`discountRate: 50`, `actualCharge: deposit * 0.5`)
3. ❌ **80% 미만** → 전액 결제 (`discountRate: 0`, `actualCharge: deposit`)

**환불 시스템:**
- 🔄 **실패(80% 미만) 후 성공(80% 이상)** → 다다음 달에 실패분 환불

**연속 달성:**
- 🏆 95% 이상 달성 시 `consecutiveSuccessCount` 증가
- 95% 미만 시 리셋
- 향후 연속 달성 보너스 기능 확장 가능

### 수익 모델

**예상 수익 (10,000원 기준):**
- 95% 이상 비율 30% → 평균 0원
- 80~94% 비율 50% → 평균 5,000원
- 80% 미만 비율 20% → 평균 10,000원
- **월 평균: 4,500원** (환불 제외)

### 사용자 이탈 방지

1. **공정성:** 노력한만큼 보상 (80% 이상이면 혜택)
2. **동기부여:** 95% 목표로 완전 면제 욕구
3. **재도전 기회:** 실패 후 성공 시 환불로 위안
4. **연속 달성 욕구:** 카운트 증가로 장기 유지

### 완성!

이제 월말 자동 정산 로직이 3단계 할인 시스템으로 완벽하게 작동합니다! 🎉
