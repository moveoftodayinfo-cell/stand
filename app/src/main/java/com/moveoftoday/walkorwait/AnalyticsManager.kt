package com.moveoftoday.walkorwait

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal
import java.util.Currency

/**
 * Firebase Analytics 이벤트 추적 관리
 */
object AnalyticsManager {
    private const val TAG = "AnalyticsManager"
    private var analytics: FirebaseAnalytics? = null
    private var fbLogger: AppEventsLogger? = null

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

        // Facebook AppEventsLogger 초기화
        fbLogger = AppEventsLogger.newLogger(context)

        Log.d(TAG, "Analytics initialized (Firebase + Facebook)")
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
        0 to "survey_screen_time",
        1 to "survey_willpower",
        2 to "google_sign_in",
        3 to "pet_selection",
        4 to "pet_name",
        5 to "intro_explanation",
        6 to "permission_settings",
        7 to "accessibility",
        9 to "app_selection",
        10 to "block_test",
        11 to "goal_input",
        12 to "walking_test",
        13 to "how_it_works",
        15 to "control_days",
        16 to "block_time",
        17 to "payment",
        18 to "widget_setup"
    )

    fun trackSurveyScreenTime(answer: String) {
        val params = Bundle().apply {
            putString("answer", answer)
        }
        analytics?.logEvent("survey_screen_time", params)
        Log.d(TAG, "Survey screen time: $answer")
    }

    fun trackSurveyWillpower(answer: String) {
        val params = Bundle().apply {
            putString("answer", answer)
        }
        analytics?.logEvent("survey_willpower", params)
        Log.d(TAG, "Survey willpower: $answer")
    }

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
        // Facebook 전환 이벤트
        fbLogger?.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL)
        Log.d(TAG, "Tutorial complete (Firebase + Facebook)")
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
        // Facebook 전환 이벤트
        val fbParams = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_ORDER_ID, source)
        }
        fbLogger?.logEvent(AppEventsConstants.EVENT_NAME_SUBSCRIBE, fbParams)
        Log.d(TAG, "Subscription start: $source (Firebase + Facebook)")
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
        // Facebook 전환 이벤트 (금액 + 통화 포함)
        val fbParams = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, productId)
            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW")
        }
        fbLogger?.logPurchase(BigDecimal.valueOf(price), Currency.getInstance("KRW"), fbParams)
        Log.d(TAG, "Purchase: $productId - KRW $price (Firebase + Facebook)")
    }

    // ========== 챌린지 ==========

    fun trackChallengeStart(challengeType: String, challengeCategory: String) {
        val params = Bundle().apply {
            putString("challenge_type", challengeType)
            putString("challenge_category", challengeCategory)
        }
        analytics?.logEvent("challenge_start", params)
        Log.d(TAG, "🏃 Challenge start: $challengeType ($challengeCategory)")
    }

    fun trackChallengeComplete(challengeType: String, durationSeconds: Long, category: String) {
        val params = Bundle().apply {
            putString("challenge_type", challengeType)
            putLong("duration_seconds", durationSeconds)
            putString("challenge_category", category)
        }
        analytics?.logEvent("challenge_complete", params)
        Log.d(TAG, "✅ Challenge complete: $challengeType (${durationSeconds}s)")
    }

    fun trackChallengeAbandon(challengeType: String, reason: String, progressPercent: Int) {
        val params = Bundle().apply {
            putString("challenge_type", challengeType)
            putString("abandon_reason", reason)
            putInt("progress_percent", progressPercent)
        }
        analytics?.logEvent("challenge_abandon", params)
        Log.d(TAG, "❌ Challenge abandon: $challengeType - $reason ($progressPercent%)")
    }

    // ========== 펫 상호작용 ==========

    fun trackPetInteraction(interactionType: String, petType: String) {
        val params = Bundle().apply {
            putString("interaction_type", interactionType)  // touch, talk, feed, play
            putString("pet_type", petType)
        }
        analytics?.logEvent("pet_interaction", params)
        Log.d(TAG, "🐾 Pet interaction: $interactionType ($petType)")
    }

    fun trackPetEvolved(petType: String, fromStage: String, toStage: String, daysToEvolve: Int) {
        val params = Bundle().apply {
            putString("pet_type", petType)
            putString("from_stage", fromStage)
            putString("to_stage", toStage)
            putInt("days_to_evolve", daysToEvolve)
        }
        analytics?.logEvent("pet_evolved", params)
        Log.d(TAG, "🎉 Pet evolved: $petType $fromStage → $toStage ($daysToEvolve days)")
    }

    fun trackPetDialogue(dialogueType: String, petMood: String) {
        val params = Bundle().apply {
            putString("dialogue_type", dialogueType)  // greeting, encouragement, celebration
            putString("pet_mood", petMood)
        }
        analytics?.logEvent("pet_dialogue", params)
        Log.d(TAG, "💬 Pet dialogue: $dialogueType (mood: $petMood)")
    }

    fun trackPetSkinEquipped(skinId: String, skinCategory: String) {
        val params = Bundle().apply {
            putString("skin_id", skinId)
            putString("skin_category", skinCategory)
        }
        analytics?.logEvent("pet_skin_equipped", params)
        Log.d(TAG, "👕 Pet skin equipped: $skinId ($skinCategory)")
    }

    // ========== 리텐션 분석 ==========

    fun trackDailyActive(streakDays: Int, totalActiveDays: Int, daysSinceInstall: Int) {
        val params = Bundle().apply {
            putInt("streak_days", streakDays)
            putInt("total_active_days", totalActiveDays)
            putInt("days_since_install", daysSinceInstall)
        }
        analytics?.logEvent("daily_active", params)
        Log.d(TAG, "📊 Daily active: streak=$streakDays, total=$totalActiveDays, since_install=$daysSinceInstall")
    }

    fun trackWeeklyRetention(weekNumber: Int, activeDaysThisWeek: Int) {
        val params = Bundle().apply {
            putInt("week_number", weekNumber)
            putInt("active_days_this_week", activeDaysThisWeek)
        }
        analytics?.logEvent("weekly_retention", params)
        Log.d(TAG, "📈 Weekly retention: week=$weekNumber, active=$activeDaysThisWeek days")
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
