package com.moveoftoday.walkorwait

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.MockupColors

@Composable
fun DepositSettingScreen(
    preferenceManager: PreferenceManager?,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }
    var promoCode by remember { mutableStateOf("") }
    var showPromoInput by remember { mutableStateOf(false) }
    var promoMessage by remember { mutableStateOf<String?>(null) }
    var isPromoApplied by remember { mutableStateOf(false) }
    var isPromoFree by remember { mutableStateOf(false) }  // 프로모션으로 무료인 경우
    val promoCodeManager = remember { PromoCodeManager(context) }

    // 단계 관리 (1: 제어요일, 2: 차단시간대, 3: 보증금)
    var currentStep by remember { mutableIntStateOf(1) }

    // BillingManager cleanup
    DisposableEffect(Unit) {
        onDispose {
            billingManager?.destroy()
        }
    }

    // 보증금 옵션 (로그 스케일)
    val depositOptions = listOf(
        1000, 2000, 3000, 5000,
        10000, 20000, 30000, 50000,
        100000, 200000, 300000, 500000
    )

    var selectedIndex by remember { mutableIntStateOf(4) } // 기본: 10,000원
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedDays by remember {
        mutableStateOf(setOf(1, 2, 3, 4, 5)) // 기본: 월~금
    }
    var selectedPeriods by remember {
        mutableStateOf(setOf("morning", "afternoon", "evening", "night")) // 기본: 24시간
    }

    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
    val periods = listOf(
        "morning" to "아침\n06-12시",
        "afternoon" to "점심\n12-18시",
        "evening" to "저녁\n18-22시",
        "night" to "밤\n22-06시"
    )

    // 금액 포맷 함수
    fun formatAmount(amount: Int): String {
        return when {
            amount >= 10000 -> "${amount / 10000}만원"
            amount >= 1000 -> "${amount / 1000}천원"
            else -> "${amount}원"
        }
    }

    when (currentStep) {
        // ========== Step 1: 제어 요일 선택 ==========
        1 -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StandColors.DarkBackground)
                    .padding(horizontal = 24.dp)
                    .padding(top = 72.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 헤더
                Text(
                    text = "제어 요일 선택",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 진행 상태 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("1", fontSize = StandTypography.bodyLarge, fontWeight = FontWeight.Bold, color = StandColors.Primary)
                    Text(" / 3", fontSize = StandTypography.bodyLarge, color = Color.White.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "목표 미달성 시\n앱을 제어할 요일을 선택하세요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 제어 요일 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dayNames.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day,
                                fontSize = StandTypography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Checkbox(
                                checked = selectedDays.contains(index),
                                onCheckedChange = { checked ->
                                    selectedDays = if (checked) {
                                        selectedDays + index
                                    } else {
                                        selectedDays - index
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = StandColors.Primary,
                                    uncheckedColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 안내 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.Primary.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "추천: 평일(월~금)",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "주말은 자유롭게, 평일만 제어하는 것을 추천합니다",
                            fontSize = StandTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 다음 버튼
                Button(
                    onClick = { currentStep = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedDays.isNotEmpty()
                ) {
                    Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ========== Step 2: 차단 시간대 선택 ==========
        2 -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StandColors.DarkBackground)
                    .padding(horizontal = 24.dp)
                    .padding(top = 72.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "차단 시간대",
                        fontSize = StandTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { currentStep = 1 }) {
                        Text("이전", fontSize = StandTypography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 진행 상태 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("2", fontSize = StandTypography.bodyLarge, fontWeight = FontWeight.Bold, color = StandColors.Primary)
                    Text(" / 3", fontSize = StandTypography.bodyLarge, color = Color.White.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "목표 미달성 시\n차단할 시간대를 선택하세요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 차단 시간대 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    periods.forEach { (periodId, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedPeriods.contains(periodId))
                                        StandColors.Primary.copy(alpha = 0.2f)
                                    else
                                        Color.White.copy(alpha = 0.1f)
                                ),
                                border = if (selectedPeriods.contains(periodId))
                                    androidx.compose.foundation.BorderStroke(2.dp, StandColors.Primary)
                                else
                                    androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                onClick = {
                                    selectedPeriods = if (selectedPeriods.contains(periodId)) {
                                        selectedPeriods - periodId
                                    } else {
                                        selectedPeriods + periodId
                                    }
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = StandTypography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPeriods.contains(periodId))
                                            Color.White
                                        else
                                            Color.White.copy(alpha = 0.9f),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 안내 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MockupColors.BlueLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MockupColors.Blue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tip",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 시간대를 선택하지 않으면 차단되지 않습니다\n• 모두 선택하면 24시간 차단됩니다",
                            fontSize = StandTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 다음 버튼
                Button(
                    onClick = { currentStep = 3 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ========== Step 3: 구독 결제 ==========
        3 -> {
            // 프리미엄 색상
            val TealPrimary = Color(0xFF00BFA5)
            val TealDark = Color(0xFF008E76)
            val NavyDark = Color(0xFF0D1B2A)
            val NavyMid = Color(0xFF1B263B)
            val GlowGold = Color(0xFFFFD700)
            val BottomSheetColor = Color(0xFF0A0A0A)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BottomSheetColor) // 기본 배경을 바텀시트 색상으로
            ) {
                // 상단 그라데이션 배경 (하단으로 부드럽게 페이드아웃)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(TealPrimary, TealDark, NavyMid, BottomSheetColor),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                ) {
                    // 뒤로가기 버튼
                    TextButton(
                        onClick = { currentStep = 2 },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text("← 이전", color = Color.White.copy(alpha = 0.7f))
                    }

                    // 중앙 메인 메시지
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isPromoFree) {
                            PixelIcon(iconName = "icon_chest", size = 48.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "무료로 시작하세요",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowGold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "프로모션 코드가 적용되었습니다",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = "하루 160원으로",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "삶을 바꿔보세요",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowGold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "95% 달성하면 다음 달 무료",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelIcon(iconName = "icon_chest", size = 16.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "지금 구독하면 친구도 무료!",
                                    fontSize = 14.sp,
                                    color = GlowGold.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // 하단 바텀 시트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(BottomSheetColor)
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 48.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 가격 정보 카드
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "월 구독료",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isPromoFree) "무료" else "${SubscriptionModel.formatPrice(SubscriptionModel.MONTHLY_PRICE)}",
                                        color = if (isPromoFree) MockupColors.Blue else Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "달성 시",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "0원",
                                        color = MockupColors.Blue,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 설정 요약 (간략하게)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📅 ${selectedDays.sorted().map { listOf("일","월","화","수","목","금","토")[it] }.joinToString(", ")}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "⏰ ${selectedPeriods.size}개 시간대",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }

                        // 프로모션 코드 (접힌 상태)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.03f)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            onClick = { showPromoInput = !showPromoInput }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PixelIcon(
                                            iconName = if (isPromoApplied) "icon_visibility" else "icon_chest",
                                            size = 16.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isPromoApplied) "프로모션 적용됨" else "프로모션 코드",
                                            fontSize = 14.sp,
                                            color = if (isPromoApplied) MockupColors.Blue else Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    Text(
                                        if (showPromoInput) "▲" else "▼",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp
                                    )
                                }

                                if (showPromoInput && !isPromoApplied) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = promoCode,
                                            onValueChange = { promoCode = it.uppercase(); promoMessage = null },
                                            placeholder = { Text("코드 입력", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                cursorColor = TealPrimary,
                                                focusedBorderColor = TealPrimary,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                            )
                                        )
                                        Button(
                                            onClick = {
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
                                            enabled = promoCode.isNotEmpty(),
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Text("적용", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (promoMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            promoMessage ?: "",
                                            fontSize = 12.sp,
                                            color = if (isPromoApplied) MockupColors.Blue else MockupColors.Red
                                        )
                                    }
                                }
                            }
                        }

                        // 오류 메시지
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MockupColors.Red.copy(alpha = 0.2f)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 13.sp,
                                    color = MockupColors.Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 결제 버튼
                        Button(
                            onClick = {
                        if (selectedDays.isNotEmpty()) {
                            isProcessing = true
                            errorMessage = null

                            // 제어 기간 자동 설정
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val today = Calendar.getInstance()
                            startDate = sdf.format(today.time)
                            today.add(Calendar.DAY_OF_MONTH, 30)
                            endDate = sdf.format(today.time)

                            scope.launch {
                                try {
                                    // 1. Firebase 익명 로그인
                                    val currentUser = auth.currentUser
                                    if (currentUser == null) {
                                        Log.d("DepositSetting", "Signing in anonymously...")
                                        auth.signInAnonymously().await()
                                        Log.d("DepositSetting", "✅ Signed in: ${auth.currentUser?.uid}")
                                    }

                                    // 프로모션으로 무료인 경우 결제 스킵
                                    if (isPromoFree) {
                                        Log.d("DepositSetting", "✅ Promo free - skipping payment")

                                        // 로컬 설정 저장
                                        preferenceManager?.saveDeposit(1) // 프로모션 무료 (1로 설정해야 차단 활성화)
                                        preferenceManager?.saveControlStartDate(startDate)
                                        preferenceManager?.saveControlEndDate(endDate)
                                        preferenceManager?.saveControlDays(selectedDays)
                                        preferenceManager?.saveBlockingPeriods(selectedPeriods)
                                        preferenceManager?.saveSuccessDays(0)
                                        preferenceManager?.setPaidDeposit(true)
                                        preferenceManager?.saveTodaySteps(0) // 걸음 수 리셋

                                        // 체험 기간을 과거로 설정 (즉시 차단 시작)
                                        val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val pastDate = Calendar.getInstance()
                                        pastDate.add(Calendar.DAY_OF_MONTH, -10)
                                        preferenceManager?.saveTrialStartDate(sdf2.format(pastDate.time))
                                        pastDate.add(Calendar.DAY_OF_MONTH, 3)
                                        preferenceManager?.saveTrialEndDate(sdf2.format(pastDate.time))

                                        isProcessing = false
                                        onComplete()
                                        return@launch
                                    }

                                    // 2. BillingManager 초기화 및 결제 시작
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
                                            Log.d("DepositSetting", "✅ Purchase success: ${purchase.orderId}")

                                            // 3. Firebase에 구독 정보 저장
                                            scope.launch {
                                                try {
                                                    val result = subscriptionManager.createSubscription(
                                                        goal = preferenceManager?.getGoal() ?: 8000,
                                                        controlDays = selectedDays.toList(),
                                                        purchase = purchase
                                                    )

                                                    if (result.isSuccess) {
                                                        // 4. 로컬에도 저장 (구독 활성화 표시용)
                                                        preferenceManager?.saveDeposit(SubscriptionModel.MONTHLY_PRICE)

                                                        // 체험 기간 없음 - 즉시 차단 시작
                                                        val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                        val cal = Calendar.getInstance()
                                                        cal.add(Calendar.DAY_OF_MONTH, -10) // 과거로 설정
                                                        val trialStartDate = sdf2.format(cal.time)
                                                        cal.add(Calendar.DAY_OF_MONTH, 3)
                                                        val trialEndDate = sdf2.format(cal.time) // 이미 만료됨

                                                        preferenceManager?.saveTrialStartDate(trialStartDate)
                                                        preferenceManager?.saveTrialEndDate(trialEndDate)

                                                        // 제어 기간 저장
                                                        preferenceManager?.saveControlStartDate(startDate)
                                                        preferenceManager?.saveControlEndDate(endDate)
                                                        preferenceManager?.saveControlDays(selectedDays)
                                                        preferenceManager?.saveBlockingPeriods(selectedPeriods)
                                                        preferenceManager?.saveSuccessDays(0)
                                                        preferenceManager?.setPaidDeposit(true)
                                                        preferenceManager?.saveTodaySteps(0) // 걸음 수 리셋

                                                        isProcessing = false
                                                        onComplete()
                                                    } else {
                                                        errorMessage = "구독 정보 저장 실패: ${result.exceptionOrNull()?.message}"
                                                        isProcessing = false
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("DepositSetting", "Error saving subscription: ${e.message}")
                                                    errorMessage = "오류: ${e.message}"
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                        onPurchaseFailure = { error ->
                                            Log.e("DepositSetting", "❌ Purchase failed: $error")
                                            errorMessage = error
                                            isProcessing = false
                                        }
                                    )

                                    // 구독 결제 시작 (단일 상품: 4,900원/월)
                                    billingManager?.startSubscription(activity)

                                } catch (e: Exception) {
                                    Log.e("DepositSetting", "Error: ${e.message}")
                                    errorMessage = "오류가 발생했습니다: ${e.message}"
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedDays.isNotEmpty() && !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPromoFree) MockupColors.Blue else StandColors.Primary
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            if (isPromoFree) "무료로 시작하기" else "결제하고 시작하기",
                            fontSize = StandTypography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                        // 🧪 테스트 모드 버튼 (디버그 빌드에서만 표시)
                        if (BuildConfig.DEBUG) {
                            OutlinedButton(
                                onClick = {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val today = Calendar.getInstance()
                                    val testStartDate = sdf.format(today.time)
                                    today.add(Calendar.DAY_OF_MONTH, 30)
                                    val testEndDate = sdf.format(today.time)

                                    preferenceManager?.saveDeposit(depositOptions[selectedIndex])
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

                                    onComplete()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MockupColors.TextMuted
                                )
                            ) {
                                Text("테스트 모드로 시작", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
