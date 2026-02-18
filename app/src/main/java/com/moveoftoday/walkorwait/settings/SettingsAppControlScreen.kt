package com.moveoftoday.walkorwait.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.delay
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsAppControlStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun rebonDisabled(): String = when (getLang()) {
        "ko" -> "rebon 비활성화됨"
        "ja" -> "rebon無効化中"
        "zh" -> "rebon已禁用"
        "es" -> "rebon desactivado"
        else -> "rebon disabled"
    }

    fun tapToEnable(): String = when (getLang()) {
        "ko" -> "탭하여 설정에서 활성화하세요"
        "ja" -> "タップして設定で有効化してください"
        "zh" -> "点击在设置中启用"
        "es" -> "Toca para activar en configuración"
        else -> "Tap to enable in settings"
    }

    fun rebonEnabled(): String = when (getLang()) {
        "ko" -> "rebon 활성화됨"
        "ja" -> "rebon有効化中"
        "zh" -> "rebon已启用"
        "es" -> "rebon activado"
        else -> "rebon enabled"
    }

    fun appLockWorking(): String = when (getLang()) {
        "ko" -> "앱 잠금이 정상 작동합니다"
        "ja" -> "アプリロックが正常に動作しています"
        "zh" -> "应用锁定正常工作"
        "es" -> "El bloqueo de apps funciona correctamente"
        else -> "App lock is working normally"
    }

    fun lockedApps(): String = when (getLang()) {
        "ko" -> "잠금 앱"
        "ja" -> "ロックアプリ"
        "zh" -> "锁定应用"
        "es" -> "Apps bloqueadas"
        else -> "Locked Apps"
    }

    fun manageLockedApps(): String = when (getLang()) {
        "ko" -> "잠금 앱 관리"
        "ja" -> "ロックアプリ管理"
        "zh" -> "管理锁定应用"
        "es" -> "Gestionar apps bloqueadas"
        else -> "Manage Locked Apps"
    }

    fun noLockedApps(): String = when (getLang()) {
        "ko" -> "잠금 앱 없음"
        "ja" -> "ロック中のアプリなし"
        "zh" -> "无锁定应用"
        "es" -> "Sin apps bloqueadas"
        else -> "No locked apps"
    }

    fun appsLocked(count: Int): String = when (getLang()) {
        "ko" -> "${count}개 앱 잠금 중"
        "ja" -> "${count}個のアプリをロック中"
        "zh" -> "已锁定${count}个应用"
        "es" -> "$count apps bloqueadas"
        else -> "$count apps locked"
    }

    fun fitnessConnection(): String = when (getLang()) {
        "ko" -> "피트니스 연결"
        "ja" -> "フィットネス連携"
        "zh" -> "健身连接"
        "es" -> "Conexión fitness"
        else -> "Fitness Connection"
    }

    fun healthConnectConnection(): String = when (getLang()) {
        "ko" -> "Health Connect 연결"
        "ja" -> "Health Connect連携"
        "zh" -> "Health Connect连接"
        "es" -> "Conexión Health Connect"
        else -> "Health Connect Connection"
    }

    fun stepDataSync(): String = when (getLang()) {
        "ko" -> "걸음수 데이터 연동"
        "ja" -> "歩数データ連携"
        "zh" -> "步数数据同步"
        "es" -> "Sincronización de pasos"
        else -> "Step data sync"
    }

    fun notificationSettings(): String = when (getLang()) {
        "ko" -> "알림 설정"
        "ja" -> "通知設定"
        "zh" -> "通知设置"
        "es" -> "Configuración de notificaciones"
        else -> "Notification Settings"
    }

    fun goalAchievementNotification(): String = when (getLang()) {
        "ko" -> "목표 달성 알림"
        "ja" -> "目標達成通知"
        "zh" -> "目标达成通知"
        "es" -> "Notificación de meta lograda"
        else -> "Goal Achievement Notification"
    }

    fun dailyGoal100Percent(): String = when (getLang()) {
        "ko" -> "일일 목표 100% 달성 시"
        "ja" -> "1日の目標100%達成時"
        "zh" -> "每日目标100%达成时"
        "es" -> "Al lograr 100% de la meta diaria"
        else -> "When daily goal 100% achieved"
    }

    fun petWorryNotification(): String = when (getLang()) {
        "ko" -> "펫 걱정 알림"
        "ja" -> "ペット心配通知"
        "zh" -> "宠物担心通知"
        "es" -> "Notificación de mascota preocupada"
        else -> "Pet Worry Notification"
    }

    fun usualExerciseTimeNoMovement(): String = when (getLang()) {
        "ko" -> "평소 운동 시간에 움직임 없을 때"
        "ja" -> "いつもの運動時間に動きがないとき"
        "zh" -> "平时运动时间无运动时"
        "es" -> "Sin movimiento en hora habitual de ejercicio"
        else -> "No movement during usual exercise time"
    }

    fun appBlockNotification(): String = when (getLang()) {
        "ko" -> "앱 차단 알림"
        "ja" -> "アプリブロック通知"
        "zh" -> "应用阻止通知"
        "es" -> "Notificación de bloqueo de app"
        else -> "App Block Notification"
    }

    fun lockedAppAttempt(): String = when (getLang()) {
        "ko" -> "잠긴 앱 실행 시도 시"
        "ja" -> "ロックされたアプリ起動試行時"
        "zh" -> "尝试启动锁定应用时"
        "es" -> "Al intentar abrir app bloqueada"
        else -> "When attempting to open locked app"
    }
}

