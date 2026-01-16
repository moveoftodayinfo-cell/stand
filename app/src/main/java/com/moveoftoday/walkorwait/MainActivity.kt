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
            enableEdgeToEdge()
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

    val isTutorialCompleted = remember { preferenceManager?.isTutorialCompleted() ?: false }
    val isPaidDeposit = remember { preferenceManager?.isPaidDeposit() ?: false }

    var showTutorial by remember { mutableStateOf(!isTutorialCompleted) }
    var showDepositSetting by remember { mutableStateOf(isTutorialCompleted && !isPaidDeposit) }
    var showGoalSetting by remember { mutableStateOf(false) }
    var showWidgetRecommendation by remember { mutableStateOf(false) }

    if (showTutorial) {
        TutorialScreen(
            preferenceManager = preferenceManager,
            onComplete = {
                showTutorial = false
                showGoalSetting = true
            }
        )
        return
    }

    if (showGoalSetting) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "일일 목표 설정",
                fontSize = StandTypography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "구독 시작 전에\n일일 걸음 목표를 먼저 설정하세요",
                fontSize = StandTypography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            GoalSettingDialog(
                currentGoal = preferenceManager?.getGoal() ?: 8000,
                onDismiss = { },
                onConfirm = { newGoal ->
                    hapticManager.success()
                    preferenceManager?.saveGoal(newGoal)
                    preferenceManager?.saveTodaySteps(0)
                    preferenceManager?.saveInitialSteps(-1)
                    showGoalSetting = false
                    showDepositSetting = true
                },
                preferenceManager = preferenceManager,
                showDismissButton = false,
                hapticManager = hapticManager,
                isInitialSetup = true
            )
        }
        return
    }

    if (showDepositSetting) {
        DepositSettingScreen(
            preferenceManager = preferenceManager,
            onComplete = {
                showDepositSetting = false
                showWidgetRecommendation = true
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
        val isTodayControlDay = preferenceManager?.isTodayControlDay() ?: false
        var isInBlockingPeriod by remember { mutableStateOf(preferenceManager?.isInBlockingPeriod() ?: true) }

        // 차단 시간대 실시간 체크
        LaunchedEffect(Unit) {
            while (true) {
                isInBlockingPeriod = preferenceManager?.isInBlockingPeriod() ?: true
                delay(1000)
            }
        }

        // 제어 요일이 아니거나, 차단 시간대가 아니면 자유 화면
        val showFreeScreen = deposit > 0 && (!isTodayControlDay || !isInBlockingPeriod)

        if (showFreeScreen) {
            // 자유 화면 메시지 결정
            val freeMessage = when {
                !isTodayControlDay -> "오늘은 자유로운 날!"
                !isInBlockingPeriod -> "지금은 자유 시간!"
                else -> "자유 시간"
            }
            val freeSubMessage = when {
                !isTodayControlDay -> "제어 요일이 아니에요"
                !isInBlockingPeriod -> "차단 시간대가 아니에요"
                else -> "자유롭게 사용하세요"
            }

            // 프리미엄 색상 (메인 화면과 동일)
            val TealPrimary = Color(0xFF00BFA5)
            val TealDark = Color(0xFF008E76)
            val NavyDark = Color(0xFF0D1B2A)
            val NavyMid = Color(0xFF1B263B)
            val BottomSheetBg = Color(0xFF0A0A0A)
            val GlowGold = Color(0xFFFFD700)

            Box(modifier = modifier.fillMaxSize()) {
                // 상단 그라데이션 배경
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(TealPrimary, TealDark, NavyMid, NavyDark),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    // 설정 버튼
                    IconButton(
                        onClick = {
                            hapticManager.click()
                            showSettingsScreen = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 중앙 자유 시간 표시
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 72.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = freeMessage,
                            color = GlowGold,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = freeSubMessage,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }

                // 하단 바텀 시트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(BottomSheetBg)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 48.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 오늘의 걸음 카드
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (goalUnit == "km") "오늘의 거리" else "오늘의 걸음",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentText,
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = unitText,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // 구독 현황 카드
                        if (deposit > 0 && totalDays > 0) {
                            val expectedCredit = SubscriptionModel.getCreditAmount(achievementRate)
                            val nextMonthPrice = SubscriptionModel.getNextMonthPrice(achievementRate)
                            val statusEmoji = SubscriptionModel.getStatusEmoji(achievementRate)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSettingsScreen = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "이번 달 ${successDays}/${totalDays}일 달성",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "$statusEmoji 다음 달 ${SubscriptionModel.formatPrice(nextMonthPrice)}",
                                            color = when {
                                                achievementRate >= 95f -> Color(0xFF4CAF50)
                                                achievementRate >= 80f -> Color(0xFFFF9800)
                                                else -> Color.White.copy(alpha = 0.6f)
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "${achievementRate.toInt()}%",
                                        color = when {
                                            achievementRate >= 95f -> Color(0xFF4CAF50)
                                            achievementRate >= 80f -> Color(0xFFFF9800)
                                            else -> Color.White.copy(alpha = 0.6f)
                                        },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ========== 프리미엄 피트니스 앱 스타일 ==========
            val TealPrimary = Color(0xFF00BFA5)
            val TealDark = Color(0xFF008E76)
            val NavyDark = Color(0xFF0D1B2A)
            val NavyMid = Color(0xFF1B263B)
            val ProgressTrack = Color(0xFF2A2A2A)
            val ProgressTeal = Color(0xFF00D9BB)
            val GlowGold = Color(0xFFFFD700)
            val BottomSheetBg = Color(0xFF0A0A0A)

            // Glow 설정 계산
            val glowConfig = remember(progress) {
                when {
                    progress < 0.5f -> Triple(0.dp, 0f, false)
                    progress < 0.7f -> Triple(12.dp, 0.2f, false)
                    progress < 0.9f -> Triple(20.dp, 0.45f, false)
                    else -> Triple(32.dp, 0.75f, true)
                }
            }

            // Pulse 애니메이션 (90% 이상일 때)
            val infiniteTransition = rememberInfiniteTransition(label = "mainPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (glowConfig.third) 1.08f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mainPulseScale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = glowConfig.second,
                targetValue = if (glowConfig.third) glowConfig.second * 1.3f else glowConfig.second,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mainPulseAlpha"
            )

            // 카운트업 애니메이션 (표시용 값 사용)
            var displaySteps by remember { mutableIntStateOf(0) }
            LaunchedEffect(currentProgressDisplay) {
                val targetSteps = currentProgressDisplay.toInt()
                val startSteps = displaySteps
                val diff = targetSteps - startSteps
                if (diff != 0) {
                    val steps = 20
                    val stepDelay = 25L
                    for (i in 1..steps) {
                        displaySteps = startSteps + (diff * i / steps)
                        delay(stepDelay)
                    }
                    displaySteps = targetSteps
                }
            }

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "mainProgress"
            )

            Box(modifier = modifier.fillMaxSize()) {
                // 상단 70% - 그라데이션 배경
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.68f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(TealPrimary, TealDark, NavyMid, NavyDark),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    // 설정 버튼 (오른쪽 상단)
                    IconButton(
                        onClick = {
                            hapticManager.click()
                            showSettingsScreen = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 접근성 서비스 경고 배너
                    if (!isAccessibilityEnabled) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 60.dp, start = 16.dp, end = 16.dp)
                                .clickable {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5722).copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Stand가 비활성화됨",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "탭하여 설정에서 활성화",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 원형 프로그레스 바 (중앙)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progressSize = 200.dp
                        val strokeWidth = 12.dp

                        // Glow 레이어
                        if (glowConfig.second > 0f) {
                            Canvas(
                                modifier = Modifier
                                    .size(progressSize)
                                    .scale(if (glowConfig.third) pulseScale else 1f)
                                    .blur(glowConfig.first)
                            ) {
                                val sweepAngle = animatedProgress * 360f
                                val arcSize = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
                                val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

                                drawArc(
                                    color = GlowGold.copy(alpha = if (glowConfig.third) pulseAlpha else glowConfig.second),
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth.toPx() * 2, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // 메인 프로그레스 바
                        Canvas(modifier = Modifier.size(progressSize)) {
                            val arcSize = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
                            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

                            // 트랙 (배경)
                            drawArc(
                                color = ProgressTrack,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )

                            // 프로그레스
                            if (animatedProgress > 0) {
                                drawArc(
                                    color = ProgressTeal,
                                    startAngle = -90f,
                                    sweepAngle = animatedProgress * 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // 중앙 텍스트
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (goalUnit == "km") String.format("%.2f", currentProgressDisplay)
                                       else "%,d".format(displaySteps),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (goalUnit == "km") "/ %.2f $unitText".format(goalDisplay)
                                       else "/ %,d $unitText".format(goal),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 상태 메시지 + 15분 휴식 버튼
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                isGoalAchieved -> "목표 달성!"
                                percentage >= 90 -> "거의 다 왔어요!"
                                percentage >= 70 -> "조금만 더!"
                                percentage >= 50 -> "절반 넘었어요"
                                else -> "오늘의 목표"
                            },
                            color = if (isGoalAchieved) GlowGold else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$percentage% 달성",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )

                        // 15분 휴식 버튼 (목표 미달성 시에만 표시)
                        if (!isGoalAchieved) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isEmergencyActive) {
                                val minutes = emergencyTimeRemaining / 60000
                                val seconds = (emergencyTimeRemaining % 60000) / 1000
                                Text(
                                    "🕐 휴식 중 ${minutes}:${seconds.toString().padStart(2, '0')}",
                                    color = GlowGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Button(
                                    onClick = {
                                        hapticManager.lightClick()
                                        showEmergencyConfirmDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("🕐 15분 휴식", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 하단 바텀 시트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(BottomSheetBg)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 48.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 누적 통계 섹션
                        val totalStepsAllTime = preferenceManager?.getTotalStepsAllTime() ?: 0L
                        val consecutiveDays = preferenceManager?.getConsecutiveDays() ?: 0
                        val totalSavedMoney = preferenceManager?.getTotalSavedMoney() ?: 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "🏆 나의 기록",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // 총 걸음 수
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "%,d".format(totalStepsAllTime),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "총 걸음",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    // 연속 달성일
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$consecutiveDays",
                                                color = if (consecutiveDays > 0) GlowGold else Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (consecutiveDays >= 3) {
                                                Text(
                                                    text = " 🔥",
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "연속 달성",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    // 총 절약 금액
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "%,d원".format(totalSavedMoney),
                                            color = if (totalSavedMoney > 0) Color(0xFF4CAF50) else Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "총 절약",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 구독 현황
                        if (totalDays > 0) {
                            val nextMonthPrice = SubscriptionModel.getNextMonthPrice(achievementRate)
                            val statusEmoji = SubscriptionModel.getStatusEmoji(achievementRate)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSettingsScreen = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "이번 달 ${successDays}/${totalDays}일 달성",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "$statusEmoji 다음 달 ${SubscriptionModel.formatPrice(nextMonthPrice)}",
                                            color = when {
                                                achievementRate >= 95f -> Color(0xFF4CAF50)
                                                achievementRate >= 80f -> Color(0xFFFF9800)
                                                else -> Color.White.copy(alpha = 0.6f)
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "${achievementRate.toInt()}%",
                                        color = when {
                                            achievementRate >= 95f -> Color(0xFF4CAF50)
                                            achievementRate >= 80f -> Color(0xFFFF9800)
                                            else -> Color.White.copy(alpha = 0.6f)
                                        },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 목표 달성 시 상단 Glow 효과
                if (isGoalAchieved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GlowGold.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        // 목표 달성 축하 애니메이션
        GoalAchievedCelebration(
            trigger = triggerCelebration,
            onAnimationEnd = { triggerCelebration = false }
        )

        // 15분 휴식 확인 다이얼로그
        if (showEmergencyConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showEmergencyConfirmDialog = false },
                containerColor = StandColors.DarkBackground,
                title = {
                    Text(
                        "15분 휴식",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "하루에 한 번만 사용할 수 있습니다.",
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "15분 동안 앱 차단이 해제됩니다.\n정말 사용하시겠습니까?",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            hapticManager.success()
                            preferenceManager?.saveEmergencyMode(true)
                            preferenceManager?.saveEmergencyStartTime(System.currentTimeMillis())
                            isEmergencyActive = true
                            showEmergencyConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StandColors.Primary
                        )
                    ) {
                        Text("사용하기", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEmergencyConfirmDialog = false }
                    ) {
                        Text("취소", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalkOrWaitPreview() {
    WalkorWaitTheme {
        WalkOrWaitScreen(steps = 5432)
    }
}