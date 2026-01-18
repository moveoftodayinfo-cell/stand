# Stand 앱 - 펫 UI/UX 이식 작업 목록

> **다음 세션 시작시 이 파일 확인하고 사용자에게 알려줄 것**

## 작업 현황

### 완료
- [x] 펫 온보딩 (선택 → 이름 → 튜토리얼 3단계)
- [x] 메인 화면 (펫 영역 + 걸음수 카드 + 대화)
- [x] 목표 설정 화면 (펫 안내)
- [x] 예치금 설정 화면 (펫 안내 3단계)
- [x] 디자인 가이드 작성 (DESIGN_GUIDE.md)
- [x] 권한 설정 화면 → 펫 형식 (PetPermissionScreen)
- [x] Health Connect 설정 → 펫 형식 (PetHealthConnectScreen)
- [x] 접근성 설정 → 펫 형식 (PetAccessibilityScreen)
- [x] 앱 선택 화면 → 펫 형식 (PetAppSelectionScreen)
- [x] MainActivity 플로우 통합 완료

### 미완료 - 앱 내부
- [ ] 설정 화면 → 펫 스타일 적용
- [ ] 알림/다이얼로그 → 펫 말풍선 스타일
- [ ] 메인 화면 → 현재 다크 테마를 펫 스타일로 변경 (선택적)

### 미완료 - 위젯
- [ ] 위젯 UI → 펫 스타일 적용
  - 펫 스프라이트 표시
  - 모노크롬 디자인
  - 걸음수 + 진행바
  - 펫 한마디 (짧은 말풍선)

## 디자인 참조
- `DESIGN_GUIDE.md` - 전체 디자인 규격
- `mockup_phone.html` - HTML 목업 (브라우저에서 확인)

## 핵심 스타일
- 컬러: `#333` (메인), `#F5F5F5` (카드), `#FFF` (배경)
- 테두리: 3px solid #333, 둥근 모서리
- 펫 영역: 180dp, 말풍선 + 스프라이트 + 하트
- 아이콘: grayscale(100%) brightness(0.2) drop-shadow

## 다음 우선순위
1. 접근성 튜토리얼 펫 형식 변환
2. Health Connect 튜토리얼 펫 형식 변환
3. 위젯 펫 스타일 적용
