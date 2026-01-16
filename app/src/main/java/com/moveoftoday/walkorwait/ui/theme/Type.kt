package com.moveoftoday.walkorwait.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material 3 Typography
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Stand 앱 Typography 시스템
 *
 * 용도별 폰트 크기 가이드:
 * - Display: 대형 숫자, 이모지 (48-72sp)
 * - Headline: 화면 제목 (24-32sp)
 * - Title: 카드/섹션 제목 (18-22sp)
 * - Body: 일반 본문 (14-16sp)
 * - Label: 캡션, 힌트 (11-13sp)
 */
object StandTypography {
    // ============================================
    // Display - 대형 숫자, 메인 걸음 수, 이모지
    // ============================================
    val displayHero = 72.sp      // 초대형 이모지 (튜토리얼)
    val displayLarge = 64.sp     // 대형 이모지/아이콘
    val displayMedium = 56.sp    // 메인 걸음 수
    val displaySmall = 48.sp     // 카드 내 큰 숫자
    val displayIcon = 40.sp      // 중간 크기 이모지

    // ============================================
    // Headline - 화면 제목
    // ============================================
    val headlineLarge = 32.sp    // 화면 대제목 ("오늘은 자유로운 날!")
    val headlineMedium = 28.sp   // 화면 제목 (튜토리얼 단계 제목)
    val headlineSmall = 24.sp    // 다이얼로그 제목, 아이콘 이모지

    // ============================================
    // Title - 카드/섹션 제목
    // ============================================
    val titleLarge = 22.sp       // 큰 카드 제목
    val titleMedium = 20.sp      // 섹션 제목, 중요 숫자
    val titleSmall = 18.sp       // 버튼 Bold 텍스트, 중간 제목

    // ============================================
    // Body - 일반 본문
    // ============================================
    val bodyLarge = 16.sp        // 주요 본문, 버튼 텍스트, 카드 내용
    val bodyMedium = 14.sp       // 일반 본문, 설명 텍스트
    val bodySmall = 13.sp        // 보조 텍스트

    // ============================================
    // Label - 캡션, 힌트
    // ============================================
    val labelLarge = 12.sp       // 캡션, 날짜 표시
    val labelMedium = 11.sp      // 아주 작은 텍스트, 힌트
}

/**
 * FontWeight 별칭 (가독성 향상)
 */
object StandFontWeight {
    val Light = FontWeight.Light
    val Normal = FontWeight.Normal
    val Medium = FontWeight.Medium
    val SemiBold = FontWeight.SemiBold
    val Bold = FontWeight.Bold
}
