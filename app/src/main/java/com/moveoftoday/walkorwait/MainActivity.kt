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
import androidx.compose.foundation.border
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var repository: UserDataRepository
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationHelper: NotificationHelper
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
            notificationHelper = NotificationHelper(this)

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

            // 튜토리얼 완료된 경우에만 권한 요청 및 서비스 시작
            // (튜토리얼 중에는 PermissionSettingsStep에서 권한 요청)
            if (preferenceManager.isTutorialCompleted()) {
                Log.d(TAG, "Tutorial completed - Checking permissions")
                checkPermissionAndStart()

                Log.d(TAG, "Requesting notification permission")
                requestNotificationPermission()

                Log.d(TAG, "Starting service")
                StepCounterService.start(this)
            } else {
                Log.d(TAG, "Tutorial not completed - skipping permission requests")
            }

            // Analytics: 메인 화면 조회
            AnalyticsManager.trackScreenView("MainScreen", "MainActivity")

            Log.d(TAG, "=== onCreate COMPLETE ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndResetDaily()
        checkWorryNotification()
    }

    /**
     * 걱정 알림 체크 - 평소 운동 시간에 움직임이 없으면 알림
     */
    private fun checkWorryNotification() {
        // 초기화 완료 체크
        if (!::preferenceManager.isInitialized || !::notificationHelper.isInitialized) {
            return
        }
        
        if (preferenceManager.shouldShowWorryNotification() &&
            !preferenceManager.hasShownWorryNotificationToday()) {
            val petName = preferenceManager.getPetName() ?: "펫"
            notificationHelper.showWorryNotification(petName)
            preferenceManager.setWorryNotificationShown()
        }
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
    val notificationHelper = remember { NotificationHelper(context) }
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository

    // Firebase 동기화 상태 관찰
    val syncCompleted by repository.syncCompleted.collectAsState()
    val userSettings by repository.userSettings.collectAsState()

    // 동기화 완료 후 설정 상태 체크
    val isTutorialCompleted = userSettings?.tutorialCompleted ?: preferenceManager?.isTutorialCompleted() ?: false
    val isPaidDeposit = userSettings?.paidDeposit ?: preferenceManager?.isPaidDeposit() ?: false
    val needsRealGoal = remember { preferenceManager?.needsRealGoalSetup() ?: false }
    val promoCodeType = remember { preferenceManager?.getPromoCodeType() }

    // 구독/프로모션 상태 체크 (백그라운드에서 확인, 로딩 화면 없음)
    var showExpiredPaymentScreen by remember { mutableStateOf(false) }

    // 앱 업데이트 상태
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateManager.UpdateInfo?>(null) }
    var updateCheckCompleted by remember { mutableStateOf(false) }

    // 앱 시작 시 업데이트 체크
    LaunchedEffect(Unit) {
        try {
            val info = AppUpdateManager.checkForUpdate(context)
            if (info.isUpdateAvailable) {
                updateInfo = info
                showUpdateDialog = true
            }
        } catch (e: Exception) {
            // 업데이트 체크 실패 시 무시
        }
        updateCheckCompleted = true
    }

    // 앱 시작 시 구독/프로모션 상태 백그라운드 확인 (Firebase 동기화 완료 후)
    LaunchedEffect(syncCompleted, isTutorialCompleted) {
        if (!syncCompleted) return@LaunchedEffect  // Firebase 동기화 완료 대기

        // Firebase에서 복원된 최신 값 사용
        val currentPromoCodeType = preferenceManager?.getPromoCodeType()
        val currentIsPaidDeposit = preferenceManager?.isPaidDeposit() ?: false

        Log.d("MainActivity", "💳 Subscription check - syncCompleted: $syncCompleted, isTutorialCompleted: $isTutorialCompleted, isPaidDeposit: $currentIsPaidDeposit, promoCodeType: $currentPromoCodeType")

        if (isTutorialCompleted) {
            // 1. 프로모션 코드 사용자: 프로모션 기간 체크
            if (currentPromoCodeType != null) {
                val isPromoValid = preferenceManager?.isInPromoFreePeriod() ?: false
                Log.d("MainActivity", "💳 Promo user - isPromoValid: $isPromoValid")
                if (!isPromoValid) {
                    // 프로모션 기간 만료 → 프로모션 정보 삭제 후 결제 화면
                    preferenceManager?.clearPromoCode()
                    preferenceManager?.setPaidDeposit(false)
                    showExpiredPaymentScreen = true
                    Log.d("MainActivity", "💳 Promo expired - showing payment screen")
                }
                return@LaunchedEffect
            }

            // 2. 유료 구독 사용자: Google Play 구독 상태 확인
            // paidDeposit: true여도 항상 Google Play에서 구독 활성화 여부 확인
            if (currentIsPaidDeposit) {
                Log.d("MainActivity", "💳 Paid user - checking Google Play subscription...")
            } else {
                Log.d("MainActivity", "💳 New user - checking Google Play subscription...")
            }

            Log.d("MainActivity", "💳 Checking Google Play subscription...")
            val billingManager = BillingManager(
                context = context,
                onConnectionReady = {}
            )

            delay(2000)  // 연결 시간 여유 확보

            billingManager.checkActiveSubscription { isActive, _ ->
                Log.d("MainActivity", "💳 Subscription check result - isActive: $isActive")
                if (isActive) {
                    // 구독 활성화 확인 - paidDeposit을 true로 설정 (복원 시 누락 방지)
                    if (preferenceManager?.isPaidDeposit() != true) {
                        preferenceManager?.setPaidDeposit(true)
                        Log.d("MainActivity", "💳 Active subscription confirmed - paidDeposit updated to true")
                    }
                } else {
                    showExpiredPaymentScreen = true
                    preferenceManager?.setPaidDeposit(false)
                    Log.d("MainActivity", "💳 No active subscription - showing payment screen")
                }
            }
        }
    }

    // 설정 플로우 상태 - 16단계 통합 튜토리얼
    // syncCompleted 후 tutorialCompleted 상태에 따라 결정
    var showPetOnboarding by remember { mutableStateOf(false) }
    var showRealGoalSetup by remember { mutableStateOf(false) }

    // Firebase 동기화 완료 후 튜토리얼 상태 결정
    LaunchedEffect(syncCompleted, isTutorialCompleted) {
        Log.d("MainActivity", "🔄 LaunchedEffect - syncCompleted: $syncCompleted, isTutorialCompleted: $isTutorialCompleted, userSettings: ${userSettings?.tutorialCompleted}, prefManager: ${preferenceManager?.isTutorialCompleted()}")
        if (syncCompleted) {
            showPetOnboarding = !isTutorialCompleted
            Log.d("MainActivity", "📱 showPetOnboarding set to: $showPetOnboarding")
            // showRealGoalSetup이 이미 true면 덮어쓰지 않음 (onComplete 콜백에서 설정된 경우)
            if (!showRealGoalSetup) {
                showRealGoalSetup = needsRealGoal && isTutorialCompleted
            }
        }
    }
    var showWidgetRecommendation by remember { mutableStateOf(false) }

    // Get pet info from Firebase or local - 동기화된 데이터 우선 사용
    var petTypeName by remember { mutableStateOf(userSettings?.petType ?: preferenceManager?.getPetType() ?: "DOG1") }
    var petType by remember { mutableStateOf(PetType.entries.find { it.name == petTypeName } ?: PetType.DOG1) }
    var petName by remember { mutableStateOf(userSettings?.petName ?: preferenceManager?.getPetName() ?: "멍이") }

    // Firebase 동기화 완료 후 펫 정보 업데이트
    LaunchedEffect(userSettings?.petType, userSettings?.petName) {
        userSettings?.let {
            petTypeName = it.petType
            petType = PetType.entries.find { p -> p.name == it.petType } ?: PetType.DOG1
            petName = it.petName
        }
    }

    // 0. Firebase 동기화 및 업데이트 체크 대기 중 로딩 화면
    // 업데이트 확인이 완료되지 않았거나, 동기화가 완료되지 않은 경우 로딩 화면 표시
    val shouldShowLoading = !syncCompleted || (!updateCheckCompleted && !showUpdateDialog)

    if (shouldShowLoading || showUpdateDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupColors.Background),
            contentAlignment = Alignment.Center
        ) {
            // 로딩 인디케이터 (다이얼로그가 없을 때만 표시)
            if (!showUpdateDialog) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MockupColors.Border,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "데이터 불러오는 중...",
                        color = MockupColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // 업데이트 다이얼로그
            if (showUpdateDialog && updateInfo != null) {
                AppUpdateDialog(
                    updateInfo = updateInfo!!,
                    onDismiss = {
                        if (!updateInfo!!.isForceUpdate) {
                            showUpdateDialog = false
                        }
                    },
                    onUpdate = {
                        AppUpdateManager.openPlayStore(context, updateInfo!!.playStoreUrl)
                    }
                )
            }
        }

        // 동기화 완료 + 업데이트 체크 완료 + 다이얼로그 닫힘 전까지 리턴
        if (!syncCompleted || !updateCheckCompleted || showUpdateDialog) {
            return
        }
    }

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
            onDataRestored = {
                // Google 로그인으로 기존 데이터 복원됨 - 튜토리얼 스킵
                val restoredPetTypeName = preferenceManager?.getPetType()
                val restoredPetName = preferenceManager?.getPetName() ?: "반려동물"
                val restoredPetType = restoredPetTypeName?.let {
                    PetType.entries.find { pet -> pet.name == it }
                } ?: PetType.DOG1

                petType = restoredPetType
                petName = restoredPetName
                petTypeName = restoredPetTypeName ?: "DOG1"
                showPetOnboarding = false
                // 데이터 복원이므로 실제 목표 설정 화면 표시하지 않음
                showRealGoalSetup = false

                // Analytics 추적
                AnalyticsManager.trackSettingsChanged("data_restored", "google_signin")

                android.util.Log.d("MainActivity", "✅ Data restored from Google account")
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

    // 3. 구독 만료 시 결제 화면 (PaymentScreen - 재결제용)
    if (showExpiredPaymentScreen) {
        com.moveoftoday.walkorwait.pet.PaymentScreen(
            petType = petType,
            petName = petName,
            preferenceManager = preferenceManager!!,
            hapticManager = hapticManager,
            onComplete = {
                // 결제 완료 시
                showExpiredPaymentScreen = false
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

    // 서버 공지 팝업
    var announcement by remember { mutableStateOf<AnnouncementManager.Announcement?>(null) }
    val announcementManager = remember { AnnouncementManager(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        announcement = announcementManager.getActiveAnnouncement()
    }

    if (announcement != null) {
        AnnouncementDialog(
            announcement = announcement!!,
            onDismiss = {
                // 오늘 그만보기
                announcementManager.dismissForToday(announcement!!.id)
                announcement = null
            },
            onPrimaryAction = {
                // 메인 버튼 클릭 시에도 오늘 그만보기
                announcementManager.dismissForToday(announcement!!.id)
                announcement = null
            },
            hapticManager = hapticManager
        )
    }

    // 단위에 따라 자동 전환
    var goalUnit by remember { mutableStateOf(preferenceManager?.getGoalUnit() ?: "steps") }
    var currentProgress by remember { mutableDoubleStateOf(preferenceManager?.getCurrentProgress() ?: 0.0) } // 비교용 (걸음 수 기준)
    var currentProgressDisplay by remember { mutableDoubleStateOf(preferenceManager?.getCurrentProgressForDisplay() ?: 0.0) } // 표시용
    var goal by remember { mutableIntStateOf(preferenceManager?.getGoal() ?: 8000) }
    var goalDisplay by remember { mutableDoubleStateOf(preferenceManager?.getGoalForDisplay() ?: 8000.0) } // 표시용
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showChallengeScreen by remember { mutableStateOf(false) }

    // 챌린지 관련 상태
    val challengeManager = remember { ChallengeManager.getInstance(context) }
    val currentChallengeProgress by challengeManager.currentProgress.collectAsState()
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var showChallengeTimer by remember { mutableStateOf(false) }
    var showChallengeCompleteDialog by remember { mutableStateOf(false) }
    var showChallengeEndedDialog by remember { mutableStateOf(false) }
    var completedChallenge by remember { mutableStateOf<Challenge?>(null) }

    // 챌린지 타이머 업데이트 (1초마다)
    LaunchedEffect(currentChallengeProgress?.status) {
        if (currentChallengeProgress?.status == ChallengeStatus.RUNNING) {
            while (currentChallengeProgress?.status == ChallengeStatus.RUNNING) {
                delay(1000)
                challengeManager.updateTimer()
            }
        }
    }

    // 앱 라이프사이클 감지 (챌린지 이탈 체크) - 다른 앱으로 갔을 때만 (화면 끄기 제외)
    val lifecycleOwner = LocalLifecycleOwner.current
    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager }
    val exitCheckScope = rememberCoroutineScope()
    var exitCheckJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // 이전 체크 취소
                    exitCheckJob?.cancel()
                    // 약간의 딜레이 후 화면 상태 체크 (전원 버튼 vs 앱 이탈 구분)
                    exitCheckJob = exitCheckScope.launch {
                        delay(100)
                        // 딜레이 후에도 화면이 켜져 있으면 = 다른 앱으로 이동 (앱 이탈)
                        // 딜레이 후 화면이 꺼져 있으면 = 전원 버튼으로 화면 끔 (이탈 아님)
                        val isScreenStillOn = powerManager.isInteractive
                        if (isScreenStillOn) {
                            val progress = challengeManager.currentProgress.value
                            if (progress != null &&
                                (progress.status == ChallengeStatus.RUNNING || progress.status == ChallengeStatus.PAUSED)) {
                                challengeManager.onAppExit()
                                // 토스트 알림 (메인 스레드에서 실행)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    val exitCount = challengeManager.currentProgress.value?.exitCount ?: 0
                                    val message = if (exitCount >= 2) {
                                        "챌린지가 종료되었어요"
                                    } else {
                                        "챌린지가 일시정지됐어요! (${exitCount}/2)"
                                    }
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // 앱으로 돌아오면 대기 중인 이탈 체크 취소
                    exitCheckJob?.cancel()
                    exitCheckJob = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            exitCheckJob?.cancel()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 설정 화면 닫을 때 펫 정보 및 목표 단위 다시 로드
    LaunchedEffect(showSettingsScreen) {
        if (!showSettingsScreen) {
            // 펫 정보 다시 로드
            val savedPetTypeName = preferenceManager?.getPetType()
            val savedPetName = preferenceManager?.getPetName()
            if (savedPetTypeName != null && savedPetTypeName != petTypeName) {
                petTypeName = savedPetTypeName
                petType = PetType.entries.find { it.name == savedPetTypeName } ?: PetType.DOG1
            }
            if (savedPetName != null && savedPetName != petName) {
                petName = savedPetName
            }
            // 목표 단위 및 목표 값 다시 로드
            goalUnit = preferenceManager?.getGoalUnit() ?: "steps"
            goal = preferenceManager?.getGoal() ?: 8000
            goalDisplay = preferenceManager?.getGoalForDisplay() ?: 8000.0
            currentProgressDisplay = preferenceManager?.getCurrentProgressForDisplay() ?: 0.0
        }
    }

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
            hapticManager = hapticManager,
            petType = petType,
            petName = petName,
            isFirstWeek = preferenceManager?.isFirstWeekOfStreak() ?: false,
            streakStartDayOfWeek = preferenceManager?.getStreakStartDayOfWeek() ?: 0
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
                // 목표 달성 순간 - 햅틱 + 애니메이션 + 알림
                hapticManager.goalAchieved()
                notificationHelper.showGoalAchievedNotification(goalDisplay, goalUnit)
                triggerCelebration = true
                preferenceManager?.checkAndRecordTodaySuccess()
                successDays = preferenceManager?.getSuccessDays() ?: 0

                // Analytics: 목표 달성 추적
                AnalyticsManager.trackGoalAchieved(goal.toInt(), currentProgress.toInt())

                // 연속 달성 업데이트 및 축하 다이얼로그 표시
                if (preferenceManager?.hasSeenStreakCelebrationToday() == false) {
                    currentStreak = preferenceManager.updateStreakOnGoalAchieved()
                    weeklyAchievements = preferenceManager.getWeeklyAchievements()
                    showStreakCelebration = true

                    // Analytics: 스트릭 마일스톤 추적
                    if (currentStreak > 0) {
                        AnalyticsManager.trackStreakMilestone(currentStreak)
                    }
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

    // 챌린지 타이머 다이얼로그
    if (showChallengeTimer && currentChallengeProgress != null) {
        ChallengeTimerDialog(
            progress = currentChallengeProgress!!,
            onStart = {
                challengeManager.beginChallenge()
            },
            onResume = {
                challengeManager.resumeChallenge()
            },
            onCancel = {
                challengeManager.cancelChallenge()
                showChallengeTimer = false
                selectedChallenge = null
            },
            onComplete = {
                completedChallenge = currentChallengeProgress?.challenge
                showChallengeTimer = false
                showChallengeCompleteDialog = true
                challengeManager.clearCurrentProgress()
                // 진동 + 알림음
                hapticManager?.success()
                try {
                    val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                    android.media.RingtoneManager.getRingtone(context, notificationUri)?.play()
                } catch (e: Exception) { /* 무시 */ }
            },
            onEnded = {
                completedChallenge = currentChallengeProgress?.challenge
                showChallengeTimer = false
                showChallengeEndedDialog = true
                challengeManager.clearCurrentProgress()
            },
            onDebugComplete = {
                challengeManager.debugCompleteChallenge()
            }
        )
    }

    // 챌린지 완료 다이얼로그
    if (showChallengeCompleteDialog && completedChallenge != null) {
        ChallengeCompleteDialog(
            challenge = completedChallenge!!,
            onDismiss = {
                showChallengeCompleteDialog = false
                completedChallenge = null
            }
        )
    }

    // 챌린지 종료 다이얼로그
    if (showChallengeEndedDialog && completedChallenge != null) {
        ChallengeEndedDialog(
            challenge = completedChallenge!!,
            onDismiss = {
                showChallengeEndedDialog = false
                completedChallenge = null
            }
        )
    }

    if (showChallengeScreen) {
        ChallengeScreen(
            onBack = { showChallengeScreen = false },
            onChallengeSelected = { challenge ->
                selectedChallenge = challenge
                // 챌린지 시작 준비 (NOT_STARTED 상태로)
                challengeManager.prepareChallenge(challenge)
                showChallengeTimer = true
                showChallengeScreen = false
            }
        )
    } else if (showSettingsScreen) {
        SettingsScreen(
            preferenceManager = preferenceManager,
            onBack = { showSettingsScreen = false }
        )
    } else {
        // Pet Main Screen (main.png 스타일)
        val petHappiness = remember { preferenceManager?.getPetHappiness() ?: 3 }

        // 자유 시간 체크: 제어 요일이 아니거나 제어 시간대가 아님
        val isFreeTime = remember {
            val isControlDay = preferenceManager?.isTodayControlDay() ?: true
            val isBlockingPeriod = preferenceManager?.isInBlockingPeriod() ?: true
            !isControlDay || !isBlockingPeriod
        }

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
            onChallengeClick = {
                hapticManager.click()
                showChallengeScreen = true
            },
            hapticManager = hapticManager,
            modifier = modifier,
            isFreeTime = isFreeTime
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
            hapticManager = hapticManager,
            petType = petType,
            petName = petName,
            isFirstWeek = preferenceManager?.isFirstWeekOfStreak() ?: false,
            streakStartDayOfWeek = preferenceManager?.getStreakStartDayOfWeek() ?: 0
        )
    }

}

/**
 * 앱 업데이트 다이얼로그 (레트로 스타일)
 */
@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateManager.UpdateInfo,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    val kenneyFont = com.moveoftoday.walkorwait.pet.rememberKenneyFont()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            if (!updateInfo.isForceUpdate) {
                onDismiss()
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(com.moveoftoday.walkorwait.pet.MockupColors.Background)
                .border(2.dp, com.moveoftoday.walkorwait.pet.MockupColors.Border, RoundedCornerShape(16.dp))
        ) {
            // 헤더 (rebon 로고)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.moveoftoday.walkorwait.pet.MockupColors.CardBackground)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "rebon",
                    fontSize = 16.sp,
                    fontFamily = kenneyFont,
                    color = com.moveoftoday.walkorwait.pet.MockupColors.TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(com.moveoftoday.walkorwait.pet.MockupColors.Border)
            )

            // 컨텐츠 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.rebon_icon_trans),
                    contentDescription = "rebon icon",
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 제목
                Text(
                    text = if (updateInfo.isForceUpdate) "업데이트 필요" else "새 버전이 있어요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.moveoftoday.walkorwait.pet.MockupColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 메시지
                Text(
                    text = updateInfo.updateMessage,
                    fontSize = 14.sp,
                    color = com.moveoftoday.walkorwait.pet.MockupColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 버전 정보
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${updateInfo.currentVersionName}",
                        fontSize = 13.sp,
                        color = com.moveoftoday.walkorwait.pet.MockupColors.TextMuted
                    )
                    Text(
                        text = " → ",
                        fontSize = 13.sp,
                        color = com.moveoftoday.walkorwait.pet.MockupColors.TextMuted
                    )
                    Text(
                        text = "${updateInfo.latestVersionName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.moveoftoday.walkorwait.pet.MockupColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 업데이트 버튼
                Button(
                    onClick = onUpdate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.moveoftoday.walkorwait.pet.MockupColors.Border,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "업데이트",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 나중에 버튼 (강제 업데이트가 아닌 경우에만)
                if (!updateInfo.isForceUpdate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "나중에",
                            fontSize = 14.sp,
                            color = com.moveoftoday.walkorwait.pet.MockupColors.TextMuted
                        )
                    }
                }
            }
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