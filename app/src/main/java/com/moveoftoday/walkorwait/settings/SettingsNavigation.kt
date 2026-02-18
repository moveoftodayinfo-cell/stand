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
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsNavigationStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun petManagement(): String = when (getLang()) {
        "ko" -> "펫 관리"
        "ja" -> "ペット管理"
        "zh" -> "宠物管理"
        "es" -> "Gestión de mascota"
        else -> "Pet Management"
    }

    fun goalSetting(): String = when (getLang()) {
        "ko" -> "목표 설정"
        "ja" -> "目標設定"
        "zh" -> "目标设置"
        "es" -> "Configuración de meta"
        else -> "Goal Setting"
    }

    fun appControl(): String = when (getLang()) {
        "ko" -> "앱 제어"
        "ja" -> "アプリ制御"
        "zh" -> "应用控制"
        "es" -> "Control de apps"
        else -> "App Control"
    }

    fun myProfile(): String = when (getLang()) {
        "ko" -> "내 정보"
        "ja" -> "マイページ"
        "zh" -> "我的信息"
        "es" -> "Mi perfil"
        else -> "My Profile"
    }

    fun friend(): String = when (getLang()) {
        "ko" -> "친구"
        "ja" -> "フレンド"
        "zh" -> "朋友"
        "es" -> "Amigo"
        else -> "Friend"
    }

    fun everyday(): String = when (getLang()) {
        "ko" -> "매일"
        "ja" -> "毎日"
        "zh" -> "每天"
        "es" -> "Todos los días"
        else -> "Everyday"
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

    fun loginToBackup(): String = when (getLang()) {
        "ko" -> "로그인하여 데이터 백업"
        "ja" -> "ログインしてデータをバックアップ"
        "zh" -> "登录备份数据"
        "es" -> "Inicia sesión para backup"
        else -> "Login to backup data"
    }

    fun feedback(): String = when (getLang()) {
        "ko" -> "피드백"
        "ja" -> "フィードバック"
        "zh" -> "反馈"
        "es" -> "Feedback"
        else -> "Feedback"
    }

    fun terms(): String = when (getLang()) {
        "ko" -> "약관"
        "ja" -> "利用規約"
        "zh" -> "条款"
        "es" -> "Términos"
        else -> "Terms"
    }

    fun stepsUnit(): String = when (getLang()) {
        "ko" -> "보"
        "ja" -> "歩"
        "zh" -> "步"
        "es" -> " pasos"
        else -> " steps"
    }

    fun minutesUnit(): String = when (getLang()) {
        "ko" -> "분"
        "ja" -> "分"
        "zh" -> "分钟"
        "es" -> " min"
        else -> " min"
    }

    fun getDayNames(): List<String> = when (getLang()) {
        "ko" -> listOf("일", "월", "화", "수", "목", "금", "토")
        "ja" -> listOf("日", "月", "火", "水", "木", "金", "土")
        "zh" -> listOf("日", "一", "二", "三", "四", "五", "六")
        "es" -> listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
        else -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }
}

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
    val petName = preferenceManager?.getPetNameV2() ?: SettingsNavigationStrings.friend()
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

            // 메뉴 목록
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 상단: 현재 펫 표시
                SettingsPetPreview(
                    petTypeV2 = petTypeV2,
                    petName = petName,
                    petLevel = petLevel,
                    preferenceManager = preferenceManager,
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.PET)
                    }
                )

                // 1. 펫 관리
                SettingsMenuCard(
                    iconName = "icon_beetle",  // PNG 아이콘
                    title = SettingsNavigationStrings.petManagement(),
                    subtitle = "$petName Lv.${petLevel.level}",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.PET)
                    }
                )

                // 2. 목표 설정
                val goalText = when (goalUnit) {
                    "km" -> "${goal / 1000.0}km"
                    "minutes" -> "${goal}${SettingsNavigationStrings.minutesUnit()}"
                    else -> "${goal}${SettingsNavigationStrings.stepsUnit()}"
                }
                val daysText = if (controlDays.isEmpty()) SettingsNavigationStrings.everyday() else {
                    val dayNames = SettingsNavigationStrings.getDayNames()
                    controlDays.sorted().map { dayNames[it % 7] }.joinToString("")
                }
                SettingsMenuCard(
                    iconName = "icon_flag",  // PNG 아이콘
                    title = SettingsNavigationStrings.goalSetting(),
                    subtitle = "$goalText / $daysText",
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.GOAL)
                    }
                )

                // 3. 앱 제어
                SettingsMenuCard(
                    iconName = "icon_shield",  // PNG 아이콘
                    title = SettingsNavigationStrings.appControl(),
                    subtitle = if (lockedApps.isEmpty()) SettingsNavigationStrings.noLockedApps() else SettingsNavigationStrings.appsLocked(lockedApps.size),
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.APP_CONTROL)
                    }
                )

                // 4. 내 정보
                SettingsMenuCard(
                    iconName = "icon_character",  // PNG 아이콘
                    title = SettingsNavigationStrings.myProfile(),
                    subtitle = if (isGoogleSignedIn) googleEmail else SettingsNavigationStrings.loginToBackup(),
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        onNavigate(SettingsDestination.PROFILE)
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // 푸터 영역
                SettingsFooter(
                    kenneyFont = kenneyFont,
                    hapticManager = hapticManager,
                    onNavigateSupport = { onNavigate(SettingsDestination.SUPPORT) }
                )

                Spacer(modifier = Modifier.height(8.dp))
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
                    color = Color.Black
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
    iconName: String,  // PNG 아이콘 이름 (icon_bell 등)
    title: String,
    subtitle: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘 (IconGodotNode PNG)
                DrawableIcon(
                    iconName = iconName,
                    size = 24.dp,
                    tint = MockupColors.TextMuted
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }
            // 화살표
            Text(
                text = ">",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.Border,
                fontFamily = kenneyFont
            )
        }
    }
}

/**
 * 설정 상단 펫 프리뷰
 */
@Composable
private fun SettingsPetPreview(
    petTypeV2: PetTypeV2?,
    petName: String,
    petLevel: PetLevel,
    preferenceManager: PreferenceManager?,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit
) {
    val stage = petLevel.stage

    // 장비 상태 가져오기
    val equipmentState = remember(preferenceManager) {
        preferenceManager?.getEquipmentState() ?: EquipmentState()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 펫 애니메이션 (2배 크기)
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (petTypeV2 != null) {
                PetSpriteV2WithEquipment(
                    petType = petTypeV2,
                    stage = stage,
                    animationType = PetAnimationTypeV2.IDLE,
                    equipmentState = equipmentState,
                    size = 160.dp,
                    monochrome = true,
                    showGlow = true,
                    applyDisplayScale = true
                )
            } else {
                // 펫 없을 때 egg 표시
                PetSpriteV2(
                    petType = PetTypeV2.CAT,
                    stage = PetGrowthStage.EGG,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = 160.dp,
                    monochrome = true
                )
            }
        }

        // 펫 이름
        Text(
            text = petName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            fontFamily = kenneyFont
        )

        // 레벨 + 성장 단계
        val stageName = stage.displayName
        Text(
            text = "Lv.${petLevel.level} · $stageName",
            fontSize = 13.sp,
            color = MockupColors.TextSecondary
        )
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
                text = SettingsNavigationStrings.feedback(),
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
                text = SettingsNavigationStrings.terms(),
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
