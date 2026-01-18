# Stand 앱 - 보증금 기반 구독 결제 시스템

## 📱 프로젝트 개요

**Stand**는 걷기 목표 달성을 통해 스마트폰 사용을 관리하는 습관 형성 앱입니다.
보증금 기반 구독 결제 시스템을 통해 사용자의 동기 부여를 강화합니다.

---

## 💰 결제 모델

### 핵심 개념
- **보증금**: 1,000원 ~ 1,000,000원 (사용자 선택)
- **결제 주기**: 매월 1회 자동 결제
- **성공 조건**: 한 달 동안 걷기 목표 90% 이상 달성
- **보상**: 성공 시 다음달 결제 면제 + 직전달 실패분 환불

### 시나리오 예시

#### 시나리오 1: 연속 성공
```
1월: 성공 (90% 달성) → 10,000원 결제
2월: 면제 (1월 성공 보상) → 0원
3월: 면제 (2월 성공 보상) → 0원
→ 계속 성공하면 무료 사용!
```

#### 시나리오 2: 실패 후 성공
```
1월: 실패 (80% 달성) → 10,000원 결제
2월: 성공 (95% 달성) → 10,000원 결제
3월: 면제 (2월 성공) → 0원
4월: 1월 실패분 환불 → 0원 (10,000원 - 10,000원)
```

#### 시나리오 3: 연속 실패 후 성공
```
1월: 실패 → 10,000원 결제
2월: 실패 → 10,000원 결제
3월: 성공 → 10,000원 결제
4월: 면제 (3월 성공) → 0원
5월: 2월 실패분만 환불 → 0원 (10,000원 - 10,000원)
```

---

## 🏗️ 시스템 아키텍처

```
┌─────────────────────┐
│   Android App       │
│  (Kotlin/Compose)   │
└──────────┬──────────┘
           │
           ├─────────────────────┐
           │                     │
           ▼                     ▼
┌──────────────────┐   ┌─────────────────┐
│ Firebase Auth    │   │ Google Play     │
│ (익명 로그인)    │   │ Billing         │
└──────────────────┘   └────────┬────────┘
                                │
           ┌────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│      Firebase Firestore          │
│  - users/{userId}                │
│    ├─ subscriptions/{monthId}    │
│    └─ dailyRecords/{dateId}      │
└──────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│    Firebase Functions            │
│  - verifyPurchase()              │
│  - processMonthlyResults()       │
└──────────────────────────────────┘
```

---

## 📂 프로젝트 구조

### 새로 추가된 파일

```
WalkorWait/
├─ app/
│  ├─ google-services.json          ← Firebase 설정 파일 (추가 필요)
│  └─ src/main/java/.../
│     ├─ BillingManager.kt          ✅ 결제 처리
│     └─ SubscriptionManager.kt     ✅ 구독 로직
│
├─ SETUP_GUIDE.md                   ✅ 설정 가이드
├─ FIREBASE_STRUCTURE.md            ✅ Firestore 데이터 구조
└─ README_PAYMENT.md                ✅ 이 파일
```

### 주요 클래스

#### 1. BillingManager.kt
Google Play Billing 라이브러리를 사용한 결제 처리
- 구독 상품 조회
- 결제 플로우 시작
- 구매 확인 (Acknowledge)
- 활성 구독 확인

#### 2. SubscriptionManager.kt
Firebase Firestore와 통합된 구독 로직
- 구독 생성 및 저장
- 월별 성공/실패 판정
- 다음달 면제/차감 계산
- 일일 걸음 수 기록

#### 3. DepositSettingScreen.kt (수정 예정)
보증금 설정 및 결제 UI
- 보증금 금액 선택 (슬라이더)
- 제어 요일 선택
- 결제 버튼 → BillingManager 호출

---

## 🔧 기술 스택

