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

## Important Notes
- 프로덕션 Firebase 데이터는 `stand-64c11` 프로젝트에 있음
- Release 빌드시 `upload-keystore.jks` 사용
- 앱 버전 업데이트는 `app/build.gradle.kts`에서 관리
