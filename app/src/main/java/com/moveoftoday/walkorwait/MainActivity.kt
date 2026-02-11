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

            // 장비 시스템 초기화
            Log.d(TAG, "Initializing equipment system")
            preferenceManager.initializeDefaultEquipment()
            val newEquipment = preferenceManager.checkAndUnlockNewEquipment()
            if (newEquipment.isNotEmpty()) {
                newEquipment.forEach { equipment ->
                    Log.d(TAG, "New equipment unlocked: ${equipment.displayName}")
                }
            }

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
                            stepSensorManager = stepSensorManager,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            Log.d(TAG, "Checking and resetting daily")
            checkAndResetDaily()

            // 자정 자동 리셋 알람 설정
            MidnightResetReceiver.scheduleMidnightAlarm(this)

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
            // 새로운 날 - 먼저 어제 걸음수/목표 저장 (리셋 전에!)
            val currentSteps = repository.getTodaySteps()
            repository.saveYesterdaySteps(currentSteps)
            Log.d(TAG, "📊 Saved yesterday steps before reset: $currentSteps")

            // 주간 그래프용 - 어제 날짜의 걸음수/목표 저장
            if (lastReset.isNotEmpty()) {
                val yesterdayGoal = preferenceManager.getGoal()
                preferenceManager.saveDailySteps(lastReset, currentSteps)
                preferenceManager.saveDailyGoal(lastReset, yesterdayGoal)
                Log.d(TAG, "📊 Weekly graph: saved $lastReset steps=$currentSteps, goal=$yesterdayGoal")
            }

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
        val yesterdaySteps = repository.getYesterdaySteps()
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
            } else {
                // 자유시간: 자동 성공으로 기록 (streak 유지)
                preferenceManager.recordFreeDayAsSuccess(yesterday)
                Log.d(TAG, "🆓 Yesterday was FREE DAY - auto success for streak")
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
    preferenceManager: PreferenceManager? = null,
    stepSensorManager: StepSensorManager? = null
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
                // 마이그레이션: promoFreeEndDate가 없는 기존 사용자 → 30일 후로 설정
                if (preferenceManager?.getPromoFreeEndDate() == null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 30)
                    val endDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                    preferenceManager?.savePromoFreeEndDate(endDate)
                    Log.d("MainActivity", "💳 Migration: Set promo end date to $endDate for existing user")
                }

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
            // 튜토리얼 미완료 시 온보딩 표시 (DEBUG 모드에서도 테스트 가능)
            showPetOnboarding = !isTutorialCompleted
            Log.d("MainActivity", "📱 showPetOnboarding set to: $showPetOnboarding (DEBUG: ${BuildConfig.DEBUG})")
            // showRealGoalSetup이 이미 true면 덮어쓰지 않음 (onComplete 콜백에서 설정된 경우)
            if (!showRealGoalSetup) {
                showRealGoalSetup = needsRealGoal && (isTutorialCompleted || BuildConfig.DEBUG)
            }
        }
    }
    var showWidgetRecommendation by remember { mutableStateOf(false) }

    // Get pet info from Firebase or local - 동기화된 데이터 우선 사용
    var petTypeName by remember { mutableStateOf(userSettings?.petType ?: preferenceManager?.getPetType() ?: "DOG1") }
    var petType by remember { mutableStateOf(PetType.entries.find { it.name == petTypeName } ?: PetType.DOG1) }
    var petName by remember {
        mutableStateOf(
            userSettings?.petName
            ?: preferenceManager?.getPetNameV2()?.takeIf { it.isNotBlank() }
            ?: preferenceManager?.getPetName()
            ?: "멍이"
        )
    }

    // V2 펫 상태 (새로운 진화 시스템)
    var petStateV2 by remember { mutableStateOf(preferenceManager?.let { PetSystemV2.getPetState(it) }) }

    // V2 레벨업/진화 다이얼로그 상태
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var levelUpOldLevel by remember { mutableIntStateOf(0) }
    var levelUpNewLevel by remember { mutableIntStateOf(0) }

    // V2 펫 자동 초기화 - 튜토리얼 테스트를 위해 비활성화
    // LaunchedEffect(Unit) {
    //     if (BuildConfig.DEBUG && preferenceManager?.isPetV2Initialized() != true) {
    //         preferenceManager?.savePetTypeV2(com.moveoftoday.walkorwait.pet.PetTypeV2.CAT)
    //         preferenceManager?.savePetNameV2("고양이")
    //         preferenceManager?.savePetLevelV2(
    //             com.moveoftoday.walkorwait.pet.PetLevel(level = 1, currentExp = 0, totalExp = 0)
    //         )
    //         preferenceManager?.savePetHappinessV2(80)
    //         preferenceManager?.setTutorialCompleted(true)
    //         if (preferenceManager?.getGoal() == 0) {
    //             preferenceManager?.saveGoal(6000)
    //         }
    //         petStateV2 = preferenceManager?.let { com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(it) }
    //         Log.i("V2Test", "V2 펫 초기화 완료: CAT BABY (레벨 1)")
    //     }
    // }

    // Firebase 동기화 완료 후 펫 정보 업데이트
    LaunchedEffect(userSettings?.petType, userSettings?.petName) {
        userSettings?.let {
            petTypeName = it.petType
            petType = PetType.entries.find { p -> p.name == it.petType } ?: PetType.DOG1
            petName = it.petName

            // Firebase에서 가져온 V1 데이터를 V2로 자동 마이그레이션
            val existingV2Type = preferenceManager?.getPetTypeV2()
            val existingV2Name = preferenceManager?.getPetNameV2()

            // V2 데이터가 없으면 V1 → V2 마이그레이션
            if (existingV2Type == null || existingV2Name.isNullOrBlank()) {
                val petTypeV2 = try {
                    when (petTypeName) {
                        "DOG1", "DOG2", "DOG3" -> com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                        "CAT1", "CAT2" -> com.moveoftoday.walkorwait.pet.PetTypeV2.CAT
                        else -> com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                    }
                } catch (e: Exception) {
                    com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                }

                preferenceManager?.savePetTypeV2(petTypeV2)
                preferenceManager?.savePetNameV2(petName)

                // V2 초기화 상태가 아니면 초기 레벨/행복도 설정
                if (preferenceManager?.isPetV2Initialized() != true) {
                    preferenceManager?.savePetLevelV2(com.moveoftoday.walkorwait.pet.PetLevel(level = 1, currentExp = 0, totalExp = 0))
                    preferenceManager?.savePetHappinessV2(100)
                }

                petStateV2 = preferenceManager?.let { pm -> com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(pm) }

                android.util.Log.d("MainActivity", "✅ Firebase sync V1 → V2 migration: $petTypeName → ${petTypeV2.name}, name: $petName")
            }
        }
    }

    // 0. Firebase 동기화 완료 전에는 무조건 로딩 화면 표시
    // 동기화가 완료되어야 tutorialCompleted 상태를 정확히 알 수 있음
    if (!syncCompleted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupColors.Background),
            contentAlignment = Alignment.Center
        ) {
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
        return
    }

    // 1. 업데이트 체크 대기 중 로딩 또는 업데이트 다이얼로그 표시
    if (!updateCheckCompleted || showUpdateDialog) {
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
                        text = "업데이트 확인 중...",
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

        if (!updateCheckCompleted || showUpdateDialog) {
            return
        }
    }

    // 1. Pet onboarding - 16단계 통합 튜토리얼 (펫 선택부터 결제/위젯까지)
    if (showPetOnboarding) {
        PetOnboardingScreen(
            onComplete = { selectedPetTypeV2, selectedPetName ->
                // V2 펫 데이터 저장
                preferenceManager?.savePetTypeV2(selectedPetTypeV2)
                preferenceManager?.savePetNameV2(selectedPetName)
                preferenceManager?.savePetLevelV2(com.moveoftoday.walkorwait.pet.PetLevel(level = 1, currentExp = 0, totalExp = 0))
                preferenceManager?.savePetHappinessV2(100)
                // 위젯 업데이트
                StepWidgetProvider.updateAllWidgets(context)
                // 선택한 펫 정보 업데이트 (V2 -> V1 호환)
                petTypeName = selectedPetTypeV2.name
                petName = selectedPetName
                // V2 상태 새로고침
                petStateV2 = preferenceManager?.let { com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(it) }

                // Firebase에 튜토리얼 완료 저장
                preferenceManager?.let { prefs ->
                    repository.saveTutorialCompletionData(
                        lockedApps = prefs.getLockedApps(),
                        blockingPeriods = prefs.getBlockingPeriods(),
                        controlDays = prefs.getControlDays(),
                        goal = prefs.getGoal(),
                        deposit = prefs.getDeposit(),
                        controlStartDate = prefs.getControlStartDate(),
                        controlEndDate = prefs.getControlEndDate(),
                        petType = selectedPetTypeV2.name,  // V2 펫 타입 이름
                        petName = selectedPetName
                    )
                }

                showPetOnboarding = false
                // 튜토리얼 완료 후 실제 목표 설정 화면 표시
                showRealGoalSetup = true
            },
            onDataRestored = {
                // Google 로그인으로 기존 데이터 복원됨 - 튜토리얼 스킵
                // V2 펫 데이터 우선, 없으면 V1 데이터 사용
                val restoredPetTypeV2 = preferenceManager?.getPetTypeV2()
                val restoredPetNameV2 = preferenceManager?.getPetNameV2()

                if (restoredPetTypeV2 != null && restoredPetNameV2?.isNotBlank() == true) {
                    // V2 데이터 복원
                    petTypeName = restoredPetTypeV2.name
                    petName = restoredPetNameV2
                    petStateV2 = preferenceManager?.let { com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(it) }
                } else {
                    // V1 데이터로 폴백 + V2로 마이그레이션
                    val restoredPetTypeName = preferenceManager?.getPetType()
                    val restoredPetName = preferenceManager?.getPetName() ?: "반려동물"
                    petTypeName = restoredPetTypeName ?: "DOG1"
                    petName = restoredPetName

                    // V1 → V2 자동 마이그레이션
                    val petTypeV2 = try {
                        // V1 타입을 V2로 매핑 (DOG1 → SHIBA, CAT1 → CAT 등)
                        when (petTypeName) {
                            "DOG1", "DOG2", "DOG3" -> com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                            "CAT1", "CAT2" -> com.moveoftoday.walkorwait.pet.PetTypeV2.CAT
                            else -> com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                        }
                    } catch (e: Exception) {
                        com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA
                    }

                    preferenceManager?.savePetTypeV2(petTypeV2)
                    preferenceManager?.savePetNameV2(petName)
                    preferenceManager?.savePetLevelV2(com.moveoftoday.walkorwait.pet.PetLevel(level = 1, currentExp = 0, totalExp = 0))
                    preferenceManager?.savePetHappinessV2(100)
                    petStateV2 = preferenceManager?.let { com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(it) }

                    android.util.Log.d("MainActivity", "✅ V1 → V2 migration: $petTypeName → ${petTypeV2.name}, name: $petName")
                }

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
        // V2 펫 타입 사용
        val goalPetType = petStateV2?.petType
            ?: preferenceManager?.getPetTypeV2()
            ?: com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA

        RealGoalSetupScreen(
            petType = goalPetType,
            petName = petStateV2?.name ?: petName,
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
        // V2 펫 타입 우선, 없으면 기본 SHIBA 사용
        val paymentPetType = petStateV2?.petType
            ?: preferenceManager?.getPetTypeV2()
            ?: com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA

        com.moveoftoday.walkorwait.pet.PaymentScreen(
            petType = paymentPetType,
            petName = petStateV2?.name ?: petName,  // V2 이름 우선
            preferenceManager = preferenceManager!!,
            hapticManager = hapticManager,
            onComplete = {
                // 결제 완료 시
                showExpiredPaymentScreen = false
            },
            petStateV2 = petStateV2  // V2 펫 상태 전달
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
    var showNotificationPanel by remember { mutableStateOf(false) }

    // 챌린지 관련 상태
    val challengeManager = remember { ChallengeManager.getInstance(context) }
    val currentChallengeProgress by challengeManager.currentProgress.collectAsState()
    val backgroundChallengeProgress by challengeManager.backgroundProgress.collectAsState()

    // 알림 상태 (챌린지 매니저, 센서 매니저 연결)
    val notifications = rememberNotifications(
        context = context,
        preferenceManager = preferenceManager,
        stepSensorManager = stepSensorManager,
        challengeManager = challengeManager
    )
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var showChallengeTimer by remember { mutableStateOf(false) }
    var showChallengeCompleteDialog by remember { mutableStateOf(false) }
    var showChallengeEndedDialog by remember { mutableStateOf(false) }
    var completedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var showRunningChallengeWarning by remember { mutableStateOf(false) }
    var pendingChallenge by remember { mutableStateOf<Challenge?>(null) }

    // 스킨 해금 다이얼로그 상태
    val justUnlockedSkin by challengeManager.justUnlockedSkin.collectAsState()
    var showSkinUnlockDialog by remember { mutableStateOf(false) }
    var skinToShow by remember { mutableStateOf<com.moveoftoday.walkorwait.pet.PetSkin?>(null) }

    // 일반 챌린지 타이머 업데이트 (1초마다) - TIME_BASED (독서, 명상, 공부, 플랭크)
    LaunchedEffect(currentChallengeProgress?.status, currentChallengeProgress?.challenge?.isRepBased) {
        val progress = currentChallengeProgress
        if (progress?.status == ChallengeStatus.RUNNING && progress.challenge.isRepBased == false && !progress.challenge.isBackgroundTimer) {
            while (currentChallengeProgress?.status == ChallengeStatus.RUNNING) {
                delay(1000)
                challengeManager.updateTimer()
            }
        }
    }

    // 백그라운드 챌린지 타이머 업데이트 (1초마다) - BACKGROUND_TIMER (간헐적 단식)
    LaunchedEffect(backgroundChallengeProgress?.status) {
        val progress = backgroundChallengeProgress
        if (progress?.status == ChallengeStatus.RUNNING) {
            // 즉시 한 번 업데이트 (startTime 기준 재계산)
            challengeManager.updateBackgroundTimer()

            while (backgroundChallengeProgress?.status == ChallengeStatus.RUNNING) {
                delay(1000)
                challengeManager.updateBackgroundTimer()
            }
        }
    }

    // 스킨 해금 다이얼로그 표시 (챌린지 완료 후 새 스킨 획득 시)
    LaunchedEffect(justUnlockedSkin) {
        if (justUnlockedSkin != null) {
            skinToShow = justUnlockedSkin
            showSkinUnlockDialog = true
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

                                // 백그라운드 타이머는 조용히 계속 진행 (토스트 없음)
                                if (progress.challenge.isBackgroundTimer) {
                                    return@launch
                                }

                                challengeManager.onAppExit()
                                // 토스트 알림 (메인 스레드에서 실행) - 일반 챌린지만
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
            // 펫 정보 다시 로드 (V2 우선, V1 폴백)
            val savedPetTypeName = preferenceManager?.getPetType()
            val savedPetName = preferenceManager?.getPetNameV2()?.takeIf { it.isNotBlank() }
                ?: preferenceManager?.getPetName()
            if (savedPetTypeName != null && savedPetTypeName != petTypeName) {
                petTypeName = savedPetTypeName
                petType = PetType.entries.find { it.name == savedPetTypeName } ?: PetType.DOG1
            }
            if (savedPetName != null && savedPetName != petName) {
                petName = savedPetName
            }
            // V2 펫 상태 다시 로드 (펫 교체 시 반영)
            preferenceManager?.let { pm ->
                val newPetState = com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(pm)
                Log.i("MainActivity", "V2 펫 로드: 기존=${petStateV2?.petType?.name}, 새로운=${newPetState?.petType?.name}")
                // 항상 새로고침 (비교 대신)
                petStateV2 = newPetState
                Log.i("MainActivity", "V2 펫 상태 새로고침 완료: ${newPetState?.petType?.name}")
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
    var showGoalAchievedDialog by remember { mutableStateOf(false) }  // 100% 달성 다이얼로그

    // 연속 달성 (Streak) 관련
    var showStreakCelebration by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableIntStateOf(preferenceManager?.getStreak() ?: 0) }
    var weeklyAchievements by remember { mutableStateOf(preferenceManager?.getWeeklyAchievements() ?: List(7) { false }) }

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
                preferenceManager?.recordTodaySuccess()  // 자유시간 포함 달성일수 기록
                successDays = preferenceManager?.getSuccessDays() ?: 0

                // 날짜별 목표/걸음수 저장 (주간 그래프용)
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                preferenceManager?.saveDailyGoal(today, goal.toInt())
                preferenceManager?.saveDailySteps(today, currentProgress.toInt())

                // 🎁 스킨 자동 해금 체크 (스트릭, 레벨 등 조건 충족 시)
                val newSkins = preferenceManager?.checkAndUnlockNewSkins() ?: emptyList()
                if (newSkins.isNotEmpty()) {
                    val skinNames = newSkins.joinToString { skin -> skin.displayName }
                    android.util.Log.d("MainActivity", "🎁 새 스킨 해금: $skinNames")
                    // 스킨 해금 다이얼로그 표시 (첫 번째 스킨)
                    skinToShow = newSkins.first()
                    showSkinUnlockDialog = true
                }

                // Analytics: 목표 달성 추적
                AnalyticsManager.trackGoalAchieved(goal.toInt(), currentProgress.toInt())

                // 연속 달성 업데이트 및 축하 다이얼로그 표시
                // (디버그 다이얼로그가 이미 열려있으면 중복 방지)
                if (preferenceManager?.hasSeenStreakCelebrationToday() == false && !showGoalAchievedDialog) {
                    currentStreak = preferenceManager.updateStreakOnGoalAchieved()
                    weeklyAchievements = preferenceManager.getWeeklyAchievements()
                    showStreakCelebration = true

                    // Analytics: 스트릭 마일스톤 추적
                    if (currentStreak > 0) {
                        AnalyticsManager.trackStreakMilestone(currentStreak)
                    }
                }
            } else if (isNowAchieved) {
                preferenceManager?.recordTodaySuccess()  // 자유시간 포함 달성일수 기록
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

    // 챌린지 다이얼로그 (타입에 따라 다름)
    // selectedChallenge 타입에 맞는 progress 선택
    val displayProgress = if (selectedChallenge?.isBackgroundTimer == true) {
        backgroundChallengeProgress
    } else {
        currentChallengeProgress
    }

    if (showChallengeTimer && displayProgress != null) {
        val progress = displayProgress

        if (progress.challenge.isRepBased) {
            // 횟수 기반 챌린지 (스쿼트)
            RepBasedChallengeDialog(
                progress = progress,
                onStart = {
                    challengeManager.beginChallenge()
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
                onDebugComplete = {
                    challengeManager.debugCompleteChallenge()
                }
            )
        } else {
            // 타이머 기반 챌린지 (독서, 명상, 공부, 플랭크, 단식)
            ChallengeTimerDialog(
                progress = progress,
                onStart = { startTimeOffsetHours ->
                    challengeManager.beginChallenge(startTimeOffsetHours)
                },
                onResume = {
                    challengeManager.resumeChallenge()
                },
                onCancel = {
                    challengeManager.cancelChallenge()
                    showChallengeTimer = false
                    selectedChallenge = null
                },
                onCheckLater = {
                    // 나중에 확인하기 - 다이얼로그만 닫기
                    showChallengeTimer = false
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

    // 진행 중인 챌린지가 있을 때 다른 챌린지 선택 시 경고
    if (showRunningChallengeWarning && currentChallengeProgress != null && pendingChallenge != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showRunningChallengeWarning = false
                pendingChallenge = null
            },
            title = {
                Text(
                    text = "진행 중인 챌린지가 있어요",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "현재 \"${currentChallengeProgress!!.challenge.name}\"이(가) 진행 중입니다.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "새로운 챌린지를 시작하려면 먼저 진행 중인 챌린지를 취소해주세요.",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        // 진행 중인 챌린지 다이얼로그 열기
                        showRunningChallengeWarning = false
                        pendingChallenge = null
                        showChallengeTimer = true
                    }
                ) {
                    Text("진행 중인 챌린지 확인", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showRunningChallengeWarning = false
                        pendingChallenge = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    if (showChallengeScreen) {
        ChallengeScreen(
            onBack = { showChallengeScreen = false },
            onChallengeSelected = { challenge ->
                selectedChallenge = challenge

                // 선택한 챌린지 타입에 맞는 progress 찾기
                val relevantProgress = if (challenge.isBackgroundTimer) {
                    backgroundChallengeProgress
                } else {
                    currentChallengeProgress
                }

                // 해당 progress가 진행 중인지 확인
                if (relevantProgress != null &&
                    relevantProgress.challenge.type == challenge.type &&
                    (relevantProgress.status == ChallengeStatus.RUNNING || relevantProgress.status == ChallengeStatus.PAUSED)) {
                    // 같은 챌린지 재선택 - 기존 progress 사용
                    showChallengeTimer = true
                } else {
                    // 다른 챌린지를 선택한 경우
                    // 일반 챌린지끼리만 충돌 체크
                    val foregroundProgress = currentChallengeProgress
                    if (!challenge.isBackgroundTimer && foregroundProgress != null &&
                        (foregroundProgress.status == ChallengeStatus.RUNNING || foregroundProgress.status == ChallengeStatus.PAUSED)) {
                        // 일반 챌린지가 진행 중인데 다른 일반 챌린지 선택 - 경고
                        pendingChallenge = challenge
                        showRunningChallengeWarning = true
                    } else {
                        // 새로운 챌린지 시작 준비
                        challengeManager.prepareChallenge(challenge)
                        showChallengeTimer = true
                    }
                }
                showChallengeScreen = false
            }
        )
    } else if (showSettingsScreen) {
        com.moveoftoday.walkorwait.settings.SettingsMainScreen(
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
            onNotificationClick = {
                hapticManager.click()
                showNotificationPanel = true
            },
            notificationCount = notifications.count { it.type != NotificationType.STATUS },  // 센서/백그라운드 챌린지 제외
            onChallengeClick = {
                hapticManager.click()
                showChallengeScreen = true
            },
            onQuickStartChallenge = { challengeType ->
                hapticManager.click()
                // 해당 챌린지 타입에 맞는 Challenge 찾기
                val challenge = challengeManager.allChallenges.find { it.type == challengeType }
                if (challenge != null) {
                    selectedChallenge = challenge

                    // 선택한 챌린지 타입에 맞는 progress 찾기
                    val relevantProgress = if (challenge.isBackgroundTimer) {
                        backgroundChallengeProgress
                    } else {
                        currentChallengeProgress
                    }

                    // 해당 progress가 진행 중인지 확인
                    if (relevantProgress != null &&
                        relevantProgress.challenge.type == challenge.type &&
                        (relevantProgress.status == ChallengeStatus.RUNNING || relevantProgress.status == ChallengeStatus.PAUSED)) {
                        // 같은 챌린지 재선택 - 기존 progress 사용
                        showChallengeTimer = true
                    } else {
                        // 다른 챌린지를 선택한 경우
                        // 일반 챌린지끼리만 충돌 체크
                        val currentProgress = currentChallengeProgress
                        if (!challenge.isBackgroundTimer && currentProgress != null &&
                            (currentProgress.status == ChallengeStatus.RUNNING || currentProgress.status == ChallengeStatus.PAUSED)) {
                            // 일반 챌린지가 진행 중인데 다른 일반 챌린지 선택 - 경고
                            pendingChallenge = challenge
                            showRunningChallengeWarning = true
                        } else {
                            // 새로운 챌린지 시작 준비
                            challengeManager.prepareChallenge(challenge)
                            showChallengeTimer = true
                        }
                    }
                }
            },
            hapticManager = hapticManager,
            modifier = modifier,
            isFreeTime = isFreeTime,
            onTestGoalClick = {
                // V2 펫 테스트 초기화 (초기화 안 되어있으면 SHIBA BABY, 레벨업 직전으로 시작)
                if (preferenceManager?.isPetV2Initialized() != true) {
                    preferenceManager?.savePetTypeV2(com.moveoftoday.walkorwait.pet.PetTypeV2.SHIBA)
                    preferenceManager?.savePetNameV2("시바")
                    preferenceManager?.savePetLevelV2(
                        com.moveoftoday.walkorwait.pet.PetLevel(level = 1, currentExp = 295, totalExp = 295)
                    )
                    preferenceManager?.savePetHappinessV2(80)
                    petStateV2 = preferenceManager?.let { com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(it) }
                    Log.i("V2Test", "🧪 V2 Pet initialized: SHIBA BABY Lv.1, exp 295/300")
                }

                // V2 상태 로그
                val currentLevel = preferenceManager?.getPetLevelV2()
                Log.i("V2Test", "📊 Current V2: Lv.${currentLevel?.level}, exp ${currentLevel?.totalExp}/300")

                // 🧪 테스트: 목표의 10%씩 증가
                val increment = (goal * 0.1).toInt().coerceAtLeast(100)  // 최소 100보
                val wasAchieved = currentProgress >= goal  // 이전 달성 상태
                val testSteps = currentProgress.toInt() + increment
                currentProgress = testSteps.toDouble()
                repository.saveTodaySteps(testSteps)

                // 날짜별 목표/걸음수 저장 (주간 그래프용)
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                preferenceManager?.saveDailyGoal(today, goal.toInt())
                preferenceManager?.saveDailySteps(today, testSteps)

                // 총거리 누적 (자유시간 포함)
                preferenceManager?.addPetSteps(increment)

                // V2 경험치 추가 및 레벨업 체크
                if (preferenceManager?.isPetV2Initialized() == true) {
                    val oldLevel = preferenceManager.getPetLevelV2()
                    val (newLevel, leveledUp) = com.moveoftoday.walkorwait.pet.PetSystemV2.addStepsAndCheckLevelUp(preferenceManager, increment)

                    if (leveledUp) {
                        // 레벨업 또는 진화 발생
                        levelUpOldLevel = oldLevel.level
                        levelUpNewLevel = newLevel.level
                        petStateV2 = com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(preferenceManager)
                        showLevelUpDialog = true
                        Log.d("MainActivity", "🎉 V2 Level Up! ${oldLevel.level} → ${newLevel.level}")
                    } else {
                        // 경험치만 증가
                        petStateV2 = com.moveoftoday.walkorwait.pet.PetSystemV2.getPetState(preferenceManager)
                    }
                }

                // 목표 100% 달성 시 처리 (자유시간 포함)
                val nowAchieved = testSteps >= goal
                if (nowAchieved) {
                    // 테스트용: 중복 체크 우회
                    preferenceManager?.clearLastSuccessDate()
                    preferenceManager?.recordTodaySuccess()
                    successDays = preferenceManager?.getSuccessDays() ?: 0

                    // 처음 100% 달성 시 다이얼로그 표시
                    if (!wasAchieved) {
                        showGoalAchievedDialog = true
                    }
                }

                Log.d("MainActivity", "🧪 Test +10%: steps=$testSteps (+$increment), goal=$goal, totalSteps=${preferenceManager?.getPetTotalSteps()}")
            },
            showGoalAchievedDialog = showGoalAchievedDialog,
            onDismissGoalDialog = { showGoalAchievedDialog = false },
            petStateV2 = petStateV2  // V2 펫 스프라이트 사용
        )
    }

    // 목표 달성 축하 애니메이션
    GoalAchievedCelebration(
        trigger = triggerCelebration,
        onAnimationEnd = { triggerCelebration = false }
    )

    // 연속 달성 축하 다이얼로그
    if (showStreakCelebration) {
        val petTotalSteps = preferenceManager?.getPetTotalSteps() ?: 0L
        val totalDistanceKm = petTotalSteps * 0.0007f

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
            successDays = preferenceManager?.getSuccessDays() ?: 0,
            totalKm = totalDistanceKm,
            isFirstWeek = preferenceManager?.isFirstWeekOfStreak() ?: false,
            streakStartDayOfWeek = preferenceManager?.getStreakStartDayOfWeek() ?: 0,
            petStateV2 = petStateV2  // V2 펫 적용
        )
    }

    // V2 레벨업/진화 축하 다이얼로그
    if (showLevelUpDialog && petStateV2 != null) {
        com.moveoftoday.walkorwait.pet.LevelUpCelebrationDialog(
            petState = petStateV2!!,
            oldLevel = levelUpOldLevel,
            newLevel = levelUpNewLevel,
            onDismiss = { showLevelUpDialog = false },
            hapticManager = hapticManager
        )
    }

    // 스킨 해금 축하 다이얼로그
    if (showSkinUnlockDialog && skinToShow != null) {
        SkinUnlockDialog(
            skin = skinToShow!!,
            petTypeV2 = petStateV2?.petType,
            petStage = petStateV2?.stage ?: com.moveoftoday.walkorwait.pet.PetGrowthStage.BABY,
            hapticManager = hapticManager,
            onEquip = {
                // 스킨 장착
                preferenceManager?.savePetSkin(skinToShow!!.id)
                challengeManager.clearJustUnlockedSkin()
                showSkinUnlockDialog = false
                skinToShow = null
                android.widget.Toast.makeText(context, "스킨을 장착했어요!", android.widget.Toast.LENGTH_SHORT).show()
            },
            onLater = {
                // 나중에
                challengeManager.clearJustUnlockedSkin()
                showSkinUnlockDialog = false
                skinToShow = null
            }
        )
    }

    // 알림 센터 패널
    if (showNotificationPanel) {
        NotificationCenterPanel(
            notifications = notifications,
            onDismiss = { showNotificationPanel = false },
            onNotificationClick = { notification ->
                handleNotificationAction(context, notification)
                showNotificationPanel = false
            },
            hapticManager = hapticManager
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