package com.moveoftoday.walkorwait

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var showCancelSubscriptionDialog by remember { mutableStateOf(false) }
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

    // 3단계 색상 및 상태 판정
    val statusColor = when {
        achievementRate >= 95f -> StandColors.Success  // 초록 (완전 달성)
        achievementRate >= 80f -> StandColors.Warning  // 주황 (부분 달성)
        else -> StandColors.Error  // 빨강 (실패)
    }

    val statusText = when {
        achievementRate >= 95f -> "🏆 완전 달성"
        achievementRate >= 80f -> "✅ 부분 달성"
        else -> "⚠️ 진행중"
    }

    val statusDescription = when {
        achievementRate >= 95f -> "+4,900 크레딧 (실질 무료)"
        achievementRate >= 80f -> "+2,400 크레딧 (실질 2,500원)"
        else -> "크레딧 없음 (정가 4,900원)"
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
    } else {
        // 프리미엄 색상
        val TealPrimary = Color(0xFF00BFA5)
        val TealDark = Color(0xFF008E76)
        val NavyDark = Color(0xFF0D1B2A)
        val NavyMid = Color(0xFF1B263B)
        val BottomSheetBg = Color(0xFF0A0A0A)
        val CardBg = Color.White.copy(alpha = 0.1f)
        val CardBgLight = Color.White.copy(alpha = 0.05f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(TealPrimary, TealDark, NavyMid, NavyDark),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 48.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        hapticManager.click()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "뒤로가기",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "설정",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 스크롤 가능한 컨텐츠
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(BottomSheetBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // ⚠️ 접근성 서비스 경고 (항상 최상단에 표시)
                    if (!isAccessibilityEnabled) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                                    )
                                    context.startActivity(intent)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5722).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Stand가 비활성화되어 있습니다",
                                        color = Color(0xFFFF5722),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "탭하여 설정에서 활성화하세요",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 💳 구독 관리 (크레딧 시스템)
                    val creditAmount = SubscriptionModel.getCreditAmount(achievementRate)
                    val effectivePrice = SubscriptionModel.getEffectivePrice(achievementRate)
                    val subscriptionTier = SubscriptionModel.getTier(achievementRate)
                    val tierColor = when (subscriptionTier) {
                        SubscriptionModel.Tier.FREE -> Color(0xFF4CAF50)
                        SubscriptionModel.Tier.DISCOUNT -> Color(0xFFFF9800)
                        SubscriptionModel.Tier.PENALTY -> Color.White.copy(alpha = 0.5f)
                    }

                    Text(
                        text = "구독 관리",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 현재 구독 상태 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBg
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Google Play 결제 금액 표시
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Google Play 결제",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "월 ${SubscriptionModel.formatPrice(SubscriptionModel.BASE_PRICE)}",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 예상 크레딧
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "예상 크레딧",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = SubscriptionModel.formatCredit(creditAmount),
                                    fontSize = StandTypography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 실질 부담 금액
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "실질 부담",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = SubscriptionModel.formatPrice(effectivePrice),
                                    fontSize = StandTypography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 달성률 프로그레스
                            Text(
                                text = "이번 달 달성률",
                                fontSize = StandTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { achievementRate / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = tierColor,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${successDays} / ${totalDays}일 달성",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${achievementRate.toInt()}%",
                                    fontSize = StandTypography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // 크레딧 안내
                            Text(
                                text = "💳 크레딧 시스템 안내",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "95% 이상",
                                    fontSize = StandTypography.bodySmall,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    "+4,900 (실질 무료)",
                                    fontSize = StandTypography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "80~95% 미만",
                                    fontSize = StandTypography.bodySmall,
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    "+2,400 (실질 2,500원)",
                                    fontSize = StandTypography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "80% 미만",
                                    fontSize = StandTypography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    "0 (정가 4,900원)",
                                    fontSize = StandTypography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // 친구 초대 카드
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val inviteCode = if (userId.isNotEmpty()) "STAND-${userId.take(6).uppercase()}" else ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TealPrimary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🎁 친구 초대하기",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "친구에게 1달 무료 쿠폰을 선물하세요",
                                fontSize = StandTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (isPaidDeposit && inviteCode.isNotEmpty()) {
                                // 유료 결제 사용자: 초대 코드 표시
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "내 초대 코드",
                                                fontSize = StandTypography.labelLarge,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = inviteCode,
                                                fontSize = StandTypography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = StandColors.GlowYellow
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                hapticManager.success()
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("invite_code", inviteCode)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "초대 코드가 복사되었습니다", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text(
                                                text = "복사",
                                                fontSize = StandTypography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        hapticManager.click()
                                        // 유료 사용자: 초대 코드 포함 공유
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
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("초대 코드와 함께 공유", fontSize = StandTypography.bodyMedium)
                                }
                            } else {
                                // 프로모션 사용자: 유료 결제 안내
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💡",
                                            fontSize = StandTypography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "유료 결제 시 친구 초대 코드를 받을 수 있어요",
                                            fontSize = StandTypography.bodySmall,
                                            color = Color(0xFFFF9800)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        hapticManager.click()
                                        // 프로모션 사용자: 앱 링크만 공유
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
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("앱 링크 공유", fontSize = StandTypography.bodyMedium)
                                }
                            }
                        }
                    }

                    // 구독 취소 버튼
                    TextButton(
                        onClick = {
                            hapticManager.warning()
                            showCancelSubscriptionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "구독 취소",
                            fontSize = StandTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // 🎯 목표 설정
                    Text(
                        text = "목표 설정",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingsItem(
                        title = "일일 걸음 목표",
                        value = "${goal}걸음",
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "goal"
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (preferenceManager?.canDecreaseGoal() == false) {
                        Text(
                            text = "⚠️ 목표 감소 가능: ${preferenceManager.getNextGoalDecreaseDate()}",
                            fontSize = StandTypography.labelLarge,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // 🔒 잠금 앱 관리
                    Text(
                        text = "잠금 앱 관리",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5722).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "차단 중인 앱",
                                        fontSize = StandTypography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5722)
                                    )
                                    Text(
                                        text = "${lockedApps.size}개",
                                        fontSize = StandTypography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
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
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = appName,
                                            fontSize = StandTypography.bodyMedium,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "🔒",
                                            fontSize = StandTypography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            hapticManager.click()
                            showAppLockScreen = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lockedApps.isEmpty()) "차단 앱 선택" else "차단 앱 수정",
                            fontSize = StandTypography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (preferenceManager?.canRemoveLockedApp() == false) {
                        Text(
                            text = "⚠️ 앱 제거 가능: ${preferenceManager.getNextAppRemoveDate()}",
                            fontSize = StandTypography.labelLarge,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // ⏰ 차단 시간대
                    Text(
                        text = "차단 시간대",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

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
                        "차단 안함"
                    } else if (blockingPeriods.size == 4) {
                        "24시간"
                    } else {
                        selectedPeriodNames
                    }

                    SettingsItem(
                        title = "차단 시간대 설정",
                        value = displayValue,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "blockingPeriods"
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (preferenceManager?.canChangeBlockingPeriods() == false) {
                        Text(
                            text = "⚠️ 시간대 변경 가능: ${preferenceManager.getNextBlockingPeriodsChangeDate()}",
                            fontSize = StandTypography.labelLarge,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TealPrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "💡 시간대별 차단",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "선택한 시간대에만 앱을 차단합니다.\n예: 업무시간(아침+점심)만 차단하고 저녁/밤은 자유",
                                fontSize = StandTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // 📅 제어 요일
                    Text(
                        text = "제어 요일",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val controlDays = preferenceManager?.getControlDays() ?: emptySet()
                    val dayNames2 = listOf("일", "월", "화", "수", "목", "금", "토")
                    val selectedDayNames = controlDays.sorted().map { dayNames2[it] }.joinToString(", ")
                    val displayDays = if (controlDays.isEmpty()) "선택 안함" else selectedDayNames

                    SettingsItem(
                        title = "제어 요일 설정",
                        value = displayDays,
                        onClick = {
                            hapticManager.click()
                            showChangeConfirmDialog = "controlDays"
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (preferenceManager?.canChangeControlDays() == false) {
                        Text(
                            text = "⚠️ 요일 변경 가능: ${preferenceManager.getNextControlDaysChangeDate()}",
                            fontSize = StandTypography.labelLarge,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // 🏃 피트니스 앱 연결
                    Text(
                        text = "피트니스 앱 연결",
                        fontSize = StandTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val healthConnectManager = remember { HealthConnectManager(context) }
                    val isHealthConnectAvailable = remember { healthConnectManager.isAvailable() }
                    val isHealthConnectConnected = preferenceManager?.isHealthConnectConnected() ?: false
                    val connectedAppName = preferenceManager?.getConnectedFitnessAppName() ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHealthConnectConnected)
                                Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else
                                CardBg
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isHealthConnectConnected) {
                                        Text(
                                            text = "✅ 연결됨",
                                            fontSize = StandTypography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (connectedAppName.isNotEmpty())
                                                "$connectedAppName 데이터 사용 중"
                                            else
                                                "Health Connect 데이터 사용 중",
                                            fontSize = StandTypography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🔋 기본 센서 비활성화됨 (배터리 절약)",
                                            fontSize = StandTypography.labelLarge,
                                            color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                                        )
                                    } else {
                                        Text(
                                            text = "정확한 걸음 측정",
                                            fontSize = StandTypography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isHealthConnectAvailable)
                                                "삼성 헬스, Google Fit 등과 연결"
                                            else
                                                "Health Connect 필요",
                                            fontSize = StandTypography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isHealthConnectConnected) "✓" else "🏃",
                                    fontSize = StandTypography.headlineLarge,
                                    color = if (isHealthConnectConnected) Color(0xFF4CAF50) else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    hapticManager.click()
                                    showFitnessAppConnectionScreen = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHealthConnectConnected)
                                        Color(0xFF4CAF50)
                                    else
                                        TealPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (isHealthConnectConnected) "연결 관리" else "연결 설정",
                                    fontSize = StandTypography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // 🧪 테스트 도구 (개발용 - 디버그 빌드에서만 표시)
                    if (BuildConfig.DEBUG) {
                        Text(
                            text = "테스트 도구",
                            fontSize = StandTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "⚠️ 개발 전용",
                                    fontSize = StandTypography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { repository.saveTodaySteps(currentSteps + 100) },
                                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+100", fontSize = StandTypography.bodyMedium)
                                    }

                                    Button(
                                        onClick = { repository.saveTodaySteps(currentSteps + 1000) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+1000", fontSize = StandTypography.bodyMedium)
                                    }

                                    Button(
                                        onClick = { repository.saveTodaySteps(goal) },
                                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("달성", fontSize = StandTypography.bodyMedium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { repository.saveTodaySteps(0) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("초기화 (0걸음)", fontSize = StandTypography.bodyMedium)
                                }
                            }
                        }
                    }

                    // 앱 정보
                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔄 앱 초기화 (개발용 - 디버그 빌드에서만 표시)
                    if (BuildConfig.DEBUG) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE53935).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ 개발자 도구",
                                    fontSize = StandTypography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        // 모든 데이터 초기화
                                        repository.setPaidDeposit(false)
                                        repository.saveDeposit(0)
                                        repository.saveGoal(8000)
                                        repository.saveControlDates("", "")
                                        repository.saveControlDays(emptySet())
                                        repository.saveSuccessDays(0)
                                        repository.saveTodaySteps(0)
                                        preferenceManager?.saveLastResetDate("")

                                        // 앱 재시작 안내
                                        android.widget.Toast.makeText(
                                            context,
                                            "앱을 재시작하세요",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🔄 앱 초기화 (처음부터)")
                                }
                            }
                        }
                    }

                    Text(
                        text = "Stand v1.0",
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            if (showGoalDialog) {
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
            }

            // 💳 크레딧 시스템 설명 다이얼로그
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
                            text = "크레딧 시스템 안내",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "💳 Stand 크레딧",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Google Play에서 매월 4,900원이 결제됩니다.\n달성률에 따라 크레딧을 지급받아 실질 부담 금액이 달라집니다.",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "🏆 95% 이상 달성",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Success
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• +4,900 크레딧 지급\n• 실질 부담: 무료\n• 완전한 성공!",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "✅ 80~95% 미만 달성",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Warning
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• +2,400 크레딧 지급\n• 실질 부담: 2,500원\n• 부분 성공!",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "❌ 80% 미만",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• 크레딧 없음\n• 실질 부담: 4,900원 (정가)\n• 다음 달 더 노력하세요!",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "🎁 친구 초대 혜택",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• 친구 초대 시 4,900 크레딧 지급\n• 초대받은 친구도 첫 달 무료\n• 내 초대 코드 공유하기",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = StandColors.PrimaryLight
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "💡 크레딧 예시",
                                        fontSize = StandTypography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StandColors.Primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "1월: 96% 달성 🏆\n" +
                                                "→ +4,900 크레딧 (실질 무료)\n\n" +
                                                "2월: 85% 달성 ✅\n" +
                                                "→ +2,400 크레딧 (실질 2,500원)\n\n" +
                                                "3월: 75% 달성 ❌\n" +
                                                "→ 크레딧 없음 (정가 4,900원)\n\n" +
                                                "누적 크레딧: 7,200",
                                        fontSize = StandTypography.bodySmall,
                                        lineHeight = 18.sp,
                                        color = Color.Gray
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

            // 🚫 구독 취소 다이얼로그
            if (showCancelSubscriptionDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelSubscriptionDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = StandColors.Error,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "구독을 취소하시겠습니까?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "⚠️ 주의사항",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.Error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "• 앱 제어 기능이 비활성화됩니다\n• 크레딧 적립이 중단됩니다\n• 현재 진행 중인 데이터가 초기화됩니다\n• Google Play에서 직접 구독을 취소해야 합니다",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = StandColors.WarningLight
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "💡 안내",
                                        fontSize = StandTypography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StandColors.Warning
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "확인을 누르면 Google Play 구독 관리 화면으로 이동합니다. 거기서 구독을 직접 취소하신 후, 앱으로 돌아오시면 데이터가 초기화됩니다.",
                                        fontSize = StandTypography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCancelSubscriptionDialog = false
                                // Google Play 구독 관리 화면 열기
                                try {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        val billingManager = BillingManager(
                                            context = context,
                                            onPurchaseSuccess = {},
                                            onPurchaseFailure = {}
                                        )
                                        billingManager.openSubscriptionManagement(activity)

                                        // 로컬 데이터 초기화
                                        repository.setPaidDeposit(false)
                                        repository.saveDeposit(0)
                                        repository.saveControlDates("", "")  // 제어 시작/종료 날짜 초기화
                                        repository.saveControlDays(emptySet())  // 제어 요일 초기화
                                        repository.saveSuccessDays(0)

                                        // UI 업데이트를 위해 즉시 반영
                                        deposit = 0
                                        successDays = 0
                                        totalDays = 0
                                        requiredDays = 0
                                    }
                                } catch (e: Exception) {
                                    // 에러 처리
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StandColors.Error
                            )
                        ) {
                            Text("구독 취소하기")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCancelSubscriptionDialog = false }
                        ) {
                            Text("닫기")
                        }
                    }
                )
            }

            // ⏰ 차단 시간대 선택 다이얼로그
            if (showBlockingPeriodsDialog) {
                BlockingPeriodsDialog(
                    currentPeriods = preferenceManager?.getBlockingPeriods() ?: emptySet(),
                    onDismiss = { showBlockingPeriodsDialog = false },
                    onConfirm = { newPeriods ->
                        preferenceManager?.saveBlockingPeriods(newPeriods)
                        preferenceManager?.saveBlockingPeriodsChangeTime()
                        showBlockingPeriodsDialog = false
                    }
                )
            }

            // 📅 제어 요일 선택 다이얼로그
            if (showControlDaysDialog) {
                ControlDaysDialog(
                    currentDays = preferenceManager?.getControlDays() ?: emptySet(),
                    onDismiss = { showControlDaysDialog = false },
                    onConfirm = { newDays ->
                        preferenceManager?.saveControlDays(newDays)
                        preferenceManager?.saveControlDaysChangeTime()
                        showControlDaysDialog = false
                    }
                )
            }

            // ⚠️ 3일 제한 확인 팝업
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

                AlertDialog(
                    onDismissRequest = { showChangeConfirmDialog = null },
                    title = {
                        Text(
                            text = "⚠️ $title",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "설정을 변경하면 3일 동안 다시 변경할 수 없습니다.",
                                fontSize = StandTypography.bodyMedium,
                                lineHeight = 20.sp
                            )
                            if (!canChange) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "⚠️ 현재 변경 불가 (가능일: $nextDate)",
                                    fontSize = StandTypography.bodySmall,
                                    color = Color(0xFFFF5722),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "정말 변경하시겠습니까?",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showChangeConfirmDialog = null
                                if (canChange) {
                                    when (type) {
                                        "goal" -> showGoalDialog = true
                                        "controlDays" -> showControlDaysDialog = true
                                        "blockingPeriods" -> showBlockingPeriodsDialog = true
                                    }
                                }
                            },
                            enabled = canChange,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canChange) Color(0xFFFF9800) else Color.Gray
                            )
                        ) {
                            Text(if (canChange) "변경하기" else "변경 불가")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showChangeConfirmDialog = null }
                        ) {
                            Text("취소")
                        }
                    }
                )
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
    var selectedPeriods by remember { mutableStateOf(currentPeriods) }

    val periods = listOf(
        "morning" to "아침 (06-12시)",
        "afternoon" to "점심 (12-18시)",
        "evening" to "저녁 (18-22시)",
        "night" to "밤 (22-06시)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "차단 시간대 선택",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "목표 미달성 시 차단할 시간대를 선택하세요",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                periods.forEach { (periodId, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPeriods.contains(periodId))
                                StandColors.PrimaryMedium
                            else
                                Color.White
                        ),
                        border = if (selectedPeriods.contains(periodId))
                            androidx.compose.foundation.BorderStroke(2.dp, StandColors.Primary)
                        else
                            null,
                        onClick = {
                            selectedPeriods = if (selectedPeriods.contains(periodId)) {
                                selectedPeriods - periodId
                            } else {
                                selectedPeriods + periodId
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = if (selectedPeriods.contains(periodId))
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                color = if (selectedPeriods.contains(periodId))
                                    StandColors.Primary
                                else
                                    Color.Black
                            )
                            if (selectedPeriods.contains(periodId)) {
                                Text(
                                    text = "✓",
                                    fontSize = StandTypography.titleMedium,
                                    color = StandColors.Primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.PrimaryLight
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Tip",
                            fontSize = StandTypography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StandColors.Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 시간대를 선택하지 않으면 차단되지 않습니다\n• 여러 시간대를 동시에 선택할 수 있습니다\n• 모두 선택하면 24시간 차단됩니다",
                            fontSize = StandTypography.bodySmall,
                            lineHeight = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPeriods) }
            ) {
                Text("적용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun ControlDaysDialog(
    currentDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }

    val days = listOf(
        0 to "일요일",
        1 to "월요일",
        2 to "화요일",
        3 to "수요일",
        4 to "목요일",
        5 to "금요일",
        6 to "토요일"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "제어 요일 선택",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "앱이 차단될 요일을 선택하세요",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                days.forEach { (dayId, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDays.contains(dayId))
                                StandColors.PrimaryMedium
                            else
                                Color.White
                        ),
                        border = if (selectedDays.contains(dayId))
                            androidx.compose.foundation.BorderStroke(2.dp, StandColors.Primary)
                        else
                            null,
                        onClick = {
                            selectedDays = if (selectedDays.contains(dayId)) {
                                selectedDays - dayId
                            } else {
                                selectedDays + dayId
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = if (selectedDays.contains(dayId))
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                color = if (selectedDays.contains(dayId))
                                    StandColors.Primary
                                else
                                    Color.Black
                            )
                            if (selectedDays.contains(dayId)) {
                                Text(
                                    text = "✓",
                                    fontSize = StandTypography.titleMedium,
                                    color = StandColors.Primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 빠른 선택 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { selectedDays = setOf(1, 2, 3, 4, 5) }, // 평일
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("평일", fontSize = StandTypography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { selectedDays = setOf(0, 6) }, // 주말
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("주말", fontSize = StandTypography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { selectedDays = setOf(0, 1, 2, 3, 4, 5, 6) }, // 매일
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("매일", fontSize = StandTypography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDays) }
            ) {
                Text("적용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
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
                color = Color(0xFF00BFA5),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
