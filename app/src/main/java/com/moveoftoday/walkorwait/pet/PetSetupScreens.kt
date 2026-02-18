package com.moveoftoday.walkorwait.pet

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.*
import kotlinx.coroutines.delay
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Pet-guided Permission Setup Screen
 * 펫이 안내하는 권한 설정 화면
 */
@Composable
fun PetPermissionScreen(
    petType: PetType,
    petName: String,
    onComplete: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(true) }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted = isGranted
        if (isGranted) hapticManager?.success()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    // Pet speech based on personality
    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "권한 설정 해줘."
        PetPersonality.CUTE -> "이거 켜주면 같이 걸을 수 있음!"
        PetPersonality.TSUNDERE -> "뭐, 설정 안 하면... 못 걷는 건 아니지만."
        PetPersonality.DIALECT -> "권한 설정 해줘"
        PetPersonality.TIMID -> "저, 이거 필요해요..."
        PetPersonality.POSITIVE -> "권한 설정하자! 금방이야!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        // Status bar with pet name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
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

        // Permission cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "필요한 권한",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                // Activity Recognition Permission
                PetPermissionCard(
                    icon = "icon_boots",
                    title = "걸음 측정",
                    description = "걸음 수를 측정해요",
                    isGranted = activityPermissionGranted,
                    onRequest = {
                        hapticManager?.lightClick()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            activityPermissionGranted = true
                        }
                    }
                )

                // Notification Permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PetPermissionCard(
                        icon = "icon_dialog",
                        title = "알림",
                        description = "진행 상황을 알려드려요",
                        isGranted = notificationPermissionGranted,
                        onRequest = {
                            hapticManager?.lightClick()
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next button
        MockupButton(
            text = "다음",
            onClick = {
                hapticManager?.success()
                onComplete()
            },
            enabled = activityPermissionGranted
        )
    }
}

@Composable
private fun PetPermissionCard(
    icon: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isGranted) Color(0xFFE8F5E9) else Color.White)
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .clickable(enabled = !isGranted) { onRequest() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PixelIcon(iconName = icon, size = 24.dp)
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }
        }

        if (isGranted) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MockupColors.Border)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "허용",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Pet-guided Health Connect Setup Screen
 * 펫이 안내하는 Health Connect 연결 화면
 */
