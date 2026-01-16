package com.moveoftoday.walkorwait

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long, // 밀리초
    val icon: android.graphics.drawable.Drawable?
)

class UsageStatsHelper(private val context: Context) {
    private val TAG = "UsageStatsHelper"
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    /**
     * 사용 통계 권한이 있는지 확인
     */
    fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            @Suppress("DEPRECATION")
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage stats permission: ${e.message}")
            false
        }
    }

    /**
     * 사용 통계 권한 요청 화면 열기
     */
    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 지난 N일간 가장 많이 사용한 앱 가져오기
     */
    fun getTopUsedApps(days: Int = 7, limit: Int = 20): List<AppUsageInfo> {
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "No usage stats permission")
            return emptyList()
        }

        try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (days * 24 * 60 * 60 * 1000L)

            Log.d(TAG, "Querying usage stats from ${android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", startTime)} to ${android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", endTime)}")

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (stats.isNullOrEmpty()) {
                Log.w(TAG, "No usage stats available")
                return emptyList()
            }

            Log.d(TAG, "Got ${stats.size} usage stats entries")

            // 패키지별로 사용 시간 집계
            val packageUsage = mutableMapOf<String, Long>()
            stats.forEach { usageStat ->
                val packageName = usageStat.packageName
                val totalTime = usageStat.totalTimeInForeground
                packageUsage[packageName] = (packageUsage[packageName] ?: 0) + totalTime
            }

            Log.d(TAG, "Aggregated ${packageUsage.size} packages")

            // 시스템 앱 및 자기 자신 제외, 사용 시간 순 정렬
            val filtered = packageUsage
                .filter { (packageName, time) ->
                    time > 0 &&
                    packageName != context.packageName &&
                    !isSystemApp(packageName)
                }

            Log.d(TAG, "After filtering: ${filtered.size} packages")

            val result = filtered
                .map { (packageName, time) ->
                    AppUsageInfo(
                        packageName = packageName,
                        appName = getAppName(packageName),
                        totalTimeInForeground = time,
                        icon = getAppIcon(packageName)
                    )
                }
                .sortedByDescending { it.totalTimeInForeground }
                .take(limit)

            Log.d(TAG, "Returning top ${result.size} apps")
            result.forEachIndexed { index, app ->
                Log.d(TAG, "#${index + 1}: ${app.appName} - ${formatUsageTime(app.totalTimeInForeground)}")
            }

            return result

        } catch (e: Exception) {
            Log.e(TAG, "Error getting usage stats: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * 시스템 앱인지 확인
     */
    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            true // 정보를 가져올 수 없으면 시스템 앱으로 간주
        }
    }

    /**
     * 앱 이름 가져오기
     */
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    /**
     * 앱 아이콘 가져오기
     */
    private fun getAppIcon(packageName: String): android.graphics.drawable.Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 사용 시간을 읽기 쉬운 형식으로 변환
     */
    fun formatUsageTime(milliseconds: Long): String {
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 0 -> "${hours}시간 ${minutes}분"
            minutes > 0 -> "${minutes}분"
            else -> "1분 미만"
        }
    }

    /**
     * 일평균 사용 시간 계산
     */
    fun getDailyAverageTime(totalMilliseconds: Long, days: Int): String {
        val averageMilliseconds = totalMilliseconds / days
        return formatUsageTime(averageMilliseconds)
    }
}
