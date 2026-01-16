package com.moveoftoday.walkorwait

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

object AppUtils {

    /**
     * 영어 앱 이름을 한글로 변환하는 맵
     */
    private val appNameKoreanMap = mapOf(
        "Photos" to "사진",
        "Google Photos" to "사진",
        "YouTube" to "유튜브",
        "Chrome" to "크롬",
        "Google Chrome" to "크롬",
        "Gmail" to "지메일",
        "Maps" to "지도",
        "Google Maps" to "지도",
        "Calendar" to "캘린더",
        "Google Calendar" to "캘린더",
        "Drive" to "드라이브",
        "Google Drive" to "드라이브",
        "Clock" to "시계",
        "Calculator" to "계산기",
        "Camera" to "카메라",
        "Files" to "파일",
        "Messages" to "메시지",
        "Phone" to "전화",
        "Contacts" to "연락처",
        "Settings" to "설정"
    )

    /**
     * 앱 이름을 한글로 변환
     */
    private fun getKoreanAppName(englishName: String): String {
        return appNameKoreanMap[englishName] ?: englishName
    }

    /**
     * 설치된 앱들을 카테고리별로 분류해서 반환
     */
    fun getInstalledAppsByCategory(context: Context): Map<AppCategory, List<AppItem>> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        android.util.Log.e("AppUtils", "🔥🔥🔥🔥🔥 NEW CODE RUNNING 🔥🔥🔥🔥🔥")
        android.util.Log.e("AppUtils", "========== 앱 검색 시작 (신규 버전) ==========")
        android.util.Log.d("AppUtils", "Total installed apps: ${installedApps.size}")

        // 주요 앱 패키지명 목록 (강제 포함)
        val popularAppPackages = setOf(
            "com.google.android.youtube",
            "com.android.chrome",
            "com.instagram.android",
            "com.facebook.katana",
            "com.zhiliaoapp.musically", // TikTok
            "com.netflix.mediaclient",
            "com.spotify.music",
            "com.twitter.android",
            "com.whatsapp",
            "com.samsung.android.game.gametools" // 게임 런처
        )

        val userApps = installedApps
            .filter { appInfo ->
                // Stand 앱 자신은 제외
                appInfo.packageName != context.packageName
            }
            .filter { appInfo ->
                // 1. 주요 앱이거나
                // 2. 런처 인텐트가 있는 앱
                val isPopularApp = popularAppPackages.contains(appInfo.packageName)
                val hasLauncher = packageManager.getLaunchIntentForPackage(appInfo.packageName) != null

                val included = isPopularApp || hasLauncher

                if (included) {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    android.util.Log.d("AppUtils", "✅ 포함: $appName (${appInfo.packageName}) - Popular:$isPopularApp, Launcher:$hasLauncher")
                }

                included
            }
            .map { appInfo ->
                val originalAppName = packageManager.getApplicationLabel(appInfo).toString()
                val appName = getKoreanAppName(originalAppName)
                val icon = try {
                    packageManager.getApplicationIcon(appInfo).toBitmap().asImageBitmap()
                } catch (e: Exception) {
                    null
                }
                val category = getAppCategory(appInfo)

                android.util.Log.d("AppUtils", "📱 $appName → $category")

                AppItem(
                    packageName = appInfo.packageName,
                    appName = appName,
                    icon = icon,
                    isLocked = false,
                    category = category
                )
            }
            .sortedBy { it.appName }

        android.util.Log.d("AppUtils", "========== 총 ${userApps.size}개 앱 발견 ==========")

        // 카테고리별로 그룹화
        val grouped = userApps.groupBy { it.category }
        grouped.forEach { (category, apps) ->
            android.util.Log.d("AppUtils", "$category: ${apps.size}개 - ${apps.map { it.appName }}")
        }

        return grouped
    }

    /**
     * 앱의 카테고리를 판단
     */
    private fun getAppCategory(appInfo: ApplicationInfo): AppCategory {
        // Android 8.0 (API 26) 이상에서는 ApplicationInfo.category 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> AppCategory.GAME
                ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MUSIC_AUDIO
                ApplicationInfo.CATEGORY_VIDEO -> AppCategory.VIDEO
                ApplicationInfo.CATEGORY_IMAGE -> AppCategory.ENTERTAINMENT
                ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_NEWS -> AppCategory.ENTERTAINMENT
                ApplicationInfo.CATEGORY_MAPS -> AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
                else -> getCategoryByPackageName(appInfo.packageName)
            }
        }

        // Android 7.1 이하에서는 패키지명으로 판단
        return getCategoryByPackageName(appInfo.packageName)
    }

    /**
     * 패키지명으로 카테고리 추측
     */
    private fun getCategoryByPackageName(packageName: String): AppCategory {
        return when {
            // 게임
            packageName.contains("game", ignoreCase = true) ||
            packageName.contains("play.games", ignoreCase = true) -> AppCategory.GAME

            // 동영상
            packageName.contains("youtube", ignoreCase = true) ||
            packageName.contains("netflix", ignoreCase = true) ||
            packageName.contains("video", ignoreCase = true) ||
            packageName.contains("tving", ignoreCase = true) ||
            packageName.contains("wavve", ignoreCase = true) -> AppCategory.VIDEO

            // 소셜
            packageName.contains("facebook", ignoreCase = true) ||
            packageName.contains("instagram", ignoreCase = true) ||
            packageName.contains("twitter", ignoreCase = true) ||
            packageName.contains("snapchat", ignoreCase = true) ||
            packageName.contains("tiktok", ignoreCase = true) ||
            packageName.contains("kakao.talk", ignoreCase = true) -> AppCategory.SOCIAL

            // 음악
            packageName.contains("music", ignoreCase = true) ||
            packageName.contains("spotify", ignoreCase = true) ||
            packageName.contains("melon", ignoreCase = true) ||
            packageName.contains("genie", ignoreCase = true) ||
            packageName.contains("bugs", ignoreCase = true) -> AppCategory.MUSIC_AUDIO

            // 쇼핑
            packageName.contains("shopping", ignoreCase = true) ||
            packageName.contains("coupang", ignoreCase = true) ||
            packageName.contains("gmarket", ignoreCase = true) ||
            packageName.contains("11st", ignoreCase = true) -> AppCategory.SHOPPING

            // 통신
            packageName.contains("messenger", ignoreCase = true) ||
            packageName.contains("whatsapp", ignoreCase = true) ||
            packageName.contains("telegram", ignoreCase = true) ||
            packageName.contains("line", ignoreCase = true) -> AppCategory.COMMUNICATION

            // 엔터테인먼트
            packageName.contains("webtoon", ignoreCase = true) ||
            packageName.contains("comic", ignoreCase = true) -> AppCategory.ENTERTAINMENT

            else -> AppCategory.OTHER
        }
    }

    /**
     * 엔터테인먼트 및 게임 카테고리만 필터링
     */
    fun getEntertainmentAndGameApps(context: Context): List<AppItem> {
        val allApps = getInstalledAppsByCategory(context)
        val entertainmentCategories = setOf(
            AppCategory.GAME,
            AppCategory.VIDEO,
            AppCategory.SOCIAL,
            AppCategory.MUSIC_AUDIO,
            AppCategory.ENTERTAINMENT
        )

        return allApps
            .filterKeys { it in entertainmentCategories }
            .values
            .flatten()
            .sortedBy { it.category.ordinal }
    }
}
