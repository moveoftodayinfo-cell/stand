# WalkorWait (rebon) - 릴리즈 팀 종합 평가 보고서

> **평가 일자**: 2026-02-14
> **버전**: 1.0.73 (versionCode 73)
> **평가 팀**: UI/UX 디자이너, 기획자, QA, CTO, 마케터, Google Analytics, Dashboard, Firebase Backend

---

## Executive Summary

| 역할 | 점수 | 상태 | 핵심 이슈 |
|------|------|------|----------|
| **UI/UX** | 7/10 | 조건부 승인 | 접근성 Content Description 없음, 색상 대비 미달 |
| **기획자** | 75/100 | 조건부 승인 | 온보딩 17단계→5단계, 구독 유도 타이밍 불명확 |
| **QA** | N/A | **NOT READY** | Critical 3개, High 7개, 테스트 커버리지 3% |
| **CTO** | 5.1/10 | 조건부 진행 | 거대 파일(4,181줄), 테스트 3%→50% 필요 |
| **마케터** | 8/10 | GO | 공유 시스템 우수, 프로모 코드 준비됨 |
| **Google Analytics** | 7.5/10 | 조건부 승인 | 이벤트 구조 우수, Funnel 누락, Cohort 분석 필요 |
| **Dashboard** | 6/10 | 조건부 승인 | 관리자 콘솔 존재, 실시간 KPI 모니터링 부재 |
| **Firebase Backend** | 6.5/10 | 조건부 승인 | 보안 규칙 개선됨, 인덱싱/쿼리 최적화 필요 |

### GO/NO-GO 결정

```
┌─────────────────────────────────────────────────┐
│  결정: 🟡 조건부 진행 (Conditional GO)          │
├─────────────────────────────────────────────────┤
│  필수 조건:                                     │
│  1. QA Critical 이슈 3개 해결                   │
│  2. 테스트 커버리지 3% → 최소 50%              │
│  3. 접근성 Content Description 추가            │
│  4. Health Connect alpha → 정식 버전           │
└─────────────────────────────────────────────────┘
```

---

## 1. UI/UX 디자이너 평가 (7/10)

### 강점

- **디자인 토큰 시스템 우수**: DesignSystem.kt, MockupColors 체계적 관리
- **레트로 다마고치 미학 일관성**: Kenney Pixel 폰트, 픽셀 아트 스타일
- **애니메이션/피드백 훌륭**: Spring animation, HapticManager 통합
- **펫 상호작용 매력적**: 터치 감정 표현, 시간대별 인사

### Critical 이슈 (P0)

| 이슈 | 현황 | 해결 방안 |
|------|------|----------|
| **Content Description** | 전체 앱에서 없음 | 모든 IconButton에 contentDescription 추가 |
| **색상 대비 (WCAG AA)** | TextPrimary #333333 미달 | #000000 또는 #1A1A1A로 변경 |
| **터치 타겟 크기** | 44dp (권장 48dp) | StandSize.iconButtonSize 48dp로 |

### 화면별 파일 크기 문제

| 파일 | 라인 | 권장 액션 |
|------|------|----------|
| SettingsScreen.kt | 3,630 | 5개 파일로 분리 |
| PetTutorialScreen.kt | 4,181 | 라우터 패턴 적용 |
| TutorialScreen.kt | 2,722 | 단계별 분리 |

---

## 2. 기획자 평가 (75/100)

### 핵심 가치 제안 분석

**강점:**
- 이중 가치 제안: 건강한 생활 + AI 펫 육성
- 저가격 접근성: 월 3,900원 (스타벅스 아메리카노 수준)
- 명확한 타겟: 운동 습관을 기르고 싶은 직장인/학생

### Critical 이슈 (P1)

| 이슈 | 현황 | 해결 방안 |
|------|------|----------|
| **온보딩 길이** | 17단계 (너무 김) | 필수 5단계 + 선택적 in-app 튜토리얼 |
| **구독 유도 타이밍** | 불명확 | 튜토리얼 완료 직후 또는 Day-3 명시 |
| **무료 체험 기간** | 7일 + 프로모 30일 | 초기 체험 3일로 단축 |
| **성인 펫 동기 부족** | 레벨 21+ 진행 느림 | 진화 분기, 특별 이벤트 추가 |

