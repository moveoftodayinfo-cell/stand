package com.moveoftoday.walkorwait

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("WalkOrWait", Context.MODE_PRIVATE)

    fun saveGoal(goal: Int) {
        prefs.edit().putInt("daily_goal", goal).apply()
    }

    fun getGoal(): Int {
        return prefs.getInt("daily_goal", 8000)
    }

    // 목표 단위 (steps 또는 km)
    fun saveGoalUnit(unit: String) {
        prefs.edit().putString("goal_unit", unit).apply()
    }

    fun getGoalUnit(): String {
        return prefs.getString("goal_unit", "steps") ?: "steps"
    }

    fun saveTodaySteps(steps: Int) {
        prefs.edit().putInt("today_steps", steps).apply()
    }

    fun getTodaySteps(): Int {
        return prefs.getInt("today_steps", 0)
    }

    // 거리 관련 함수 (km 단위)
    fun saveTodayDistance(distanceKm: Double) {
        prefs.edit().putFloat("today_distance", distanceKm.toFloat()).apply()
    }

    fun getTodayDistance(): Double {
        return prefs.getFloat("today_distance", 0f).toDouble()
    }

    fun saveLockedApps(packageNames: Set<String>) {
        prefs.edit().putStringSet("locked_apps", packageNames).apply()
    }

    fun getLockedApps(): Set<String> {
        return prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
    }

    // 15분 타이머 관련
    fun saveAppStartTime(packageName: String, startTime: Long) {
        prefs.edit().putLong("start_time_$packageName", startTime).apply()
    }

    fun getAppStartTime(packageName: String): Long {
        return prefs.getLong("start_time_$packageName", 0)
    }

    fun saveAppUsedTime(packageName: String, usedTime: Long) {
        prefs.edit().putLong("used_time_$packageName", usedTime).apply()
    }

    fun getAppUsedTime(packageName: String): Long {
        return prefs.getLong("used_time_$packageName", 0)
    }

    fun isAppBlockedToday(packageName: String): Boolean {
        return prefs.getBoolean("blocked_$packageName", false)
    }

    fun setAppBlockedToday(packageName: String, blocked: Boolean) {
        prefs.edit().putBoolean("blocked_$packageName", blocked).apply()
    }

    fun resetDailyData() {
        val editor = prefs.edit()
        // 모든 타이머와 차단 상태 리셋
        val allKeys = prefs.all.keys
        allKeys.forEach { key ->
            if (key.startsWith("start_time_") ||
                key.startsWith("used_time_") ||
                key.startsWith("blocked_")) {
                editor.remove(key)
            }
        }
        // 긴급 모드도 리셋
        editor.putBoolean("emergency_mode", false)
        editor.putLong("emergency_start_time", 0)
        editor.apply()
    }

    fun getLastResetDate(): String {
        return prefs.getString("last_reset_date", "") ?: ""
    }

    fun saveLastResetDate(date: String) {
        prefs.edit().putString("last_reset_date", date).apply()
    }

    // 긴급 15분 모드
    fun saveEmergencyMode(enabled: Boolean) {
        prefs.edit().putBoolean("emergency_mode", enabled).apply()
    }

    fun isEmergencyMode(): Boolean {
        return prefs.getBoolean("emergency_mode", false)
    }

    fun saveEmergencyStartTime(time: Long) {
        prefs.edit().putLong("emergency_start_time", time).apply()
    }

    fun getEmergencyStartTime(): Long {
        return prefs.getLong("emergency_start_time", 0)
    }

    // 센서 초기값 저장/불러오기
    fun saveInitialSteps(steps: Int) {
        prefs.edit().putInt("initial_steps", steps).apply()
    }

    fun getInitialSteps(): Int {
        return prefs.getInt("initial_steps", -1)
    }

    // 목표 변경 제한 (첫 번째 변경은 제한 없이 허용)
    fun canDecreaseGoal(): Boolean {
        // 한 번도 변경한 적 없으면 허용 (튜토리얼 후 첫 변경)
        val hasEverChanged = prefs.getBoolean("has_ever_changed_goal", false)
        if (!hasEverChanged) return true

        val lastDecrease = prefs.getLong("last_goal_decrease", 0)
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        return lastDecrease < threeDaysAgo
    }

    fun saveGoalDecreaseTime() {
        prefs.edit()
            .putLong("last_goal_decrease", System.currentTimeMillis())
            .putBoolean("has_ever_changed_goal", true)
            .apply()
    }

    fun getNextGoalDecreaseDate(): String {
        val hasEverChanged = prefs.getBoolean("has_ever_changed_goal", false)
        if (!hasEverChanged) return "언제든지 가능"

        val lastDecrease = prefs.getLong("last_goal_decrease", 0)
        if (lastDecrease == 0L) return "언제든지 가능"

        val nextAllowed = lastDecrease + (3 * 24 * 60 * 60 * 1000L)
        val daysLeft = ((nextAllowed - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() + 1
        return if (daysLeft > 0) "${daysLeft}일 후" else "언제든지 가능"
    }

    // 앱 제거 제한
    fun canRemoveLockedApp(): Boolean {
        val lastRemove = prefs.getLong("last_app_remove", 0)
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        return lastRemove < threeDaysAgo
    }

    fun saveAppRemoveTime() {
        prefs.edit().putLong("last_app_remove", System.currentTimeMillis()).apply()
    }

    fun getNextAppRemoveDate(): String {
        val lastRemove = prefs.getLong("last_app_remove", 0)
        if (lastRemove == 0L) return "언제든지 가능"

        val nextAllowed = lastRemove + (3 * 24 * 60 * 60 * 1000L)
        val daysLeft = ((nextAllowed - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() + 1
        return if (daysLeft > 0) "${daysLeft}일 후" else "언제든지 가능"
    }

    // 제어 요일 변경 제한
    fun canChangeControlDays(): Boolean {
        val lastChange = prefs.getLong("last_control_days_change", 0)
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        return lastChange < threeDaysAgo
    }

    fun saveControlDaysChangeTime() {
        prefs.edit().putLong("last_control_days_change", System.currentTimeMillis()).apply()
    }

    fun getNextControlDaysChangeDate(): String {
        val lastChange = prefs.getLong("last_control_days_change", 0)
        if (lastChange == 0L) return "언제든지 가능"

        val nextAllowed = lastChange + (3 * 24 * 60 * 60 * 1000L)
        val daysLeft = ((nextAllowed - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() + 1
        return if (daysLeft > 0) "${daysLeft}일 후" else "언제든지 가능"
    }

    // 차단 시간대 변경 제한
    fun canChangeBlockingPeriods(): Boolean {
        val lastChange = prefs.getLong("last_blocking_periods_change", 0)
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        return lastChange < threeDaysAgo
    }

    fun saveBlockingPeriodsChangeTime() {
        prefs.edit().putLong("last_blocking_periods_change", System.currentTimeMillis()).apply()
    }

    fun getNextBlockingPeriodsChangeDate(): String {
        val lastChange = prefs.getLong("last_blocking_periods_change", 0)
        if (lastChange == 0L) return "언제든지 가능"

        val nextAllowed = lastChange + (3 * 24 * 60 * 60 * 1000L)
        val daysLeft = ((nextAllowed - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() + 1
        return if (daysLeft > 0) "${daysLeft}일 후" else "언제든지 가능"
    }

    // 보증금 시스템
    fun saveDeposit(amount: Int) {
        prefs.edit().putInt("deposit_amount", amount).apply()
    }

    fun getDeposit(): Int {
        return prefs.getInt("deposit_amount", 0)
    }

    fun saveControlStartDate(date: String) {
        prefs.edit().putString("control_start_date", date).apply()
    }

    fun getControlStartDate(): String {
        return prefs.getString("control_start_date", "") ?: ""
    }

    fun saveControlEndDate(date: String) {
        prefs.edit().putString("control_end_date", date).apply()
    }

    fun getControlEndDate(): String {
        return prefs.getString("control_end_date", "") ?: ""
    }

    fun saveControlDays(days: Set<Int>) {
        // 0=일요일, 1=월요일, ..., 6=토요일
        prefs.edit().putStringSet("control_days", days.map { it.toString() }.toSet()).apply()
    }

    fun getControlDays(): Set<Int> {
        val days = prefs.getStringSet("control_days", setOf("1", "2", "3", "4", "5")) ?: setOf("1", "2", "3", "4", "5")
        return days.map { it.toInt() }.toSet()
    }

    fun saveSuccessDays(days: Int) {
        prefs.edit().putInt("success_days", days).apply()
    }

    fun getSuccessDays(): Int {
        return prefs.getInt("success_days", 0)
    }

    fun incrementSuccessDay() {
        val current = getSuccessDays()
        saveSuccessDays(current + 1)
    }

    // 누적 통계
    fun getTotalStepsAllTime(): Long {
        return prefs.getLong("total_steps_all_time", 0L)
    }

    fun addToTotalSteps(steps: Int) {
        val current = getTotalStepsAllTime()
        prefs.edit().putLong("total_steps_all_time", current + steps).apply()
    }

    fun getConsecutiveDays(): Int {
        return prefs.getInt("consecutive_days", 0)
    }

    fun setConsecutiveDays(days: Int) {
        prefs.edit().putInt("consecutive_days", days).apply()
    }

    fun incrementConsecutiveDays() {
        val current = getConsecutiveDays()
        prefs.edit().putInt("consecutive_days", current + 1).apply()
    }

    fun resetConsecutiveDays() {
        prefs.edit().putInt("consecutive_days", 0).apply()
    }

    fun getTotalSavedMoney(): Int {
        return prefs.getInt("total_saved_money", 0)
    }

    fun addToSavedMoney(amount: Int) {
        val current = getTotalSavedMoney()
        prefs.edit().putInt("total_saved_money", current + amount).apply()
    }

    fun getTotalControlDays(): Int {
        // 제어 기간 내 선택한 요일 총 개수 계산
        val startDate = getControlStartDate()
        val endDate = getControlEndDate()
        val controlDays = getControlDays()

        if (startDate.isEmpty() || endDate.isEmpty()) return 0

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = sdf.parse(startDate) ?: return 0
            val end = sdf.parse(endDate) ?: return 0

            val calendar = Calendar.getInstance()
            calendar.time = start

            var count = 0
            while (!calendar.time.after(end)) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=일요일
                if (controlDays.contains(dayOfWeek)) {
                    count++
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            return count
        } catch (e: Exception) {
            return 0
        }
    }

    fun getRequiredSuccessDays(): Int {
        // 90% 계산
        val total = getTotalControlDays()
        return (total * 0.9).toInt()
    }

    fun savePreviousDeposit(amount: Int) {
        // 실패한 달 보증금 저장 (다음 달 성공 시 환급용)
        prefs.edit().putInt("previous_deposit", amount).apply()
    }

    fun getPreviousDeposit(): Int {
        return prefs.getInt("previous_deposit", 0)
    }

    fun isPaidDeposit(): Boolean {
        return prefs.getBoolean("is_paid_deposit", false)
    }

    fun setPaidDeposit(paid: Boolean) {
        prefs.edit().putBoolean("is_paid_deposit", paid).apply()
    }

    // 오늘이 제어 요일인지 체크
    fun isTodayControlDay(): Boolean {
        val controlDays = getControlDays()
        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1 // 0=일요일
        return controlDays.contains(dayOfWeek)
    }

    // 제어 기간 내인지 체크
    fun isInControlPeriod(): Boolean {
        val startDate = getControlStartDate()
        val endDate = getControlEndDate()

        if (startDate.isEmpty() || endDate.isEmpty()) return false

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance().time
            val start = sdf.parse(startDate) ?: return false
            val end = sdf.parse(endDate) ?: return false

            return !today.before(start) && !today.after(end)
        } catch (e: Exception) {
            return false
        }
    }

    // 어제 목표 달성 체크했는지
    fun getLastCheckDate(): String {
        return prefs.getString("last_check_date", "") ?: ""
    }

    fun saveLastCheckDate(date: String) {
        prefs.edit().putString("last_check_date", date).apply()
    }

    // 제어 기간 종료 여부
    fun isControlPeriodEnded(): Boolean {
        val endDate = getControlEndDate()
        if (endDate.isEmpty()) return false

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()
            val end = sdf.parse(endDate) ?: return false

            return today.time.after(end)
        } catch (e: Exception) {
            return false
        }
    }

    // 튜토리얼 완료 여부
    fun isTutorialCompleted(): Boolean {
        return prefs.getBoolean("tutorial_completed", false)
    }

    fun setTutorialCompleted(completed: Boolean) {
        prefs.edit().putBoolean("tutorial_completed", completed).apply()
    }

    // ✨ 오늘 목표 달성 체크 및 기록
    fun checkAndRecordTodaySuccess() {
        // 오늘이 제어 요일인지 확인
        if (!isTodayControlDay()) return

        // 제어 기간 내인지 확인
        if (!isInControlPeriod()) return

        // 이미 오늘 기록했는지 확인
        val lastSuccessDate = prefs.getString("last_success_date", "") ?: ""
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastSuccessDate == today) {
            // 이미 오늘 기록함
            return
        }

        // 목표 달성 시 기록
        val todaySteps = getTodaySteps()
        val goal = getGoal()

        if (todaySteps >= goal) {
            incrementSuccessDay()
            incrementConsecutiveDays() // 연속 달성일 증가
            prefs.edit().putString("last_success_date", today).apply()
            android.util.Log.d("PreferenceManager", "✅ Today SUCCESS recorded! Steps: $todaySteps >= Goal: $goal, Consecutive: ${getConsecutiveDays()}")
        }
    }

    fun getLastSuccessDate(): String {
        return prefs.getString("last_success_date", "") ?: ""
    }

    // Firebase 동기화 타임스탬프
    fun saveLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_sync_timestamp", timestamp).apply()
    }

    fun getLastSyncTimestamp(): Long {
        return prefs.getLong("last_sync_timestamp", 0L)
    }

    // 어제 걸음 수
    fun saveYesterdaySteps(steps: Int) {
        prefs.edit().putInt("yesterday_steps", steps).apply()
    }

    fun getYesterdaySteps(): Int {
        return prefs.getInt("yesterday_steps", 0)
    }

    // 걸음 수 리셋 날짜
    fun saveLastStepResetDate(date: String) {
        prefs.edit().putString("last_step_reset_date", date).apply()
    }

    fun getLastStepResetDate(): String {
        return prefs.getString("last_step_reset_date", "") ?: ""
    }

    // 차단 시간대 설정
    // "morning": 06:00-12:00, "afternoon": 12:00-18:00, "evening": 18:00-22:00, "night": 22:00-06:00
    fun saveBlockingPeriods(periods: Set<String>) {
        prefs.edit().putStringSet("blocking_periods", periods).apply()
    }

    fun getBlockingPeriods(): Set<String> {
        // 기본값: 모든 시간대 (24시간 차단)
        return prefs.getStringSet("blocking_periods", setOf("morning", "afternoon", "evening", "night"))
            ?: setOf("morning", "afternoon", "evening", "night")
    }

    fun isInBlockingPeriod(): Boolean {
        val periods = getBlockingPeriods()
        if (periods.isEmpty()) return false // 시간대 선택 안하면 차단 안함

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            hour in 6..11 && periods.contains("morning") -> true      // 아침: 06:00-11:59
            hour in 12..17 && periods.contains("afternoon") -> true   // 점심: 12:00-17:59
            hour in 18..21 && periods.contains("evening") -> true     // 저녁: 18:00-21:59
            (hour >= 22 || hour < 6) && periods.contains("night") -> true  // 밤: 22:00-05:59
            else -> false
        }
    }

    // 3일 무료 체험 기간
    fun saveTrialStartDate(date: String) {
        prefs.edit().putString("trial_start_date", date).apply()
    }

    fun getTrialStartDate(): String {
        return prefs.getString("trial_start_date", "") ?: ""
    }

    fun saveTrialEndDate(date: String) {
        prefs.edit().putString("trial_end_date", date).apply()
    }

    fun getTrialEndDate(): String {
        return prefs.getString("trial_end_date", "") ?: ""
    }

    fun isInTrialPeriod(): Boolean {
        val trialEndDate = getTrialEndDate()
        if (trialEndDate.isEmpty()) return false

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance().time
            val endDate = sdf.parse(trialEndDate) ?: return false

            return !today.after(endDate)
        } catch (e: Exception) {
            return false
        }
    }

    fun getTrialDaysRemaining(): Int {
        val trialEndDate = getTrialEndDate()
        if (trialEndDate.isEmpty()) return 0

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()
            val endDate = sdf.parse(trialEndDate) ?: return 0
            val endCalendar = Calendar.getInstance()
            endCalendar.time = endDate

            val diffInMillis = endCalendar.timeInMillis - today.timeInMillis
            val daysRemaining = (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1

            return if (daysRemaining > 0) daysRemaining else 0
        } catch (e: Exception) {
            return 0
        }
    }

    // ===== 걸음 속도 추적 기능 =====

    // 걸음 수 기록 저장 (timestamp:steps 형식)
    fun saveStepRecord(timestamp: Long, steps: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordKey = "step_record_$today"

        val existingRecords = prefs.getString(recordKey, "") ?: ""
        val newRecord = "$timestamp:$steps"

        val updatedRecords = if (existingRecords.isEmpty()) {
            newRecord
        } else {
            "$existingRecords,$newRecord"
        }

        prefs.edit().putString(recordKey, updatedRecords).apply()
    }

    // 거리 기록 저장 (timestamp:distance 형식, km 단위)
    fun saveDistanceRecord(timestamp: Long, distanceKm: Double) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordKey = "distance_record_$today"

        val existingRecords = prefs.getString(recordKey, "") ?: ""
        val newRecord = "$timestamp:$distanceKm"

        val updatedRecords = if (existingRecords.isEmpty()) {
            newRecord
        } else {
            "$existingRecords,$newRecord"
        }

        prefs.edit().putString(recordKey, updatedRecords).apply()
    }

    // 오늘의 걸음 기록 가져오기
    private fun getStepRecords(): List<Pair<Long, Int>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordKey = "step_record_$today"
        val records = prefs.getString(recordKey, "") ?: ""

        if (records.isEmpty()) return emptyList()

        return records.split(",").mapNotNull { record ->
            val parts = record.split(":")
            if (parts.size == 2) {
                try {
                    Pair(parts[0].toLong(), parts[1].toInt())
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    // 오늘의 거리 기록 가져오기
    private fun getDistanceRecords(): List<Pair<Long, Double>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordKey = "distance_record_$today"
        val records = prefs.getString(recordKey, "") ?: ""

        if (records.isEmpty()) return emptyList()

        return records.split(",").mapNotNull { record ->
            val parts = record.split(":")
            if (parts.size == 2) {
                try {
                    Pair(parts[0].toLong(), parts[1].toDouble())
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    // 평균 걸음 속도 계산 (걸음/시간)
    fun getAverageStepsPerHour(): Double {
        val records = getStepRecords()
        if (records.size < 2) return 0.0

        // 최근 2시간 데이터 사용
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        val recentRecords = records.filter { it.first >= twoHoursAgo }

        if (recentRecords.size < 2) {
            // 2시간 데이터가 없으면 전체 데이터 사용
            val firstRecord = records.first()
            val lastRecord = records.last()
            val timeDiff = (lastRecord.first - firstRecord.first) / 1000.0 / 3600.0 // 시간 단위
            val stepDiff = lastRecord.second - firstRecord.second

            return if (timeDiff > 0) stepDiff / timeDiff else 0.0
        }

        val firstRecord = recentRecords.first()
        val lastRecord = recentRecords.last()
        val timeDiff = (lastRecord.first - firstRecord.first) / 1000.0 / 3600.0 // 시간 단위
        val stepDiff = lastRecord.second - firstRecord.second

        return if (timeDiff > 0) stepDiff / timeDiff else 0.0
    }

    // 평균 거리 속도 계산 (km/시간)
    fun getAverageDistancePerHour(): Double {
        val records = getDistanceRecords()
        if (records.size < 2) return 0.0

        // 최근 2시간 데이터 사용
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        val recentRecords = records.filter { it.first >= twoHoursAgo }

        if (recentRecords.size < 2) {
            // 2시간 데이터가 없으면 전체 데이터 사용
            val firstRecord = records.first()
            val lastRecord = records.last()
            val timeDiff = (lastRecord.first - firstRecord.first) / 1000.0 / 3600.0 // 시간 단위
            val distanceDiff = lastRecord.second - firstRecord.second

            return if (timeDiff > 0) distanceDiff / timeDiff else 0.0
        }

        val firstRecord = recentRecords.first()
        val lastRecord = recentRecords.last()
        val timeDiff = (lastRecord.first - firstRecord.first) / 1000.0 / 3600.0 // 시간 단위
        val distanceDiff = lastRecord.second - firstRecord.second

        return if (timeDiff > 0) distanceDiff / timeDiff else 0.0
    }

    // 목표까지 예상 시간 계산 (분 단위) - 단위에 따라 자동 선택
    fun getEstimatedTimeToGoal(): Int {
        val goalUnit = getGoalUnit()

        return if (goalUnit == "km") {
            // km 모드
            val currentDistance = getTodayDistance()
            val goal = getGoal() // goal은 여전히 Int이므로 km로 해석
            val remainingDistance = goal - currentDistance

            if (remainingDistance <= 0) return 0

            val avgKmPerHour = getAverageDistancePerHour()
            if (avgKmPerHour <= 0) {
                // 평균 속도 데이터가 없으면 일반적인 걷기 속도 사용 (시속 4km)
                return ((remainingDistance / 4.0) * 60).toInt()
            }

            val hoursNeeded = remainingDistance / avgKmPerHour
            (hoursNeeded * 60).toInt() // 분 단위로 변환
        } else {
            // 걸음 수 모드
            val currentSteps = getTodaySteps()
            val goal = getGoal()
            val remainingSteps = goal - currentSteps

            if (remainingSteps <= 0) return 0

            val avgStepsPerHour = getAverageStepsPerHour()
            if (avgStepsPerHour <= 0) {
                // 평균 속도 데이터가 없으면 일반적인 걷기 속도 사용 (100걸음/분)
                return (remainingSteps / 100.0).toInt()
            }

            val hoursNeeded = remainingSteps / avgStepsPerHour
            (hoursNeeded * 60).toInt() // 분 단위로 변환
        }
    }

    // 예상 도달 시간 문자열로 반환
    fun getEstimatedArrivalTime(): String {
        val minutes = getEstimatedTimeToGoal()
        if (minutes <= 0) return "달성!"

        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours == 0 -> "${remainingMinutes}분 후"
            remainingMinutes == 0 -> "${hours}시간 후"
            else -> "${hours}시간 ${remainingMinutes}분 후"
        }
    }

    // 현재 진행 상황 가져오기 (목표와 비교용 - 항상 걸음 수 기준)
    fun getCurrentProgress(): Double {
        return if (getGoalUnit() == "km") {
            // km를 걸음 수로 환산해서 비교 (1km = 1300보)
            getTodayDistance() * 1300
        } else {
            getTodaySteps().toDouble()
        }
    }

    // 현재 진행 상황 표시용 (단위에 맞게 변환)
    fun getCurrentProgressForDisplay(): Double {
        return if (getGoalUnit() == "km") {
            getTodayDistance() // km 그대로 반환
        } else {
            getTodaySteps().toDouble() // 걸음 수 그대로 반환
        }
    }

    // 목표 표시용 (단위에 맞게 변환)
    fun getGoalForDisplay(): Double {
        val goal = getGoal()
        return if (getGoalUnit() == "km") {
            goal / 1300.0 // 걸음 수를 km로 변환
        } else {
            goal.toDouble()
        }
    }

    // 남은 수치 가져오기 (단위에 맞게)
    fun getRemainingToGoal(): Double {
        val goal = getGoal()
        return if (getGoalUnit() == "km") {
            (goal / 1300.0) - getTodayDistance() // km 단위로 계산
        } else {
            (goal - getTodaySteps()).toDouble()
        }
    }

    // ===== 프로모션 코드 관련 =====

    // 사용한 프로모션 코드 목록
    fun isPromoCodeUsed(code: String): Boolean {
        val usedCodes = prefs.getStringSet("used_promo_codes", emptySet()) ?: emptySet()
        return usedCodes.contains(code)
    }

    fun saveUsedPromoCode(code: String) {
        val usedCodes = prefs.getStringSet("used_promo_codes", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        usedCodes.add(code)
        prefs.edit().putStringSet("used_promo_codes", usedCodes).apply()
        prefs.edit().putString("applied_promo_code", code).apply()
    }

    fun getAppliedPromoCode(): String? {
        return prefs.getString("applied_promo_code", null)
    }

    fun savePromoCodeType(type: String) {
        prefs.edit().putString("promo_code_type", type).apply()
    }

    fun getPromoCodeType(): String? {
        return prefs.getString("promo_code_type", null)
    }

    fun savePromoHostId(hostId: String) {
        prefs.edit().putString("promo_host_id", hostId).apply()
    }

    fun getPromoHostId(): String? {
        return prefs.getString("promo_host_id", null)
    }

    fun clearPromoCode() {
        prefs.edit()
            .remove("applied_promo_code")
            .remove("promo_code_type")
            .remove("promo_host_id")
            .apply()
    }

    // 프로모션 무료 기간 종료일
    fun savePromoFreeEndDate(date: String) {
        prefs.edit().putString("promo_free_end_date", date).apply()
    }

    fun getPromoFreeEndDate(): String? {
        return prefs.getString("promo_free_end_date", null)
    }

    fun isInPromoFreePeriod(): Boolean {
        val endDate = getPromoFreeEndDate() ?: return false
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val end = sdf.parse(endDate) ?: return false
            return !Date().after(end)
        } catch (e: Exception) {
            return false
        }
    }

    // ===== Health Connect 설정 =====

    // Health Connect 사용 여부 (연결 완료 시 true로 설정)
    fun setUseHealthConnect(use: Boolean) {
        prefs.edit().putBoolean("use_health_connect", use).apply()
    }

    fun useHealthConnect(): Boolean {
        return prefs.getBoolean("use_health_connect", false)
    }

    // Health Connect 연결 상태
    fun setHealthConnectConnected(connected: Boolean) {
        prefs.edit().putBoolean("health_connect_connected", connected).apply()
    }

    fun isHealthConnectConnected(): Boolean {
        return prefs.getBoolean("health_connect_connected", false)
    }

    // 연결된 피트니스 앱 이름 저장
    fun setConnectedFitnessAppName(name: String) {
        prefs.edit().putString("connected_fitness_app_name", name).apply()
    }

    fun getConnectedFitnessAppName(): String {
        return prefs.getString("connected_fitness_app_name", "") ?: ""
    }

    // Health Connect 연결 해제
    fun disconnectHealthConnect() {
        prefs.edit()
            .putBoolean("use_health_connect", false)
            .putBoolean("health_connect_connected", false)
            .putString("connected_fitness_app_name", "")
            .apply()
    }

    // ===== 연속 달성 (Streak) =====

    // 현재 연속 달성 일수
    fun getStreak(): Int {
        return prefs.getInt("streak_count", 0)
    }

    fun setStreak(count: Int) {
        prefs.edit().putInt("streak_count", count).apply()
    }

    // 마지막 달성 날짜
    fun getLastAchievedDate(): String {
        return prefs.getString("last_achieved_date", "") ?: ""
    }

    fun setLastAchievedDate(date: String) {
        prefs.edit().putString("last_achieved_date", date).apply()
    }

    // 오늘 달성 축하 다이얼로그를 이미 봤는지
    fun hasSeenStreakCelebrationToday(): Boolean {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return prefs.getString("last_streak_celebration_date", "") == today
    }

    fun setStreakCelebrationSeen() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        prefs.edit().putString("last_streak_celebration_date", today).apply()
    }

    // 목표 달성 시 호출 - 연속 달성 업데이트
    fun updateStreakOnGoalAchieved(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastAchievedDate = getLastAchievedDate()

        if (lastAchievedDate == today) {
            // 오늘 이미 달성함 - 변경 없음
            return getStreak()
        }

        val currentStreak = getStreak()
        val newStreak: Int

        newStreak = if (lastAchievedDate.isEmpty()) {
            // 첫 달성
            1
        } else {
            // 어제 달성했는지 확인
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            try {
                val lastDate = sdf.parse(lastAchievedDate)
                val todayDate = sdf.parse(today)
                val diffInDays = ((todayDate?.time ?: 0) - (lastDate?.time ?: 0)) / (1000 * 60 * 60 * 24)

                if (diffInDays == 1L) {
                    // 연속 달성
                    currentStreak + 1
                } else {
                    // 연속 끊김 - 다시 1부터
                    1
                }
            } catch (e: Exception) {
                1
            }
        }

        setStreak(newStreak)
        setLastAchievedDate(today)
        return newStreak
    }

    // 이번 주 달성 기록 (일~토)
    fun getWeeklyAchievements(): List<Boolean> {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()

        // 이번 주 일요일로 이동
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)

        val achievements = mutableListOf<Boolean>()
        val today = sdf.format(java.util.Date())
        val lastAchievedDate = getLastAchievedDate()

        // 일~토 7일간 체크
        for (i in 0 until 7) {
            val dateStr = sdf.format(calendar.time)
            val achieved = when {
                dateStr == today && getCurrentProgress() >= getGoal() -> true
                dateStr == lastAchievedDate -> true
                dateStr < today -> {
                    // 과거 날짜는 streak 기록으로 추정
                    val daysDiff = ((sdf.parse(today)?.time ?: 0) - (sdf.parse(dateStr)?.time ?: 0)) / (1000 * 60 * 60 * 24)
                    daysDiff <= getStreak()
                }
                else -> false // 미래 날짜
            }
            achievements.add(achieved)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return achievements
    }
}