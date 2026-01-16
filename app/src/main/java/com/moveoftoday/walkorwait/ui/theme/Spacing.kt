package com.moveoftoday.walkorwait.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stand 앱 Spacing 시스템
 *
 * 일관된 간격을 위한 표준 값 정의
 * 4dp 기반 그리드 시스템 사용
 */
object StandSpacing {
    // ============================================
    // Base Spacing (기본 간격)
    // ============================================
    val none = 0.dp
    val xs = 4.dp      // 아주 작은 간격
    val sm = 8.dp      // 작은 간격
    val md = 12.dp     // 중간 간격
    val lg = 16.dp     // 큰 간격 (기본 패딩)
    val xl = 20.dp     // 더 큰 간격
    val xxl = 24.dp    // 섹션 간격
    val xxxl = 32.dp   // 대형 간격

    // ============================================
    // Component Spacing (컴포넌트별 간격)
    // ============================================
    val cardPadding = 16.dp           // 카드 내부 패딩
    val cardPaddingLarge = 20.dp      // 큰 카드 내부 패딩
    val screenPadding = 16.dp         // 화면 좌우 패딩
    val screenPaddingLarge = 24.dp    // 넓은 화면 패딩
    val listItemPadding = 12.dp       // 리스트 아이템 패딩
    val buttonPadding = 16.dp         // 버튼 내부 패딩

    // ============================================
    // Vertical Spacing (세로 간격)
    // ============================================
    val sectionGap = 24.dp            // 섹션 간 간격
    val itemGap = 8.dp                // 아이템 간 간격
    val itemGapLarge = 12.dp          // 큰 아이템 간 간격
    val textGap = 4.dp                // 텍스트 간 간격
    val paragraphGap = 16.dp          // 문단 간 간격

    // ============================================
    // Icon Spacing (아이콘 간격)
    // ============================================
    val iconGap = 8.dp                // 아이콘-텍스트 간격
    val iconGapLarge = 12.dp          // 큰 아이콘 간격
}

/**
 * 컴포넌트 크기 상수
 */
object StandSize {
    // Button Heights
    val buttonHeight = 56.dp          // 기본 버튼 높이
    val buttonHeightSmall = 48.dp     // 작은 버튼 높이
    val buttonHeightMini = 36.dp      // 미니 버튼 높이

    // Icon Sizes
    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconLarge = 36.dp
    val iconXLarge = 48.dp

    // Avatar/App Icon Sizes
    val appIcon = 36.dp               // 앱 아이콘 크기
    val appIconLarge = 48.dp          // 큰 앱 아이콘

    // Card
    val cardCornerRadius = 12.dp      // 카드 모서리 둥글기
    val cardCornerRadiusLarge = 16.dp // 큰 카드 모서리

    // Progress Bar
    val progressBarHeight = 8.dp      // 프로그레스 바 높이
    val progressBarHeightLarge = 12.dp
}

/**
 * 라인 높이 상수
 */
object StandLineHeight {
    val tight = 16.sp
    val normal = 18.sp
    val relaxed = 20.sp
    val loose = 24.sp
}
