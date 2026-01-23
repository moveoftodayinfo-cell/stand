package com.moveoftoday.walkorwait

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics 이벤트 추적 관리
 */
object AnalyticsManager {
    private const val TAG = "AnalyticsManager"
    private var analytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context).apply {
            // 광고 ID 수집 완전 비활성화
            setAnalyticsCollectionEnabled(true)
            // 이 줄이 핵심: 광고 ID 수집 비활성화
            @Suppress("DEPRECATION")
            setUserProperty("allow_personalized_ads", "false")
        }
        // 광고 ID 수집 비활성화 (deprecated but still works)
        @Suppress("DEPRECATION")
        analytics?.setAnalyticsCollectionEnabled(true)
        Log.d(TAG, "✅ Analytics initialized (AD_ID disabled)")
    }

    // ========== 화면 조회 ==========

    fun trackScreenView(screenName: String, screenClass: String? = null) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        Log.d(TAG, "📱 Screen: $screenName")
    }

    // ========== 온보딩/튜토리얼 ==========

    // 튜토리얼 단계 이름 (GA에서 이해하기 쉽게)
    private val tutorialStepNames = mapOf(
        0 to "pet_selection",
        1 to "pet_name",
        2 to "intro_explanation",
        3 to "permission_settings",
        4 to "fitness_connection",
        5 to "accessibility",
        6 to "app_selection",
        7 to "block_test",
        8 to "goal_input",
        9 to "walking_test",
        10 to "unlocked",
        11 to "control_days",
        12 to "block_time",
        13 to "emergency_button",
        14 to "payment",
        15 to "widget_setup"
    )

    fun trackTutorialBegin() {
        analytics?.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null)
        Log.d(TAG, "📖 Tutorial begin")
    }

    fun trackTutorialStep(stepNumber: Int) {
        val stepName = tutorialStepNames[stepNumber] ?: "step_$stepNumber"
        val params = Bundle().apply {
            putInt("step_number", stepNumber)
            putString("step_name", stepName)
        }
        analytics?.logEvent("tutorial_step", params)
        Log.d(TAG, "📖 Tutorial step: $stepNumber ($stepName)")
    }

    fun trackTutorialExit(stepNumber: Int) {
        val stepName = tutorialStepNames[stepNumber] ?: "step_$stepNumber"
        val params = Bundle().apply {
            putInt("exit_step", stepNumber)
            putString("exit_step_name", stepName)
        }
        analytics?.logEvent("tutorial_exit", params)
        Log.d(TAG, "🚪 Tutorial exit at step: $stepNumber ($stepName)")
    }

    fun trackTutorialComplete() {
        analytics?.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null)
        Log.d(TAG, "✅ Tutorial complete")
    }

    fun trackPetSelected(petType: String) {
        val params = Bundle().apply {
            putString("pet_type", petType)
        }
        analytics?.logEvent("pet_selected", params)
        Log.d(TAG, "🐾 Pet selected: $petType")
    }

    // ========== 목표 설정 ==========

    fun trackGoalSet(goalSteps: Int) {
        val params = Bundle().apply {
            putInt("goal_steps", goalSteps)
        }
        analytics?.logEvent("goal_set", params)
        Log.d(TAG, "🎯 Goal set: $goalSteps steps")
    }

    fun trackGoalAchieved(goalSteps: Int, actualSteps: Int) {
        val params = Bundle().apply {
            putInt("goal_steps", goalSteps)
            putInt("actual_steps", actualSteps)
            putDouble("achievement_rate", actualSteps.toDouble() / goalSteps * 100)
        }
        analytics?.logEvent("goal_achieved", params)
        Log.d(TAG, "🏆 Goal achieved: $actualSteps / $goalSteps")
    }

    // ========== 앱 차단 ==========

    fun trackAppBlocked(packageName: String) {
        val params = Bundle().apply {
            putString("blocked_app", packageName)
        }
        analytics?.logEvent("app_blocked", params)
        Log.d(TAG, "🔒 App blocked: $packageName")
    }

    fun trackAppUnlocked(packageName: String) {
        val params = Bundle().apply {
            putString("unlocked_app", packageName)
        }
        analytics?.logEvent("app_unlocked", params)
        Log.d(TAG, "🔓 App unlocked: $packageName")
    }

    fun trackBlockedAppSelected(appCount: Int) {
        val params = Bundle().apply {
            putInt("selected_app_count", appCount)
        }
        analytics?.logEvent("blocked_apps_selected", params)
        Log.d(TAG, "📱 Blocked apps selected: $appCount")
    }

    // ========== 구독/결제 ==========

    fun trackSubscriptionStart(source: String) {
        val params = Bundle().apply {
            putString("source", source)  // "google_play" or "promo_code"
        }
        analytics?.logEvent("subscription_start", params)
        Log.d(TAG, "💳 Subscription start: $source")
    }

    fun trackPromoCodeUsed(codeType: String) {
        val params = Bundle().apply {
            putString("code_type", codeType)  // "REBONFREE", "TEST-FREE", "REBON-XXXXX"
        }
        analytics?.logEvent("promo_code_used", params)
        Log.d(TAG, "🎁 Promo code used: $codeType")
    }

    fun trackPurchaseCompleted(productId: String, price: Double) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, productId)
            putDouble(FirebaseAnalytics.Param.VALUE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, "KRW")
        }
        analytics?.logEvent(FirebaseAnalytics.Event.PURCHASE, params)
        Log.d(TAG, "💰 Purchase: $productId - ₩$price")
    }

    // ========== 스트릭 ==========

    fun trackStreakMilestone(streakDays: Int) {
        val params = Bundle().apply {
            putInt("streak_days", streakDays)
        }
        analytics?.logEvent("streak_milestone", params)
        Log.d(TAG, "🔥 Streak milestone: $streakDays days")
    }

    fun trackStreakShared(streakDays: Int) {
        val params = Bundle().apply {
            putInt("streak_days", streakDays)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SHARE, params)
        Log.d(TAG, "📤 Streak shared: $streakDays days")
    }

    // ========== 친구 초대 ==========

    fun trackInviteCodeGenerated() {
        analytics?.logEvent("invite_code_generated", null)
        Log.d(TAG, "🎟️ Invite code generated")
    }

    fun trackInviteCodeShared() {
        analytics?.logEvent("invite_code_shared", null)
        Log.d(TAG, "📤 Invite code shared")
    }

    // ========== 위젯 ==========

    fun trackWidgetAdded() {
        analytics?.logEvent("widget_added", null)
        Log.d(TAG, "📱 Widget added")
    }

    // ========== 설정 ==========

    fun trackSettingsChanged(settingName: String, value: String) {
        val params = Bundle().apply {
            putString("setting_name", settingName)
            putString("setting_value", value)
        }
        analytics?.logEvent("settings_changed", params)
        Log.d(TAG, "⚙️ Setting changed: $settingName = $value")
    }

    // ========== 권한 ==========

    fun trackPermissionGranted(permissionType: String) {
        val params = Bundle().apply {
            putString("permission_type", permissionType)
        }
        analytics?.logEvent("permission_granted", params)
        Log.d(TAG, "✅ Permission granted: $permissionType")
    }

    fun trackPermissionDenied(permissionType: String) {
        val params = Bundle().apply {
            putString("permission_type", permissionType)
        }
        analytics?.logEvent("permission_denied", params)
        Log.d(TAG, "❌ Permission denied: $permissionType")
    }

    // ========== 에러 ==========

    fun trackError(errorType: String, errorMessage: String) {
        val params = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        analytics?.logEvent("app_error", params)
        Log.e(TAG, "❌ Error: $errorType - $errorMessage")
    }

    // ========== 사용자 속성 ==========

    fun setUserProperty(name: String, value: String) {
        analytics?.setUserProperty(name, value)
        Log.d(TAG, "👤 User property: $name = $value")
    }

    fun setUserGoal(goalSteps: Int) {
        setUserProperty("daily_goal", goalSteps.toString())
    }

    fun setUserPetType(petType: String) {
        setUserProperty("pet_type", petType)
    }

    fun setUserSubscriptionType(type: String) {
        setUserProperty("subscription_type", type)  // "paid", "promo", "free"
    }
}
