package com.moveoftoday.walkorwait

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.theme.WalkorWaitTheme
import com.moveoftoday.walkorwait.ui.components.*
import com.moveoftoday.walkorwait.pet.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.blur
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var repository: UserDataRepository
    private lateinit var preferenceManager: PreferenceManager
    private var stepCount = mutableIntStateOf(0)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startService()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 허용 여부와 상관없이 계속 진행
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== onCreate START ===")

        try {
            // Application에서 Repository 가져오기
            Log.d(TAG, "Getting Repository from Application")
            val app = application as WalkorWaitApp
            repository = app.userDataRepository
            preferenceManager = PreferenceManager(this) // 하위 호환성을 위해 유지

            Log.d(TAG, "Loading today steps")
            stepCount.intValue = repository.getTodaySteps()
            Log.d(TAG, "Today steps: ${stepCount.intValue}")

            Log.d(TAG, "Initializing StepSensorManager")
            stepSensorManager = StepSensorManager(this)

            Log.d(TAG, "Setting up UI")
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.WHITE
                ),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.WHITE
                )
            )
            setContent {
                Log.d(TAG, "Inside setContent")
                WalkorWaitTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        WalkOrWaitScreen(
                            steps = stepCount.intValue,
                            preferenceManager = preferenceManager,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            Log.d(TAG, "Checking and resetting daily")
            checkAndResetDaily()

            Log.d(TAG, "Checking permissions")
            checkPermissionAndStart()

            Log.d(TAG, "Requesting notification permission")
            requestNotificationPermission()

            Log.d(TAG, "Starting service")
            StepCounterService.start(this)

            Log.d(TAG, "=== onCreate COMPLETE ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndResetDaily()
    }

    private fun checkAndResetDaily() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastReset = preferenceManager.getLastResetDate()

        if (lastReset != today) {
            // 새로운 날
            val lastCheckDate = preferenceManager.getLastCheckDate()
            if (lastCheckDate != today && lastCheckDate.isNotEmpty()) {
                checkYesterdayGoal(lastCheckDate)
            }

            stepCount.intValue = 0
            repository.saveTodaySteps(0) // Repository를 통해 Firebase에도 자동 저장
            preferenceManager.resetDailyData()
            preferenceManager.saveLastResetDate(today)
            stepSensorManager.resetDailySteps()

            StepWidgetProvider.updateAllWidgets(this)

            preferenceManager.saveLastCheckDate(today)

            if (preferenceManager.isControlPeriodEnded()) {
                handlePeriodEnd()
            }
        }
    }

    private fun checkYesterdayGoal(yesterday: String) {
        val yesterdaySteps = repository.getTodaySteps()
        val goal = repository.getGoal()

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val yesterdayDate = sdf.parse(yesterday) ?: return
            val calendar = java.util.Calendar.getInstance()
            calendar.time = yesterdayDate

            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
            val controlDays = repository.getControlDays()

            val isControlDay = controlDays.contains(dayOfWeek)
            val isSuccess = yesterdaySteps >= goal

            // 누적 통계 업데이트 - 어제 걸음 수를 총 걸음에 추가
            if (yesterdaySteps > 0) {
                preferenceManager.addToTotalSteps(yesterdaySteps)
                Log.d(TAG, "📊 Added $yesterdaySteps to total steps. New total: ${preferenceManager.getTotalStepsAllTime()}")
            }

            // Firebase에 일일 기록 저장 (구독 관련은 제외, 일반 걸음 수만 저장)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Repository를 통해 자동으로 Firebase에 저장됨
                    Log.d(TAG, "✅ Daily record auto-synced to Firebase: $yesterday")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to save daily record: ${e.message}")
                }
            }

            if (isControlDay) {
                if (isSuccess) {
                    repository.saveSuccessDays(repository.getSuccessDays() + 1)
                    Log.d(TAG, "✅ Yesterday SUCCESS: $yesterdaySteps >= $goal")
                } else {
                    // 목표 미달성 시 연속 달성일 리셋
                    preferenceManager.resetConsecutiveDays()
                    Log.d(TAG, "❌ Yesterday FAILED: $yesterdaySteps < $goal. Consecutive days reset.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking yesterday: ${e.message}")
        }
    }

    private fun handlePeriodEnd() {
        val totalDays = preferenceManager.getTotalControlDays()
        val successDays = repository.getSuccessDays()
        val requiredDays = preferenceManager.getRequiredSuccessDays()

        val successRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

        Log.d(TAG, "📊 Period ended: $successDays/$totalDays (${successRate.toInt()}%)")

        // Firebase에 월말 결과 저장 및 다음 달 구독 생성
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonthId = sdf.format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subscriptionManager = SubscriptionManager(this@MainActivity)
                val result = subscriptionManager.processMonthlyResult(
                    currentMonthId = currentMonthId,
                    totalDays = totalDays,
                    successDays = successDays
                )

                if (result.isSuccess) {
                    Log.d(TAG, "✅ Monthly result processed successfully")
                } else {
                    Log.e(TAG, "❌ Failed to process monthly result: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing monthly result: ${e.message}")
            }
        }

        // 로컬 데이터 처리
        if (successDays >= requiredDays) {
            Log.d(TAG, "🎉 SUCCESS! Next month exempt")
        } else {
            Log.d(TAG, "❌ FAILED! Deposit charged")
            val deposit = repository.getDeposit()
            preferenceManager.savePreviousDeposit(deposit)
        }

        // 다음 달을 위해 초기화
        repository.saveSuccessDays(0)
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startService()
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        } else {
            startService()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun startService() {
        StepCounterService.start(this)
    }

    override fun onPause() {
        super.onPause()
        StepWidgetProvider.updateAllWidgets(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun WalkOrWaitScreen(
    modifier: Modifier = Modifier,
    steps: Int = 0,
    preferenceManager: PreferenceManager? = null
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    // 설정 완료 상태 체크 - 새 16단계 통합 튜토리얼 사용
    val isTutorialCompleted = remember { preferenceManager?.isTutorialCompleted() ?: false }
    val isPaidDeposit = remember { preferenceManager?.isPaidDeposit() ?: false }
    val needsRealGoal = remember { preferenceManager?.needsRealGoalSetup() ?: false }

    // 설정 플로우 상태 - 16단계 통합 튜토리얼
    var showPetOnboarding by remember { mutableStateOf(!isTutorialCompleted) }
    var showRealGoalSetup by remember { mutableStateOf(needsRealGoal && isTutorialCompleted) }
    var showWidgetRecommendation by remember { mutableStateOf(false) }

    // Get pet info for main screen - mutableState로 변경하여 튜토리얼 후 업데이트 반영
    var petTypeName by remember { mutableStateOf(preferenceManager?.getPetType() ?: "DOG1") }
    var petType by remember { mutableStateOf(PetType.entries.find { it.name == petTypeName } ?: PetType.DOG1) }
    var petName by remember { mutableStateOf(preferenceManager?.getPetName() ?: "멍이") }

    // 1. Pet onboarding - 16단계 통합 튜토리얼 (펫 선택부터 결제/위젯까지)
    if (showPetOnboarding) {
        PetOnboardingScreen(
            onComplete = { selectedPetType, selectedPetName ->
                preferenceManager?.savePetType(selectedPetType.name)
                preferenceManager?.savePetName(selectedPetName)
                preferenceManager?.savePetHappiness(3)
                // 위젯 업데이트
                StepWidgetProvider.updateAllWidgets(context)
                // 선택한 펫 정보 업데이트
                petType = selectedPetType
                petName = selectedPetName
                petTypeName = selectedPetType.name
                showPetOnboarding = false
                // 튜토리얼 완료 후 실제 목표 설정 화면 표시
                showRealGoalSetup = true
            },
            hapticManager = hapticManager,
            preferenceManager = preferenceManager
        )
        return
    }

    // 2. 실제 목표 설정 (튜토리얼 완료 후)
    if (showRealGoalSetup) {
        RealGoalSetupScreen(
            petType = petType,
            petName = petName,
            preferenceManager = preferenceManager,
            hapticManager = hapticManager,
            onComplete = {
                preferenceManager?.setNeedsRealGoalSetup(false)
                showRealGoalSetup = false
            }
        )
        return
    }

    // 위젯 추천 다이얼로그
    if (showWidgetRecommendation) {
        WidgetRecommendationDialog(
            onDismiss = { showWidgetRecommendation = false },
            hapticManager = hapticManager
        )
    }

    // 단위에 따라 자동 전환
    val goalUnit = remember { preferenceManager?.getGoalUnit() ?: "steps" }
    var currentProgress by remember { mutableDoubleStateOf(preferenceManager?.getCurrentProgress() ?: 0.0) } // 비교용 (걸음 수 기준)
    var currentProgressDisplay by remember { mutableDoubleStateOf(preferenceManager?.getCurrentProgressForDisplay() ?: 0.0) } // 표시용
    var goal by remember { mutableIntStateOf(preferenceManager?.getGoal() ?: 8000) }
    val goalDisplay = remember { preferenceManager?.getGoalForDisplay() ?: 8000.0 } // 표시용
    var showSettingsScreen by remember { mutableStateOf(false) }

    var previousGoalAchieved by remember { mutableStateOf(false) }
    var triggerCelebration by remember { mutableStateOf(false) }

    // 연속 달성 (Streak) 관련
    var showStreakCelebration by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableIntStateOf(preferenceManager?.getStreak() ?: 0) }
    var weeklyAchievements by remember { mutableStateOf(preferenceManager?.getWeeklyAchievements() ?: List(7) { false }) }

    // 연속 달성 축하 다이얼로그
    if (showStreakCelebration) {
        StreakCelebrationDialog(
            streakCount = currentStreak,
            weeklyAchievements = weeklyAchievements,
            onDismiss = {
                preferenceManager?.setStreakCelebrationSeen()
                showStreakCelebration = false
            },
            hapticManager = hapticManager
        )
    }

    var deposit by remember { mutableIntStateOf(preferenceManager?.getDeposit() ?: 0) }
    var successDays by remember { mutableIntStateOf(preferenceManager?.getSuccessDays() ?: 0) }
    var totalDays by remember { mutableIntStateOf(preferenceManager?.getTotalControlDays() ?: 0) }
    var requiredDays by remember { mutableIntStateOf(preferenceManager?.getRequiredSuccessDays() ?: 0) }
    val startDate = remember { preferenceManager?.getControlStartDate() ?: "" }
    val endDate = remember { preferenceManager?.getControlEndDate() ?: "" }

    // ✨ 접근성 체크 (deposit 선언 이후!)
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var showAccessibilityAlert by remember { mutableStateOf(false) }

    val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

    fun formatAmount(amount: Int): String {
        return when {
            amount >= 10000 -> "${amount / 10000}만원"
            amount >= 1000 -> "${amount / 1000}천원"
            else -> "${amount}원"
        }
    }

    // 1초마다 진행 상황 업데이트 + 목표 달성 체크 + 접근성 체크
    LaunchedEffect(Unit) {
        while (true) {
            currentProgress = preferenceManager?.getCurrentProgress() ?: 0.0 // 비교용
            currentProgressDisplay = preferenceManager?.getCurrentProgressForDisplay() ?: 0.0 // 표시용

            val isNowAchieved = currentProgress >= goal
            if (isNowAchieved && !previousGoalAchieved) {
                // 목표 달성 순간 - 햅틱 + 애니메이션
                hapticManager.goalAchieved()
                triggerCelebration = true
                preferenceManager?.checkAndRecordTodaySuccess()
                successDays = preferenceManager?.getSuccessDays() ?: 0

                // 연속 달성 업데이트 및 축하 다이얼로그 표시
                if (preferenceManager?.hasSeenStreakCelebrationToday() == false) {
                    currentStreak = preferenceManager.updateStreakOnGoalAchieved()
                    weeklyAchievements = preferenceManager.getWeeklyAchievements()
                    showStreakCelebration = true
                }
            } else if (isNowAchieved) {
                preferenceManager?.checkAndRecordTodaySuccess()
                successDays = preferenceManager?.getSuccessDays() ?: 0
            }
            previousGoalAchieved = isNowAchieved

            // ✨ 접근성 서비스 체크
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            isAccessibilityEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true

            if (deposit > 0 && !isAccessibilityEnabled) {
                showAccessibilityAlert = true
            }

            delay(1000)
        }
    }

    var emergencyTimeRemaining by remember { mutableStateOf(0L) }
    var isEmergencyActive by remember { mutableStateOf(false) }
    var showEmergencyConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val isEmergency = preferenceManager?.isEmergencyMode() ?: false
            val startTime = preferenceManager?.getEmergencyStartTime() ?: 0L

            if (isEmergency && startTime > 0) {
                val elapsed = System.currentTimeMillis() - startTime
                val limit = 15 * 60 * 1000L
                val remaining = limit - elapsed

                if (remaining > 0) {
                    emergencyTimeRemaining = remaining
                    isEmergencyActive = true
                } else {
                    isEmergencyActive = false
                    preferenceManager?.saveEmergencyMode(false)
                }
            } else {
                isEmergencyActive = false
            }

            delay(1000)
        }
    }

    val progress = (currentProgress.toFloat() / goal).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val isGoalAchieved = currentProgress >= goal

    // 단위에 맞게 텍스트 생성
    val unitText = if (goalUnit == "km") "km" else "걸음"
    val currentText = if (goalUnit == "km") String.format("%.2f", currentProgressDisplay) else currentProgressDisplay.toInt().toString()
    val goalText = if (goalUnit == "km") String.format("%.2f", goalDisplay) else goal.toString()

    if (showSettingsScreen) {
        SettingsScreen(
            preferenceManager = preferenceManager,
            onBack = { showSettingsScreen = false }
        )
    } else {
        // Pet Main Screen (main.png 스타일)
        val petHappiness = remember { preferenceManager?.getPetHappiness() ?: 3 }

        PetMainScreen(
            petType = petType,
            petName = petName,
            happinessLevel = petHappiness,
            stepCount = currentProgress.toInt(),
            goalSteps = goal,
            streakCount = currentStreak,
            onSettingsClick = {
                hapticManager.click()
                showSettingsScreen = true
            },
            hapticManager = hapticManager,
            modifier = modifier
        )
    }

    // 목표 달성 축하 애니메이션
    GoalAchievedCelebration(
        trigger = triggerCelebration,
        onAnimationEnd = { triggerCelebration = false }
    )

    // 연속 달성 축하 다이얼로그
    if (showStreakCelebration) {
        StreakCelebrationDialog(
            streakCount = currentStreak,
            weeklyAchievements = weeklyAchievements,
            onDismiss = {
                preferenceManager?.setStreakCelebrationSeen()
                showStreakCelebration = false
            },
            hapticManager = hapticManager
        )
    }

}

@Preview(showBackground = true)
@Composable
fun WalkOrWaitPreview() {
    WalkorWaitTheme {
        WalkOrWaitScreen(steps = 5432)
    }
}