### 수익화 구조 분석

```
현재 구조:
├─ 월간 구독: 3,900원
├─ 연간 구독: 39,000원 (2개월 무료)
├─ 7일 무료 체험
├─ 프로모 코드: 30일 무료 (친구 초대, 이벤트)
└─ 펫 변경: 1,000원 (일회성)
```

**권장 개선:**
- 무료 체험: 7일 → 3일
- 펫 스킨 미시 결제: 80~400원대 추가
- Host-Guest 공동 도전 기능

### 리텐션 메커니즘

| 시스템 | 상태 | 개선 필요 |
|--------|------|----------|
| 스트릭 시스템 | ✅ 우수 | 리셋 메커니즘 명확화 |
| 펫 성장 시스템 | ✅ 우수 | 성인 단계 추가 활동 |
| 월별 보상 | ⚠️ 코드만 | UI에서 시각화 필요 |
| 친구 초대 | ⚠️ 약함 | Duo Challenge 추가 |

---

## 3. QA 평가 (NOT READY)

### Critical 이슈 (3개)

#### 1. HealthConnect Null Safety
```kotlin
// HealthConnectManager.kt:45-54
// 문제: healthConnectClient lazy 초기화 실패 시 crash
// 해결: 명시적 error state 추가, retry logic 구현
```

#### 2. Non-null Assertions (!!.) 11개
```
- MainActivity.kt: updateInfo!!, announcement!!, currentChallengeProgress!!, skinToShow!!
- PetMainScreen.kt, PetSetupScreensV2.kt: selectedPet!!, selectedPetType!!
- SettingsPetScreen.kt: lockedSkinInfo!!
```
**해결:** `?.let { }` 또는 `requireNotNull()` 사용

#### 3. Consume Purchase Race Condition
```kotlin
// BillingManager.kt:534-604
// 문제: onPurchaseSuccess() 먼저 호출 후 consume 재시도
// 결과: 사용자 서비스 받았지만 consume 미완료 → 무한 루프 위험
// 해결: consume 성공 후에만 onPurchaseSuccess() 호출
```

### High 이슈 (7개)

| 이슈 | 파일 | 영향 |
|------|------|------|
| Firebase Sync Timeout | UserDataRepository.kt:131-136 | 로딩 무한 대기 |
| CoroutineScope Leak | BillingManager.kt | 메모리 누수, 배터리 |
| StepCounterService Lifecycle | StepCounterService.kt:60-92 | NPE crash |
| Firebase Data Sync | UserDataRepository.kt:150-299 | 데이터 손실 |
| Network Error Recovery | BillingManager.kt:222-242 | 구독 실패 |
| Foreground Service Type | StepCounterService.kt:45 | Android 12+ crash |
| Test Coverage | 전체 | 3% (목표 80%) |

### 보안 취약점

#### Firestore Rules Bypass (CRITICAL)
```javascript
// firestore.rules:24-28
match /promoCodes/{code} {
  allow update: if request.auth != null;  // 🔴 모든 인증 사용자가 프로모 코드 수정 가능
}
```
**해결:**
```javascript
allow update: if isAdmin() || (request.auth.uid == request.resource.data.userId);
```

#### Admin Email Hardcoded
```javascript
// firestore.rules:8
request.auth.token.email == 'moveoftoday.info@gmail.com'
```
**해결:** Firebase custom claims 사용 (`request.auth.token.admin == true`)

### 테스트 현황

| 항목 | 현재 | 목표 |
|------|------|------|
| 테스트 파일 | 3개 | 30개+ |
| 커버리지 | ~3% | 80% |
| 테스트되지 않은 핵심 파일 | BillingManager, UserDataRepository, MainActivity | 전체 테스트 |

### 우선 테스트 대상

1. **BillingManager** (630줄) - 결제 플로우
2. **UserDataRepository** (1,061줄) - Firebase 동기화
3. **HealthConnectManager** - 권한 거부 시나리오
4. **StepCounterService** - 라이프사이클
5. **ChallengeManager** - 목표 달성 판정

---

## 4. CTO 평가 (5.1/10)

### 정량적 분석

