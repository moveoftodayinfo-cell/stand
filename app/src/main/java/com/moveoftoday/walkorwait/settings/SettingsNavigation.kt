package com.moveoftoday.walkorwait.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*

/**
 * 설정 화면 네비게이션 상태
 */
enum class SettingsDestination {
    MAIN,
    PROFILE,
    PET,
    GOAL,
    APP_CONTROL,
    SUPPORT
}

/**
 * 설정 메인 화면 (2단계 네비게이션)
 */
@Composable
fun SettingsMainScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository
    val hapticManager = remember { HapticManager(context) }

    // 현재 네비게이션 상태
    var currentDestination by remember { mutableStateOf(SettingsDestination.MAIN) }

    // 뒤로가기 처리
    val handleBack: () -> Unit = {
        if (currentDestination == SettingsDestination.MAIN) {
            onBack()
        } else {
            currentDestination = SettingsDestination.MAIN
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 메인 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.MAIN,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            SettingsMainContent(
                preferenceManager = preferenceManager,
                repository = repository,
                hapticManager = hapticManager,
                onBack = onBack,
                onNavigate = { destination -> currentDestination = destination }
            )
        }

        // 내 정보 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.PROFILE,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            SettingsProfileScreen(
                preferenceManager = preferenceManager,
                repository = repository,
                hapticManager = hapticManager,
                onBack = handleBack
            )
        }

        // 펫 관리 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.PET,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            SettingsPetScreen(
                preferenceManager = preferenceManager,
                repository = repository,
                hapticManager = hapticManager,
                onBack = handleBack
            )
        }

        // 목표 설정 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.GOAL,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            SettingsGoalScreen(
                preferenceManager = preferenceManager,
                repository = repository,
                hapticManager = hapticManager,
                onBack = handleBack
            )
        }

        // 앱 제어 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.APP_CONTROL,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            SettingsAppControlScreen(
                preferenceManager = preferenceManager,
                repository = repository,
                hapticManager = hapticManager,
                onBack = handleBack
            )
        }

        // 고객 지원 화면
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.SUPPORT,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            SettingsSupportScreen(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                onBack = handleBack
            )
        }
    }
}

/**
 * 메인 설정 화면 내용 (카테고리 목록)
 */
@Composable
private fun SettingsMainContent(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val auth = remember { FirebaseAuth.getInstance() }

    // 사용자 정보
    val isGoogleSignedIn = auth.currentUser != null && auth.currentUser?.isAnonymous != true
    val googleEmail = auth.currentUser?.email ?: ""

    // 펫 정보
    val petTypeV2 = preferenceManager?.getPetTypeV2()
    val petName = preferenceManager?.getPetNameV2() ?: "친구"
    val petLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()

    // 목표 정보
    val goal = repository.getGoal()
    val goalUnit = preferenceManager?.getGoalUnit() ?: "steps"
    val controlDays = preferenceManager?.getControlDays() ?: emptySet()

    // 앱 제어 정보
    val lockedApps = preferenceManager?.getLockedApps() ?: emptySet()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더
            SettingsHeader(
                title = "setting",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            // 스크롤 가능한 메뉴 목록
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 내 정보
                SettingsMenuCard(
                    icon = "○",
                    title = "내 정보",
                    subtitle = if (isGoogleSignedIn) googleEmail else "로그인하여 데이터 백업",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.PROFILE)
                    }
                )

                // 2. 펫 관리
                SettingsMenuCard(
                    icon = "◆",
                    title = "펫 관리",
                    subtitle = "$petName Lv.${petLevel.level}",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.PET)
                    }
                )

                // 3. 목표 설정
                val goalText = when (goalUnit) {
                    "km" -> "${goal / 1000.0}km"
                    "minutes" -> "${goal}분"
                    else -> "${goal}보"
                }
                val daysText = if (controlDays.isEmpty()) "매일" else {
                    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
                    controlDays.sorted().map { dayNames[it % 7] }.joinToString("")
                }
                SettingsMenuCard(
                    icon = "◎",
                    title = "목표 설정",
                    subtitle = "$goalText / $daysText",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.GOAL)
                    }
                )

                // 4. 앱 제어
                SettingsMenuCard(
                    icon = "▣",
                    title = "앱 제어",
                    subtitle = if (lockedApps.isEmpty()) "잠금 앱 없음" else "${lockedApps.size}개 앱 잠금 중",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.APP_CONTROL)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 푸터 영역
                SettingsFooter(
                    kenneyFont = kenneyFont,
                    hapticManager = hapticManager,
                    onNavigateSupport = { onNavigate(SettingsDestination.SUPPORT) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * 설정 헤더 (공통)
 */
@Composable
fun SettingsHeader(
    title: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 30.dp, bottom = 16.dp)
        ) {
            // 뒤로가기 버튼
            Box(
                modifier = Modifier
                    .clickable { onBack() }
                    .align(Alignment.CenterStart)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            // 중앙 타이틀
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.Border,
                fontFamily = kenneyFont,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 하단 구분선
        HorizontalDivider(
            color = MockupColors.Border,
            thickness = 3.dp
        )
    }
}

/**
 * 설정 메뉴 카드
 */
@Composable
private fun SettingsMenuCard(
    icon: String,
    title: String,
    subtitle: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘
                Text(
                    text = icon,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }
            // 화살표
            Text(
                text = ">",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.Border,
                fontFamily = kenneyFont
            )
        }
    }
}

/**
 * 설정 푸터 (버전, 링크, 브랜딩)
 */
@Composable
private fun SettingsFooter(
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    hapticManager: HapticManager,
    onNavigateSupport: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 버전 + 링크
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                fontSize = 13.sp,
                color = MockupColors.TextMuted,
                fontFamily = kenneyFont
            )
            Text(
                text = "·",
                fontSize = 13.sp,
                color = MockupColors.TextMuted
            )
            Text(
                text = "피드백",
                fontSize = 13.sp,
                color = MockupColors.TextSecondary,
                modifier = Modifier.clickable {
                    hapticManager.click()
                    onNavigateSupport()
                }
            )
            Text(
                text = "·",
                fontSize = 13.sp,
                color = MockupColors.TextMuted
            )
            Text(
                text = "약관",
                fontSize = 13.sp,
                color = MockupColors.TextSecondary,
                modifier = Modifier.clickable {
                    hapticManager.click()
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://stand-64c11.web.app/terms.html")
                    )
                    context.startActivity(intent)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 브랜딩
        Text(
            text = "rebon",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.Border,
            fontFamily = kenneyFont
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "© 2025 moveoftoday",
            fontSize = 11.sp,
            color = MockupColors.TextMuted
        )
    }
}