### Android
- **언어**: Kotlin
- **UI**: Jetpack Compose
- **결제**: Google Play Billing Library 7.1.1
- **백엔드**: Firebase
  - Authentication (익명 로그인)
  - Firestore Database
  - Cloud Functions

### 의존성
```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Google Play Billing
implementation("com.android.billingclient:billing-ktx:7.1.1")
```

---

## 🚀 시작하기

### 1. 설정 완료
[SETUP_GUIDE.md](SETUP_GUIDE.md)를 참고하여 다음을 완료하세요:
- [ ] Firebase 프로젝트 생성
- [ ] google-services.json 추가
- [ ] Firebase Authentication 활성화
- [ ] Firestore Database 생성
- [ ] Google Play Console 구독 상품 생성
- [ ] Firebase Functions 배포

### 2. 빌드 및 실행
```bash
# Android Studio에서
1. Sync Project with Gradle Files
2. Build > Make Project
3. Run 'app'
```

### 3. 테스트
- Internal Testing 트랙으로 배포
- 테스트 계정으로 로그인
- 보증금 설정 및 결제 테스트

---

## 📊 Firestore 데이터 구조

자세한 내용은 [FIREBASE_STRUCTURE.md](FIREBASE_STRUCTURE.md) 참고

### users/{userId}/subscriptions/{monthId}
```json
{
  "monthId": "2026-01",
  "deposit": 10000,
  "isPaid": true,
  "isExempt": false,
  "actualCharge": 10000,
  "refundAmount": 0,
  "totalDays": 20,
  "successDays": 18,
  "achievementRate": 90.0,
  "isSuccess": true
}
```

---

## 🔐 보안

### Firestore 보안 규칙
- 사용자는 본인 데이터만 읽기/쓰기 가능
- 구독 데이터는 Firebase Functions에서만 쓰기 가능
- 영수증 검증은 서버(Functions)에서 처리

### 결제 검증
- Google Play Developer API로 영수증 검증
- 클라이언트에서는 검증 결과만 수신
- 부정 사용 방지

---

## 📈 향후 개선 사항

### Phase 1: 기본 기능 (현재)
- [x] Firebase & Billing 연동
- [x] 구독 생성 및 결제
- [ ] 월말 자동 판정
- [ ] 면제/차감 로직 구현
- [ ] UI 통합

### Phase 2: 고도화
- [ ] 다양한 보증금 옵션 지원
- [ ] 실시간 구독 상태 동기화
- [ ] 환불 자동화
- [ ] 통계 대시보드

### Phase 3: 사용자 경험
- [ ] 푸시 알림 (월말 알림, 성공/실패 알림)
- [ ] 성취 배지 시스템
- [ ] 친구 초대 및 리더보드

---

## ⚠️ 주의사항

### 결제 관련
1. **테스트 환경에서만 테스트하세요**
   - 프로덕션 출시 전까지는 Internal Testing 사용
   - 테스트 계정으로만 결제

2. **실제 청구 방지**
   - Play Console에서 라이선스 테스터 등록 필수
   - 테스트 구매는 자동으로 취소됨

3. **구독 상품 ID 일치**
   - `BillingManager.SUBSCRIPTION_PRODUCT_ID` = Play Console 상품 ID
   - 불일치 시 결제 실패

### Firebase 관련
1. **google-services.json 보안**
   - `.gitignore`에 추가 (민감 정보 포함)
   - GitHub 등에 업로드 금지

2. **Firestore 보안 규칙**
   - 프로덕션 모드로 시작
   - 보안 규칙 반드시 설정

---

## 🆘 문제 해결

문제가 발생하면:
1. [SETUP_GUIDE.md](SETUP_GUIDE.md)의 "문제 해결" 섹션 확인
2. Logcat에서 오류 메시지 확인
3. Firebase Console에서 데이터 확인

---

## 📄 라이선스

이 프로젝트는 교육 및 개발 목적으로 작성되었습니다.

---

## 👨‍💻 개발자

질문이나 피드백이 있으시면 연락주세요!
