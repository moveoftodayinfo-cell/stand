package com.moveoftoday.walkorwait.pet

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pet-guided Deposit Setting Screen matching mockup layout
 * 3 Steps: 제어 요일 → 차단 시간대 → 결제
 */
@Composable
fun PetDepositSettingScreen(
    petType: PetType,
    petName: String,
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    startAtStep: Int = 0,  // 기존 사용자는 2로 설정하여 결제 화면으로 바로 이동
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var currentStep by remember { mutableIntStateOf(startAtStep) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 프로모션 관련
    var promoCode by remember { mutableStateOf("") }
    var showPromoInput by remember { mutableStateOf(false) }
    var promoMessage by remember { mutableStateOf<String?>(null) }
    var isPromoApplied by remember { mutableStateOf(false) }
    var isPromoFree by remember { mutableStateOf(false) }
    val promoCodeManager = remember { PromoCodeManager(context) }

    // 설정값 (재결제 시에는 기존 저장된 값 사용)
    var selectedDays by remember {
        mutableStateOf(
            if (startAtStep > 0) preferenceManager?.getControlDays() ?: setOf(1, 2, 3, 4, 5)
            else setOf(1, 2, 3, 4, 5)
        )
    }
    var selectedPeriods by remember {
        mutableStateOf(
            if (startAtStep > 0) preferenceManager?.getBlockingPeriods() ?: setOf("morning", "afternoon", "evening", "night")
            else setOf("morning", "afternoon", "evening", "night")
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            billingManager?.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        when (currentStep) {
            0 -> ControlDaysStep(
                petType = petType,
                petName = petName,
                selectedDays = selectedDays,
                onDaysChanged = { selectedDays = it },
                onNext = {
                    hapticManager?.click()
                    currentStep = 1
                },
                hapticManager = hapticManager
            )
            1 -> BlockingPeriodsStep(
                petType = petType,
                petName = petName,
                selectedPeriods = selectedPeriods,
                onPeriodsChanged = { selectedPeriods = it },
                onBack = { currentStep = 0 },
                onNext = {
                    hapticManager?.click()
                    currentStep = 2
                },
                hapticManager = hapticManager
            )
            2 -> PaymentStep(
                petType = petType,
                petName = petName,
                selectedDays = selectedDays,
                selectedPeriods = selectedPeriods,
                isProcessing = isProcessing,
                errorMessage = errorMessage,
                promoCode = promoCode,
                showPromoInput = showPromoInput,
                promoMessage = promoMessage,
                isPromoApplied = isPromoApplied,
                isPromoFree = isPromoFree,
                onPromoCodeChanged = { promoCode = it.uppercase() },
                onShowPromoInput = { showPromoInput = it },
                onApplyPromo = {
                    if (promoCode.isNotEmpty()) {
                        promoMessage = "확인 중..."
                        scope.launch {
                            val result = promoCodeManager.validateAndApply(promoCode)
                            when (result) {
                                is PromoCodeManager.PromoResult.Success -> {
                                    promoMessage = result.message
                                    isPromoApplied = true
                                    isPromoFree = result.freeDays > 0
                                    if (result.freeDays > 0) {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val calendar = Calendar.getInstance()
                                        calendar.add(Calendar.DAY_OF_MONTH, result.freeDays)
                                        val endDate = sdf.format(calendar.time)
                                        preferenceManager?.savePromoFreeEndDate(endDate)
                                        // Firebase에 프로모션 정보 동기화
                                        val app = context.applicationContext as WalkorWaitApp
                                        app.userDataRepository.savePromoInfo(
                                            code = promoCode.uppercase(),
                                            type = preferenceManager?.getPromoCodeType(),
                                            hostId = preferenceManager?.getPromoHostId(),
                                            endDate = endDate
                                        )
                                    }
                                }
                                is PromoCodeManager.PromoResult.Error -> {
                                    promoMessage = result.message
                                    isPromoApplied = false
                                    isPromoFree = false
                                }
                            }
                        }
                    }
                },
                onBack = { currentStep = 1 },
                onPayment = {
                    isProcessing = true
                    errorMessage = null

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = Calendar.getInstance()
                    val startDate = sdf.format(today.time)
                    today.add(Calendar.DAY_OF_MONTH, 30)
                    val endDate = sdf.format(today.time)

                    scope.launch {
                        try {
                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                auth.signInAnonymously().await()
                            }

                            if (isPromoFree) {
                                preferenceManager?.saveDeposit(1)
                                preferenceManager?.saveControlStartDate(startDate)
                                preferenceManager?.saveControlEndDate(endDate)
                                preferenceManager?.saveControlDays(selectedDays)
                                preferenceManager?.saveBlockingPeriods(selectedPeriods)
                                preferenceManager?.saveSuccessDays(0)
                                preferenceManager?.setPaidDeposit(true)
                                preferenceManager?.saveTodaySteps(0)

                                val pastDate = Calendar.getInstance()
                                pastDate.add(Calendar.DAY_OF_MONTH, -10)
                                preferenceManager?.saveTrialStartDate(sdf.format(pastDate.time))
                                pastDate.add(Calendar.DAY_OF_MONTH, 3)
                                preferenceManager?.saveTrialEndDate(sdf.format(pastDate.time))

                                isProcessing = false
                                hapticManager?.success()
                                onComplete()
                                return@launch
                            }

                            val activity = context as? Activity
                            if (activity == null) {
                                errorMessage = "Activity를 찾을 수 없습니다"
                                isProcessing = false
                                return@launch
                            }

                            val subscriptionManager = SubscriptionManager(context)
                            billingManager = BillingManager(
                                context = context,
                                onPurchaseSuccess = { purchase ->
                                    scope.launch {
                                        try {
                                            val result = subscriptionManager.createSubscription(
                                                goal = preferenceManager?.getGoal() ?: 8000,
                                                controlDays = selectedDays.toList(),
                                                purchase = purchase
                                            )

                                            if (result.isSuccess) {
                                                preferenceManager?.saveDeposit(SubscriptionModel.MONTHLY_PRICE)
                                                val cal = Calendar.getInstance()
                                                cal.add(Calendar.DAY_OF_MONTH, -10)
                                                preferenceManager?.saveTrialStartDate(sdf.format(cal.time))
                                                cal.add(Calendar.DAY_OF_MONTH, 3)
                                                preferenceManager?.saveTrialEndDate(sdf.format(cal.time))
                                                preferenceManager?.saveControlStartDate(startDate)
                                                preferenceManager?.saveControlEndDate(endDate)
                                                preferenceManager?.saveControlDays(selectedDays)
                                                preferenceManager?.saveBlockingPeriods(selectedPeriods)
                                                preferenceManager?.saveSuccessDays(0)
                                                preferenceManager?.setPaidDeposit(true)
                                                preferenceManager?.saveTodaySteps(0)

                                                isProcessing = false
                                                hapticManager?.success()
                                                onComplete()
                                            } else {
                                                errorMessage = "구독 정보 저장 실패"
                                                isProcessing = false
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "오류: ${e.message}"
                                            isProcessing = false
                                        }
                                    }
                                },
                                onPurchaseFailure = { error ->
                                    errorMessage = error
                                    isProcessing = false
                                }
                            )

                            billingManager?.startSubscription(activity)

                        } catch (e: Exception) {
                            errorMessage = "오류가 발생했습니다: ${e.message}"
                            isProcessing = false
                        }
                    }
                },
                onTestMode = if (BuildConfig.DEBUG) {
                    {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val today = Calendar.getInstance()
                        val testStartDate = sdf.format(today.time)
                        today.add(Calendar.DAY_OF_MONTH, 30)
                        val testEndDate = sdf.format(today.time)

                        preferenceManager?.saveDeposit(10000)
                        preferenceManager?.saveControlStartDate(testStartDate)
                        preferenceManager?.saveControlEndDate(testEndDate)
                        preferenceManager?.saveControlDays(selectedDays)
                        preferenceManager?.saveBlockingPeriods(selectedPeriods)
                        preferenceManager?.saveSuccessDays(0)
                        preferenceManager?.setPaidDeposit(true)

                        val pastDate = Calendar.getInstance()
                        pastDate.add(Calendar.DAY_OF_MONTH, -10)
                        preferenceManager?.saveTrialStartDate(sdf.format(pastDate.time))
                        pastDate.add(Calendar.DAY_OF_MONTH, 3)
                        preferenceManager?.saveTrialEndDate(sdf.format(pastDate.time))
                        preferenceManager?.saveTodaySteps(0)

                        hapticManager?.success()
                        onComplete()
                    }
                } else null,
                hapticManager = hapticManager,
                showNavigation = startAtStep == 0  // 재결제 시(startAtStep > 0) 상단 네비게이션 숨김
            )
        }
    }
}

/**
 * Step 1: Control Days Selection
 */
@Composable
private fun ControlDaysStep(
    petType: PetType,
    petName: String,
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit,
    onNext: () -> Unit,
    hapticManager: HapticManager?
) {
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "어떤 요일에 걸을 거야?"
        PetPersonality.CUTE -> "언제 걸을 거야~?"
        PetPersonality.TSUNDERE -> "요일... 정해야지."
        PetPersonality.DIALECT -> "어느 요일에 걸을 낀지 정하자"
        PetPersonality.TIMID -> "저, 요일 정해주세요..."
        PetPersonality.POSITIVE -> "언제 걸을지 정하자!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = "1/3",
                fontSize = 16.sp,
                color = MockupColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area
        PetArea(
            petType = petType,
            isWalking = false,
            speechText = speechText,
            happinessLevel = 3,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Days selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "제어 요일 선택",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dayNames.forEachIndexed { index, day ->
                        val isSelected = selectedDays.contains(index)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MockupColors.Border
                                    else Color.White
                                )
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                .clickable {
                                    hapticManager?.lightClick()
                                    onDaysChanged(
                                        if (isSelected) selectedDays - index
                                        else selectedDays + index
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MockupColors.TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "목표 미달성 시 이 요일에 앱이 제어됩니다",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Tip card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MockupColors.CardBackground)
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PixelIcon(iconName = "icon_star", size = 24.dp)
            Column {
                Text(
                    text = "추천: 평일 (월~금)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = "주말은 자유롭게 쉬어가세요",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        MockupButton(
            text = "다음",
            onClick = onNext,
            enabled = selectedDays.isNotEmpty()
        )
    }
}

/**
 * Step 2: Blocking Periods Selection
 */
@Composable
private fun BlockingPeriodsStep(
    petType: PetType,
    petName: String,
    selectedPeriods: Set<String>,
    onPeriodsChanged: (Set<String>) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    hapticManager: HapticManager?
) {
    val periods = listOf(
        "morning" to Pair("아침", "06-12시"),
        "afternoon" to Pair("점심", "12-18시"),
        "evening" to Pair("저녁", "18-22시"),
        "night" to Pair("밤", "22-06시")
    )

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "언제 차단할 거야?"
        PetPersonality.CUTE -> "차단 시간 정하자~"
        PetPersonality.TSUNDERE -> "시간대... 정해야지."
        PetPersonality.DIALECT -> "몇 시에 차단할 낀지 정하자"
        PetPersonality.TIMID -> "저, 시간대 정해주세요..."
        PetPersonality.POSITIVE -> "시간대 정하자! 거의 다 왔어!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← 이전",
                fontSize = 14.sp,
                color = MockupColors.TextMuted,
                modifier = Modifier.clickable { onBack() }
            )
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = "2/3",
                fontSize = 16.sp,
                color = MockupColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area
        PetArea(
            petType = petType,
            isWalking = false,
            speechText = speechText,
            happinessLevel = 3,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Periods selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "차단 시간대 선택",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEach { (periodId, labels) ->
                        val isSelected = selectedPeriods.contains(periodId)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MockupColors.Border
                                    else Color.White
                                )
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                .clickable {
                                    hapticManager?.lightClick()
                                    onPeriodsChanged(
                                        if (isSelected) selectedPeriods - periodId
                                        else selectedPeriods + periodId
                                    )
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = labels.first,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MockupColors.TextPrimary
                                )
                                Text(
                                    text = labels.second,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MockupColors.TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = if (selectedPeriods.size == 4) "24시간 차단됩니다"
                    else if (selectedPeriods.isEmpty()) "시간대를 선택하지 않으면 차단되지 않습니다"
                    else "선택한 시간대에만 차단됩니다",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        MockupButton(
            text = "다음",
            onClick = onNext
        )
    }
}

/**
 * Step 3: Payment
 */
@Composable
private fun PaymentStep(
    petType: PetType,
    petName: String,
    selectedDays: Set<Int>,
    selectedPeriods: Set<String>,
    isProcessing: Boolean,
    errorMessage: String?,
    promoCode: String,
    showPromoInput: Boolean,
    promoMessage: String?,
    isPromoApplied: Boolean,
    isPromoFree: Boolean,
    onPromoCodeChanged: (String) -> Unit,
    onShowPromoInput: (Boolean) -> Unit,
    onApplyPromo: () -> Unit,
    onBack: () -> Unit,
    onPayment: () -> Unit,
    onTestMode: (() -> Unit)?,
    hapticManager: HapticManager?,
    showNavigation: Boolean = true  // 재결제 시 false로 설정하여 상단 네비게이션 숨김
) {
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    val speechText = when {
        isPromoFree -> when (petType.personality) {
            PetPersonality.TOUGH -> "무료로 시작하자."
            PetPersonality.CUTE -> "무료야! 럭키~"
            PetPersonality.TSUNDERE -> "무, 무료라니... 좋은 거 아냐!"
            PetPersonality.DIALECT -> "무료로 시작이다"
            PetPersonality.TIMID -> "저, 무료라니 다행이에요..."
            PetPersonality.POSITIVE -> "무료다! 대박!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "이제 시작해볼까."
            PetPersonality.CUTE -> "마지막이야! 가보자고~"
            PetPersonality.TSUNDERE -> "뭐, 결제... 해야겠지."
            PetPersonality.DIALECT -> "이제 시작이다"
            PetPersonality.TIMID -> "저, 마지막 단계예요..."
            PetPersonality.POSITIVE -> "마지막! 거의 다 왔어!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Status bar (재결제 시 숨김)
        if (showNavigation) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← 이전",
                    fontSize = 14.sp,
                    color = MockupColors.TextMuted,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = petName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = "3/3",
                    fontSize = 16.sp,
                    color = MockupColors.TextMuted
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
        }

        // Pet area
        PetArea(
            petType = petType,
            isWalking = true,
            speechText = speechText,
            happinessLevel = 5,
            backgroundColor = if (isPromoFree) MockupColors.AchievedCard else MockupColors.CardBackground,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Price card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "월 구독료",
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted
                        )
                        Text(
                            text = if (isPromoFree) "무료" else "${SubscriptionModel.formatPrice(SubscriptionModel.MONTHLY_PRICE)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPromoFree) Color(0xFF4CAF50) else MockupColors.TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "95% 달성 시",
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted
                        )
                        Text(
                            text = "0원",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PixelIcon(iconName = "icon_target", size = 16.dp)
                        Text(
                            text = selectedDays.sorted().map { dayNames[it] }.joinToString(", "),
                            fontSize = 12.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PixelIcon(iconName = "icon_thunder", size = 16.dp)
                        Text(
                            text = "${selectedPeriods.size}개 시간대",
                            fontSize = 12.sp,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Promo code card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MockupColors.Border),
            onClick = { onShowPromoInput(!showPromoInput) }
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelIcon(iconName = "icon_star", size = 18.dp)
                        Text(
                            text = if (isPromoApplied) "프로모션 적용됨" else "프로모션 코드",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPromoApplied) Color(0xFF4CAF50) else MockupColors.TextPrimary
                        )
                    }
                    Text(
                        text = if (showPromoInput) "▲" else "▼",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted
                    )
                }

                if (showPromoInput && !isPromoApplied) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCode,
                            onValueChange = onPromoCodeChanged,
                            placeholder = { Text("코드 입력", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MockupColors.Border,
                                unfocusedBorderColor = MockupColors.Border
                            )
                        )
                        Button(
                            onClick = onApplyPromo,
                            enabled = promoCode.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border)
                        ) {
                            Text("적용")
                        }
                    }
                    if (promoMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = promoMessage,
                            fontSize = 12.sp,
                            color = if (isPromoApplied) Color(0xFF4CAF50) else Color(0xFFE57373)
                        )
                    }
                }
            }
        }

        // Error message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(15.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE57373))
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(15.dp),
                    fontSize = 14.sp,
                    color = Color(0xFFE57373)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Payment button
        MockupButton(
            text = if (isProcessing) "처리 중..." else if (isPromoFree) "무료로 시작하기" else "결제하고 시작하기",
            onClick = onPayment,
            enabled = !isProcessing
        )

        // Test mode button (debug only)
        if (onTestMode != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onTestMode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF9800)
                )
            ) {
                Text("테스트 모드로 시작", fontWeight = FontWeight.Bold)
            }
        }
    }
}
