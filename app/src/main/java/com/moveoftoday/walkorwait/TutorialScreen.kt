package com.moveoftoday.walkorwait

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.MockupColors

@Composable
fun TutorialScreen(
    preferenceManager: PreferenceManager?,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    var currentStep by remember { mutableIntStateOf(0) }

    val totalSteps = 10 // 전체 단계 수

    Box(modifier = Modifier.fillMaxSize()) {
        // 각 스텝 렌더링
        when (currentStep) {
            0 -> WelcomeStep(
                hapticManager = hapticManager,
                onNext = {
                    hapticManager.lightOn()
                    currentStep = 1
                }
            )
            1 -> PermissionStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 2 }
            )
            2 -> FitnessAppConnectionTutorialStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 3 }
            )
            3 -> AccessibilityStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 4 }
            )
            4 -> AppSelectionStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 5 }
            )
            5 -> TestBlockingStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 6 }
            )
            6 -> GoalInputStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 7 }
            )
            7 -> WalkingStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 8 }
            )
            8 -> UnlockedStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 9 }
            )
            9 -> EmergencyButtonStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = {
                    hapticManager.goalAchieved()
                    preferenceManager?.setTutorialCompleted(true)
                    currentStep = 10
                }
            )
            10 -> {
                onComplete()
            }
        }
    }
}

/**
 * 튜토리얼 프로그레스바 컴포넌트
 */
@Composable
fun TutorialProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentStep.toFloat() / totalSteps).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "설정 진행",
                fontSize = StandTypography.labelLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "$currentStep / $totalSteps",
                fontSize = StandTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = StandColors.AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 프로그레스바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                StandColors.WarmLightDim,
                                StandColors.WarmLight,
                                StandColors.WarmLightBright
                            )
                        )
                    )
            )
        }
    }
}

// 1. 환영 화면 - 프리미엄 피트니스 스타일
@Composable
fun WelcomeStep(hapticManager: HapticManager? = null, onNext: () -> Unit) {
    // 프리미엄 색상
    val TealPrimary = Color(0xFF00BFA5)
    val TealDark = Color(0xFF008E76)
    val NavyDark = Color(0xFF0D1B2A)
    val NavyMid = Color(0xFF1B263B)
    val BottomSheetBg = Color(0xFF0A0A0A)

    // 페이드인 애니메이션
    var isVisible by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "fadeAlpha"
    )
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
        hapticManager?.lightOn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BottomSheetBg)
    ) {
        // 상단 70% - Teal 그라데이션 배경
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(TealPrimary, TealDark, NavyMid, NavyDark),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .offset(y = (-slideOffset).dp)
                    .alpha(fadeAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 로고/아이콘
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text("🏃", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "rebon",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "일어서세요",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "멈춰 있지 마세요\n한 걸음씩, 당신의 삶을 바꿔보세요",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }
        }

        // 하단 바텀 시트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(BottomSheetBg)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 72.dp)
                .alpha(fadeAlpha)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "앉아있는 시간을 걷는 시간으로",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "목표를 달성하면 앱이 해제됩니다",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 시작 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "시작하기",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TealPrimary)
                            .clickable(enabled = isVisible) {
                                hapticManager?.heavyClick()
                                onNext()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. 권한 요청
@Composable
fun PermissionStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 1,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(true) }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로그레스바
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "권한 설정",
                    fontSize = StandTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "rebon이 제대로 작동하려면\n아래 권한이 필요해요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(48.dp))

        PermissionCard(
            title = "🚶 걸음 측정",
            description = "걸음 수를 측정합니다",
            isGranted = activityPermissionGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "🔔 알림",
                description = "진행 상황을 알려드려요",
                isGranted = notificationPermissionGranted,
                onRequest = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = activityPermissionGranted
        ) {
            Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
        }
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MockupColors.Blue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = StandTypography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            if (isGranted) {
                Text(
                    text = "✓",
                    fontSize = StandTypography.headlineSmall,
                    color = MockupColors.Blue
                )
            } else {
                Button(onClick = onRequest) {
                    Text("허용")
                }
            }
        }
    }
}