| 항목 | 수치 |
|------|------|
| 총 코드량 | 46,313줄 |
| Kotlin 파일 | 88개 |
| 평균 파일 크기 | ~526줄 |
| Log 문장 | 556개 |
| 테스트 파일 | 3개 |
| 버전 | 1.0.73 |

### 아키텍처 점수

| 영역 | 점수 | 상태 |
|------|------|------|
| 아키텍처 건전성 | 5.5/10 | 우려 수준 |
| 확장성 | 4.0/10 | 제한적 |
| 유지보수성 | 4.5/10 | 악화 추세 |
| 기술 부채 | 3.0/10 | 심각 수준 |
| 인프라 최적화 | 6.5/10 | 개선 가능 |
| 로드맵 준비도 | 5.0/10 | 부분 준비 |

### 거대 파일 문제 (God File)

| 파일 | 라인 | 업계 기준 대비 |
|------|------|----------------|
| PetTutorialScreen.kt | 4,181 | 523% 초과 |
| SettingsScreen.kt | 3,630 | 454% 초과 |
| TutorialScreen.kt | 2,722 | 340% 초과 |
| PreferenceManager.kt | 2,340 | 293% 초과 |
| PetModel.kt | 1,546 | 193% 초과 |

**업계 기준:** 800줄 이하

### ViewModel 부재 문제

- 대부분의 화면에서 ViewModel 없음
- Repository 직접 호출로 테스트 어려움
- 상태 관리 분산

### 릴리즈 블로커

| 항목 | 현황 | 필수 조치 |
|------|------|----------|
| 테스트 커버리지 | 3% | → 50% 이상 |
| Health Connect | alpha 1.1.0 | → 정식 1.0.0 |
| 프로덕션 로깅 | 556개 | 레벨 구분 필요 |
| Crashlytics | 미확인 | 설정 검증 |

### 권장 액션 플랜

**Phase 1 (1주일):** 차단 요소 제거
- 테스트 50% 상향
- Health Connect 버전 업그레이드
- 프로덕션 로깅 레벨 설정

**Phase 2 (1-2주일):** 리스크 경감
- 성능 프로파일링
- 회귀 테스트
- 보안 감사

**Phase 3 (3-4일):** 문서화 & 준비
- 릴리즈 노트 작성
- 롤백 계획 수립
- 모니터링 대시보드 설정

---

## 5. 마케터 평가 (8/10)

### ASO (App Store Optimization) 권장

**키워드:** "걸음수", "펫 키우기", "AI 반려동물", "건강 습관", "보상형 건강앱"

**핵심 메시지:**
```
"매일의 걸음이 펫의 성장이 되는 경험"
"AI 펫과 함께하는 일일 챌린지"
"포켓몬스터를 현실에서 경험하세요"
```

### 타겟 오디언스

| 타겟 | 비중 | 핵심 메시지 |
|------|------|------------|
| 건강 의식 있는 20-40대 여성 | 48% | "작은 걸음이 큰 변화를" |
| 게임 경험자 + 운동 좋아하는 사람 | 32% | "포켓몬GO 다음은 rebon" |
| 직장인/학생 | 20% | "집중 시간이 반려동물 성장 시간" |

### 공유 시스템 분석

**강점:**
- ✅ 2가지 공유 형식 (Full Card + Transparent Sticker)
- ✅ Instagram Story용 9:16 비율
- ✅ 펫 스프라이트 + 성취 통계 시각화
- ✅ Viral Coefficient 높음

**개선 필요:**
- ⚠️ 친구 초대 딥링크 없음
- ⚠️ 공유 인센티브 부족
- ⚠️ 텍스트 자동 캡션 없음

### 권장 바이럴 전략

**1단계 (즉시):**
- 친구 초대 딥링크 구현
- 공유 후 축하 메시지 추가

**2단계 (1-2주):**
- 주간 챌린지: "3명 초대 시 펫 스킨 해금"
- 공유 레벨링: 5회 공유 → 배지 획득

**3단계 (1개월 후):**
- 팀 챌린지: "5명 7일 연속 달성 시 특별 스킨"
- #rebon 해시태그 캠페인

### 마케팅 채널 계획