/**
 * 앱 제어 화면 (잠금 앱, 피트니스 연결, 접근성)
 */
@Composable
fun SettingsAppControlScreen(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()

    // 상태
    var lockedApps by remember { mutableStateOf(preferenceManager?.getLockedApps() ?: emptySet<String>()) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    // 알림 설정 상태
    var goalNotificationEnabled by remember { mutableStateOf(preferenceManager?.isGoalNotificationEnabled() ?: true) }
    var worryNotificationEnabled by remember { mutableStateOf(preferenceManager?.isWorryNotificationEnabled() ?: true) }
    var blockNotificationEnabled by remember { mutableStateOf(preferenceManager?.isBlockNotificationEnabled() ?: true) }

    // 다이얼로그/화면 상태
    var showAppLockScreen by remember { mutableStateOf(false) }
    var showFitnessAppConnectionScreen by remember { mutableStateOf(false) }

    // 주기적으로 접근성 서비스 체크
    LaunchedEffect(Unit) {
        while (true) {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            isAccessibilityEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true
            delay(1000)
        }
    }

    // 하위 화면 표시
    if (showAppLockScreen) {
        AppLockScreen(
            preferenceManager = preferenceManager,
            onBack = { showAppLockScreen = false },
            onLockedAppsChanged = { newApps -> lockedApps = newApps }
        )
        return
    }

    if (showFitnessAppConnectionScreen) {
        FitnessAppConnectionScreen(
            onBack = { showFitnessAppConnectionScreen = false },
            onConnectionComplete = { showFitnessAppConnectionScreen = false }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "app control",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            // 스크롤 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // ========== 접근성 서비스 상태 ==========
                if (!isAccessibilityEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(3.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                            .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MockupColors.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    SettingsAppControlStrings.rebonDisabled(),
                                    color = MockupColors.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = kenneyFont
                                )
                                Text(
                                    SettingsAppControlStrings.tapToEnable(),
                                    color = MockupColors.TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MockupColors.TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    SettingsAppControlStrings.rebonEnabled(),
                                    color = MockupColors.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = kenneyFont
                                )
                                Text(
                                    SettingsAppControlStrings.appLockWorking(),
                                    color = MockupColors.TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // ========== 잠금 앱 ==========
                RetroSectionTitle(SettingsAppControlStrings.lockedApps(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager.click()
                            showAppLockScreen = true
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
                                text = SettingsAppControlStrings.manageLockedApps(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (lockedApps.isEmpty()) SettingsAppControlStrings.noLockedApps() else SettingsAppControlStrings.appsLocked(lockedApps.size),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        Text(
                            text = ">",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Border,
                            fontFamily = kenneyFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 피트니스 앱 연결 ==========
                RetroSectionTitle(SettingsAppControlStrings.fitnessConnection(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager.click()
                            showFitnessAppConnectionScreen = true
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
                                text = SettingsAppControlStrings.healthConnectConnection(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = SettingsAppControlStrings.stepDataSync(),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        Text(
                            text = ">",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Border,
                            fontFamily = kenneyFont
                        )
                    }
                }

                // ========== 알림 설정 ==========
                RetroSectionTitle(SettingsAppControlStrings.notificationSettings(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // 목표 달성 알림
                        NotificationToggleRow(
                            title = SettingsAppControlStrings.goalAchievementNotification(),
                            description = SettingsAppControlStrings.dailyGoal100Percent(),
                            enabled = goalNotificationEnabled,
                            kenneyFont = kenneyFont,
                            onToggle = {
                                goalNotificationEnabled = !goalNotificationEnabled
                                preferenceManager?.setGoalNotificationEnabled(goalNotificationEnabled)
                                hapticManager.click()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MockupColors.Border)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 펫 걱정 알림
                        NotificationToggleRow(
                            title = SettingsAppControlStrings.petWorryNotification(),
                            description = SettingsAppControlStrings.usualExerciseTimeNoMovement(),
                            enabled = worryNotificationEnabled,
                            kenneyFont = kenneyFont,
                            onToggle = {
                                worryNotificationEnabled = !worryNotificationEnabled
                                preferenceManager?.setWorryNotificationEnabled(worryNotificationEnabled)
                                hapticManager.click()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MockupColors.Border)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 앱 차단 알림
                        NotificationToggleRow(
                            title = SettingsAppControlStrings.appBlockNotification(),
                            description = SettingsAppControlStrings.lockedAppAttempt(),
                            enabled = blockNotificationEnabled,
                            kenneyFont = kenneyFont,
                            onToggle = {
                                blockNotificationEnabled = !blockNotificationEnabled
                                preferenceManager?.setBlockNotificationEnabled(blockNotificationEnabled)
                                hapticManager.click()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 알림 토글 행
 */
@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    enabled: Boolean,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MockupColors.TextSecondary
            )
        }
        // 토글 스위치 (레트로 스타일 - 흑백)
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp)
                .border(
                    2.dp,
                    if (enabled) MockupColors.TextPrimary else MockupColors.Border,
                    RoundedCornerShape(14.dp)
                )
                .background(
                    if (enabled) MockupColors.Border.copy(alpha = 0.2f) else MockupColors.CardBackground,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onToggle() },
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(22.dp)
                    .background(
                        if (enabled) MockupColors.TextPrimary else MockupColors.Border,
                        RoundedCornerShape(11.dp)
                    )
            )
        }
    }
}
