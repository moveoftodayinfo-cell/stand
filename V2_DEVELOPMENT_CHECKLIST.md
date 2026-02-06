# V2 Pet System 개발 체크리스트

**Last Updated: 2026-02-04 18:40**

---

## 완료됨 (Completed)

### 코드 구현
- [x] **PetEvolutionModel.kt** - 데이터 모델 (레벨, 성장단계, 경험치)
- [x] **PetSystemV2Integration.kt** - V2 통합 헬퍼
- [x] **PetSpriteAnimationV2.kt** - 스프라이트 애니메이션
- [x] **PetDialoguesV2.kt** - 성격별 대사 시스템
- [x] **PetSetupScreensV2.kt** - 펫 선택/이름 입력 화면

### 기능 테스트 (2026-02-04)
- [x] **레벨업 다이얼로그** - Lv.1→Lv.2 테스트 완료
- [x] **EGG→BABY 진화** - "알 → 아기" 테스트 완료
- [x] **BABY→TEEN 진화** - "아기 → 성장기" 테스트 완료
- [x] **TEEN→ADULT 진화** - "성장기 → 성체" 테스트 완료
- [x] **V2 스프라이트 로딩** - 모든 펫/단계 작동 확인
- [x] **V2 성격 대사** - 진화 대사 정상 표시
- [x] **DEBUG 온보딩 스킵** - 빠른 테스트용
- [x] **테스트 모드** - rebon 타이틀 길게 누르기

### 에셋 (모두 완료!)
- [x] **EGG** - idle (공통)
- [x] **SHIBA** - baby, teen, adult (모든 애니메이션)
- [x] **CAT** - baby, teen, adult (10프레임씩)
- [x] **PIG** - baby, teen, adult (17프레임씩)
- [x] **RACCOON** - baby, teen, adult (17프레임씩)
- [x] **HAMSTER** - baby, teen, adult (17프레임씩)
- [x] **PENGUIN** - baby, teen, adult (4프레임씩)

---

## 진행 필요 (TODO)

### 우선순위 높음 (Phase 1)
- [ ] **펫 교체 UI** - 설정에서 펫 변경 기능

### 기능 구현 (Phase 2)
- [ ] **위젯 (Glance API)** - V2 스프라이트 위젯 표시
- [ ] **Rich Notification** - 챌린지 진행 중 펫 알림
- [ ] **외형 해금 시스템** - 도달한 단계 영구 해금, 자유 선택

### 고급 기능 (Phase 3)
- [ ] **Floating Overlay** - Dynamic Island 스타일
- [ ] **악세사리 시스템** - 모자, 안경 등 커스터마이징
- [ ] **배경 테마** - 계절별/테마별 배경

---

## 에셋 매트릭스

| 펫 | EGG | BABY | TEEN | ADULT |
|----|-----|------|------|-------|
| SHIBA | ✓ | ✓ | ✓ | ✓ |
| CAT | ✓ | ✓ | ✓ | ✓ |
| PIG | ✓ | ✓ | ✓ | ✓ |
| RACCOON | ✓ | ✓ | ✓ | ✓ |
| HAMSTER | ✓ | ✓ | ✓ | ✓ |
| PENGUIN | ✓ | ✓ | ✓ | ✓ |

**범례:** ✓ = 완료

---

## 테스트 명령어

```bash
# 앱 데이터 초기화 후 설치
adb shell pm clear com.moveoftoday.walkorwait
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.moveoftoday.walkorwait/.MainActivity

# 테스트 모드 (rebon 길게 누르기)
adb shell input swipe 540 157 540 157 2000

# 로그 확인
adb logcat -d | grep -iE "V2Test|FrameLoader|evolution"
```

---

## 파일 위치

```
app/src/main/
├── java/.../pet/
│   ├── PetEvolutionModel.kt      # 데이터 모델
│   ├── PetSystemV2Integration.kt # V2 헬퍼
│   ├── PetSpriteAnimationV2.kt   # 애니메이션
│   ├── PetDialoguesV2.kt         # 대사
│   ├── PetSetupScreensV2.kt      # 셋업 화면
│   ├── PetMainScreen.kt          # 메인 화면
│   └── PetComponents.kt          # UI 컴포넌트
│
└── assets/pets/
    ├── egg/idle/                  # ✓ 공통 알
    ├── shiba/                     # ✓ 완료
    ├── cat/                       # ✓ 완료
    ├── pig/                       # ✓ 완료
    ├── raccoon/                   # ✓ 완료
    ├── hamster/                   # ✓ 완료
    └── penguin/                   # ✓ 완료
```