| Phase | 채널 | 예산 | 목표 |
|-------|------|------|------|
| 론칭 전 | 인플루언서 | $500 | 300명 Early Access |
| 론칭 당일 | TikTok/YouTube Short | $500 | 50,000 impressions |
| 론칭 1주 | Google App Campaigns | $1,000 | 500-1,000 설치 |
| 론칭 4주 | Push/Email 캠페인 | N/A | Day-7 리텐션 35% |

### 경쟁사 대비 차별화

| vs | rebon 우위 |
|-----|-----------|
| Pokemon Go | 실내 가능, 저전력 |
| Apple Health | AI 펫 육성으로 감정 연결 |
| Strava | 심플하고 귀여운 디자인 |
| Google Fit | 펫 + 공유 문화 |

---

## 6. Google Analytics 팀 평가 (7.5/10)

### 이벤트 추적 현황

**AnalyticsManager.kt 분석 (270줄)**

| 카테고리 | 이벤트 | 상태 |
|----------|--------|------|
| 온보딩 | tutorial_begin, tutorial_step, tutorial_exit, tutorial_complete | ✅ 완료 |
| 목표 | goal_set, goal_achieved | ✅ 완료 |
| 앱 차단 | app_blocked, app_unlocked, blocked_apps_selected | ✅ 완료 |
| 구독 | subscription_start, promo_code_used, purchase (GA4 표준) | ✅ 완료 |
| 스트릭 | streak_milestone, share (GA4 표준) | ✅ 완료 |
| 친구 초대 | invite_code_generated, invite_code_shared | ✅ 완료 |
| 위젯 | widget_added | ✅ 완료 |
| 설정 | settings_changed | ✅ 완료 |
| 권한 | permission_granted, permission_denied | ✅ 완료 |
| 에러 | app_error | ✅ 완료 |

### 강점

- **체계적인 이벤트 구조**: AnalyticsManager 싱글톤으로 중앙 집중화
- **튜토리얼 단계별 추적**: 16단계 각각 이름 매핑 (pet_selection, goal_input 등)
- **GA4 표준 이벤트 활용**: SCREEN_VIEW, TUTORIAL_COMPLETE, PURCHASE, SHARE
- **사용자 속성 관리**: daily_goal, pet_type, subscription_type
- **광고 ID 비활성화**: GDPR/개인정보 보호 준수

### Critical 이슈 (P1)

| 이슈 | 현황 | 해결 방안 |
|------|------|----------|
| **Funnel 분석 누락** | 튜토리얼 단계 이탈률 추적 가능하나 Funnel 미설정 | GA4 Console에서 Funnel 생성 |
| **Cohort 분석 미지원** | 가입일별 리텐션 추적 어려움 | first_open_time 사용자 속성 추가 |
| **챌린지 이벤트 누락** | 챌린지 시작/완료/포기 추적 없음 | challenge_start, challenge_complete, challenge_abandon 추가 |
| **펫 상호작용 추적 부재** | 펫 터치, 대화, 진화 이벤트 없음 | pet_interaction, pet_evolved 추가 |

### 권장 추가 이벤트

```kotlin
// 챌린지 관련
trackChallengeStart(challengeType: String)
trackChallengeComplete(challengeType: String, duration: Long)
trackChallengeAbandon(challengeType: String, reason: String)

// 펫 상호작용
trackPetInteraction(interactionType: String)  // touch, talk, feed
trackPetEvolved(fromStage: String, toStage: String)
trackPetDialogue(dialogueType: String)

// 리텐션 분석용
trackDailyActive(streakDays: Int, totalDays: Int)
trackWeeklyActive(weekNumber: Int)
```

### 대시보드 설정 권장

**GA4 Console 필수 설정:**

1. **Funnel 보고서**
   - 튜토리얼 완주율: tutorial_begin → tutorial_complete
   - 구독 전환율: tutorial_complete → subscription_start → purchase

2. **Cohort 보고서**
   - 가입일별 Day-1, Day-7, Day-30 리텐션

3. **Real-time 모니터링**
   - 활성 사용자, 화면별 분포, 에러 발생

4. **Custom Dimensions**
   - pet_type, subscription_type, daily_goal

### 예상 KPI 지표

