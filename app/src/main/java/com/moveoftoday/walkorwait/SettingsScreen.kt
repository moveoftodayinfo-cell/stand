package com.moveoftoday.walkorwait

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.PetDepositSettingScreen
import com.moveoftoday.walkorwait.pet.PetSprite
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository
    val hapticManager = remember { HapticManager(context) }
    val scope = rememberCoroutineScope()

    var currentSteps by remember { mutableIntStateOf(repository.getTodaySteps()) }
    var goal by remember { mutableIntStateOf(repository.getGoal()) }
    var deposit by remember { mutableIntStateOf(repository.getDeposit()) }
    var successDays by remember { mutableIntStateOf(repository.getSuccessDays()) }
    var totalDays by remember { mutableIntStateOf(preferenceManager?.getTotalControlDays() ?: 0) }
    var requiredDays by remember {
        mutableIntStateOf(
            preferenceManager?.getRequiredSuccessDays() ?: 0
        )
    }
    val startDate = remember { repository.getControlStartDate() }
    val endDate = remember { repository.getControlEndDate() }
    val isPaidDeposit = remember { repository.isPaidDeposit() }

    // 접근성 서비스 체크
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var showAppLockScreen by remember { mutableStateOf(false) }
    var showDepositSettingScreen by remember { mutableStateOf(false) }
    var showDepositInfoDialog by remember { mutableStateOf(false) }
    var showFitnessAppConnectionScreen by remember { mutableStateOf(false) }
    var showBlockingPeriodsDialog by remember { mutableStateOf(false) }
    var showControlDaysDialog by remember { mutableStateOf(false) }
    var showChangeConfirmDialog by remember { mutableStateOf<String?>(null) } // "goal", "controlDays", "blockingPeriods"
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // Google 로그인 관련 상태
    val auth = remember { FirebaseAuth.getInstance() }
    var isGoogleSignedIn by remember { mutableStateOf(auth.currentUser != null && auth.currentUser?.isAnonymous != true) }
    var googleEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Google Sign-In 함수 (Credential Manager 사용)
    fun performGoogleSignIn() {
        isGoogleLoading = true
        scope.launch {
            val result = GoogleSignInHelper.signIn(context)
            when (result) {
                is GoogleSignInHelper.SignInResult.Success -> {
                    val firebaseResult = GoogleSignInHelper.signInToFirebase(result.idToken)
                    if (firebaseResult.isSuccess) {
                        // Repository 동기화 시작
                        repository.startSync()

                        isGoogleLoading = false
                        isGoogleSignedIn = true
                        googleEmail = auth.currentUser?.email ?: ""
                        hapticManager.success()
                        Toast.makeText(context, "Google 계정 연결 완료!", Toast.LENGTH_SHORT).show()
                    } else {
                        isGoogleLoading = false
                        Toast.makeText(context, "Firebase 로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                is GoogleSignInHelper.SignInResult.Error -> {
                    isGoogleLoading = false
                    if (!result.isCancelled) {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 앱 제어 섹션 접기/펼치기 상태 (기본: 접힘)
    var isAppControlExpanded by remember { mutableStateOf(false) }

    // 펫 변경 관련 상태 - Ref로 관리하여 콜백에서 최신 값 접근
    val pendingPetTypeRef = remember { mutableStateOf<PetType?>(null) }
    val pendingPetNameRef = remember { mutableStateOf("") }
    val showPetChangeDialogRef = remember { mutableStateOf(false) }

    // 외부에서 사용할 변수
    var showPetChangeDialog by showPetChangeDialogRef

    // 펫 변경용 BillingManager (nullable state - 다이얼로그 열 때 생성)
    var petChangeBillingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 펫 변경 결제 시작 함수
    fun startPetChangePurchase(newPetType: PetType, newPetName: String) {
        pendingPetTypeRef.value = newPetType
        pendingPetNameRef.value = newPetName

        // 먼저 다이얼로그 닫기 (결제 UI가 뜨기 전에)
        showPetChangeDialogRef.value = false

        val activity = context as? android.app.Activity ?: return

        // 약간의 지연 후 결제 시작 (다이얼로그 닫힌 후)
        scope.launch {
            kotlinx.coroutines.delay(100)

            if (petChangeBillingManager == null) {
                petChangeBillingManager = BillingManager(
                    context = context,
                    onPurchaseSuccess = { _ ->
                        // 결제 성공 시 펫 변경 저장
                        val petType = pendingPetTypeRef.value
                        val petName = pendingPetNameRef.value

                        if (petType != null) {
                            try {
                                val appContext = context.applicationContext
                                preferenceManager?.savePetType(petType.name)
                                preferenceManager?.savePetName(petName)
                                // Firebase에도 동기화
                                val app = appContext as WalkorWaitApp
                                app.userDataRepository.savePetInfo(petType.name, petName)
                                StepWidgetProvider.updateAllWidgets(appContext)
                                Toast.makeText(appContext, "펫이 변경되었습니다!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "Pet change failed: ${e.message}")
                            }
                        }
                        pendingPetTypeRef.value = null
                        pendingPetNameRef.value = ""
                    },
                    onPurchaseFailure = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
            petChangeBillingManager?.startPetChangePurchase(activity)
        }
    }

    // Analytics: 설정 화면 조회 추적
    LaunchedEffect(Unit) {
        try {
            AnalyticsManager.trackScreenView("SettingsScreen", "SettingsScreen")
        } catch (e: Exception) {
            // Analytics 실패는 무시
        }
    }

    // 1초마다 업데이트 + 접근성 체크
    LaunchedEffect(Unit) {
        while (true) {
            try {
                currentSteps = repository.getTodaySteps()
                goal = repository.getGoal()
                deposit = repository.getDeposit()
                successDays = repository.getSuccessDays()
                totalDays = preferenceManager?.getTotalControlDays() ?: 0
                requiredDays = preferenceManager?.getRequiredSuccessDays() ?: 0

                // 접근성 서비스 체크
                val enabledServices = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                isAccessibilityEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true
            } catch (e: Exception) {
                // 업데이트 실패는 무시
            }
            delay(1000)
        }
    }

    val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

    // 2단계 색상 판정 (블루/레드만 사용)
    val statusColor = when {
        achievementRate >= 95f -> MockupColors.Blue   // 달성
        else -> MockupColors.Red                       // 미달성
    }

    val statusText = when {
        achievementRate >= 95f -> "완전 달성"
        achievementRate >= 80f -> "부분 달성"
        else -> "진행중"
    }

    val statusDescription = when {
        achievementRate >= 95f -> "친구 초대 쿠폰 획득!"
        else -> "95% 달성 시 친구 쿠폰"
    }

    fun formatAmount(amount: Int): String {
        return when {
            amount >= 10000 -> "${amount / 10000}만원"
            amount >= 1000 -> "${amount / 1000}천원"
            else -> "${amount}원"
        }
    }

    if (showAppLockScreen) {
        AppLockScreen(
            preferenceManager = preferenceManager,
            onBack = { showAppLockScreen = false }
        )
    } else if (showDepositSettingScreen) {
        val savedPetType = preferenceManager?.getPetType()?.let {
            PetType.entries.find { pet -> pet.name == it }
        } ?: PetType.DOG1
        val savedPetName = preferenceManager?.getPetName() ?: "반려동물"

        PetDepositSettingScreen(
            petType = savedPetType,
            petName = savedPetName,
            preferenceManager = preferenceManager,
            hapticManager = hapticManager,
            startAtStep = 2,  // 결제 화면으로 바로 이동
            onComplete = { showDepositSettingScreen = false }
        )
    } else if (showFitnessAppConnectionScreen) {
        FitnessAppConnectionScreen(
            onBack = { showFitnessAppConnectionScreen = false },
            onConnectionComplete = { showFitnessAppConnectionScreen = false }
        )
    } else if (showGoalDialog) {
        // 목표 설정 (풀스크린)
        GoalSettingDialog(
            currentGoal = goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                repository.saveGoal(newGoal)
                goal = newGoal
                showGoalDialog = false
                // 위젯 업데이트 (목표 단위 변경 시 위젯 반영)
                StepWidgetProvider.updateAllWidgets(context)
            },
            preferenceManager = preferenceManager,
            hapticManager = hapticManager
        )
    } else if (showBlockingPeriodsDialog) {
        // 차단 시간대 선택 (풀스크린)
        val canRemovePeriods = preferenceManager?.canChangeBlockingPeriods() ?: true
        BlockingPeriodsDialog(
            currentPeriods = preferenceManager?.getBlockingPeriods() ?: emptySet(),
            canRemove = canRemovePeriods,
            nextRemoveDate = if (!canRemovePeriods) preferenceManager?.getNextBlockingPeriodsChangeDate() ?: "" else "",
            onDismiss = { showBlockingPeriodsDialog = false },
            onConfirm = { newPeriods, hasRemovals ->
                preferenceManager?.saveBlockingPeriods(newPeriods)
                // 제거가 있을 때만 변경 시간 기록
                if (hasRemovals) {
                    preferenceManager?.saveBlockingPeriodsChangeTime()
                }
                showBlockingPeriodsDialog = false
            }
        )
    } else if (showControlDaysDialog) {
        // 제어 요일 선택 (풀스크린)
        val canRemoveDays = preferenceManager?.canChangeControlDays() ?: true
        ControlDaysDialog(
            currentDays = preferenceManager?.getControlDays() ?: emptySet(),
            canRemove = canRemoveDays,
            nextRemoveDate = if (!canRemoveDays) preferenceManager?.getNextControlDaysChangeDate() ?: "" else "",
            onDismiss = { showControlDaysDialog = false },
            onConfirm = { newDays, hasRemovals ->
                preferenceManager?.saveControlDays(newDays)
                // 제거가 있을 때만 변경 시간 기록
                if (hasRemovals) {
                    preferenceManager?.saveControlDaysChangeTime()
                }
                showControlDaysDialog = false
            }
        )
    } else {
        // 깔끔한 레트로 스타일 - 3색 시스템 (Black/White, Red, Blue)
        val kenneyFont = rememberKenneyFont()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupColors.Background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 헤더 - 깔끔한 레트로 스타일
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(
                            width = 3.dp,
                            color = MockupColors.Border,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 뒤로가기 버튼
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(3.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                .background(MockupColors.Background, RoundedCornerShape(8.dp))
                                .clickable {
                                    hapticManager.click()
                                    onBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "<",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Border,
                                fontFamily = kenneyFont
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Settings",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Border,
                            fontFamily = kenneyFont
                        )
                    }
                }

                // 스크롤 가능한 컨텐츠 - 깔끔한 레트로 스타일
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // ⚠️ 접근성 서비스 경고 (항상 최상단에 표시)
                    if (!isAccessibilityEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(3.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                                .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                                    )
                                    context.startActivity(intent)
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MockupColors.Red)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "rebon 비활성화됨",
                                        color = MockupColors.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontFamily = kenneyFont
                                    )
                                    Text(
                                        "탭하여 설정에서 활성화하세요",
                                        color = MockupColors.TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // 💳 구독 관리 (친구 쿠폰 시스템)
                    val earnedCoupon = SubscriptionModel.earnsFriendCoupon(achievementRate)
                    val statusColor = if (earnedCoupon) MockupColors.Blue else MockupColors.TextMuted

                    // 섹션 타이틀
                    RetroSectionTitle(title = "구독 관리", fontFamily = kenneyFont)

                    // 이번 달 달성 현황 카드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, if (earnedCoupon) MockupColors.Blue else MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // 달성률 헤더 (크게 강조)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "이번 달 달성률",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                                Text(
                                    text = "${achievementRate.toInt()}%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    fontFamily = kenneyFont
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 레트로 스타일 프로그레스 바
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(4.dp))
                                    .background(MockupColors.Background, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(achievementRate / 100f)
                                        .background(statusColor, RoundedCornerShape(2.dp))
                                )
                                // 95% 마커
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .offset(x = (0.95f * 280).dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight()
                                            .background(MockupColors.Blue)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${successDays}/${totalDays}일 성공",
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                                Text(
                                    text = "목표 95%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Blue
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 쿠폰 혜택 박스 (강조)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (earnedCoupon) MockupColors.BlueLight else MockupColors.CardBackground
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (earnedCoupon) "친구 초대 쿠폰 획득!" else "95% 달성하면",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (earnedCoupon) MockupColors.Blue else MockupColors.TextPrimary
                                        )
                                        Text(
                                            text = if (earnedCoupon) "친구에게 1달 무료 선물하세요" else "친구 초대 쿠폰을 드려요!",
                                            fontSize = 13.sp,
                                            color = if (earnedCoupon) MockupColors.Blue else MockupColors.TextMuted
                                        )
                                    }
                                    PixelIcon(
                                        iconName = if (earnedCoupon) "icon_trophy" else "icon_chest",
                                        size = 32.dp
                                    )
                                }
                            }
                        }
                    }

                    // 펫 변경 카드
                    val currentPetType = preferenceManager?.getPetType()
                    val currentPetName = preferenceManager?.getPetName() ?: "친구"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager.click()
                                showPetChangeDialog = true
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "펫 변경",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary,
                                    fontFamily = kenneyFont
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "현재: $currentPetName",
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
                                    .background(MockupColors.Background, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "₩1,000",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary,
                                    fontFamily = kenneyFont
                                )
                            }
                        }
                    }

                    // 친구 초대 카드
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val inviteCode = if (userId.isNotEmpty()) "REBON-${userId.take(6).uppercase()}" else ""

                    // 프로모션 코드 사용자인지 확인 (무료 사용자는 초대 코드 발급 불가)
                    val promoCodeType = preferenceManager?.getPromoCodeType()
                    val isPromoUser = promoCodeType != null
                    val canShareInviteCode = isPaidDeposit && !isPromoUser && inviteCode.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(3.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "친구 초대",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "친구에게 1달 무료 쿠폰을 선물하세요",
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (canShareInviteCode) {
                                // 유료 결제 사용자: 초대 코드 표시
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                        .background(MockupColors.Background, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "내 초대 코드",
                                                fontSize = 12.sp,
                                                color = MockupColors.TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = inviteCode,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupColors.Blue,
                                                fontFamily = kenneyFont
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .border(2.dp, MockupColors.Blue, RoundedCornerShape(6.dp))
                                                .background(MockupColors.CardBackground, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    hapticManager.success()
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("invite_code", inviteCode)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "복사 완료!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "복사",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupColors.Blue,
                                                fontFamily = kenneyFont
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                RetroButton(
                                    text = "초대 코드와 함께 공유",
                                    onClick = {
                                        hapticManager.click()
                                        val shareText = """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait

🎁 초대 코드: $inviteCode
위 코드를 입력하면 1달 무료!
                                        """.trimIndent()

                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "친구에게 공유하기")
                                        context.startActivity(shareIntent)
                                    },
                                    backgroundColor = MockupColors.Blue,
                                    fontFamily = kenneyFont
                                )
                            } else {
                                // 프로모션 사용자: 유료 결제 안내
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "유료 결제 시 초대 코드를 받을 수 있어요",
                                            fontSize = 13.sp,
                                            color = MockupColors.Red
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                RetroButton(
                                    text = "앱 링크 공유",
                                    onClick = {
                                        hapticManager.click()
                                        val shareText = """
🏃 rebon - 걸어서 앱을 해제하세요!

친구가 rebon 앱을 추천했어요.
목표 걸음수를 달성하면 앱이 해제되는 신개념 건강 앱!

📱 앱 다운로드: https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
                                        """.trimIndent()

                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "친구에게 공유하기")
                                        context.startActivity(shareIntent)
                                    },
                                    backgroundColor = MockupColors.Blue,
                                    fontFamily = kenneyFont
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🎯 앱 제어 (접기/펼치기 가능)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager.click()
                                isAppControlExpanded = !isAppControlExpanded
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "앱 제어",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                            Text(
                                text = if (isAppControlExpanded) "▲" else "▼",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextMuted,
                                fontFamily = kenneyFont
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isAppControlExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 🎯 목표 설정
                            RetroSectionTitle(title = "목표 설정", fontFamily = kenneyFont)

                            RetroSettingsItem(
                        title = "일일 걸음 목표",
                        value = "${goal}보",
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "goal"
                        },
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canDecreaseGoal() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "목표 감소 가능: ${preferenceManager.getNextGoalDecreaseDate()}",
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔒 잠금 앱 관리
                    RetroSectionTitle(title = "잠금 앱", fontFamily = kenneyFont)

                    val lockedApps = preferenceManager?.getLockedApps() ?: emptySet()

                    // 차단 앱 목록 표시
                    if (lockedApps.isNotEmpty()) {
                        val packageManager = context.packageManager
                        val lockedAppItems = remember(lockedApps) {
                            lockedApps.mapNotNull { packageName ->
                                try {
                                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                                    val appName =
                                        packageManager.getApplicationLabel(appInfo).toString()
                                    val iconBitmap =
                                        packageManager.getApplicationIcon(appInfo).toBitmap()
                                            .asImageBitmap()
                                    Triple(packageName, appName, iconBitmap)
                                } catch (e: Exception) {
                                    null
                                }
                            }.sortedBy { it.second }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .border(3.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                                .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "차단 중",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Red,
                                        fontFamily = kenneyFont
                                    )
                                    Text(
                                        text = "${lockedApps.size}개",
                                        fontSize = 14.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                lockedAppItems.forEach { (packageName, appName, iconBitmap) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = iconBitmap,
                                            contentDescription = appName,
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = ColorFilter.colorMatrix(
                                                ColorMatrix().apply { setToSaturation(0f) }
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = appName,
                                            fontSize = 13.sp,
                                            color = MockupColors.Red,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "X",
                                            fontSize = 14.sp,
                                            fontFamily = kenneyFont,
                                            color = MockupColors.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    RetroButton(
                        text = if (lockedApps.isEmpty()) "앱 선택" else "앱 수정",
                        onClick = {
                            hapticManager.click()
                            showAppLockScreen = true
                        },
                        backgroundColor = MockupColors.Red,
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canRemoveLockedApp() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "앱 제거 가능: ${preferenceManager.getNextAppRemoveDate()}",
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // ⏰ 차단 시간대
                    RetroSectionTitle(title = "차단 시간대", fontFamily = kenneyFont)

                    val blockingPeriods = preferenceManager?.getBlockingPeriods() ?: emptySet()
                    val periodNames = mapOf(
                        "morning" to "아침",
                        "afternoon" to "점심",
                        "evening" to "저녁",
                        "night" to "밤"
                    )
                    val selectedPeriodNames =
                        blockingPeriods.mapNotNull { periodNames[it] }.joinToString(", ")
                    val displayValue = if (blockingPeriods.isEmpty()) {
                        "없음"
                    } else if (blockingPeriods.size == 4) {
                        "24시간"
                    } else {
                        selectedPeriodNames
                    }

                    RetroSettingsItem(
                        title = "시간대 설정",
                        value = displayValue,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "blockingPeriods"
                        },
                        fontFamily = kenneyFont
                    )

                    if (preferenceManager?.canChangeBlockingPeriods() == false) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "시간대 변경 가능: ${preferenceManager.getNextBlockingPeriodsChangeDate()}",
                            fontSize = 13.sp,
                            color = MockupColors.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(2.dp, MockupColors.Blue, RoundedCornerShape(8.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Tip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "선택한 시간대에만 앱을 차단합니다.\n예: 업무 시간만 차단, 저녁/밤은 자유",
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📅 제어 요일
                    RetroSectionTitle(title = "제어 요일", fontFamily = kenneyFont)

                    val controlDays = preferenceManager?.getControlDays() ?: emptySet()
                    val dayNames2 = listOf("일", "월", "화", "수", "목", "금", "토")
                    val selectedDayNames = controlDays.sorted().map { dayNames2[it] }.joinToString(", ")
                    val displayDays = if (controlDays.isEmpty()) "없음" else selectedDayNames

                    RetroSettingsItem(
                        title = "요일 설정",
                        value = displayDays,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "controlDays"
                        },
                        fontFamily = kenneyFont
                    )

                            if (preferenceManager?.canChangeControlDays() == false) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "요일 변경 가능: ${preferenceManager.getNextControlDaysChangeDate()}",
                                    fontSize = 13.sp,
                                    color = MockupColors.Red,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🏃 피트니스 앱 연결
                    RetroSectionTitle(title = "피트니스 연결", fontFamily = kenneyFont)

                    val healthConnectManager = remember { HealthConnectManager(context) }
                    val isHealthConnectAvailable = remember { healthConnectManager.isAvailable() }
                    val isHealthConnectConnected = preferenceManager?.isHealthConnectConnected() ?: false
                    val connectedAppName = preferenceManager?.getConnectedFitnessAppName() ?: ""

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(
                                3.dp,
                                if (isHealthConnectConnected) MockupColors.Blue else MockupColors.Border,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isHealthConnectConnected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isHealthConnectConnected) {
                                        Text(
                                            text = "연결됨",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.Blue,
                                            fontFamily = kenneyFont
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (connectedAppName.isNotEmpty())
                                                "$connectedAppName 데이터 사용 중"
                                            else
                                                "Health Connect 데이터 사용 중",
                                            fontSize = 13.sp,
                                            color = MockupColors.TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🔋 배터리 절약 모드",
                                            fontSize = 13.sp,
                                            color = MockupColors.Blue
                                        )
                                    } else {
                                        Text(
                                            text = "걸음 측정",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.TextPrimary,
                                            fontFamily = kenneyFont
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isHealthConnectAvailable)
                                                "삼성 헬스, Google Fit 연결"
                                            else
                                                "Health Connect 필요",
                                            fontSize = 13.sp,
                                            color = MockupColors.TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = if (isHealthConnectConnected) "OK" else "?",
                                    fontSize = 24.sp,
                                    fontFamily = kenneyFont,
                                    color = if (isHealthConnectConnected) MockupColors.Blue else MockupColors.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            RetroButton(
                                text = if (isHealthConnectConnected) "관리" else "연결",
                                onClick = {
                                    hapticManager.click()
                                    showFitnessAppConnectionScreen = true
                                },
                                backgroundColor = if (isHealthConnectConnected) MockupColors.Blue else MockupColors.Blue,
                                fontFamily = kenneyFont
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 👤 계정
                    RetroSectionTitle(title = "계정", fontFamily = kenneyFont)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(
                                3.dp,
                                if (isGoogleSignedIn) MockupColors.Blue else MockupColors.Border,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isGoogleSignedIn) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isGoogleSignedIn && !isGoogleLoading) {
                                hapticManager.click()
                                performGoogleSignIn()
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (isGoogleSignedIn) {
                                    Text(
                                        text = "연결됨",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Blue,
                                        fontFamily = kenneyFont
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = googleEmail,
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "데이터 자동 백업 중",
                                        fontSize = 13.sp,
                                        color = MockupColors.Blue
                                    )
                                } else {
                                    Text(
                                        text = "Google 계정",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary,
                                        fontFamily = kenneyFont
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "연결하면 데이터가 자동 백업됩니다",
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }
                            }
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MockupColors.Blue,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isGoogleSignedIn) "OK" else "?",
                                    fontSize = 24.sp,
                                    fontFamily = kenneyFont,
                                    color = if (isGoogleSignedIn) MockupColors.Blue else MockupColors.TextMuted
                                )
                            }
                        }
                    }

                    if (!isGoogleSignedIn) {
                        RetroButton(
                            text = "Google 로그인",
                            onClick = {
                                hapticManager.click()
                                performGoogleSignIn()
                            },
                            backgroundColor = MockupColors.Blue,
                            fontFamily = kenneyFont
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    // 불편사항 접수
                    Spacer(modifier = Modifier.height(16.dp))

                    RetroSectionTitle("불편사항 접수", kenneyFont)

                    Spacer(modifier = Modifier.height(8.dp))

                    RetroCard(onClick = { showFeedbackDialog = true }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "피드백 보내기",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                                Text(
                                    text = "버그 신고, 기능 제안 등",
                                    fontSize = 13.sp,
                                    color = MockupColors.TextSecondary
                                )
                            }
                            PixelIcon(iconName = "icon_chat", size = 28.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MockupColors.Border.copy(alpha = 0.2f), thickness = 2.dp)

                    // 앱 정보
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "rebon v${BuildConfig.VERSION_NAME}",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        fontFamily = kenneyFont,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 개발자 정보
                    Text(
                        text = "© moveoftoday",
                        fontSize = 11.sp,
                        color = MockupColors.TextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 웹사이트, 인스타그램 링크
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "web",
                            fontSize = 11.sp,
                            color = MockupColors.Blue,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://moveoftoday.life/"))
                                context.startActivity(intent)
                            }
                        )
                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            color = MockupColors.TextMuted
                        )
                        Text(
                            text = "insta",
                            fontSize = 11.sp,
                            color = MockupColors.Blue,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/moveoftoday/"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 🎁 혜택 안내 다이얼로그
            if (showDepositInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showDepositInfoDialog = false },
                    icon = {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = StandColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "달성 혜택 안내",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelIcon(iconName = "icon_trophy", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "95% 달성하면",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Blue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "친구 초대 쿠폰을 드려요!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• 친구에게 쿠폰을 선물하면\n• 친구가 1달 무료로 사용!\n• 매달 95% 달성하면 매달 쿠폰 획득",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MockupColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelIcon(iconName = "icon_chest", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "친구 초대 방법",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. 내 초대 코드 복사하기\n2. 친구에게 카톡으로 공유\n3. 친구가 코드 입력하면 끝!",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MockupColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MockupColors.BlueLight
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "꿀팁",
                                            fontSize = StandTypography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MockupColors.Blue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "매일 꾸준히 걸으면 95% 달성은\n어렵지 않아요! 친구들과 함께\n건강해지세요",
                                        fontSize = StandTypography.bodySmall,
                                        lineHeight = 18.sp,
                                        color = MockupColors.TextPrimary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDepositInfoDialog = false }
                        ) {
                            Text("확인")
                        }
                    }
                )
            }

            // 3일 제한 확인 팝업 - 레트로 스타일
            showChangeConfirmDialog?.let { type ->
                val title = when (type) {
                    "goal" -> "걸음 목표 변경"
                    "controlDays" -> "제어 요일 변경"
                    "blockingPeriods" -> "차단 시간대 변경"
                    else -> "설정 변경"
                }
                // 목표는 낮추기만 제한, 요일/시간대는 제거만 제한
                val canRemove = when (type) {
                    "goal" -> preferenceManager?.canDecreaseGoal() ?: true
                    "controlDays" -> preferenceManager?.canChangeControlDays() ?: true
                    "blockingPeriods" -> preferenceManager?.canChangeBlockingPeriods() ?: true
                    else -> true
                }
                val nextDate = when (type) {
                    "goal" -> preferenceManager?.getNextGoalDecreaseDate() ?: ""
                    "controlDays" -> preferenceManager?.getNextControlDaysChangeDate() ?: ""
                    "blockingPeriods" -> preferenceManager?.getNextBlockingPeriodsChangeDate() ?: ""
                    else -> ""
                }
                // 요일/시간대는 추가는 항상 가능
                val isAddRemoveType = type == "controlDays" || type == "blockingPeriods"

                // 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showChangeConfirmDialog = null },
                    contentAlignment = Alignment.Center
                ) {
                    // 팝업 카드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                            .background(MockupColors.Background, RoundedCornerShape(16.dp))
                            .clickable(enabled = false) { }
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 요일/시간대는 추가 자유, 제거만 제한 안내
                            if (isAddRemoveType) {
                                Text(
                                    text = "추가는 자유롭게 가능합니다.\n제거는 3일 동안 다시 변경할 수 없습니다.",
                                    fontSize = 15.sp,
                                    color = MockupColors.TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Text(
                                    text = "목표를 낮추면 3일 동안\n다시 낮출 수 없습니다.",
                                    fontSize = 15.sp,
                                    color = MockupColors.TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }

                            if (!canRemove) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (isAddRemoveType) "제거 가능일: $nextDate" else "변경 가능일: $nextDate",
                                        fontSize = 14.sp,
                                        color = MockupColors.Red,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 버튼 영역
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 취소 버튼
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .clickable { showChangeConfirmDialog = null }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "취소",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary,
                                        fontFamily = kenneyFont
                                    )
                                }

                                // 변경하기 버튼 (추가/제거 타입은 항상 가능, 제거만 제한됨)
                                val canProceed = canRemove || isAddRemoveType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(
                                            if (canProceed) MockupColors.Red else MockupColors.TextMuted,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = canProceed) {
                                            showChangeConfirmDialog = null
                                            when (type) {
                                                "goal" -> showGoalDialog = true
                                                "controlDays" -> showControlDaysDialog = true
                                                "blockingPeriods" -> showBlockingPeriodsDialog = true
                                            }
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (canProceed) "변경" else "불가",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = kenneyFont
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 펫 변경 다이얼로그
            if (showPetChangeDialog) {
                PetChangeDialog(
                    currentPetType = preferenceManager?.getPetType(),
                    currentPetName = preferenceManager?.getPetName() ?: "",
                    onDismiss = { showPetChangeDialog = false },
                    onConfirm = { newPetType, newPetName ->
                        startPetChangePurchase(newPetType, newPetName)
                    },
                    hapticManager = hapticManager
                )
            }

            // 불편사항 접수 다이얼로그
            if (showFeedbackDialog) {
                FeedbackDialog(
                    onDismiss = { showFeedbackDialog = false },
                    onSubmitted = {
                        Toast.makeText(context, "피드백이 전송되었습니다!", Toast.LENGTH_SHORT).show()
                        showFeedbackDialog = false
                    },
                    hapticManager = hapticManager
                )
            }

        }
    }
}

@Composable
private fun BlockingPeriodsDialog(
    currentPeriods: Set<String>,
    canRemove: Boolean,
    nextRemoveDate: String,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>, Boolean) -> Unit  // hasRemovals 추가
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPeriods by remember { mutableStateOf(currentPeriods) }

    val periods = listOf(
        "morning" to "아침\n06-12시",
        "afternoon" to "점심\n12-18시",
        "evening" to "저녁\n18-22시",
        "night" to "밤\n22-06시"
    )

    // 제거 여부 확인
    val hasRemovals = currentPeriods.any { it !in selectedPeriods }
    // 제거 불가 상태에서 제거하려고 할 때
    val isRemovalBlocked = !canRemove && hasRemovals

    // 풀스크린 스타일 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 타이틀
            Text(
                text = "차단 시간대",
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "차단할 시간대를 선택하세요",
                fontSize = 16.sp,
                color = MockupColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 시간대 선택 - 가로 배열
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                periods.forEach { (periodId, label) ->
                    val isSelected = selectedPeriods.contains(periodId)
                    val wasOriginallySelected = currentPeriods.contains(periodId)
                    // 원래 선택되어 있었고 제거 불가 상태면 잠금 표시
                    val isLocked = wasOriginallySelected && !canRemove

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = when {
                                    isLocked && isSelected -> MockupColors.TextMuted
                                    isSelected -> MockupColors.Border
                                    else -> Color(0xFFE0E0E0)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                when {
                                    isLocked && isSelected -> Color(0xFFE8E8E8)
                                    isSelected -> Color(0xFFE0E0E0)
                                    else -> Color.White
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected && isLocked) {
                                    // 잠금 상태에서 해제 시도 - 아무것도 안함 (안내만 표시됨)
                                } else {
                                    selectedPeriods = if (isSelected) {
                                        selectedPeriods - periodId
                                    } else {
                                        selectedPeriods + periodId
                                    }
                                }
                            }
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLocked) MockupColors.TextMuted else MockupColors.TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            if (isLocked && isSelected) {
                                Text(
                                    text = "🔒",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 (제거 불가 시 다른 안내)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (!canRemove) MockupColors.Red else MockupColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (!canRemove) MockupColors.RedLight else Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    if (!canRemove) {
                        Text(
                            text = "제거 제한 중",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "추가만 가능 · 제거 가능일: $nextRemoveDate",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    } else {
                        Text(
                            text = "Tip",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "선택하지 않으면 차단되지 않습니다",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                }

                // 적용 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border, RoundedCornerShape(12.dp))
                        .clickable { onConfirm(selectedPeriods, hasRemovals) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "적용",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ControlDaysDialog(
    currentDays: Set<Int>,
    canRemove: Boolean,
    nextRemoveDate: String,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>, Boolean) -> Unit  // hasRemovals 추가
) {
    val kenneyFont = rememberKenneyFont()
    var selectedDays by remember { mutableStateOf(currentDays) }

    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    // 제거 여부 확인
    val hasRemovals = currentDays.any { it !in selectedDays }

    // 풀스크린 스타일 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 타이틀
            Text(
                text = "제어 요일",
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "제어할 요일을 선택하세요",
                fontSize = 16.sp,
                color = MockupColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 요일 선택 - 가로 배열
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayNames.forEachIndexed { index, day ->
                    val isSelected = selectedDays.contains(index)
                    val wasOriginallySelected = currentDays.contains(index)
                    // 원래 선택되어 있었고 제거 불가 상태면 잠금
                    val isLocked = wasOriginallySelected && !canRemove

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isLocked -> MockupColors.TextMuted
                                isSelected -> MockupColors.TextPrimary
                                else -> MockupColors.TextMuted
                            }
                        )
                        if (isLocked && isSelected) {
                            Text(text = "🔒", fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (!checked && isLocked) {
                                    // 잠금 상태에서 해제 시도 - 무시
                                } else {
                                    selectedDays = if (checked) {
                                        selectedDays + index
                                    } else {
                                        selectedDays - index
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (isLocked) MockupColors.TextMuted else MockupColors.Border,
                                uncheckedColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 빠른 선택 버튼 (제거 불가 시 기존 선택 유지하면서 추가만)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "평일" to setOf(1, 2, 3, 4, 5),
                    "주말" to setOf(0, 6),
                    "매일" to setOf(0, 1, 2, 3, 4, 5, 6)
                ).forEach { (label, days) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                if (canRemove) {
                                    selectedDays = days
                                } else {
                                    // 제거 불가 시 기존 선택 유지 + 새로운 것만 추가
                                    selectedDays = currentDays + days
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 (제거 불가 시 다른 안내)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (!canRemove) MockupColors.Red else MockupColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (!canRemove) MockupColors.RedLight else Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    if (!canRemove) {
                        Text(
                            text = "제거 제한 중",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "추가만 가능 · 제거 가능일: $nextRemoveDate",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    } else {
                        Text(
                            text = "추천: 평일(월~금)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "주말은 자유롭게!",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                }

                // 적용 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border, RoundedCornerShape(12.dp))
                        .clickable { onConfirm(selectedDays, hasRemovals) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "적용",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = StandTypography.bodyLarge,
                color = MockupColors.Blue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============ 깔끔한 레트로 스타일 컴포넌트 ============

@Composable
private fun RetroSectionTitle(
    title: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MockupColors.TextPrimary,
        fontFamily = fontFamily,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun RetroSettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MockupColors.TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MockupColors.Blue,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ">",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.Border,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

@Composable
private fun RetroButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun RetroMiniButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun RetroCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        content()
    }
}

/**
 * 펫 변경 다이얼로그
 */
@Composable
private fun PetChangeDialog(
    currentPetType: String?,
    currentPetName: String,
    onDismiss: () -> Unit,
    onConfirm: (PetType, String) -> Unit,
    hapticManager: HapticManager
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPet by remember { mutableStateOf<PetType?>(currentPetType?.let {
        try { PetType.valueOf(it) } catch (e: Exception) { null }
    }) }
    var petName by remember { mutableStateOf(currentPetName) }

    val petTypes = PetType.entries.toList()

    // 풀스크린 오버레이
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // 다이얼로그 카드
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {} // 클릭 이벤트 전파 방지
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.Background, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = "펫 변경",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kenneyFont,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "새로운 친구를 선택하세요",
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 펫 선택 그리드 (3x2) - 튜토리얼 스타일
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        petTypes.take(3).forEach { pet ->
                            val isSelected = selectedPet == pet
                            Card(
                                onClick = {
                                    hapticManager.click()
                                    selectedPet = pet
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = MockupColors.Border
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetSprite(
                                        petType = pet,
                                        isWalking = false,
                                        size = 56.dp,
                                        monochrome = true,
                                        frameDurationMs = 500
                                    )
                                }
                            }
                        }
                    }
                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        petTypes.drop(3).take(3).forEach { pet ->
                            val isSelected = selectedPet == pet
                            Card(
                                onClick = {
                                    hapticManager.click()
                                    selectedPet = pet
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = MockupColors.Border
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetSprite(
                                        petType = pet,
                                        isWalking = false,
                                        size = 56.dp,
                                        monochrome = true,
                                        frameDurationMs = 500
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 이름 입력
                OutlinedTextField(
                    value = petName,
                    onValueChange = { if (it.length <= 10) petName = it },
                    label = { Text("펫 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MockupColors.Border,
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 가격 안내
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(MockupColors.Background, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "펫 변경 비용: ",
                            fontSize = 14.sp,
                            color = MockupColors.TextSecondary
                        )
                        Text(
                            text = "₩1,000",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .clickable {
                                hapticManager.click()
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                    }

                    // 결제 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .background(
                                if (selectedPet != null && petName.isNotBlank()) MockupColors.Border
                                else MockupColors.TextMuted,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = selectedPet != null && petName.isNotBlank()) {
                                hapticManager.success()
                                selectedPet?.let { pet ->
                                    onConfirm(pet, petName)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "결제하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = kenneyFont
                        )
                    }
                }
            }
        }
    }
}

/**
 * 불편사항 접수 다이얼로그
 */
@Composable
private fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    hapticManager: HapticManager
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(FeedbackManager.Category.BUG) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        screenshotUri = uri
    }

    val categories = FeedbackManager.Category.entries.toList()

    // 풀스크린 다이얼로그
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(MockupColors.Background, RoundedCornerShape(8.dp))
                        .clickable {
                            hapticManager.click()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "<",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.Border,
                        fontFamily = kenneyFont
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "불편사항 접수",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 카테고리 선택
            Text(
                text = "분류",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(3).forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MockupColors.Blue else MockupColors.Border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.drop(3).forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MockupColors.Blue else MockupColors.Border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MockupColors.BlueLight else MockupColors.CardBackground,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                        )
                    }
                }
                // 빈 공간 채우기
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 제목
            Text(
                text = "제목",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                placeholder = { Text("간단한 제목을 입력하세요") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MockupColors.Border,
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 내용
            Text(
                text = "내용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= 500) content = it },
                placeholder = { Text("자세한 내용을 입력하세요\n\n어떤 상황에서 문제가 발생했는지,\n기대했던 동작은 무엇인지 알려주세요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MockupColors.Border,
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                )
            )
            Text(
                text = "${content.length}/500",
                fontSize = 12.sp,
                color = MockupColors.TextMuted,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 스크린샷 첨부
            Text(
                text = "스크린샷 (선택)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 2.dp,
                        color = if (screenshotUri != null) MockupColors.Blue else Color(0xFFCCCCCC),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (screenshotUri != null) MockupColors.BlueLight else MockupColors.CardBackground,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        hapticManager.click()
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (screenshotUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Blue,
                            fontFamily = kenneyFont
                        )
                        Column {
                            Text(
                                text = "이미지 첨부됨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                            Text(
                                text = "탭하여 변경",
                                fontSize = 12.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextMuted,
                            fontFamily = kenneyFont
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "탭하여 이미지 선택",
                            fontSize = 13.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 제출 버튼
            val canSubmit = title.isNotBlank() && content.isNotBlank() && !isSubmitting

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .background(
                        if (canSubmit) MockupColors.Blue else MockupColors.TextMuted,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canSubmit) {
                        hapticManager.success()
                        isSubmitting = true
                        scope.launch {
                            val result = FeedbackManager.submitFeedback(
                                context = context,
                                category = selectedCategory,
                                title = title,
                                content = content,
                                screenshotUri = screenshotUri
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                onSubmitted()
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "전송 실패: ${result.exceptionOrNull()?.message}",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }else {
                    Text(
                        text = "접수하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 안내 문구
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                    .background(MockupColors.CardBackground, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "안내",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "접수된 내용은 빠른 시일 내에 검토하겠습니다.\n개인정보는 문의 처리 목적으로만 사용됩니다.",
                        fontSize = 12.sp,
                        color = MockupColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
