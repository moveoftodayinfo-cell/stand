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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(3.dp, MockupColors.Green, RoundedCornerShape(12.dp))
                            .background(MockupColors.GreenLight, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MockupColors.Green)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "rebon 활성화됨",
                                    color = MockupColors.Green,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = kenneyFont
                                )
                                Text(
                                    "앱 잠금이 정상 작동합니다",
                                    color = MockupColors.TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // ========== 잠금 앱 ==========
                RetroSectionTitle("잠금 앱", kenneyFont)

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
                                text = "잠금 앱 관리",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (lockedApps.isEmpty()) "잠금 앱 없음" else "${lockedApps.size}개 앱 잠금 중",
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
                RetroSectionTitle("피트니스 연결", kenneyFont)

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
                                text = "Health Connect 연결",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = "걸음수 데이터 연동",
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