| KPI | 목표 | 측정 방법 |
|-----|------|----------|
| 튜토리얼 완주율 | 60%+ | tutorial_complete / tutorial_begin |
| Day-7 리텐션 | 25%+ | Cohort 분석 |
| 구독 전환율 | 5%+ | subscription_start / tutorial_complete |
| 일일 목표 달성률 | 40%+ | goal_achieved / 일일 활성 |

---

## 7. Dashboard 팀 평가 (6/10)

### 현재 대시보드 현황

**admin/rebon_admin.html 분석**

| 기능 | 상태 | 비고 |
|------|------|------|
| 사용자 목록 조회 | ✅ 존재 | Firestore users 컬렉션 |
| 프로모 코드 관리 | ✅ 존재 | 생성/수정/삭제 |
| 공지사항 관리 | ✅ 존재 | 앱 내 공지 표시 |
| 채팅 로그 조회 | ✅ 존재 | AI 펫 대화 기록 |
| 피드백 조회 | ✅ 존재 | 사용자 피드백 |
| **실시간 KPI** | ❌ 부재 | 별도 구현 필요 |
| **차트/그래프** | ❌ 부재 | 데이터 시각화 없음 |
| **알림 시스템** | ❌ 부재 | 이상 감지 알림 없음 |

### 강점

- **Firebase 직접 연동**: 실시간 Firestore 조회
- **관리자 인증**: moveoftoday.info@gmail.com 이메일 인증
- **기본 CRUD 완비**: 사용자, 프로모코드, 공지사항 관리

### Critical 이슈 (P1)

| 이슈 | 현황 | 해결 방안 |
|------|------|----------|
| **실시간 KPI 부재** | 수동으로 데이터 확인 필요 | 대시보드 상단 KPI 카드 추가 |
| **차트 시각화 없음** | 숫자만 나열됨 | Chart.js/D3.js로 트렌드 그래프 |
| **알림 시스템 없음** | 이상 징후 감지 불가 | Firebase Cloud Functions + Slack 연동 |
| **모바일 미최적화** | 데스크톱만 지원 | 반응형 레이아웃 추가 |

### 권장 대시보드 KPI 카드

```
┌──────────────────────────────────────────────────────────────┐
│  [실시간 KPI 대시보드]                                        │
├──────────┬──────────┬──────────┬──────────┬──────────────────┤
│  DAU     │  WAU     │  MAU     │  구독자   │  매출 (MTD)      │
│  1,234   │  5,678   │  12,345  │  456     │  ₩1,778,200      │
│  ↑12%    │  ↑8%     │  ↑15%    │  ↑5%     │  ↑22%            │
└──────────┴──────────┴──────────┴──────────┴──────────────────┘
```

### 필수 추가 기능

**Phase 1 (즉시):**
1. 상단 KPI 카드 (DAU, WAU, 구독자, 매출)
2. 신규 가입자 일별 차트
3. 튜토리얼 완주율 그래프

**Phase 2 (1-2주):**
1. 구독 전환 Funnel 시각화
2. 챌린지 카테고리별 완료율
3. 펫 종류별 분포

**Phase 3 (1개월):**
1. 이상 징후 자동 알림 (Slack/Email)
2. A/B 테스트 결과 대시보드
3. 코호트 리텐션 히트맵

### 데이터 소스 연동

| 데이터 | 소스 | 갱신 주기 |
|--------|------|----------|
| 사용자 수 | Firestore users | 실시간 |
| 매출 | Google Play Console API | 일별 |
| 앱 성능 | Firebase Crashlytics | 실시간 |
| 마케팅 | Google Ads API | 일별 |
| 이벤트 분석 | GA4 Data API | 시간별 |

---

## 8. Firebase Backend 팀 평가 (6.5/10)

### Firestore 구조 분석

**컬렉션 구조:**

```
firestore/
├── apiConfig/          # API 키 설정 (Claude AI)
├── announcements/      # 공지사항
├── promoCodes/         # 프로모 코드
├── chatLogs/           # AI 펫 대화 로그
├── feedback/           # 사용자 피드백
├── petChangeHistory/   # 펫 교체 이력 (전체)
└── users/{userId}/
    ├── subscriptions/  # 구독 정보
    ├── dailyRecords/   # 일일 기록
    ├── userData/       # 사용자 데이터
    ├── dailySteps/     # 일일 걸음수
    ├── challengeHistory/# 챌린지 이력
    ├── challengeStats/ # 챌린지 통계
    ├── unlockedTitles/ # 획득 칭호
    └── petChanges/     # 펫 교체 이력 (개인)
```

