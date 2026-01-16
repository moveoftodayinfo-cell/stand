package com.moveoftoday.walkorwait

import androidx.compose.ui.graphics.Color

/**
 * 지원하는 피트니스 앱 목록
 */
enum class FitnessApp(
    val appName: String,
    val packageName: String,
    val icon: String,  // 이모지
    val color: Color
) {
    SAMSUNG_HEALTH(
        appName = "삼성 헬스",
        packageName = "com.sec.android.app.shealth",
        icon = "📱",
        color = Color(0xFF4CAF50)
    ),
    GOOGLE_FIT(
        appName = "Google Fit",
        packageName = "com.google.android.apps.fitness",
        icon = "🏃",
        color = Color(0xFF4285F4)
    ),
    GARMIN(
        appName = "Garmin Connect",
        packageName = "com.garmin.android.apps.connectmobile",
        icon = "⌚",
        color = Color(0xFF007CC3)
    ),
    FITBIT(
        appName = "Fitbit",
        packageName = "com.fitbit.FitbitMobile",
        icon = "💪",
        color = Color(0xFF00B0B9)
    );

    companion object {
        fun fromPackageName(packageName: String): FitnessApp? {
            return values().find { it.packageName == packageName }
        }
    }
}

/**
 * 데이터 소스 타입
 */
enum class DataSource {
    HEALTH_CONNECT,  // Health Connect 사용
    SENSOR          // 기본 센서 사용 (Fallback)
}

/**
 * 목표 타입
 */
enum class GoalType {
    STEPS,      // 걸음 수
    DISTANCE    // 거리 (km)
}

/**
 * 목표 데이터
 */
data class GoalData(
    val type: GoalType,
    val value: Float  // 걸음 수 또는 km
) {
    fun toDisplayString(): String {
        return when (type) {
            GoalType.STEPS -> "${value.toInt()}걸음"
            GoalType.DISTANCE -> "${String.format("%.1f", value)}km"
        }
    }
}