// 3. 피트니스 앱 연결 (튜토리얼)
@Composable
fun FitnessAppConnectionTutorialStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 2,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf<List<FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.createPermissionRequestContract()
    ) { granted ->
        scope.launch {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                isConnecting = false
                // Health Connect 연결 설정 저장
                val firstApp = installedApps.firstOrNull()
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(firstApp?.appName ?: "")
                // 서비스 재시작
                StepCounterService.stop(context)
                StepCounterService.start(context)
                // 자동으로 다음 단계로
                delay(500)
                onNext()
            } else {
                isConnecting = false
            }
        }
    }

    // 초기화
    LaunchedEffect(Unit) {
        // Health Connect 사용 가능 여부 체크
        isHealthConnectAvailable = healthConnectManager.isAvailable()

        // 설치된 피트니스 앱 목록은 항상 가져오기 (Health Connect 여부와 무관)
        installedApps = healthConnectManager.getInstalledFitnessApps()

        // Health Connect 사용 가능하면 권한 체크
        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            // 이미 권한이 있으면 자동으로 다음으로
            if (hasPermissions) {
                delay(1000)
                onNext()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏃",
                    fontSize = StandTypography.displayLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "피트니스 앱 연결",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "사용 중인 피트니스 앱과 연결하면\n정확한 걸음 측정이 가능해요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(48.dp))

        // 설치된 앱이 있으면 (Health Connect 여부와 상관없이)
        if (installedApps.isNotEmpty()) {
            Text(
                text = "발견된 피트니스 앱",
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            installedApps.forEach { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = app.icon, fontSize = StandTypography.headlineSmall)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = app.appName, fontSize = StandTypography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "설치됨 ✓", fontSize = StandTypography.labelMedium, color = MockupColors.Blue)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Connect 사용 가능 여부에 따라 버튼 변경
            if (isHealthConnectAvailable) {
                Button(
                    onClick = {
                        isConnecting = true
                        permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = installedApps.firstOrNull()?.color ?: MockupColors.Blue
                    )
                ) {
                    Text(
                        text = if (isConnecting) "연결 중..." else "${installedApps.firstOrNull()?.appName ?: "피트니스 앱"}과 연결하기",
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Health Connect 없으면 설치 안내
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MockupColors.TextMuted.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Health Connect 필요",
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${installedApps.firstOrNull()?.appName ?: "피트니스 앱"}과 연결하려면\nHealth Connect 앱이 필요합니다",
                            fontSize = StandTypography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { healthConnectManager.openHealthConnectPlayStore() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MockupColors.TextMuted
                            )
                        ) {
                            Text("Play Store에서 설치")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "* Android 9 이상 지원",
                            fontSize = StandTypography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        // 설치된 앱이 없으면
        else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 추천 피트니스 앱",
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.WarmLightBright
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "아래 앱 중 하나를 설치하면\n더 정확한 걸음 측정이 가능해요",
                        fontSize = StandTypography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 추천 앱 목록
                    listOf(
                        "삼성 헬스",
                        "Google Fit",
                        "Garmin Connect",
                        "Fitbit"
                    ).forEach { appName ->
                        Text(
                            text = "• $appName",
                            fontSize = StandTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "나중에 설정에서 연결할 수도 있어요",
                    fontSize = StandTypography.labelLarge,
                    color = StandColors.WarmLight
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 나중에 하기 버튼
        TextButton(
            onClick = {
                hapticManager?.click()
                onNext()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("나중에 하기 (기본 센서 사용)", color = Color.White.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "* 기본 센서는 덜 정확하며 부정 방지 기능이 제한됩니다",
            fontSize = StandTypography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
            }
        }
    }
}

// 4. 접근성 설정 (rebon ON - 필수!)
@Composable
fun AccessibilityStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 3,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(isChecking) {
        if (isChecking) {
            while (true) {
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )

                if (enabledServices?.contains("com.moveoftoday.walkorwait") == true) {
                    hapticManager?.success()
                    delay(1000)
                    onNext()
                    break
                }

                delay(1000)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "앱 제어 설정",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "이제 앱을 제어할 준비가 되었어요!\n접근성 권한을 켜주세요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MockupColors.TextMuted.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚙️ 설정 방법",
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. 아래 버튼을 눌러 설정 화면으로 이동\n2. 설정 화면에서 'rebon' 찾기\n3. rebon을 ON으로 전환\n4. 확인 버튼 누르기\n\n✅ ON 확인되면 자동으로 다음 단계로!",
                            fontSize = StandTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MockupColors.TextMuted
                    )
                ) {
                    Text("설정 화면으로", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "⚠️ rebon ON을 해야 다음 단계로 진행됩니다",
                    fontSize = StandTypography.labelLarge,
                    color = MockupColors.Red,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 4. 앱 선택
@Composable
fun AppSelectionStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 4,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    // 모든 설치된 앱 가져오기 (카테고리 필터링 제거)
    val appsByCategory = remember {
        val allApps = AppUtils.getInstalledAppsByCategory(context)

        // 디버그: 설치된 앱 개수 확인
        android.util.Log.d("TutorialScreen", "Total categories: ${allApps.size}")
        allApps.forEach { (category, apps) ->
            android.util.Log.d("TutorialScreen", "$category: ${apps.size} apps")
        }

        // 모든 앱 반환 (필터링 제거)
        allApps
    }

    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var expandedCategories by remember { mutableStateOf(setOf<AppCategory>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "제어할 앱 선택",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "목표 미달성 시\n사용이 제한될 앱을 선택하세요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(24.dp))

        // 앱이 없는 경우 안내 메시지
        val totalApps = appsByCategory.values.flatten().size
        if (totalApps == 0) {
            EmptyState(
                icon = "📱",
                title = "제어할 앱이 없습니다",
                description = "Play Store에서\nYouTube, Chrome, Instagram 등\n앱을 설치해주세요",
                modifier = Modifier.weight(1f)
            )
        } else {
            // 선택된 앱 개수 표시
            if (selectedApps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.PrimaryLight
                    )
                ) {
                    Text(
                        text = "✓ ${selectedApps.size}개 선택됨",
                        fontSize = StandTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.Primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 스크롤 가능한 앱 목록
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
            appsByCategory.forEach { (category, apps) ->
                // 카테고리 헤더
                item(key = "header_$category") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            expandedCategories = if (category in expandedCategories) {
                                expandedCategories - category
                            } else {
                                expandedCategories + category
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${getCategoryIcon(category)} ${category.displayName}",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${apps.size}개",
                                fontSize = StandTypography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 카테고리가 펼쳐져 있으면 앱 목록 표시
                if (category in expandedCategories) {
                    items(
                        items = apps,
                        key = { app -> app.packageName }
                    ) { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.icon?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = app.appName,
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                Checkbox(
                                    checked = selectedApps.contains(app.packageName),
                                    onCheckedChange = { checked ->
                                        selectedApps = if (checked) {
                                            selectedApps + app.packageName
                                        } else {
                                            selectedApps - app.packageName
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                hapticManager?.success()
                preferenceManager?.saveLockedApps(selectedApps)
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedApps.isNotEmpty() || totalApps == 0
        ) {
            Text(
                text = if (totalApps == 0) "건너뛰기" else "다음 (${selectedApps.size}개 선택)",
                fontSize = StandTypography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
            }
        }
    }
}

// 카테고리별 아이콘
private fun getCategoryIcon(category: AppCategory): String {
    return when (category) {
        AppCategory.GAME -> "🎮"
        AppCategory.VIDEO -> "🎬"
        AppCategory.SOCIAL -> "💬"
        AppCategory.MUSIC_AUDIO -> "🎵"
        AppCategory.ENTERTAINMENT -> "🎪"
        AppCategory.PRODUCTIVITY -> "💼"
        AppCategory.COMMUNICATION -> "📱"
        AppCategory.SHOPPING -> "🛒"
        AppCategory.OTHER -> "📦"
    }
}

// 5. 차단 체험 - 조명이 꺼지는 효과
@Composable
fun TestBlockingStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 5,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var hasLeftApp by remember { mutableStateOf(false) }
    var canProceed by remember { mutableStateOf(false) }

    // 깜빡이는 애니메이션 (차단 상태)
    val infiniteTransition = rememberInfiniteTransition(label = "blockBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    DisposableEffect(Unit) {
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    hasLeftApp = true
                }
                else -> {}
            }
        }

        lifecycleOwner?.lifecycle?.addObserver(observer)

        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // 백그라운드 갔다온 후 5초 대기
    LaunchedEffect(hasLeftApp) {
        if (hasLeftApp) {
            delay(5000) // 5초 대기
            hapticManager?.success()
            canProceed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 어두운 배경 + 깜빡이는 효과 (차단 상태)
        if (!canProceed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLightDim.copy(alpha = blinkAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 600f
                        )
                    )
            )
        } else {
            // 성공 시 밝아지는 효과
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLight.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 꺼진 전구 아이콘 (차단 상태)
                Box(contentAlignment = Alignment.Center) {
                    if (!canProceed) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(blinkAlpha * 0.5f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightDim.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    Text(
                        text = if (canProceed) "💡" else "🔒",
                        fontSize = StandTypography.displayLarge,
                        modifier = Modifier.alpha(if (canProceed) 1f else blinkAlpha + 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (canProceed) "체험 완료!" else "앱이 차단되었어요!",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canProceed) StandColors.WarmLightBright else StandColors.WarmLightDim
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (canProceed)
                        "앱 차단을 체험하셨네요!\n이제 다음 단계에서 해제해볼까요?"
                    else
                        "지금 선택한 앱을 실행해보세요.\n차단 메시지가 뜰 거예요!",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canProceed)
                            StandColors.WarmLight.copy(alpha = 0.15f)
                        else
                            StandColors.WarmLightDim.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        when {
                            canProceed -> {
                                Text(
                                    text = "💡 체험 완료!",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightBright
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "이제 걸어서 불을 켜볼까요?",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            hasLeftApp -> {
                                Text(
                                    text = "확인 중...",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightDim
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "차단을 제대로 확인하셨는지\n확인 중이에요.",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = "📱 앱을 실행해보세요",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightDim
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. 홈 버튼을 눌러 나가기\n2. 선택한 앱 실행\n3. 차단 메시지 확인\n4. rebon으로 돌아오기",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (canProceed) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StandColors.WarmLight
                        )
                    ) {
                        Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                    }
                } else if (hasLeftApp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = StandColors.WarmLightDim
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "곧 다음으로 진행할 수 있어요",
                        fontSize = StandTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Button(
                        onClick = { /* 비활성화 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text("앱을 실행해보세요", fontSize = StandTypography.titleSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "차단된 앱을 실행해야 다음으로 진행됩니다",
                        fontSize = StandTypography.labelLarge,
                        color = StandColors.WarmLightDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 6. 목표 입력
@Composable
fun GoalInputStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 6,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }

    var selectedUnit by remember { mutableStateOf("steps") } // "steps" or "km"
    var hasHealthConnectPermission by remember { mutableStateOf(false) }

    // 슬라이더 값 (걸음: 50-70, km: 0.04-0.1)
    var stepsSliderValue by remember { mutableFloatStateOf(60f) } // 기본값 60보
    var kmSliderValue by remember { mutableFloatStateOf(0.07f) } // 기본값 0.07km

    // Health Connect 권한 확인
    LaunchedEffect(Unit) {
        hasHealthConnectPermission = healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "목표 설정",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "체험을 위해\n목표를 설정해주세요",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(32.dp))

        // 단위 선택
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUnit == "steps")
                        StandColors.WarmLight.copy(alpha = 0.2f)
                    else
                        Color.White.copy(alpha = 0.1f)
                ),
                border = if (selectedUnit == "steps")
                    androidx.compose.foundation.BorderStroke(2.dp, StandColors.WarmLight)
                else
                    null,
                onClick = { selectedUnit = "steps" }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "걸음 수",
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "steps") StandColors.WarmLightBright else Color.White
                    )
                    Text(
                        text = "기본 센서",
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUnit == "km")
                        StandColors.WarmLight.copy(alpha = 0.2f)
                    else if (!hasHealthConnectPermission)
                        Color.White.copy(alpha = 0.05f)
                    else
                        Color.White.copy(alpha = 0.1f)
                ),
                border = if (selectedUnit == "km")
                    androidx.compose.foundation.BorderStroke(2.dp, StandColors.WarmLight)
                else
                    null,
                onClick = {
                    if (hasHealthConnectPermission) {
                        selectedUnit = "km"
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "거리 (km)",
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "km")
                            StandColors.WarmLightBright
                        else if (!hasHealthConnectPermission)
                            Color.White.copy(alpha = 0.4f)
                        else
                            Color.White
                    )
                    Text(
                        text = if (hasHealthConnectPermission) "피트니스 연결" else "연결 필요",
                        fontSize = StandTypography.labelLarge,
                        color = if (hasHealthConnectPermission) Color.White.copy(alpha = 0.6f) else MockupColors.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 슬라이더로 목표 설정
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 선택된 값 표시
            Text(
                text = if (selectedUnit == "steps") {
                    "${stepsSliderValue.toInt()}보"
                } else {
                    String.format("%.2fkm", kmSliderValue)
                },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = StandColors.WarmLightBright
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedUnit == "steps") "50보 ~ 70보" else "0.04km ~ 0.1km",
                fontSize = StandTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 슬라이더
            if (selectedUnit == "steps") {
                Slider(
                    value = stepsSliderValue,
                    onValueChange = { stepsSliderValue = it },
                    valueRange = 50f..70f,
                    steps = 19, // 50-70 사이 20개 값 (1보 단위)
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = StandColors.WarmLightBright,
                        activeTrackColor = StandColors.WarmLight,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            } else {
                Slider(
                    value = kmSliderValue,
                    onValueChange = { kmSliderValue = it },
                    valueRange = 0.04f..0.1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = StandColors.WarmLightBright,
                        activeTrackColor = StandColors.WarmLight,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "목표 달성하면",
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.WarmLightBright
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "차단된 앱이 해제됩니다!\n다음 단계에서 직접 걸어보세요.",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }

        // km 선택 시 Health Connect 안내
        if (selectedUnit == "km" && !hasHealthConnectPermission) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MockupColors.TextMuted.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ 피트니스 앱 연결 필요",
                        fontSize = StandTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "km 단위를 사용하려면 피트니스 앱과 연결해야 합니다.",
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                if (selectedUnit == "steps") {
                    val steps = stepsSliderValue.toInt()
                    // 목표 설정 (걸음 수)
                    preferenceManager?.saveGoal(steps)
                    preferenceManager?.saveGoalUnit("steps")
                    onNext()
                } else { // km
                    val km = kmSliderValue.toDouble()
                    // km를 걸음 수로 변환 (1km ≈ 1300보)
                    val steps = (km * 1300).toInt()
                    preferenceManager?.saveGoal(steps)
                    preferenceManager?.saveGoalUnit("km")
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = true // 슬라이더는 항상 유효한 값
        ) {
            Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
        }
            }
        }
    }
}

// 7. 걷기 체험 - 걸음마다 불이 켜지는 애니메이션
@Composable
fun WalkingStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 7,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository

    // 목표 단위 확인 (steps or km)
    val goalUnit = remember { preferenceManager?.getGoalUnit() ?: "steps" }
    val isKmMode = goalUnit == "km"

    // 튜토리얼 시작 시점의 걸음 수를 기록 (Health Connect 덮어쓰기 방지)
    val baselineSteps = remember { repository.getTodaySteps() }
    var currentSteps by remember { mutableIntStateOf(0) }
    val targetSteps = repository.getGoal()
    var hasLeftApp by remember { mutableStateOf(false) }
    var goalJustAchieved by remember { mutableStateOf(false) }
    var previousSteps by remember { mutableIntStateOf(0) }

    // 걸음 감지 시 불빛 깜빡임 애니메이션
    var stepFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (stepFlash) 1f else 0.3f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "flashAlpha",
        finishedListener = { stepFlash = false }
    )

    // 목표 달성 시 빛나는 효과
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        while (true) {
            val rawSteps = repository.getTodaySteps()
            // 튜토리얼 시작 시점부터의 걸음 수만 계산
            val newSteps = maxOf(0, rawSteps - baselineSteps)
            val wasAchieved = currentSteps >= targetSteps

            // 걸음 수가 증가하면 불빛 깜빡임
            if (newSteps > previousSteps && newSteps < targetSteps) {
                stepFlash = true
                hapticManager?.lightOn()
            }
            previousSteps = currentSteps
            currentSteps = newSteps
            val isNowAchieved = currentSteps >= targetSteps

            // 목표 달성 순간 햅틱
            if (isNowAchieved && !wasAchieved && !goalJustAchieved) {
                hapticManager?.goalAchieved()
                goalJustAchieved = true
            }
            delay(1000)
        }
    }
    DisposableEffect(Unit) {
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    hasLeftApp = true
                }
                else -> {}
            }
        }

        lifecycleOwner?.lifecycle?.addObserver(observer)

        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val isGoalAchieved = currentSteps >= targetSteps

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 빛 효과
        if (isGoalAchieved) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glowAlpha * 0.4f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLightBright.copy(alpha = 0.6f),
                                StandColors.WarmLight.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 1000f
                        )
                    )
            )
        } else if (stepFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(flashAlpha * 0.3f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLight.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // km 모드일 때 거리로 표시
                val targetDisplay = if (isKmMode) {
                    String.format("%.2fkm", targetSteps / 1300.0)
                } else {
                    "${targetSteps}보"
                }

                Text(
                    text = if (isGoalAchieved && !hasLeftApp)
                        "목표 달성!\n이제 앱을 실행해보세요"
                    else if (isGoalAchieved && hasLeftApp)
                        "체험 완료!"
                    else
                        "$targetDisplay 걸어보세요!",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    color = if (isGoalAchieved) StandColors.WarmLightBright else Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 불빛 아이콘 Row (걸음 수에 따라 켜짐)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val lightsCount = targetSteps.coerceAtMost(10)
                    val litLights = ((currentSteps.toFloat() / targetSteps) * lightsCount).toInt()

                    for (i in 0 until lightsCount) {
                        val isLit = i < litLights
                        Box(
                            modifier = Modifier
                                .size(if (isLit) 28.dp else 24.dp)
                                .padding(2.dp)
                                .background(
                                    brush = if (isLit) Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightBright,
                                            StandColors.WarmLight.copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    ) else Brush.radialGradient(
                                        colors = listOf(
                                            Color.Gray.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLit) {
                                Text(
                                    text = "💡",
                                    fontSize = 14.sp,
                                    modifier = Modifier.alpha(if (i == litLights - 1 && stepFlash) flashAlpha else 1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 걸음 수 표시 (빛나는 효과)
                Box(contentAlignment = Alignment.Center) {
                    if (isGoalAchieved) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .alpha(glowAlpha * 0.5f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightBright.copy(alpha = 0.8f),
                                            StandColors.WarmLight.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // km 모드일 때 거리로 표시
                        val currentDisplay = if (isKmMode) {
                            String.format("%.2f", currentSteps / 1300.0)
                        } else {
                            currentSteps.toString()
                        }
                        val targetDisplaySmall = if (isKmMode) {
                            String.format("%.2f km", targetSteps / 1300.0)
                        } else {
                            "$targetSteps 보"
                        }

                        Text(
                            text = currentDisplay,
                            fontSize = StandTypography.displayHero,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoalAchieved) StandColors.WarmLightBright else StandColors.WarmLight
                        )
                        Text(
                            text = "/ $targetDisplaySmall",
                            fontSize = StandTypography.headlineSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 프로그레스바 (따뜻한 조명 그라데이션)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (isGoalAchieved) listOf(
                                        StandColors.WarmLight,
                                        StandColors.WarmLightBright
                                    ) else listOf(
                                        StandColors.WarmLightDim,
                                        StandColors.WarmLight
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isGoalAchieved && hasLeftApp -> StandColors.WarmLight.copy(alpha = 0.15f)
                            isGoalAchieved -> StandColors.WarmLight.copy(alpha = 0.1f)
                            else -> StandColors.WarmLightDim.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        when {
                            isGoalAchieved && hasLeftApp -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PixelIcon(iconName = "icon_trophy", size = 18.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "완벽해요!",
                                        fontSize = StandTypography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StandColors.WarmLightBright
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "목표를 달성하고 앱도 실행해보셨네요!\n이제 앱이 해제된 상태입니다.",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            isGoalAchieved -> {
                                Text(
                                    text = "앱을 실행해보세요!",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLight
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. 홈 버튼으로 나가기\n2. 차단했던 앱 실행\n3. 이제 앱이 열립니다!\n4. rebon으로 돌아오기",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = "🚶 걸어보세요",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLight
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "폰을 들고 걸으세요!\n걸을수록 불이 켜집니다.",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 테스트 버튼들 (튜토리얼용)
                if (!isGoalAchieved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StandColors.WarmLightDim.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "테스트 도구",
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLight
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        val newSteps = currentSteps + 5
                                        repository.saveTodaySteps(newSteps)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StandColors.WarmLightDim
                                    )
                                ) {
                                    Text("+5", fontSize = StandTypography.bodyMedium, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        repository.saveTodaySteps(targetSteps)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StandColors.WarmLight
                                    )
                                ) {
                                    Text("달성", fontSize = StandTypography.bodyMedium, color = StandColors.DarkBackground)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isGoalAchieved && hasLeftApp) {
                    Button(
                        onClick = {
                            // 다음 단계로 이동 (걸음 수는 UnlockedStep에서 리셋)
                            onNext()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StandColors.WarmLight
                        )
                    ) {
                        Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                    }
                } else {
                    Button(
                        onClick = { /* 비활성화 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text(
                            text = if (!isGoalAchieved) "걸음 수 달성 필요" else "앱 실행 필요",
                            fontSize = StandTypography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (!isGoalAchieved)
                            "걸음 수를 채워주세요"
                        else
                            "앱을 실행해보세요",
                        fontSize = StandTypography.labelLarge,
                        color = StandColors.WarmLightDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 8. 해제 확인 - 조명이 밝아지는 효과
@Composable
fun UnlockedStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 8,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    // 빛나는 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "unlockGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 빛 효과
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.WarmLightBright.copy(alpha = 0.6f),
                            StandColors.WarmLight.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 빛나는 전구 아이콘
                Box(
                    modifier = Modifier.scale(glowScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .alpha(glowAlpha)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        StandColors.WarmLightBright.copy(alpha = 0.8f),
                                        StandColors.WarmLight.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "💡",
                        fontSize = StandTypography.displayLarge
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "목표 달성!\n앱이 해제되었어요!",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StandColors.WarmLightBright,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "설정한 걸음 수를 걸으니\n선택한 앱을 다시 사용할 수 있어요!",
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLight.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "rebon의 핵심",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "매일 목표를 달성하면 앱을 자유롭게!\n실패하면 차단됩니다.",
                            fontSize = StandTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        hapticManager?.success()
                        onNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StandColors.WarmLight
                    )
                ) {
                    Text("다음", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                }
            }
        }
    }
}

// 9. 휴식 버튼 설명
@Composable
fun EmergencyButtonStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 9,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "잠시 쉬어가기",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.WarmLight.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🕐",
                    fontSize = StandTypography.displaySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "15분 휴식 모드",
                    fontSize = StandTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = StandColors.WarmLightBright,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "• 급한 일이 있을 때 15분간 앱 사용 가능\n• 하루에 1회만 사용 가능\n• 15분 후 자동으로 다시 차단",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Tip",
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = StandColors.WarmLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "급한 업무나 연락이 필요할 때\n잠시 쉬어가세요",
            fontSize = StandTypography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StandColors.WarmLight
            )
        ) {
            Text("튜토리얼 완료!", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
        }
            }
        }
    }
}

// 10. 크레딧 시스템 설명 - 따뜻한 조명 테마
@Composable
fun SubscriptionStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 10,
    totalSteps: Int = 11,
    onNext: () -> Unit
) {
    // 부드러운 빛나는 효과
    val infiniteTransition = rememberInfiniteTransition(label = "subscriptionGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 조명 효과
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.WarmLight.copy(alpha = 0.5f),
                            StandColors.WarmLightDim.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 전구 아이콘
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .alpha(glowAlpha)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        StandColors.WarmLightBright.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "💡",
                        fontSize = StandTypography.displaySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "크레딧 시스템",
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StandColors.WarmLightBright
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Google Play 결제: 월 4,900원",
                    fontSize = StandTypography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 크레딧 시스템 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "달성률에 따른 크레딧 지급",
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = StandColors.WarmLightBright,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 95% 이상 - 가장 밝은 조명
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = StandTypography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("95% 이상", fontSize = StandTypography.bodyLarge, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+4,900",
                                    fontSize = StandTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightBright
                                )
                                Text(
                                    text = "실질 무료",
                                    fontSize = StandTypography.bodySmall,
                                    color = StandColors.WarmLight
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = StandColors.WarmLightDim.copy(alpha = 0.3f)
                        )

                        // 80-94% - 중간 밝기
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = StandTypography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("80~95% 미만", fontSize = StandTypography.bodyLarge, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+2,400",
                                    fontSize = StandTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLight
                                )
                                Text(
                                    text = "실질 2,500원",
                                    fontSize = StandTypography.bodySmall,
                                    color = StandColors.WarmLightDim
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = StandColors.WarmLightDim.copy(alpha = 0.3f)
                        )

                        // 80% 미만 - 어두운 조명
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔒", fontSize = StandTypography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("80% 미만", fontSize = StandTypography.bodyLarge, color = Color.White.copy(alpha = 0.5f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "0",
                                    fontSize = StandTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "정가 4,900원",
                                    fontSize = StandTypography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 친구 초대 안내
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLightBright.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁", fontSize = StandTypography.headlineSmall)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "친구 초대 혜택",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                            Text(
                                text = "구독자는 친구 1명 무료 초대 가능!",
                                fontSize = StandTypography.bodyMedium,
                                color = StandColors.WarmLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "* Google Play에서 매월 4,900원 자동 결제\n* 크레딧으로 실질 부담 금액이 달라집니다",
                    fontSize = StandTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        hapticManager?.goalAchieved()
                        onNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StandColors.WarmLight
                    )
                ) {
                    Text("시작하기", fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                }
            }
        }
    }
}