### Security Rules 분석

**firestore.rules 현황:**

| 컬렉션 | Read | Write | 평가 |
|--------|------|-------|------|
| apiConfig | 전체 허용 | Admin만 | ⚠️ API키 노출 위험 |
| announcements | 전체 허용 | Admin만 | ✅ 적절 |
| promoCodes | 전체 허용 | Admin만 | ✅ 개선됨 |
| chatLogs | Admin만 | 인증 사용자 | ✅ 적절 |
| feedback | Admin만 | 전체 허용 | ⚠️ 스팸 위험 |
| users/{userId} | 본인+Admin | 본인만 | ✅ 적절 |

### 강점

- **보안 규칙 체계화**: isAdmin() 헬퍼 함수 사용
- **사용자 데이터 격리**: userId 기반 접근 제어
- **하위 컬렉션 보호**: 모든 서브컬렉션에 규칙 적용
- **친구 초대 필드 제한**: affectedKeys().hasOnly() 사용

### Critical 이슈 (P0)

| 이슈 | 심각도 | 해결 방안 |
|------|--------|----------|
| **apiConfig 전체 공개** | HIGH | 앱 내 API 키 직접 저장 또는 Cloud Functions 사용 |
| **feedback 스팸 취약** | MEDIUM | rate limiting 또는 reCAPTCHA 추가 |
| **Admin 이메일 하드코딩** | MEDIUM | Custom Claims 사용 권장 |

### 인덱싱 최적화 필요

**현재 누락된 인덱스:**

```javascript
// 추천 인덱스 (firestore.indexes.json)
{
  "indexes": [
    {
      "collectionGroup": "dailySteps",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "date", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "challengeHistory",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "completedAt", "order": "DESCENDING" },
        { "fieldPath": "challengeType", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "subscriptions",
      "queryScope": "COLLECTION_GROUP",
      "fields": [
        { "fieldPath": "inviteCode", "order": "ASCENDING" }
      ]
    }
  ]
}
```

### 쿼리 최적화 필요

**UserDataRepository.kt 분석 (165회 Firestore 호출):**

| 이슈 | 위치 | 해결 방안 |
|------|------|----------|
| N+1 쿼리 | 사용자별 서브컬렉션 조회 | 배치 읽기 또는 캐싱 |
| 반복 조회 | 동일 문서 여러 번 읽기 | 메모리 캐시 추가 |
| 전체 문서 로드 | 필요 필드만 선택 필요 | select() 사용 |

### Cloud Functions 권장 추가

**현재 없음 → 추가 필요:**

```javascript
// 권장 Cloud Functions

// 1. 일일 통계 집계 (스케줄)
exports.aggregateDailyStats = functions.pubsub
  .schedule('0 0 * * *')
  .onRun(async (context) => {
    // DAU, 목표 달성률, 구독 전환 집계
  });

// 2. 구독 만료 알림
exports.subscriptionExpiryNotification = functions.pubsub
  .schedule('0 9 * * *')
  .onRun(async (context) => {
    // 만료 7일 전 사용자 FCM 푸시
  });

// 3. 비활성 사용자 윈백
exports.winbackInactiveUsers = functions.pubsub
  .schedule('0 10 * * MON')
  .onRun(async (context) => {
    // 7일 미접속 사용자 푸시
  });

// 4. 이상 징후 감지
exports.detectAnomalies = functions.pubsub
  .schedule('*/30 * * * *')
  .onRun(async (context) => {
    // 비정상 구독, 프로모 코드 남용 감지
  });
```

### 권장 액션 플랜

**즉시 (P0):**
1. apiConfig 접근 제한 (인증 필수 또는 Functions 경유)
2. feedback rate limiting 추가
3. Composite Index 설정

**1-2주 (P1):**
1. Cloud Functions 기본 설정
2. 일일 통계 집계 함수
3. 구독 만료 알림 함수

