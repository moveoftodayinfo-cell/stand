# Stand 앱 UI/UX 디자인 가이드

> 이 가이드는 Stand 앱의 일관된 UI/UX를 유지하기 위한 규칙입니다.
> 모든 신규 기능 및 수정 사항은 이 가이드를 준수해야 합니다.

---

## 목차
1. [디자인 원칙](#1-디자인-원칙)
2. [색상 시스템](#2-색상-시스템)
3. [타이포그래피](#3-타이포그래피)
4. [간격 시스템](#4-간격-시스템)
5. [컴포넌트 가이드](#5-컴포넌트-가이드)
6. [애니메이션](#6-애니메이션)
7. [레이아웃 패턴](#7-레이아웃-패턴)
8. [코드 컨벤션](#8-코드-컨벤션)

---

## 1. 디자인 원칙

### 핵심 가치
| 원칙 | 설명 |
|------|------|
| **일관성** | 동일한 요소는 항상 동일하게 표현 |
| **계층성** | 크기, 색상, 간격으로 정보 우선순위 전달 |
| **접근성** | 충분한 대비, 최소 터치 영역 56dp |
| **피드백** | 모든 인터랙션에 시각적 반응 제공 |

### 금지 사항
- 새로운 색상 임의 추가 금지 (반드시 `StandColors` 사용)
- 하드코딩된 dp/sp 값 사용 금지 (반드시 `StandSpacing`/`StandTypography` 사용)
- 커스텀 버튼 직접 구현 금지 (기존 컴포넌트 재사용)

---

## 2. 색상 시스템

### 2.1 주요 색상 (StandColors)

```kotlin
// 파일: ui/theme/Color.kt

// Primary - 주요 액션, 강조
StandColors.Primary          // #6200EE (자주색)
StandColors.PrimaryLight     // Primary 10% 투명도
StandColors.PrimaryMedium    // Primary 15% 투명도

// 상태 색상
StandColors.Success          // #4CAF50 (녹색) - 성공, 달성
StandColors.Warning          // #FF9800 (주황) - 경고
StandColors.Error            // #FF5722 (빨강) - 오류, 위험

// 배경
StandColors.CardBackground   // #F5F5F5 (밝은 회색)
StandColors.DarkBackground   // #1A1A2E (어두운 배경)

// 텍스트
StandColors.TextPrimary      // Black
StandColors.TextSecondary    // Gray
StandColors.TextOnDark       // White
```

### 2.2 프리미엄 색상 (PremiumColors)

```kotlin
// 파일: ui/components/PremiumComponents.kt

// 그라데이션
PremiumColors.TealPrimary    // #00BFA5 (밝은 틸)
PremiumColors.TealDark       // #008E76 (어두운 틸)
PremiumColors.NavyDark       // #0D1B2A (진한 파랑)

// Glow 효과
PremiumColors.GlowGold       // #FFD700 (금색)
PremiumColors.GlowAmber      // #FFC107 (앰버)
```

### 2.3 색상 사용 규칙

| 상황 | 사용할 색상 |
|------|------------|
| 주요 버튼 | `StandColors.Primary` |
| 위험 버튼 (삭제/취소) | `StandColors.Error` |
| 목표 달성 표시 | `StandColors.Success` |
| 경고 메시지 | `StandColors.Warning` |
| 설정 카드 배경 | `StandColors.CardBackground` |
| 프리미엄/튜토리얼 배경 | `PremiumGradientBackground` |
| 달성 시 빛 효과 | `PremiumColors.GlowGold` |

### 2.4 투명도 규칙 (Alpha)

```kotlin
Alpha.CARD_BACKGROUND = 0.1f   // 카드 배경
Alpha.SELECTED = 0.15f         // 선택 상태
Alpha.OVERLAY = 0.2f           // 오버레이
Alpha.DISABLED = 0.38f         // 비활성화
```

---

## 3. 타이포그래피

### 3.1 크기 계층 (StandTypography)

```kotlin
// 파일: ui/theme/Type.kt

// Display (48-72sp) - 강조 숫자, 이모지
displayHero    = 72.sp    // 튜토리얼 대형 이모지
displayLarge   = 64.sp    // 대형 이모지
displayMedium  = 56.sp    // 메인 걸음 수
displaySmall   = 48.sp    // 카드 내 큰 숫자

// Headline (24-32sp) - 화면 제목
headlineLarge  = 32.sp    // 화면 대제목
headlineMedium = 28.sp    // 튜토리얼 단계 제목
headlineSmall  = 24.sp    // 다이얼로그 제목

// Title (18-22sp) - 섹션 제목
titleLarge     = 22.sp    // 큰 카드 제목
titleMedium    = 20.sp    // 섹션 제목
titleSmall     = 18.sp    // 버튼 Bold 텍스트

// Body (13-16sp) - 본문
bodyLarge      = 16.sp    // 주요 본문, 버튼
bodyMedium     = 14.sp    // 일반 본문
bodySmall      = 13.sp    // 보조 텍스트

// Label (11-12sp) - 캡션
labelLarge     = 12.sp    // 캡션, 날짜
labelMedium    = 11.sp    // 힌트
```

### 3.2 타이포그래피 사용 예시

| 요소 | 크기 | 굵기 | 코드 예시 |
|------|------|------|----------|
| 메인 걸음 수 | 56sp | Bold | `fontSize = StandTypography.displayMedium` |
| 화면 제목 | 32sp | Bold | `fontSize = StandTypography.headlineLarge` |
| 섹션 헤더 | 20sp | Bold | `fontSize = StandTypography.titleMedium` |
| 버튼 텍스트 | 16sp | Bold | `fontSize = StandTypography.bodyLarge` |
| 설명 텍스트 | 14sp | Normal | `fontSize = StandTypography.bodyMedium` |
| 날짜/캡션 | 12sp | Normal | `fontSize = StandTypography.labelLarge` |

### 3.3 굵기 (StandFontWeight)

```kotlin
StandFontWeight.Light     // 300
StandFontWeight.Normal    // 400 - 본문
StandFontWeight.Medium    // 500
StandFontWeight.SemiBold  // 600
StandFontWeight.Bold      // 700 - 제목, 버튼
```

---

## 4. 간격 시스템

### 4.1 기본 간격 (4dp 기반)

```kotlin
// 파일: ui/theme/Spacing.kt

StandSpacing.none  = 0.dp
StandSpacing.xs    = 4.dp     // 텍스트 간격
StandSpacing.sm    = 8.dp     // 아이템 간격
StandSpacing.md    = 12.dp    // 리스트 아이템
StandSpacing.lg    = 16.dp    // 기본 패딩
StandSpacing.xl    = 20.dp    // 큰 카드 패딩
StandSpacing.xxl   = 24.dp    // 섹션 간격
StandSpacing.xxxl  = 32.dp    // 대형 간격
```

### 4.2 컴포넌트별 간격

```kotlin
// 패딩
StandSpacing.cardPadding      = 16.dp   // 카드 내부
StandSpacing.screenPadding    = 16.dp   // 화면 좌우
StandSpacing.buttonPadding    = 16.dp   // 버튼 내부

// 간격
StandSpacing.sectionGap       = 24.dp   // 섹션 사이
StandSpacing.itemGap          = 8.dp    // 아이템 사이
StandSpacing.textGap          = 4.dp    // 텍스트 사이
StandSpacing.iconGap          = 8.dp    // 아이콘-텍스트
```

### 4.3 크기 (StandSize)

```kotlin
// 버튼 높이
StandSize.buttonHeight        = 56.dp   // 기본
StandSize.buttonHeightSmall   = 48.dp   // 작은
StandSize.buttonHeightMini    = 36.dp   // 미니

// 아이콘
StandSize.iconSmall           = 16.dp
StandSize.iconMedium          = 24.dp
StandSize.iconLarge           = 36.dp
StandSize.iconXLarge          = 48.dp

// 모서리 라운드
StandSize.cardCornerRadius    = 12.dp   // 기본
StandSize.cardCornerRadiusLarge = 16.dp // 큰 카드
```

---

## 5. 컴포넌트 가이드

### 5.1 버튼

#### PrimaryButton (주요 버튼)
```kotlin
// 용도: 주요 액션 (다음, 저장, 확인)
PrimaryButton(
    text = "다음",
    onClick = { /* action */ },
    enabled = true
)
```
- 배경: Primary (#6200EE)
- 텍스트: White, Bold, 16sp
- 높이: 56dp
- 모서리: 12dp

#### DangerButton (위험 버튼)
```kotlin
// 용도: 위험 액션 (삭제, 취소, 초기화)
DangerButton(
    text = "삭제",
    onClick = { /* action */ }
)
```
- 배경: Error (#FF5722)
- 텍스트: White, Bold, 16sp

#### SecondaryButton (보조 버튼)
```kotlin
// 용도: 보조 액션 (건너뛰기, 나중에)
SecondaryButton(
    text = "건너뛰기",
    onClick = { /* action */ }
)
```
- 스타일: OutlinedButton
- 테두리: Primary
- 텍스트: Primary

### 5.2 카드

#### StatusCard (상태 카드)
```kotlin
// 용도: 상태별 정보 표시
StatusCard(statusType = StatusType.SUCCESS) {
    Text("목표 달성!")
}

// StatusType: SUCCESS, WARNING, ERROR, PRIMARY
```

#### ProgressCard (진행률 카드)
```kotlin
// 용도: 진행 상황 표시
ProgressCard(
    title = "오늘의 걸음",
    currentValue = "5,234",
    progress = 0.65f,
    statusType = StatusType.PRIMARY,
    subtitle = "목표: 8,000걸음"
)
```

#### SettingItem (설정 아이템)
```kotlin
// 용도: 설정 화면 항목
SettingItem(
    title = "목표 걸음 수",
    subtitle = "8,000걸음",
    onClick = { /* navigate */ }
)
```

### 5.3 배너

#### WarningBanner (경고 배너)
```kotlin
// 용도: 중요 경고 표시
WarningBanner(
    title = "접근성 서비스 비활성화",
    description = "서비스를 활성화해주세요",
    onClick = { /* action */ }
)
```

### 5.4 빈 상태

#### EmptyState
```kotlin
// 용도: 데이터 없음 표시
EmptyState(
    icon = "📊",
    title = "데이터가 없습니다",
    description = "걸음 데이터가 아직 없습니다"
)
```

### 5.5 헤더

#### SectionHeader
```kotlin
// 용도: 섹션 구분
SectionHeader(
    title = "통계",
    subtitle = "최근 7일"
)
```

---

## 6. 애니메이션

### 6.1 애니메이션 사용 기준

| 상황 | 애니메이션 | 파일 |
|------|-----------|------|
| 목표 달성 | `LightOnAnimation` | StandAnimations.kt |
| 순간 강조 | `SparkleAnimation` | StandAnimations.kt |
| 진행률 90%+ | Pulse (scale + alpha) | PremiumComponents.kt |
| 숫자 변경 | Count-up animation | PremiumComponents.kt |

### 6.2 LightOnAnimation (불빛 효과)
```kotlin
// 목표 달성 시 빛나는 효과
LightOnAnimation(
    isActive = goalAchieved
)
```
- 주기: 1500ms 무한 반복
- 효과: Alpha (0.3→1→0.3), Scale (0.95→1.05)

### 6.3 CircularProgressWithGlow
```kotlin
// 원형 프로그레스 + Glow
CircularProgressWithGlow(
    progress = 0.85f,
    currentValue = 6800,
    targetValue = 8000,
    unit = "걸음"
)
```
- 90% 이상: Pulse 애니메이션 자동 적용
- Glow 강도: 진행률에 비례

---

## 7. 레이아웃 패턴

### 7.1 화면 기본 구조

```kotlin
@Composable
fun ExampleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(StandSpacing.screenPadding)
    ) {
        // 섹션 1
        SectionHeader(title = "섹션 제목")
        Spacer(modifier = Modifier.height(StandSpacing.itemGap))
        // 컨텐츠...

        Spacer(modifier = Modifier.height(StandSpacing.sectionGap))

        // 섹션 2
        SectionHeader(title = "다음 섹션")
        // 컨텐츠...
    }
}
```

### 7.2 프리미엄 레이아웃 (튜토리얼용)

```kotlin
@Composable
fun TutorialStep() {
    PremiumLayout(
        topContent = {
            // 70% - 그라데이션 영역
            // 이모지, 제목 등
        },
        bottomContent = {
            // 30% - 바텀시트 영역
            // 버튼, 설명 등
        }
    )
}
```

### 7.3 설정 화면 패턴

```kotlin
Column {
    // 경고 배너 (필요시)
    WarningBanner(...)

    Spacer(modifier = Modifier.height(StandSpacing.sectionGap))

    // 설정 항목들
    SettingItem(title = "항목 1", ...)
    Spacer(modifier = Modifier.height(StandSpacing.itemGap))
    SettingItem(title = "항목 2", ...)

    Spacer(modifier = Modifier.height(StandSpacing.sectionGap))

    // 액션 버튼
    PrimaryButton(text = "저장", ...)
}
```

---

## 8. 코드 컨벤션

### 8.1 Import 순서

```kotlin
// 1. Android/Compose
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.ui.*

// 2. 프로젝트 테마
import com.moveoftoday.walkorwait.ui.theme.*

// 3. 프로젝트 컴포넌트
import com.moveoftoday.walkorwait.ui.components.*
```

### 8.2 Composable 함수 구조

```kotlin
@Composable
fun MyComponent(
    // 필수 파라미터
    title: String,
    onClick: () -> Unit,
    // 선택 파라미터 (기본값)
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 상태
    var state by remember { mutableStateOf(false) }

    // UI
    Column(modifier = modifier) {
        // 구현
    }
}
```

### 8.3 색상/크기 사용

```kotlin
// 올바른 사용
Text(
    color = StandColors.TextPrimary,
    fontSize = StandTypography.bodyMedium,
    modifier = Modifier.padding(StandSpacing.md)
)

// 잘못된 사용 (하드코딩 금지)
Text(
    color = Color.Black,           // X
    fontSize = 14.sp,              // X
    modifier = Modifier.padding(12.dp)  // X
)
```

### 8.4 컴포넌트 재사용

```kotlin
// 올바른 사용 - 기존 컴포넌트 사용
PrimaryButton(text = "확인", onClick = { })

// 잘못된 사용 - 직접 구현 금지
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6200EE)
    )
) {
    Text("확인")
}
```

---

## 파일 위치 참조

| 항목 | 경로 |
|------|------|
| 색상 | `app/src/main/java/com/moveoftoday/walkorwait/ui/theme/Color.kt` |
| 타이포그래피 | `app/src/main/java/com/moveoftoday/walkorwait/ui/theme/Type.kt` |
| 간격 | `app/src/main/java/com/moveoftoday/walkorwait/ui/theme/Spacing.kt` |
| 테마 | `app/src/main/java/com/moveoftoday/walkorwait/ui/theme/Theme.kt` |
| 표준 컴포넌트 | `app/src/main/java/com/moveoftoday/walkorwait/ui/components/StandComponents.kt` |
| 프리미엄 컴포넌트 | `app/src/main/java/com/moveoftoday/walkorwait/ui/components/PremiumComponents.kt` |
| 애니메이션 | `app/src/main/java/com/moveoftoday/walkorwait/StandAnimations.kt` |

---

## 체크리스트 (새 기능 추가 시)

- [ ] `StandColors`에서 색상 선택했는가?
- [ ] `StandTypography`에서 폰트 크기 선택했는가?
- [ ] `StandSpacing`에서 간격 선택했는가?
- [ ] 기존 컴포넌트(`StandComponents`)를 재사용했는가?
- [ ] 버튼 높이 56dp를 유지했는가?
- [ ] 카드 모서리 12dp를 유지했는가?
- [ ] 화면 패딩 16dp를 적용했는가?
- [ ] 하드코딩된 색상/크기가 없는가?

---

*이 가이드는 Stand 앱의 디자인 일관성을 위해 작성되었습니다.*
*문의: 새로운 디자인 요소가 필요한 경우, 기존 시스템을 확장하여 추가합니다.*