@Composable
fun PetHealthConnectScreen(
    petType: PetType,
    petName: String,
    onComplete: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf<List<FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.createPermissionRequestContract()
    ) { granted ->
        scope.launch {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                isConnecting = false
                hapticManager?.success()
                val firstApp = installedApps.firstOrNull()
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(firstApp?.appName ?: "")
                StepCounterService.stop(context)
                StepCounterService.start(context)
                delay(500)
                onComplete()
            } else {
                isConnecting = false
            }
        }
    }

    LaunchedEffect(Unit) {
        isHealthConnectAvailable = healthConnectManager.isAvailable()
        installedApps = healthConnectManager.getInstalledFitnessApps()
        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                delay(1000)
                onComplete()
            }
        }
    }

    // Pet speech based on state
    val speechText = when {
        hasPermissions -> when (petType.personality) {
            PetPersonality.TOUGH -> "연결 완료."
            PetPersonality.CUTE -> "연결됐어! 야타~"
            PetPersonality.TSUNDERE -> "뭐, 잘했어."
            PetPersonality.DIALECT -> "연결 됐네 마~"
            PetPersonality.TIMID -> "연, 연결됐어요...!"
            PetPersonality.POSITIVE -> "완벽해!"
        }
        installedApps.isNotEmpty() -> when (petType.personality) {
            PetPersonality.TOUGH -> "피트니스 앱 연결해."
            PetPersonality.CUTE -> "이 앱이랑 연결하면 더 정확해짐!"
            PetPersonality.TSUNDERE -> "연결하면... 더 정확하긴 해."
            PetPersonality.DIALECT -> "피트니스 앱 연결하자"
            PetPersonality.TIMID -> "이, 이거 연결하면 좋아요..."
            PetPersonality.POSITIVE -> "피트니스 앱 발견! 연결하자!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "기본 센서로 해도 돼."
            PetPersonality.CUTE -> "피트니스 앱 없어도 다이죠부~"
            PetPersonality.TSUNDERE -> "없으면... 기본 센서라도."
            PetPersonality.DIALECT -> "기본 센서로 해도 되긴 하네 마~"
            PetPersonality.TIMID -> "없, 없어도 괜찮아요..."
            PetPersonality.POSITIVE -> "기본 센서로도 충분해!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area
        PetArea(
            petType = petType,
            isWalking = hasPermissions,
            speechText = speechText,
            happinessLevel = if (hasPermissions) 5 else 3,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Content card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelIcon(iconName = "icon_heart", size = 40.dp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "피트니스 앱 연결",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (installedApps.isNotEmpty()) {
                    Text(
                        text = "발견된 앱",
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    installedApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = app.icon, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                                Text(
                                    text = if (hasPermissions) "연결됨 ✓" else "설치됨",
                                    fontSize = 12.sp,
                                    color = if (hasPermissions) Color(0xFF4CAF50) else MockupColors.TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (isHealthConnectAvailable && !hasPermissions) {
                        MockupButton(
                            text = if (isConnecting) "연결 중..." else "연결하기",
                            onClick = {
                                hapticManager?.lightClick()
                                isConnecting = true
                                permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                            },
                            enabled = !isConnecting
                        )
                    } else if (!isHealthConnectAvailable) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Health Connect 필요",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            MockupButton(
                                text = "Play Store에서 설치",
                                onClick = {
                                    hapticManager?.lightClick()
                                    healthConnectManager.openHealthConnectPlayStore()
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "피트니스 앱이 없어요\n기본 센서로 측정합니다",
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "추천 앱",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf("📱 삼성 헬스", "🏃 Google Fit").forEach { app ->
                            Text(
                                text = "• $app",
                                fontSize = 12.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Skip button
        if (!hasPermissions) {
            TextButton(
                onClick = {
                    hapticManager?.click()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "나중에 하기 (기본 센서 사용)",
                    color = MockupColors.TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            MockupButton(
                text = "다음",
                onClick = {
                    hapticManager?.success()
                    onComplete()
                }
            )
        }
    }
}

/**
 * Pet-guided Accessibility Setup Screen
 * 펫이 안내하는 접근성 설정 화면
 */
@Composable
fun PetAccessibilityScreen(
    petType: PetType,
    petName: String,
    onComplete: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(false) }

    // Check accessibility status periodically
    LaunchedEffect(Unit) {
        while (!isEnabled) {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            isEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true

            if (isEnabled) {
                hapticManager?.success()
                delay(1000)
                onComplete()
            }
            delay(1000)
        }
    }

    // Pet speech based on state
    val speechText = when {
        isEnabled -> when (petType.personality) {
            PetPersonality.TOUGH -> "좋아. 준비됐어."
            PetPersonality.CUTE -> "야타~! 이제 시작이야!"
            PetPersonality.TSUNDERE -> "뭐, 잘했어."
            PetPersonality.DIALECT -> "됐네 마! 이제 시작이라!"
            PetPersonality.TIMID -> "해, 해냈어요...!"
            PetPersonality.POSITIVE -> "완벽해! 이제 같이 걷자!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "rebon을 켜줘. 중요해."
            PetPersonality.CUTE -> "이거 켜주면 같이 놀 수 있음!"
            PetPersonality.TSUNDERE -> "켜줘... 아니면 같이 못 걸어."
            PetPersonality.DIALECT -> "이거 켜줘 중요하다"
            PetPersonality.TIMID -> "저, 이거 켜주세요... 꼭이요..."
            PetPersonality.POSITIVE -> "이것만 켜면 돼! 할 수 있어!"
        }
    }

    val backgroundColor = if (isEnabled) MockupColors.AchievedBackground else MockupColors.Background
    val cardColor = if (isEnabled) MockupColors.AchievedCard else MockupColors.CardBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area
        PetArea(
            petType = petType,
            isWalking = isEnabled,
            speechText = speechText,
            happinessLevel = if (isEnabled) 5 else 2,
            backgroundColor = cardColor,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Setup guide card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelIcon(iconName = "icon_gear", size = 40.dp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "앱 제어 설정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(15.dp))

                if (isEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelIcon(iconName = "icon_trophy", size = 24.dp)
                        Text(
                            text = "설정 완료!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    // Setup steps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "설정 방법",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        listOf(
                            "1. 아래 버튼 누르기",
                            "2. 'rebon' 찾기",
                            "3. rebon을 ON으로",
                            "4. 확인 누르기"
                        ).forEach { step ->
                            Text(
                                text = step,
                                fontSize = 14.sp,
                                color = MockupColors.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "✓ ON 되면 자동으로 다음 단계!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isEnabled) {
            MockupButton(
                text = "설정 화면으로",
                onClick = {
                    hapticManager?.click()
                    // 접근성 설정 후 앱으로 자동 복귀하기 위한 플래그 설정
                    com.moveoftoday.walkorwait.PreferenceManager(context).setAwaitingAccessibilityReturn(true)
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "⚠️ rebon ON 해야 다음으로 진행됩니다",
                fontSize = 12.sp,
                color = Color(0xFFE57373),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Pet-guided App Selection Screen
 * 펫이 안내하는 앱 선택 화면
 */
@Composable
fun PetAppSelectionScreen(
    petType: PetType,
    petName: String,
    preferenceManager: PreferenceManager?,
    onComplete: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    val appsByCategory = remember {
        AppUtils.getInstalledAppsByCategory(context)
    }

    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var expandedCategories by remember { mutableStateOf(setOf<AppCategory>()) }

    val totalApps = appsByCategory.values.flatten().size

    // Pet speech
    val speechText = when {
        selectedApps.size >= 3 -> when (petType.personality) {
            PetPersonality.TOUGH -> "좋아. 그 정도면 돼."
            PetPersonality.CUTE -> "와~ 많이 골랐어! 대박ㅋㅋ"
            PetPersonality.TSUNDERE -> "뭐, 적당히 골랐네."
            PetPersonality.DIALECT -> "잘 골랐네"
            PetPersonality.TIMID -> "이, 이 정도면... 괜찮아요..."
            PetPersonality.POSITIVE -> "완벽한 선택이야!"
        }
        selectedApps.isNotEmpty() -> when (petType.personality) {
            PetPersonality.TOUGH -> "더 골라도 돼."
            PetPersonality.CUTE -> "더 골라볼래~?"
            PetPersonality.TSUNDERE -> "그것만? 뭐, 상관없지만."
            PetPersonality.DIALECT -> "더 골라도 된다"
            PetPersonality.TIMID -> "더, 더 고르시겠어요...?"
            PetPersonality.POSITIVE -> "좋아! 더 골라도 돼!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "제어할 앱 골라."
            PetPersonality.CUTE -> "어떤 앱 잠글까~?"
            PetPersonality.TSUNDERE -> "앱 골라... 안 걸으면 잠기는 거야."
            PetPersonality.DIALECT -> "앱 골라줘"
            PetPersonality.TIMID -> "저, 앱을 골라주세요..."
            PetPersonality.POSITIVE -> "자주 쓰는 앱을 골라봐!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area (smaller)
        PetArea(
            petType = petType,
            isWalking = selectedApps.isNotEmpty(),
            speechText = speechText,
            happinessLevel = when {
                selectedApps.size >= 3 -> 5
                selectedApps.isNotEmpty() -> 4
                else -> 3
            },
            modifier = Modifier.height(140.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Selected count
        if (selectedApps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MockupColors.CardBackground)
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                PixelIcon(iconName = "icon_star", size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${selectedApps.size}개 선택됨",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // App list
        if (totalApps == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📱", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "제어할 앱이 없습니다",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = "앱을 설치해주세요",
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appsByCategory.forEach { (category, apps) ->
                    // Category header
                    item(key = "header_$category") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MockupColors.CardBackground)
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                .clickable {
                                    hapticManager?.lightClick()
                                    expandedCategories = if (category in expandedCategories) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${getCategoryIcon(category)} ${stringResource(category.displayNameRes)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (category in expandedCategories) "▲" else "▼ ${apps.size}개",
                                fontSize = 14.sp,
                                color = MockupColors.TextMuted
                            )
                        }
                    }

                    // Apps in category
                    if (category in expandedCategories) {
                        items(apps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedApps.contains(app.packageName))
                                            Color(0xFFE3F2FD)
                                        else Color.White
                                    )
                                    .border(
                                        width = if (selectedApps.contains(app.packageName)) 2.dp else 1.dp,
                                        color = MockupColors.Border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        hapticManager?.lightClick()
                                        selectedApps = if (selectedApps.contains(app.packageName)) {
                                            selectedApps - app.packageName
                                        } else {
                                            selectedApps + app.packageName
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.icon?.let { bitmap ->
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    color = MockupColors.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedApps.contains(app.packageName)) {
                                    Text(
                                        text = "✓",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Border
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Next button
        MockupButton(
            text = if (totalApps == 0) "건너뛰기" else "다음 (${selectedApps.size}개 선택)",
            onClick = {
                hapticManager?.success()
                preferenceManager?.saveLockedApps(selectedApps)
                onComplete()
            },
            enabled = selectedApps.isNotEmpty() || totalApps == 0
        )
    }
}

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

// ============ RealGoalSetupScreen 다국어 지원 ============
private object RealGoalStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun dailyGoalSetting(): String = when (getLang()) {
        "ko" -> "매일 목표 설정"
        "ja" -> "毎日の目標設定"
        "zh" -> "每日目标设置"
        "es" -> "Meta diaria"
        else -> "Daily Goal"
    }

    fun steps(): String = when (getLang()) {
        "ko" -> "걸음 수"
        "ja" -> "歩数"
        "zh" -> "步数"
        "es" -> "Pasos"
        else -> "Steps"
    }

    fun distanceKm(): String = when (getLang()) {
        "ko" -> "거리 (km)"
        "ja" -> "距離 (km)"
        "zh" -> "距离 (km)"
        "es" -> "Distancia (km)"
        else -> "Distance (km)"
    }

    fun stepsFormat(steps: Int): String = when (getLang()) {
        "ko" -> "%,d보".format(steps)
        "ja" -> "%,d歩".format(steps)
        "zh" -> "%,d步".format(steps)
        else -> "%,d steps".format(steps)
    }

    fun aboutSteps(steps: Int): String = when (getLang()) {
        "ko" -> "약 %,d보".format(steps)
        "ja" -> "約%,d歩".format(steps)
        "zh" -> "约%,d步".format(steps)
        "es" -> "~%,d pasos".format(steps)
        else -> "~%,d steps".format(steps)
    }

    fun aboutKm(km: Float): String = when (getLang()) {
        "ko" -> "약 %.1fkm".format(km)
        "ja" -> "約%.1fkm".format(km)
        "zh" -> "约%.1fkm".format(km)
        "es" -> "~%.1fkm".format(km)
        else -> "~%.1fkm".format(km)
    }

    fun minSteps(): String = when (getLang()) {
        "ko" -> "1,000보"
        "ja" -> "1,000歩"
        "zh" -> "1,000步"
        else -> "1,000"
    }

    fun maxSteps(): String = when (getLang()) {
        "ko" -> "55,000보"
        "ja" -> "55,000歩"
        "zh" -> "55,000步"
        else -> "55,000"
    }

    fun halfMarathon(): String = when (getLang()) {
        "ko" -> "하프마라톤"
        "ja" -> "ハーフマラソン"
        "zh" -> "半程马拉松"
        "es" -> "Media Maratón"
        else -> "Half Marathon"
    }

    fun fullMarathon(): String = when (getLang()) {
        "ko" -> "풀마라톤"
        "ja" -> "フルマラソン"
        "zh" -> "全程马拉松"
        "es" -> "Maratón"
        else -> "Full Marathon"
    }

    fun halfBtn(): String = when (getLang()) {
        "ko" -> "하프 21.1km"
        "ja" -> "ハーフ 21.1km"
        "zh" -> "半马 21.1km"
        "es" -> "Media 21.1km"
        else -> "Half 21.1km"
    }

    fun fullBtn(): String = when (getLang()) {
        "ko" -> "풀 42.195km"
        "ja" -> "フル 42.195km"
        "zh" -> "全马 42.195km"
        "es" -> "Maratón 42.195km"
        else -> "Full 42.195km"
    }

    fun start(): String = when (getLang()) {
        "ko" -> "시작하기"
        "ja" -> "始める"
        "zh" -> "开始"
        "es" -> "Empezar"
        else -> "Start"
    }

    // 펫 성격별 대사
    fun speechLoyal(): String = when (getLang()) {
        "ko" -> "매일 달성할 목표 정해."
        "ja" -> "毎日達成する目標を決めろ。"
        "zh" -> "设定每天要达成的目标。"
        "es" -> "Establece tu meta diaria."
        else -> "Set your daily goal."
    }

    fun speechTsundere(): String = when (getLang()) {
        "ko" -> "매일 달성할 목표... 정해."
        "ja" -> "毎日の目標...決めて。"
        "zh" -> "每天的目标...定吧。"
        "es" -> "Tu meta diaria... ponla."
        else -> "Your daily goal... set it."
    }

    fun speechFoodie(): String = when (getLang()) {
        "ko" -> "매일 목표! 얼마나 걸을래~?"
        "ja" -> "毎日の目標！どれくらい歩く~？"
        "zh" -> "每日目标！想走多少~？"
        "es" -> "¡Meta diaria! ¿Cuánto caminarás~?"
        else -> "Daily goal! How much will you walk~?"
    }

    fun speechPlayful(): String = when (getLang()) {
        "ko" -> "매일 얼마나 걸을끼고?"
        "ja" -> "毎日どれくらい歩く~？"
        "zh" -> "每天想走多少~？"
        "es" -> "¿Cuánto caminarás cada día~?"
        else -> "How much will you walk daily~?"
    }

    fun speechTimid(): String = when (getLang()) {
        "ko" -> "매일 달성할 목표를 정해주세요..."
        "ja" -> "毎日の目標を決めてください..."
        "zh" -> "请设定每天的目标..."
        "es" -> "Por favor establece tu meta diaria..."
        else -> "Please set your daily goal..."
    }

    fun speechClumsy(): String = when (getLang()) {
        "ko" -> "매일 달성할 목표를 정하자!"
        "ja" -> "毎日の目標を決めよう！"
        "zh" -> "来定每天的目标吧！"
        "es" -> "¡Vamos a poner tu meta diaria!"
        else -> "Let's set your daily goal!"
    }
}

/**
 * Real Goal Setup Screen - 튜토리얼 완료 후 실제 목표 설정
 * Pet Layout 약속: Title(32sp, Kenney) → Display Area(240dp, stripes) → Instruction(22sp) → Action Button
 *
 * 걸음수: 100보 단위
 * 거리: 0.5km 단위, 최대 42.195km (풀마라톤), 21.1km (하프마라톤) 포함
 */
@Composable
fun RealGoalSetupScreen(
    petType: PetTypeV2,
    petName: String,
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager?,
    onComplete: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()

    // 단위 선택: "steps" or "km"
    var selectedUnit by remember { mutableStateOf(preferenceManager?.getGoalUnit() ?: "steps") }

    // 슬라이더 값 (걸음수 기준으로 저장)
    var goalSteps by remember { mutableStateOf(8000f) }

    // 범위: 걸음수 1000~55000 (100보 단위), 풀마라톤 42.195km = 약 54854보 포함
    val stepsRange = 1000f..55000f
    val stepsStep = 100f // 100보 단위

    // 현재 표시값 (km는 걸음수에서 계산)
    val displayKm = goalSteps / 1300f

    // 특별 거리 라벨
    val specialLabel = when {
        selectedUnit == "km" && kotlin.math.abs(displayKm - 42.195f) < 0.2f -> RealGoalStrings.fullMarathon()
        selectedUnit == "km" && kotlin.math.abs(displayKm - 21.1f) < 0.2f -> RealGoalStrings.halfMarathon()
        else -> null
    }

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> RealGoalStrings.speechLoyal()
        PetPersonalityV2.TSUNDERE -> RealGoalStrings.speechTsundere()
        PetPersonalityV2.FOODIE -> RealGoalStrings.speechFoodie()
        PetPersonalityV2.PLAYFUL -> RealGoalStrings.speechPlayful()
        PetPersonalityV2.TIMID -> RealGoalStrings.speechTimid()
        PetPersonalityV2.CLUMSY -> RealGoalStrings.speechClumsy()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(horizontal = 20.dp)
            .padding(bottom = 72.dp),  // 네비게이션 바 고려
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // 1. Title - 32sp, Kenney Font
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Display Area - 240dp, 수평 줄무늬, SpeechBubble + PetSpriteWithSyncedGlow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = 4.dp.toPx()
                    val stripeColor = Color(0xFFF0F0F0)
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = stripeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                        )
                        y += stripeHeightPx * 2
                    }
                }
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SpeechBubble(text = speechText, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                PetSpriteV2WithGlow(
                    petType = petType,
                    stage = PetGrowthStage.BABY,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = 140.dp,
                    monochrome = true,
                    showGlow = true,
                    applyDisplayScale = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Instruction - 22sp
        Text(
            text = RealGoalStrings.dailyGoalSetting(),
            fontSize = 22.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Middle Content - 걸음수/km 선택 + 슬라이더
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 단위 선택 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 걸음 수 버튼
                Button(
                    onClick = {
                        hapticManager?.click()
                        selectedUnit = "steps"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedUnit == "steps") MockupColors.Border else Color(0xFFE0E0E0),
                        contentColor = if (selectedUnit == "steps") Color.White else MockupColors.TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(RealGoalStrings.steps(), fontWeight = FontWeight.Bold)
                }

                // km 버튼
                Button(
                    onClick = {
                        hapticManager?.click()
                        selectedUnit = "km"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedUnit == "km") MockupColors.Border else Color(0xFFE0E0E0),
                        contentColor = if (selectedUnit == "km") Color.White else MockupColors.TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(RealGoalStrings.distanceKm(), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 현재 값 표시 영역 (높이 고정 - 탭 전환 시 레이아웃 일관성)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedUnit == "km") {
                            if (kotlin.math.abs(displayKm - 42.195f) < 0.2f) "42.195 km"
                            else if (kotlin.math.abs(displayKm - 21.1f) < 0.2f) "21.1 km"
                            else String.format("%.1f km", displayKm)
                        } else {
                            RealGoalStrings.stepsFormat(goalSteps.toInt())
                        },
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )

                    // 환산 값 표시
                    Text(
                        text = if (selectedUnit == "km") {
                            RealGoalStrings.aboutSteps(goalSteps.toInt())
                        } else {
                            RealGoalStrings.aboutKm(displayKm)
                        },
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 슬라이더 (goalSteps 기준으로 통일 - 모드 전환 시 위치 유지)
            Slider(
                value = goalSteps,
                onValueChange = { newValue ->
                    hapticManager?.lightClick()
                    if (selectedUnit == "km") {
                        // km 모드: 0.5km 단위로 반올림 (1300보 * 0.5 = 650보 단위)
                        val kmStep = 650f
                        goalSteps = (kotlin.math.round(newValue / kmStep) * kmStep).coerceIn(stepsRange)
                        // 특별값 (하프/풀마라톤) 근처면 스냅
                        val currentKm = goalSteps / 1300f
                        if (kotlin.math.abs(currentKm - 21.1f) < 0.3f) {
                            goalSteps = 21.1f * 1300f
                        } else if (kotlin.math.abs(currentKm - 42.195f) < 0.3f) {
                            goalSteps = 42.195f * 1300f
                        }
                    } else {
                        // 걸음 수 모드: 100보 단위로 반올림
                        goalSteps = (kotlin.math.round(newValue / stepsStep) * stepsStep).coerceIn(stepsRange)
                    }
                },
                valueRange = stepsRange,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MockupColors.Border,
                    activeTrackColor = MockupColors.Border,
                    inactiveTrackColor = Color(0xFFE0E0E0)
                )
            )

            // 범위 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedUnit == "km") "0.5km" else RealGoalStrings.minSteps(),
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
                Text(
                    text = if (selectedUnit == "km") "42.195km" else RealGoalStrings.maxSteps(),
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }

            // 퀵 선택 버튼 영역 (높이 고정 - 레이아웃 밀림 방지)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.height(36.dp)) {
                if (selectedUnit == "km") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 하프마라톤
                        Button(
                            onClick = {
                                hapticManager?.click()
                                goalSteps = 21.1f * 1300f
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (kotlin.math.abs(displayKm - 21.1f) < 0.2f) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                contentColor = if (kotlin.math.abs(displayKm - 21.1f) < 0.2f) Color.White else MockupColors.TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text(RealGoalStrings.halfBtn(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 풀마라톤
                        Button(
                            onClick = {
                                hapticManager?.click()
                                goalSteps = 42.195f * 1300f
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (kotlin.math.abs(displayKm - 42.195f) < 0.2f) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                contentColor = if (kotlin.math.abs(displayKm - 42.195f) < 0.2f) Color.White else MockupColors.TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text(RealGoalStrings.fullBtn(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Action Button
        MockupButton(
            text = RealGoalStrings.start(),
            onClick = {
                hapticManager?.success()
                preferenceManager?.saveGoal(goalSteps.toInt())
                preferenceManager?.saveGoalUnit(selectedUnit)
                onComplete()
            }
        )
    }
}
