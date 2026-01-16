package com.moveoftoday.walkorwait.ui.theme

import androidx.compose.ui.graphics.Color

// Material Theme Colors (기존 유지)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Stand 앱 디자인 시스템 색상
 */
object StandColors {
    // ============================================
    // Primary Colors (주요 색상)
    // ============================================
    val Primary = Color(0xFF6200EE)
    val AccentPurple = Primary  // 별칭
    val PrimaryLight = Primary.copy(alpha = Alpha.CARD_BACKGROUND)
    val PrimaryMedium = Primary.copy(alpha = Alpha.SELECTED)

    // ============================================
    // Status Colors (상태 색상)
    // ============================================
    val Success = Color(0xFF4CAF50)
    val SuccessGreen = Success  // 별칭
    val SuccessLight = Success.copy(alpha = Alpha.CARD_BACKGROUND)

    val Warning = Color(0xFFFF9800)
    val WarningOrange = Warning  // 별칭
    val WarningLight = Warning.copy(alpha = Alpha.CARD_BACKGROUND)

    val Error = Color(0xFFFF5722)
    val ErrorLight = Error.copy(alpha = Alpha.CARD_BACKGROUND)
    val ErrorMedium = Error.copy(alpha = Alpha.SELECTED)

    // ============================================
    // Light Effect Colors (조명 효과 색상)
    // ============================================
    val WarmLight = Color(0xFFFFE4B5)
    val WarmLightBright = Color(0xFFFFD700)
    val WarmLightDim = Color(0xFFDEB887)
    val GlowYellow = Color(0xFFFFEB3B)
    val GlowAmber = Color(0xFFFFC107)

    // ============================================
    // Background Colors (배경 색상)
    // ============================================
    val DarkBackground = Color(0xFF1A1A2E)
    val LightBackground = Color(0xFFF8F9FA)
    val CardBackground = Color(0xFFF5F5F5)
    val DisabledBackground = Color.Gray.copy(alpha = Alpha.CARD_BACKGROUND)

    // ============================================
    // Text Colors (텍스트 색상)
    // ============================================
    val TextPrimary = Color.Black
    val TextSecondary = Color.Gray
    val TextOnDark = Color.White
}

/**
 * 표준 Alpha 값
 */
object Alpha {
    const val CARD_BACKGROUND = 0.1f
    const val SELECTED = 0.15f
    const val OVERLAY = 0.2f
    const val DISABLED = 0.38f
    const val MEDIUM = 0.4f
    const val HIGH = 0.6f
    const val VERY_HIGH = 0.8f
}
