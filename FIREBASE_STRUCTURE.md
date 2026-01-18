# Firebase Firestore 데이터 구조 설계

## 컬렉션 구조

### users/{userId}
사용자 기본 정보

```
{
  userId: String,              // Firebase Auth UID
  email: String?,              // 이메일 (선택)
  createdAt: Timestamp,        // 가입일
  currentDeposit: Int,         // 현재 보증금 금액
  isSubscriptionActive: Boolean // 구독 활성화 여부
}
```

### users/{userId}/subscriptions/{monthId}
월별 구독 및 성공/실패 기록

**monthId 형식:** `YYYY-MM` (예: `2026-01`)

```
{
  monthId: String,             // "2026-01"
  deposit: Int,                // 해당 월 보증금 금액

  // 결제 정보
  isPaid: Boolean,             // 결제 완료 여부
  isExempt: Boolean,           // 결제 면제 여부 (이전달 성공 시 true)
  actualCharge: Int,           // 실제 청구 금액 (면제 시 0, 차감 적용된 금액)
  purchaseToken: String?,      // Google Play 구매 토큰
  orderId: String?,            // Google Play 주문 ID

  // 이전달 차감
  refundAmount: Int,           // 다다음달에 차감할 금액 (직전달 실패 시 보증금 금액)

  // 걸음 수 및 성공 여부
  totalDays: Int,              // 제어 요일 총 일수
  successDays: Int,            // 목표 달성 일수
  achievementRate: Float,      // 달성률 (%)
  isSuccess: Boolean,          // 90% 이상 달성 여부

  // 제어 설정
  goal: Int,                   // 일일 걸음 목표
  controlDays: List<Int>,      // 제어 요일 [0=일, 1=월, ..., 6=토]

  // 타임스탬프
  startDate: Timestamp,        // 해당 월 시작일
  endDate: Timestamp,          // 해당 월 종료일
  createdAt: Timestamp,        // 생성일
  updatedAt: Timestamp         // 수정일
}
```

### users/{userId}/dailyRecords/{dateId}
일별 걸음 수 기록

**dateId 형식:** `YYYY-MM-DD` (예: `2026-01-15`)

```
{
  dateId: String,              // "2026-01-15"
  steps: Int,                  // 해당 일의 총 걸음 수
  goal: Int,                   // 목표 걸음 수
  isSuccess: Boolean,          // 목표 달성 여부
  isControlDay: Boolean,       // 제어 요일인지 여부
  date: Timestamp              // 날짜
}
```

## 비즈니스 로직

### 1. 신규 사용자 구독 시작
```
1. 사용자가 보증금 금액 선택 (1,000 ~ 1,000,000원)
2. Google Play Billing으로 결제
3. users/{userId}/subscriptions/2026-01 생성:
   - deposit: 10000
   - isPaid: true
   - isExempt: false
   - actualCharge: 10000
   - refundAmount: 0
```

### 2. 월말 성공/실패 판정
```
매월 말일 23:59:59에:
1. 해당 월의 successDays와 totalDays 계산
2. achievementRate = (successDays / totalDays) * 100
3. isSuccess = achievementRate >= 90
4. 다음달 구독 문서 생성/업데이트
```

### 3. 다음달 결제 처리

#### Case 1: 이번달 성공 (90% 이상)
```
다음달(2026-02) 문서:
- isExempt: true
- actualCharge: 0
- refundAmount: 0

다다음달(2026-03) 문서:
- 직전달(2026-01) 실패했다면:
  - refundAmount: 직전달 deposit (차감할 금액)
```

#### Case 2: 이번달 실패
```
다음달(2026-02) 문서:
- isExempt: false
- actualCharge: deposit (정상 결제)
- refundAmount: deposit (다다음달에 차감할 금액 기록)

Google Play Billing으로 정상 결제 진행
```

#### Case 3: 다다음달 차감 적용
```
예: 2026-01 실패, 2026-02 성공
2026-03 문서:
- actualCharge: deposit - 2026-01.refundAmount
- 만약 actualCharge <= 0이면:
  - isExempt: true
  - actualCharge: 0
```

### 4. 결제 플로우

#### 정기 결제 (매월 1일)
```
1. 지난달 문서에서 isSuccess 확인
2. isSuccess == true:
   - 이번달 결제 면제 (isExempt = true)
3. isSuccess == false:
   - Google Play Billing 구독 자동 갱신
   - actualCharge 계산 (refundAmount 차감 적용)
4. 결제 완료 후 구매 토큰 저장
5. Firebase Functions에서 영수증 검증
```

## 예시 시나리오

### 시나리오 1: 연속 실패 후 성공
```
2026-01:
  - deposit: 10000
  - isPaid: true, isExempt: false, actualCharge: 10000
  - isSuccess: false (실패)
  - refundAmount: 0

2026-02:
  - deposit: 10000
  - isPaid: true, isExempt: false, actualCharge: 10000
  - isSuccess: true (성공!)
  - refundAmount: 10000 (다다음달 차감 예정)

2026-03:
  - deposit: 10000
  - isPaid: false, isExempt: true, actualCharge: 0 (2월 성공으로 면제)
  - refundAmount: 0

2026-04:
  - deposit: 10000
  - actualCharge: 10000 - 10000 = 0 (2월 성공 보상으로 1월 실패분 차감)
  - isPaid: false, isExempt: true
```

### 시나리오 2: 성공 연속
```
2026-01:
  - deposit: 10000
  - isPaid: true, actualCharge: 10000
  - isSuccess: true

2026-02:
  - isExempt: true, actualCharge: 0 (면제)
  - isSuccess: true

2026-03:
  - isExempt: true, actualCharge: 0 (면제)
  - isSuccess: true

→ 계속 무료 사용 가능!
```

## 보안 규칙

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 본인 데이터만 읽기/쓰기 가능
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // 구독 데이터는 서버(Functions)에서만 쓰기 가능
    match /users/{userId}/subscriptions/{monthId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if false; // Functions only
    }
  }
}
```

## Firebase Functions

### 1. verifyPurchase (영수증 검증)
```typescript
// Google Play Developer API로 구매 검증
// 검증 완료 시 Firestore 업데이트
```

### 2. processMonthlyResult (월말 처리)
```typescript
// Scheduled Function (매월 말일 23:59)
// 1. 해당 월 성공/실패 판정
// 2. 다음달 구독 문서 생성
// 3. 결제 면제/차감 계산
```

### 3. handleSubscriptionRenewal (구독 갱신)
```typescript
// Google Play Realtime Developer Notifications
// 구독 갱신/취소/환불 처리
```
