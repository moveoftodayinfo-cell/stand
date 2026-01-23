package com.moveoftoday.walkorwait

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class AppBlockService : AccessibilityService() {

    private val TAG = "AppBlockService"
    private val handler = Handler(Looper.getMainLooper())
    private var lastBlockedTime = 0L
    private lateinit var prefs: PreferenceManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var hapticManager: HapticManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== Service onCreate ===")
        prefs = PreferenceManager(this)
        notificationHelper = NotificationHelper(this)
        hapticManager = HapticManager(this)
        checkAndResetDaily()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 모든 이벤트 로깅 (디버깅용)
        val packageName = event?.packageName?.toString()
        val className = event?.className?.toString()
        val eventType = event?.eventType

        // 주요 앱들 감지 로깅
        val watchedApps = listOf("youtube", "chrome", "gmail", "music")
        if (packageName != null && watchedApps.any { packageName.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "🎯 WATCHED APP - Package: $packageName, Class: $className, EventType: $eventType")
        }

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName == null) return

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔍 App opened: $packageName")
            Log.d(TAG, "   Class: $className")

            // 자기 자신 앱은 차단하지 않음
            if (packageName == this.packageName) {
                Log.d(TAG, "✓ Own package - ignoring")
                return
            }

            // 시스템 UI 무시
            if (packageName == "com.android.systemui" ||
                packageName == "com.samsung.android.app.cocktailbarservice" ||
                packageName == "com.sec.android.app.launcher") {
                Log.d(TAG, "✓ System UI - ignoring")
                return
            }

            // 차단 로직 실행
            checkAndBlockApp(packageName)
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    private fun checkAndResetDaily() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastReset = prefs.getLastResetDate()

        Log.d(TAG, "Check daily reset - Today: $today, Last: $lastReset")

        if (lastReset != today) {
            prefs.resetDailyData()
            prefs.saveLastResetDate(today)
            notificationHelper.cancelEmergencyNotification()
            Log.d(TAG, "Daily data reset!")
        }
    }

    private fun checkAndBlockApp(packageName: String) {
        val lockedApps = prefs.getLockedApps()
        val isTutorialCompleted = prefs.isTutorialCompleted()

        Log.d(TAG, "Locked apps: $lockedApps")
        Log.d(TAG, "Tutorial completed: $isTutorialCompleted")

        // 잠금 앱이 아니면 무시
        if (!lockedApps.contains(packageName)) {
            Log.d(TAG, "$packageName is not in locked list")
            return
        }

        Log.d(TAG, "✓ $packageName is locked!")

        // ========================================
        // 🎓 튜토리얼 모드: 모든 조건 무시하고 차단
        // ========================================
        if (!isTutorialCompleted) {
            Log.d(TAG, "🎓 TUTORIAL MODE - bypassing all conditions")

            val goal = prefs.getGoal()
            val currentProgress = prefs.getCurrentProgress()

            Log.d(TAG, "Tutorial - Progress: $currentProgress / Goal: $goal")

            // 목표 달성했으면 차단 안함
            if (currentProgress >= goal) {
                Log.d(TAG, "🎓 Tutorial - Goal achieved! No blocking")
                return
            }

            // 목표 미달성이면 차단!
            Log.d(TAG, "🎓 Tutorial - Goal NOT achieved - BLOCKING!")
            blockAppImmediately(packageName, "튜토리얼: 목표를 달성하세요!")
            return
        }

        // ========================================
        // 일반 모드: 모든 조건 확인
        // ========================================

        // ✨ 보증금 확인 - 보증금이 없으면 차단 안함
        val deposit = prefs.getDeposit()
        if (deposit <= 0) {
            Log.d(TAG, "No deposit set - allowing app")
            return
        }

        Log.d(TAG, "Deposit: $deposit")

        // ✨ 제어 요일 확인 - 오늘이 제어 요일이 아니면 차단 안함
        if (!prefs.isTodayControlDay()) {
            Log.d(TAG, "Today is NOT a control day - allowing app")
            return
        }

        Log.d(TAG, "Today IS a control day - checking conditions")

        // 체험 기간 확인
        if (prefs.isInTrialPeriod()) {
            Log.d(TAG, "In trial period - allowing app (${prefs.getTrialDaysRemaining()} days remaining)")
            return
        }

        // 차단 시간대 확인
        if (!prefs.isInBlockingPeriod()) {
            Log.d(TAG, "Not in blocking period - allowing app")
            return
        }

        Log.d(TAG, "In blocking period - checking goal")

        val goal = prefs.getGoal()
        val goalUnit = prefs.getGoalUnit()
        val currentProgress = prefs.getCurrentProgress()

        Log.d(TAG, "Progress: $currentProgress / Goal: $goal ($goalUnit)")

        // 목표 달성하면 차단하지 않음
        if (currentProgress >= goal) {
            Log.d(TAG, "Goal achieved! No blocking")
            notificationHelper.cancelEmergencyNotification()
            return
        }

        Log.d(TAG, "Goal NOT achieved")

        // 긴급 모드 체크
        val isEmergencyMode = prefs.isEmergencyMode()
        val emergencyStartTime = prefs.getEmergencyStartTime()
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "Emergency mode: $isEmergencyMode, Start: $emergencyStartTime")

        if (isEmergencyMode && emergencyStartTime > 0) {
            val emergencyElapsed = currentTime - emergencyStartTime
            val emergencyLimit = 15 * 60 * 1000L // 15분

            if (emergencyElapsed < emergencyLimit) {
                // 긴급 15분 아직 유효
                val remainingTime = emergencyLimit - emergencyElapsed
                val minutes = remainingTime / 60000
                val seconds = (remainingTime % 60000) / 1000

                Log.d(TAG, "Emergency mode active - Remaining: ${minutes}m ${seconds}s")

                // Notification 표시
                notificationHelper.showEmergencyNotification(minutes, seconds)

                // Toast도 표시
                handler.post {
                    Toast.makeText(
                        this,
                        "🆘 긴급 사용 중 - 남은 시간: ${minutes}분 ${seconds}초",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            } else {
                // 긴급 15분 만료
                Log.d(TAG, "Emergency mode EXPIRED")
                prefs.saveEmergencyMode(false)
                notificationHelper.cancelEmergencyNotification()
                blockAppImmediately(packageName, "긴급 시간이 끝났어요")
                return
            }
        }

        // 긴급 모드 아니면 완전 차단
        Log.d(TAG, "No emergency mode - BLOCKING")
        blockAppImmediately(packageName, "목표까지 조금 더 걸어주세요")
    }

    private fun blockAppImmediately(packageName: String, message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockedTime < 1000) {
            Log.d(TAG, "Duplicate block prevented")
            return
        }
        lastBlockedTime = currentTime

        Log.d(TAG, "!!! BLOCKING APP NOW !!!")

        // Analytics: 앱 차단 추적
        AnalyticsManager.trackAppBlocked(packageName)

        // 🚫 차단 햅틱 피드백
        hapticManager.blocked()

        // 상세 정보 가져오기 (표시용 단위에 맞게)
        val goalUnit = prefs.getGoalUnit()
        val goal = prefs.getGoalForDisplay() // km 모드면 km로 변환된 값
        val currentProgress = prefs.getCurrentProgressForDisplay() // km 모드면 km로 변환된 값
        val remaining = prefs.getRemainingToGoal()
        val estimatedTime = prefs.getEstimatedArrivalTime()

        // 표시 텍스트 생성
        val unitText = if (goalUnit == "km") "km" else "걸음"
        val currentText = if (goalUnit == "km") String.format("%.2f", currentProgress) else currentProgress.toInt().toString()
        val goalText = if (goalUnit == "km") String.format("%.2f", goal) else goal.toInt().toString()
        val remainingText = if (goalUnit == "km") String.format("%.2f", remaining) else remaining.toInt().toString()

        // Notification 표시 (상세 정보 포함)
        notificationHelper.showBlockedNotification(
            packageName,
            currentProgress,
            goal,
            remaining,
            estimatedTime,
            goalUnit
        )

        // 홈 화면으로 이동
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)

        // Toast 알림 (친근한 메시지)
        handler.post {
            val detailedMessage = "🚶 조금만 더 걸어볼까요?\n$currentText / $goalText $unitText\n$remainingText $unitText 남았어요"
            Toast.makeText(this, detailedMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "=== Service CONNECTED ===")
        prefs = PreferenceManager(this)
        notificationHelper = NotificationHelper(this)
        hapticManager = HapticManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.cancelEmergencyNotification()
    }
}