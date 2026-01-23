package com.moveoftoday.walkorwait

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectManager(private val context: Context) {
    private val TAG = "HealthConnectManager"

    companion object {
        // 필요한 권한 목록
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
        )
    }

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    /**
     * Health Connect 사용 가능 여부 확인
     */
    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        val available = status == HealthConnectClient.SDK_AVAILABLE
        Log.d(TAG, "🔍 isAvailable - status: $status, available: $available")
        return available
    }

    /**
     * 권한 확인
     */
    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            PERMISSIONS.all { it in granted }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check permissions: ${e.message}")
            false
        }
    }

    /**
     * 권한 요청 계약 (Activity에서 사용)
     */
    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * 설치된 피트니스 앱 감지
     */
    fun getInstalledFitnessApps(): List<FitnessApp> {
        val packageManager = context.packageManager
        return FitnessApp.values().filter { app ->
            try {
                packageManager.getPackageInfo(app.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /**
     * 오늘 걸음 수 가져오기
     */
    suspend fun getTodaySteps(): Int {
        return try {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )

            val response = healthConnectClient.readRecords(request)
            val totalSteps = response.records.sumOf { it.count.toInt() }

            Log.d(TAG, "✅ Today's steps from Health Connect: $totalSteps")
            totalSteps

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get steps: ${e.message}")
            0
        }
    }

    /**
     * 오늘 거리 가져오기 (미터 단위)
     */
    suspend fun getTodayDistance(): Double {
        return try {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val request = ReadRecordsRequest(
                recordType = DistanceRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )

            val response = healthConnectClient.readRecords(request)
            val totalDistance = response.records.sumOf { it.distance.inMeters }

            Log.d(TAG, "✅ Today's distance: ${totalDistance}m")
            totalDistance

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get distance: ${e.message}")
            0.0
        }
    }

    /**
     * 특정 날짜의 걸음 수 가져오기
     */
    suspend fun getStepsForDate(date: LocalDate): Int {
        return try {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )

            val response = healthConnectClient.readRecords(request)
            response.records.sumOf { it.count.toInt() }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get steps for date $date: ${e.message}")
            0
        }
    }

    /**
     * 특정 기간의 걸음 수 가져오기
     */
    suspend fun getStepsForPeriod(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int> {
        return try {
            val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )

            val response = healthConnectClient.readRecords(request)

            // 날짜별로 그룹화
            response.records
                .groupBy { record ->
                    record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                }
                .mapValues { (_, records) ->
                    records.sumOf { it.count.toInt() }
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get steps for period: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Health Connect 앱 열기
     */
    fun openHealthConnect() {
        try {
            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open Health Connect: ${e.message}")
            // Fallback: Play Store로 이동
            openHealthConnectPlayStore()
        }
    }

    /**
     * Health Connect Play Store 페이지 열기
     */
    fun openHealthConnectPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open Play Store: ${e.message}")
        }
    }
}