**1개월 (P2):**
1. 쿼리 최적화 (캐싱, 배치)
2. 비활성 사용자 윈백 함수
3. 이상 징후 감지 시스템

---

## 우선순위별 액션 아이템

### P0: 릴리즈 블로커 (1-2주 내 필수)

| # | 담당 | 액션 | 예상 시간 |
|---|------|------|----------|
| 1 | QA/Dev | !! assertion 제거 (11개) | 4시간 |
| 2 | Dev | Consume purchase race condition 수정 | 6시간 |
| 3 | Dev | Firestore security rules 수정 | 1시간 |
| 4 | Dev | HealthConnect null-safety 추가 | 4시간 |
| 5 | Dev | CoroutineScope lifecycle 관리 | 8시간 |
| 6 | QA | 테스트 커버리지 3% → 50% | 2-3주 |

### P1: 출시 전 해결 (2-4주)

| # | 담당 | 액션 |
|---|------|------|
| 7 | UI/UX | Content Description 전체 추가 |
| 8 | UI/UX | 색상 대비 WCAG AA 달성 |
| 9 | 기획 | 온보딩 17단계 → 5단계 축소 |
| 10 | 기획 | 구독 유도 플로우 명시화 |
| 11 | Dev | Health Connect alpha → 정식 버전 |
| 12 | Dev | 프로덕션 로깅 레벨 구분 |
| 13 | Analytics | 챌린지/펫 상호작용 이벤트 추가 |
| 14 | Analytics | GA4 Funnel 보고서 설정 |
| 15 | Dashboard | 실시간 KPI 카드 추가 |
| 16 | Backend | apiConfig 접근 제한 (인증 필수) |
| 17 | Backend | Firestore Composite Index 설정 |

### P2: 출시 후 개선 (1-3개월)

| # | 담당 | 액션 |
|---|------|------|
| 18 | Dev | PetTutorialScreen 분해 (4,181줄 → 5개 파일) |
| 19 | Dev | ViewModel 계층 추가 |
| 20 | 기획 | 성인 펫 추가 활동 설계 |
| 21 | 마케팅 | 친구 초대 딥링크 구현 |
| 22 | 마케팅 | 팀 챌린지 기능 |
| 23 | Analytics | Cohort 리텐션 분석 설정 |
| 24 | Dashboard | 차트/그래프 시각화 추가 |
| 25 | Dashboard | 이상 징후 Slack 알림 연동 |
| 26 | Backend | Cloud Functions 기본 설정 (집계, 알림) |
| 27 | Backend | 쿼리 최적화 (캐싱, 배치) |

---

## 릴리즈 타임라인 제안

```
Week 1-2: Critical 이슈 해결
├─ !! assertion 제거
├─ Consume race condition 수정
├─ Firestore rules 수정
└─ HealthConnect null-safety

Week 3-4: 테스트 & 검증
├─ 테스트 커버리지 50% 달성
├─ 성능 프로파일링
├─ 보안 감사
└─ 회귀 테스트

Week 5: Soft Launch
├─ 10,000 사용자 대상
├─ Crashlytics 모니터링
└─ 피드백 수집

Week 6: 전체 배포
└─ Play Store 전체 공개
```

---

## 부록: 각 팀원 상세 보고서

### A. UI/UX 디자이너 상세
[위 섹션 1 참조]

### B. 기획자 상세
[위 섹션 2 참조]

### C. QA 상세
[위 섹션 3 참조]

### D. CTO 상세
[위 섹션 4 참조]

### E. 마케터 상세
[위 섹션 5 참조]

### F. Google Analytics 팀 상세
[위 섹션 6 참조]

### G. Dashboard 팀 상세
[위 섹션 7 참조]

### H. Firebase Backend 팀 상세
[위 섹션 8 참조]

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-02-14 | 1.0 | 초기 릴리즈 팀 평가 |
| 2026-02-15 | 1.1 | Google Analytics, Dashboard, Firebase Backend 팀 평가 추가 |

---

*이 문서는 WalkorWait(rebon) 앱 릴리즈 전 종합 평가 보고서입니다.*
*지속적으로 업데이트되며, 팀 논의를 위한 기준 문서로 활용됩니다.*
