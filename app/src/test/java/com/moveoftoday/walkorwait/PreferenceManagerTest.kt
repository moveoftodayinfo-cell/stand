package com.moveoftoday.walkorwait

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PreferenceManager 단위 테스트
 *
 * SharedPreferences 기반 데이터 저장/불러오기 검증
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreferenceManagerTest {

    private lateinit var context: Context
    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferenceManager = PreferenceManager(context)
        // 테스트 전 데이터 초기화
        clearPreferences()
    }

    private fun clearPreferences() {
        val prefs = context.getSharedPreferences("WalkOrWait", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // ===== 목표 설정 테스트 =====

    @Test
    fun `목표 저장 및 불러오기`() {
        preferenceManager.saveGoal(10000)
        assertEquals(10000, preferenceManager.getGoal())
    }

    @Test
    fun `목표 기본값은 8000`() {
        assertEquals(8000, preferenceManager.getGoal())
    }

    @Test
    fun `목표 여러번 변경`() {
        preferenceManager.saveGoal(5000)
        assertEquals(5000, preferenceManager.getGoal())

        preferenceManager.saveGoal(15000)
        assertEquals(15000, preferenceManager.getGoal())
    }

    // ===== 걸음 수 테스트 =====

    @Test
    fun `오늘 걸음 수 저장 및 불러오기`() {
        preferenceManager.saveTodaySteps(5432)
        assertEquals(5432, preferenceManager.getTodaySteps())
    }

    @Test
    fun `걸음 수 기본값은 0`() {
        assertEquals(0, preferenceManager.getTodaySteps())
    }

    @Test
    fun `걸음 수 업데이트`() {
        preferenceManager.saveTodaySteps(1000)
        preferenceManager.saveTodaySteps(2000)
        assertEquals(2000, preferenceManager.getTodaySteps())
    }

    // ===== 목표 단위 테스트 =====

    @Test
    fun `목표 단위 기본값은 steps`() {
        assertEquals("steps", preferenceManager.getGoalUnit())
    }

    @Test
    fun `목표 단위 km로 변경`() {
        preferenceManager.saveGoalUnit("km")
        assertEquals("km", preferenceManager.getGoalUnit())
    }

    // ===== 앱 잠금 테스트 =====

    @Test
    fun `잠금 앱 저장 및 불러오기`() {
        val apps = setOf("com.youtube", "com.instagram", "com.tiktok")
        preferenceManager.saveLockedApps(apps)
        assertEquals(apps, preferenceManager.getLockedApps())
    }

    @Test
    fun `잠금 앱 기본값은 빈 Set`() {
        assertTrue(preferenceManager.getLockedApps().isEmpty())
    }

    @Test
    fun `잠금 앱 추가 및 제거`() {
        preferenceManager.saveLockedApps(setOf("com.youtube"))
        assertEquals(1, preferenceManager.getLockedApps().size)

        preferenceManager.saveLockedApps(setOf("com.youtube", "com.instagram"))
        assertEquals(2, preferenceManager.getLockedApps().size)

        preferenceManager.saveLockedApps(emptySet())
        assertTrue(preferenceManager.getLockedApps().isEmpty())
    }

    // ===== 긴급 모드 테스트 =====

    @Test
    fun `긴급 모드 기본값은 false`() {
        assertFalse(preferenceManager.isEmergencyMode())
    }

    @Test
    fun `긴급 모드 활성화`() {
        preferenceManager.saveEmergencyMode(true)
        assertTrue(preferenceManager.isEmergencyMode())
    }

    @Test
    fun `긴급 모드 비활성화`() {
        preferenceManager.saveEmergencyMode(true)
        preferenceManager.saveEmergencyMode(false)
        assertFalse(preferenceManager.isEmergencyMode())
    }

    @Test
    fun `긴급 모드 시작 시간 저장`() {
        val startTime = System.currentTimeMillis()
        preferenceManager.saveEmergencyStartTime(startTime)
        assertEquals(startTime, preferenceManager.getEmergencyStartTime())
    }

    // ===== 보증금 테스트 =====

    @Test
    fun `보증금 저장 및 불러오기`() {
        preferenceManager.saveDeposit(4900)
        assertEquals(4900, preferenceManager.getDeposit())
    }

    @Test
    fun `보증금 기본값은 0`() {
        assertEquals(0, preferenceManager.getDeposit())
    }

    // ===== 성공일 테스트 =====

    @Test
    fun `성공일 증가`() {
        assertEquals(0, preferenceManager.getSuccessDays())

        preferenceManager.incrementSuccessDay()
        assertEquals(1, preferenceManager.getSuccessDays())

        preferenceManager.incrementSuccessDay()
        assertEquals(2, preferenceManager.getSuccessDays())
    }

    @Test
    fun `성공일 직접 설정`() {
        preferenceManager.saveSuccessDays(10)
        assertEquals(10, preferenceManager.getSuccessDays())
    }

    // ===== 연속 달성일 테스트 =====

    @Test
    fun `연속 달성일 증가`() {
        assertEquals(0, preferenceManager.getConsecutiveDays())

        preferenceManager.incrementConsecutiveDays()
        assertEquals(1, preferenceManager.getConsecutiveDays())
    }

    @Test
    fun `연속 달성일 리셋`() {
        preferenceManager.setConsecutiveDays(5)
        preferenceManager.resetConsecutiveDays()
        assertEquals(0, preferenceManager.getConsecutiveDays())
    }

    // ===== 누적 걸음 수 테스트 =====

    @Test
    fun `누적 걸음 수 추가`() {
        assertEquals(0L, preferenceManager.getTotalStepsAllTime())

        preferenceManager.addToTotalSteps(5000)
        assertEquals(5000L, preferenceManager.getTotalStepsAllTime())

        preferenceManager.addToTotalSteps(3000)
        assertEquals(8000L, preferenceManager.getTotalStepsAllTime())
    }

    // ===== 제어 요일 테스트 =====

    @Test
    fun `제어 요일 기본값은 평일`() {
        val controlDays = preferenceManager.getControlDays()
        // 1=월요일, 2=화요일, 3=수요일, 4=목요일, 5=금요일
        assertTrue(controlDays.containsAll(setOf(1, 2, 3, 4, 5)))
        assertEquals(5, controlDays.size)
    }

    @Test
    fun `제어 요일 변경`() {
        val weekendOnly = setOf(0, 6) // 일요일, 토요일
        preferenceManager.saveControlDays(weekendOnly)
        assertEquals(weekendOnly, preferenceManager.getControlDays())
    }

    // ===== 차단 시간대 테스트 =====

    @Test
    fun `차단 시간대 기본값은 24시간`() {
        val periods = preferenceManager.getBlockingPeriods()
        assertTrue(periods.contains("morning"))
        assertTrue(periods.contains("afternoon"))
        assertTrue(periods.contains("evening"))
        assertTrue(periods.contains("night"))
    }

    @Test
    fun `차단 시간대 변경`() {
        val eveningOnly = setOf("evening")
        preferenceManager.saveBlockingPeriods(eveningOnly)
        assertEquals(eveningOnly, preferenceManager.getBlockingPeriods())
    }

    // ===== 튜토리얼 테스트 =====

    @Test
    fun `튜토리얼 기본값은 미완료`() {
        assertFalse(preferenceManager.isTutorialCompleted())
    }

    @Test
    fun `튜토리얼 완료 설정`() {
        preferenceManager.setTutorialCompleted(true)
        assertTrue(preferenceManager.isTutorialCompleted())
    }

    // ===== 결제 상태 테스트 =====

    @Test
    fun `결제 상태 기본값은 false`() {
        assertFalse(preferenceManager.isPaidDeposit())
    }

    @Test
    fun `결제 상태 변경`() {
        preferenceManager.setPaidDeposit(true)
        assertTrue(preferenceManager.isPaidDeposit())
    }

    // ===== Health Connect 테스트 =====

    @Test
    fun `Health Connect 기본값은 미사용`() {
        assertFalse(preferenceManager.useHealthConnect())
    }

    @Test
    fun `Health Connect 사용 설정`() {
        preferenceManager.setUseHealthConnect(true)
        assertTrue(preferenceManager.useHealthConnect())
    }

    @Test
    fun `Health Connect 연결 해제`() {
        preferenceManager.setUseHealthConnect(true)
        preferenceManager.setHealthConnectConnected(true)
        preferenceManager.setConnectedFitnessAppName("Samsung Health")

        preferenceManager.disconnectHealthConnect()

        assertFalse(preferenceManager.useHealthConnect())
        assertFalse(preferenceManager.isHealthConnectConnected())
        assertEquals("", preferenceManager.getConnectedFitnessAppName())
    }

    // ===== 일일 데이터 리셋 테스트 =====

    @Test
    fun `일일 데이터 리셋시 긴급 모드 초기화`() {
        preferenceManager.saveEmergencyMode(true)
        preferenceManager.saveEmergencyStartTime(System.currentTimeMillis())

        preferenceManager.resetDailyData()

        assertFalse(preferenceManager.isEmergencyMode())
        assertEquals(0L, preferenceManager.getEmergencyStartTime())
    }

    // ===== 절약 금액 테스트 =====

    @Test
    fun `절약 금액 누적`() {
        assertEquals(0, preferenceManager.getTotalSavedMoney())

        preferenceManager.addToSavedMoney(4900)
        assertEquals(4900, preferenceManager.getTotalSavedMoney())

        preferenceManager.addToSavedMoney(2400)
        assertEquals(7300, preferenceManager.getTotalSavedMoney())
    }

    // ===== 동기화 타임스탬프 테스트 =====

    @Test
    fun `동기화 타임스탬프 저장`() {
        val timestamp = System.currentTimeMillis()
        preferenceManager.saveLastSyncTimestamp(timestamp)
        assertEquals(timestamp, preferenceManager.getLastSyncTimestamp())
    }

    @Test
    fun `동기화 타임스탬프 기본값은 0`() {
        assertEquals(0L, preferenceManager.getLastSyncTimestamp())
    }
}
