# WalkorWait (Stand) - Claude Code Project Guide

## Project Overview
WalkorWait (Stand)는 사용자가 걷거나 기다리는 시간을 추적하고, AI 펫과 상호작용하며, 챌린지를 수행하는 Android 앱입니다.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **Backend**: Firebase (Firestore, Authentication, Storage, Cloud Functions)
- **Billing**: Google Play Billing Library
- **Build**: Gradle (Kotlin DSL)

## Project Structure
```
app/src/main/java/com/moveoftoday/walkorwait/
├── ui/                    # Composable UI screens
├── viewmodel/             # ViewModels
├── data/                  # Data models
├── repository/            # Firebase repositories
├── service/               # Background services
├── widget/                # Home screen widgets
└── util/                  # Utility classes
```

## Build Commands
```bash
# Debug build
./gradlew.bat assembleDebug

# Release bundle (for Play Store)
./gradlew.bat clean bundleRelease

# Run tests
./gradlew.bat test

# Check compilation
./gradlew.bat compileDebugKotlin
```

## Key Files
- `app/build.gradle.kts` - App-level build configuration
- `build.gradle.kts` - Project-level build configuration
- `app/src/main/AndroidManifest.xml` - App manifest
- `firebase.json` - Firebase configuration
- `keystore.properties` - Signing key configuration (DO NOT COMMIT)

## Firebase Structure
- **users/**: User profiles and progress
- **chatLogs/**: AI pet chat history
- **promoCodes/**: Promo code management
- **announcements/**: App announcements
- **apiConfig/**: API configurations (Claude AI key)

## Development Guidelines

### Code Style
- Use Kotlin idioms (data classes, sealed classes, extension functions)
- Follow Android best practices
- Use `remember` and `LaunchedEffect` properly in Compose
- Handle lifecycle correctly in ViewModels

### Security
- NEVER hardcode API keys or secrets
- Use `keystore.properties` for signing keys
- Validate all user inputs
- Use Firebase Security Rules

### Testing
- Write unit tests for ViewModels
- Test critical user flows
- Run `./gradlew.bat test` before commits

### Git Workflow
- Commit messages in conventional format
- Test build before pushing
- Keep commits atomic and focused

## Android Widget (RemoteViews) 제한사항

**중요: 위젯 레이아웃에서 사용 가능한 뷰가 제한됨!**

### 사용 가능한 뷰
- `FrameLayout`, `LinearLayout`, `RelativeLayout`, `GridLayout`
- `TextView`, `ImageView`, `Button`, `ImageButton`
- `ProgressBar`, `Chronometer`, `AnalogClock`
- `ListView`, `GridView`, `StackView`, `AdapterViewFlipper`

### 사용 불가능한 뷰
- `<View>` (divider 등) → **`ImageView`로 대체**
- `ConstraintLayout`
- `RecyclerView`
- Custom Views
- Compose UI

### 예시: Divider 만들기
```xml
<!-- 잘못된 방법 (에러 발생) -->
<View
    android:layout_width="1dp"
    android:layout_height="match_parent"
    android:background="#DDDDDD" />

<!-- 올바른 방법 -->
<ImageView
    android:layout_width="1dp"
    android:layout_height="match_parent"
    android:background="#DDDDDD"
    android:contentDescription="divider" />
```

### 에러 메시지
```
Class not allowed to be inflated android.view.View
```

## Unicode 텍스트 심볼 사용 가이드

**아이콘이 필요할 때 Unicode 심볼 우선 사용!**

### 규칙 (CRITICAL)

1. **오래된 Unicode 문자** (☀, ☁, ★, ❤, ⚡, ⚠ 등)
   - ✅ **무조건 `UnicodeSymbols` 사용**
   - `\uFE0E` 붙여서 모노크롬 텍스트로 렌더링
   - 예: `UnicodeSymbols.SUN`, `UnicodeSymbols.STAR`

2. **새로운 이모지** (😀, 🐶, 🍕, 🎉 등)
   - ❌ **절대 사용하지 말 것** (컬러 이모지 렌더링됨)
   - ✅ **대신 픽셀 아트 아이콘 요청**
   - `\uFE0E` 붙여도 작동 안함 (텍스트 버전이 존재하지 않음)

### 판단 방법
- **작동 O**: 1990년대 이전부터 있던 기호 (날씨, 도형, 화살표, 하트, 별)
- **작동 X**: 2010년대 이후 스마트폰용으로 만들어진 이모지 (얼굴, 동물, 음식, 물건)

### 장점
- 추가 파일 없음 (앱 용량 절약)
- 모든 해상도에서 선명
- 일관된 모노크롬 스타일
- 코드에서 바로 수정 가능

### 자주 쓰는 심볼
| 카테고리 | 심볼 |
|---------|------|
| 날씨 | ☀ ☁ ☂ ☔ ❄ ⚡ ☾ ⛅ |
| 방향 | ← → ↑ ↓ ↔ ↕ ◀ ▶ |
| 상태 | ● ○ ◉ ■ □ ▣ ✓ ✗ |
| 감정 | ♡ ♥ ★ ☆ ✦ ✧ |
| 기타 | ⚙ ⏰ ⏱ ⌛ ♪ ♫ |

### 텍스트 스타일 강제 (이모지 방지)
```kotlin
// \uFE0E (Unicode Variation Selector-15) 사용
// 컬러 이모지 대신 모노크롬 텍스트로 렌더링

// ❌ 나쁜 예: 직접 사용
val sunIcon = "☀\uFE0E"

// ✅ 좋은 예: UnicodeSymbols 객체 사용
val sunIcon = UnicodeSymbols.SUN
val cloudIcon = UnicodeSymbols.CLOUD
```

### UnicodeSymbols 객체 (권장)
`UnicodeSymbols.kt` 파일에 모든 심볼 상수가 정의되어 있습니다:

```kotlin
// 날씨
UnicodeSymbols.SUN          // ☀
UnicodeSymbols.MOON         // ☾
UnicodeSymbols.CLOUD        // ☁
UnicodeSymbols.UMBRELLA     // ☂
UnicodeSymbols.SNOWFLAKE    // ❄
UnicodeSymbols.LIGHTNING    // ⚡

// 감정/상태
UnicodeSymbols.SPARKLES     // ✨
UnicodeSymbols.STAR         // ⭐
UnicodeSymbols.HEART        // ❤
UnicodeSymbols.FIRE         // 🔥
UnicodeSymbols.MUSCLE       // 💪

// 동작/상태
UnicodeSymbols.CHECK        // ✓
UnicodeSymbols.CHECKMARK    // ✅
UnicodeSymbols.WARNING      // ⚠

// 방향
UnicodeSymbols.UP_ARROW     // ↑
UnicodeSymbols.TRIANGLE_UP  // △
```

### 사용 예시
```kotlin
// WeatherWidgetProvider.kt
private fun getWeatherSymbol(icon: String?): String {
    return when (icon) {
        "sunny" -> UnicodeSymbols.SUN
        "cloudy" -> UnicodeSymbols.CLOUD
        "rainy" -> UnicodeSymbols.UMBRELLA
        "snowy" -> UnicodeSymbols.SNOWFLAKE
        else -> UnicodeSymbols.CIRCLE_OUTLINE
    }
}
```

## Important Notes
- 프로덕션 Firebase 데이터는 `stand-64c11` 프로젝트에 있음
- Release 빌드시 `upload-keystore.jks` 사용
- 앱 버전 업데이트는 `app/build.gradle.kts`에서 관리
