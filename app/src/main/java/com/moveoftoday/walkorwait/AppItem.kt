package com.moveoftoday.walkorwait

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.ImageBitmap

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val isLocked: Boolean,
    val usageTime: String? = null, // 사용 시간 (예: "2시간 30분")
    val category: AppCategory = AppCategory.OTHER
)

enum class AppCategory(@StringRes val displayNameRes: Int) {
    SOCIAL(R.string.app_category_social),
    VIDEO(R.string.app_category_video),
    GAME(R.string.app_category_game),
    ENTERTAINMENT(R.string.app_category_entertainment),
    SHOPPING(R.string.app_category_shopping),
    COMMUNICATION(R.string.app_category_communication),
    MUSIC_AUDIO(R.string.app_category_music_audio),
    PRODUCTIVITY(R.string.app_category_productivity),
    OTHER(R.string.app_category_other)
}