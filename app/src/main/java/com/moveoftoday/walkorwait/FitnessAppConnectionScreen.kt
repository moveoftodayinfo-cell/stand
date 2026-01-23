package com.moveoftoday.walkorwait

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.rememberKenneyFont

@Composable
fun FitnessAppConnectionScreen(
    onBack: () -> Unit = {},
    onConnectionComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val hapticManager = remember { HapticManager(context) }
    val scope = rememberCoroutineScope()
    val kenneyFont = rememberKenneyFont()

    var installedApps by remember { mutableStateOf<List<FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(preferenceManager.isHealthConnectConnected()) }
    var isConnecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var connectionSuccess by remember { mutableStateOf(false) }
    var selectedAppName by remember { mutableStateOf("") }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.createPermissionRequestContract()
    ) { granted ->
        scope.launch {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                errorMessage = null
                connectionSuccess = true
                isConnected = true
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(selectedAppName)

                StepCounterService.stop(context)
                StepCounterService.start(context)
            } else {
                errorMessage = "권한이 필요합니다"
            }
            isConnecting = false
        }
    }

    // 초기화
    LaunchedEffect(Unit) {
        isHealthConnectAvailable = healthConnectManager.isAvailable()
        installedApps = healthConnectManager.getInstalledFitnessApps()

        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions && preferenceManager.isHealthConnectConnected()) {
                selectedAppName = preferenceManager.getConnectedFitnessAppName()
            }
        }
    }

    // 튜토리얼과 동일한 화이트 레트로 스타일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MockupColors.CardBackground)
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
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
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
                        text = "피트니스 앱 연결",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        fontFamily = kenneyFont
                    )
                }
            }

            // 메인 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 부츠 아이콘
                PixelIcon(iconName = "icon_boots", size = 80.dp)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "정확한 걸음 측정을 위해\n피트니스 앱을 연결하세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "사용 중인 앱의 데이터를 사용하여\n더 정확하고 공정한 측정이 가능합니다",
                    fontSize = 14.sp,
                    color = MockupColors.TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Health Connect 사용 불가 상태
                if (!isHealthConnectAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                            .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                PixelIcon(iconName = "icon_dialog", size = 24.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Health Connect 필요",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Red,
                                    fontFamily = kenneyFont
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "피트니스 앱과 연결하려면\nHealth Connect 앱이 필요합니다",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = MockupColors.TextSecondary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "(Android 14 이상은 기본 내장)",
                                fontSize = 12.sp,
                                color = MockupColors.TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Play Store 버튼
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                    .background(MockupColors.Red, RoundedCornerShape(10.dp))
                                    .clickable {
                                        hapticManager.click()
                                        healthConnectManager.openHealthConnectPlayStore()
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Play Store에서 설치",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = kenneyFont
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "나중에 하기 (기본 센서 사용)",
                                fontSize = 14.sp,
                                color = MockupColors.TextMuted,
                                modifier = Modifier
                                    .clickable {
                                        hapticManager.click()
                                        onBack()
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                // Health Connect 사용 가능 상태
                else {
                    // 연결된 상태 (preferenceManager 기준)
                    if (isConnected) {
                        val connectedAppName = preferenceManager.getConnectedFitnessAppName()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                                .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                                .padding(20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PixelIcon(iconName = "icon_star", size = 24.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "연결 완료!",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Blue,
                                        fontFamily = kenneyFont
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (connectedAppName.isNotEmpty())
                                        "$connectedAppName 와 연결되었습니다"
                                    else
                                        "피트니스 앱이 성공적으로 연결되었습니다",
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    color = MockupColors.TextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "배터리 절약 모드 활성화",
                                    fontSize = 14.sp,
                                    color = MockupColors.Blue,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 앱 재시작 안내
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                        .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "변경사항을 적용하려면 앱을 재시작하세요",
                                        fontSize = 13.sp,
                                        color = MockupColors.Red,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 앱 재시작 버튼
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                                        .background(MockupColors.Blue, RoundedCornerShape(10.dp))
                                        .clickable {
                                            hapticManager.success()
                                            val activity = context as? android.app.Activity
                                            activity?.let {
                                                val intent = it.packageManager.getLaunchIntentForPackage(it.packageName)
                                                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                it.startActivity(intent)
                                                it.finish()
                                            }
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "앱 재시작하기",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = kenneyFont
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 연결 해제 버튼
                                Text(
                                    text = "연결 해제",
                                    fontSize = 14.sp,
                                    color = MockupColors.TextMuted,
                                    modifier = Modifier
                                        .clickable {
                                            hapticManager.warning()
                                            preferenceManager.disconnectHealthConnect()
                                            isConnected = false

                                            StepCounterService.stop(context)
                                            StepCounterService.start(context)

                                            android.widget.Toast.makeText(
                                                context,
                                                "연결이 해제되었습니다",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                    // 연결 필요
                    else {
                        // 설치된 앱 목록
                        if (installedApps.isNotEmpty()) {
                            Text(
                                text = "발견된 피트니스 앱",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                            installedApps.forEach { app ->
                                RetroFitnessAppCard(
                                    app = app,
                                    isInstalled = true,
                                    isConnecting = isConnecting,
                                    kenneyFont = kenneyFont,
                                    hapticManager = hapticManager,
                                    onConnect = {
                                        isConnecting = true
                                        errorMessage = null
                                        selectedAppName = app.appName
                                        permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 설치되지 않은 앱
                        val notInstalledApps = FitnessApp.values().filter { it !in installedApps }
                        if (notInstalledApps.isNotEmpty()) {
                            Text(
                                text = "설치 가능한 앱",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                            notInstalledApps.forEach { app ->
                                RetroFitnessAppCard(
                                    app = app,
                                    isInstalled = false,
                                    isConnecting = false,
                                    kenneyFont = kenneyFont,
                                    hapticManager = hapticManager,
                                    onConnect = {}
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 에러 메시지
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                    .background(MockupColors.RedLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = MockupColors.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MockupColors.Border.copy(alpha = 0.2f),
                            thickness = 2.dp
                        )

                        // 나중에 하기
                        Text(
                            text = "나중에 하기 (기본 센서 사용)",
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted,
                            modifier = Modifier
                                .clickable {
                                    hapticManager.click()
                                    onBack()
                                }
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "* 기본 센서는 덜 정확하며 부정 방지 기능이 제한됩니다",
                            fontSize = 12.sp,
                            color = MockupColors.TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun RetroFitnessAppCard(
    app: FitnessApp,
    isInstalled: Boolean,
    isConnecting: Boolean = false,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    hapticManager: HapticManager,
    onConnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(
                width = 2.dp,
                color = if (isInstalled) MockupColors.Border else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                Color.White,
                RoundedCornerShape(12.dp)
            )
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
                // 앱 아이콘 - 앱 고유 색상 배경
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(app.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.icon,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 앱 정보
                Column {
                    Text(
                        text = app.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = if (isInstalled) "설치됨" else "설치 필요",
                        fontSize = 12.sp,
                        color = if (isInstalled) MockupColors.Blue else MockupColors.TextMuted
                    )
                }
            }

            // 연결 버튼
            if (isInstalled) {
                Box(
                    modifier = Modifier
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(
                            if (isConnecting) MockupColors.TextMuted else app.color,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isConnecting) {
                            hapticManager.click()
                            onConnect()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isConnecting) "연결 중..." else "연결",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "설치 필요",
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )
                }
            }
        }
    }
}

// Keep the old FitnessAppCard for backward compatibility
@Composable
fun FitnessAppCard(
    app: FitnessApp,
    isInstalled: Boolean,
    isConnecting: Boolean = false,
    onConnect: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    RetroFitnessAppCard(
        app = app,
        isInstalled = isInstalled,
        isConnecting = isConnecting,
        kenneyFont = kenneyFont,
        hapticManager = hapticManager,
        onConnect = onConnect
    )
}
