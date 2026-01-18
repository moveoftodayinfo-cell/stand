package com.moveoftoday.walkorwait

import androidx.compose.ui.graphics.ImageBitmap

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val isLocked: Boolean,
    val usageTime: String? = null, // 사용 시간 (예: "2시간 30분")
    val category: AppCategory = AppCategory.OTHER
)

enum class AppCategory(val displayName: String) {
    SOCIAL("소셜"),
    VIDEO("동영상"),
    GAME("게임"),
    ENTERTAINMENT("엔터테인먼트"),
    SHOPPING("쇼핑"),
    COMMUNICATION("통신"),
    MUSIC_AUDIO("음악"),
    PRODUCTIVITY("생산성"),
    OTHER("기타")
}