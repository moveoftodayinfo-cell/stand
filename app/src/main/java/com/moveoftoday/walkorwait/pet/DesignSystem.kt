package com.moveoftoday.walkorwait.pet

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 디자인 시스템 토큰
 * - 다마고치/레트로 스타일: 각진 모서리, 얇은 테두리
 * - 일관된 간격 체계: 4, 8, 12, 16, 24dp
 */

// ============================================
// Spacing (간격)
// ============================================
object Spacing {
    val xs = 4.dp    // 아이콘-텍스트 사이
    val sm = 8.dp    // 밀접한 요소 간격
    val md = 12.dp   // 기본 컴포넌트 간격
    val lg = 16.dp   // 섹션 내 큰 간격
    val xl = 24.dp   // 섹션 간 구분
}

// ============================================
// Radius (모서리 둥글기) - 다마고치 스타일: 각진 형태
// ============================================
object Radius {
    val xs = 2.dp    // 프로그레스 바, 태그
    val sm = 4.dp    // 버튼, 카드, 입력
    val md = 6.dp    // 디스플레이 영역
}

// ============================================
// Border (테두리 두께)
// ============================================
object Border {
    val thin = 1.dp      // 프로그레스 바
    val medium = 1.5.dp  // 버튼, 카드, 입력
    val thick = 2.dp     // 디스플레이 영역
}

// ============================================
// Size (컴포넌트 크기)
// ============================================
object Size {
    val buttonHeight = 48.dp
    val inputHeight = 48.dp
    val iconButtonSize = 44.dp
    val iconSize = 16.dp
    val displayAreaHeight = 240.dp
    val progressBarHeight = 12.dp
}

// ============================================
// Typography (폰트 크기)
// ============================================
object FontSize {
    val xs = 11.sp    // 캡션, 힌트
    val sm = 13.sp    // 보조 텍스트
    val md = 15.sp    // 본문
    val lg = 18.sp    // 버튼, 강조
    val xl = 24.sp    // 소제목
    val xxl = 32.sp   // 달성률
    val display = 40.sp  // 걸음수 (큰 숫자)
}

// ============================================
// Padding (안쪽 여백)
// ============================================
object Padding {
    val screen = 16.dp           // 화면 좌우
    val card = 16.dp             // 카드 내부
    val button = 12.dp           // 버튼 가로
    val buttonVertical = 8.dp    // 버튼 세로
    val speechBubble = 12.dp     // 말풍선 가로
    val speechBubbleVertical = 8.dp  // 말풍선 세로
    val dialog = 24.dp           // 다이얼로그
}
