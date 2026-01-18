package com.moveoftoday.walkorwait

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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.pet.MockupColors
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

    // 1초마다 업데이트 + 접근성 체크
    LaunchedEffect(Unit) {
        while (true) {
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
        DepositSettingScreen(
            preferenceManager = preferenceManager,
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
            },
            preferenceManager = preferenceManager,
            hapticManager = hapticManager
        )
    } else if (showBlockingPeriodsDialog) {
        // 차단 시간대 선택 (풀스크린)
        BlockingPeriodsDialog(
            currentPeriods = preferenceManager?.getBlockingPeriods() ?: emptySet(),
            onDismiss = { showBlockingPeriodsDialog = false },
            onConfirm = { newPeriods ->
                preferenceManager?.saveBlockingPeriods(newPeriods)
                preferenceManager?.saveBlockingPeriodsChangeTime()
                showBlockingPeriodsDialog = false
            }
        )
    } else if (showControlDaysDialog) {
        // 제어 요일 선택 (풀스크린)
        ControlDaysDialog(
            currentDays = preferenceManager?.getControlDays() ?: emptySet(),
            onDismiss = { showControlDaysDialog = false },
            onConfirm = { newDays ->
                preferenceManager?.saveControlDays(newDays)
                preferenceManager?.saveControlDaysChangeTime()
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
                        .background(MockupColors.CardBackground)
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
                                        "Stand 비활성화됨",
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

                    // 친구 초대 카드
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val inviteCode = if (userId.isNotEmpty()) "STAND-${userId.take(6).uppercase()}" else ""

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

                            if (isPaidDeposit && inviteCode.isNotEmpty()) {
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
🏃 Stand - 걸어서 앱을 해제하세요!

친구가 Stand 앱을 추천했어요.
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
🏃 Stand - 걸어서 앱을 해제하세요!

친구가 Stand 앱을 추천했어요.
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

                    // 앱 정보
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Stand v1.0",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        fontFamily = kenneyFont,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

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
                val canChange = when (type) {
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

                            Text(
                                text = "설정을 변경하면 3일 동안\n다시 변경할 수 없습니다.",
                                fontSize = 15.sp,
                                color = MockupColors.TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )

                            if (!canChange) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "변경 가능일: $nextDate",
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

                                // 변경하기 버튼
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(3.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(
                                            if (canChange) MockupColors.Red else MockupColors.TextMuted,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = canChange) {
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
                                        text = if (canChange) "변경" else "불가",
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
        }
    }
}

@Composable
private fun BlockingPeriodsDialog(
    currentPeriods: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPeriods by remember { mutableStateOf(currentPeriods) }

    val periods = listOf(
        "morning" to "아침\n06-12시",
        "afternoon" to "점심\n12-18시",
        "evening" to "저녁\n18-22시",
        "night" to "밤\n22-06시"
    )

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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) MockupColors.Border else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isSelected) Color(0xFFE0E0E0) else Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPeriods = if (isSelected) {
                                    selectedPeriods - periodId
                                } else {
                                    selectedPeriods + periodId
                                }
                            }
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MockupColors.TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
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
                        .clickable { onConfirm(selectedPeriods) }
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ControlDaysDialog(
    currentDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    var selectedDays by remember { mutableStateOf(currentDays) }

    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MockupColors.TextPrimary else MockupColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedDays = if (checked) {
                                    selectedDays + index
                                } else {
                                    selectedDays - index
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MockupColors.Border,
                                uncheckedColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 빠른 선택 버튼
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
                            .clickable { selectedDays = days }
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

            // 추천 안내
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
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
                        .clickable { onConfirm(selectedDays) }
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

            Spacer(modifier = Modifier.height(24.dp))
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
