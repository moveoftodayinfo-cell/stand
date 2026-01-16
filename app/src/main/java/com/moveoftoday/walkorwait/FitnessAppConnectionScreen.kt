package com.moveoftoday.walkorwait

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessAppConnectionScreen(
    onBack: () -> Unit = {},
    onConnectionComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf<List<FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
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
                // 연결 성공 시 설정 저장
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(selectedAppName)

                // 서비스 재시작하여 Health Connect 모드로 전환
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
        // Health Connect 사용 가능 여부 체크
        isHealthConnectAvailable = healthConnectManager.isAvailable()

        // 설치된 피트니스 앱 목록은 항상 가져오기 (Health Connect 여부와 무관)
        installedApps = healthConnectManager.getInstalledFitnessApps()

        // Health Connect 사용 가능하면 권한 체크
        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            // 이미 연결된 상태면 연결 앱 이름 불러오기
            if (hasPermissions && preferenceManager.isHealthConnectConnected()) {
                selectedAppName = preferenceManager.getConnectedFitnessAppName()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("피트니스 앱 연결", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로가기", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StandColors.DarkBackground
                )
            )
        },
        containerColor = StandColors.DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StandColors.DarkBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 헤더
            Text(
                text = "🏃",
                fontSize = StandTypography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "정확한 걸음 측정을 위해\n피트니스 앱을 연결하세요",
                fontSize = StandTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "사용 중인 앱의 데이터를 사용하여\n더 정확하고 공정한 측정이 가능합니다",
                fontSize = StandTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Health Connect 사용 불가 상태
            if (!isHealthConnectAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StandSpacing.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.Warning.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(StandSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Health Connect 필요",
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = StandColors.Warning,
                            modifier = Modifier.padding(bottom = StandSpacing.md)
                        )

                        Text(
                            text = "피트니스 앱과 연결하려면\nHealth Connect 앱이 필요합니다",
                            fontSize = StandTypography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = StandSpacing.sm)
                        )

                        Text(
                            text = "(Android 14 이상은 기본 내장)",
                            fontSize = StandTypography.labelLarge,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = StandSpacing.lg)
                        )

                        Button(
                            onClick = { healthConnectManager.openHealthConnectPlayStore() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StandColors.Warning
                            )
                        ) {
                            Text("Play Store에서 설치")
                        }

                        Spacer(modifier = Modifier.height(StandSpacing.sm))

                        TextButton(onClick = onBack) {
                            Text("나중에 하기 (기본 센서 사용)", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            // Health Connect 사용 가능 상태
            else {
                // 연결된 상태
                if (hasPermissions) {
                    val connectedAppName = preferenceManager.getConnectedFitnessAppName()

                    StatusCard(
                        statusType = StatusType.SUCCESS,
                        modifier = Modifier.padding(bottom = StandSpacing.lg)
                    ) {
                        Text(
                            text = "✅ 연결 완료!",
                            fontSize = StandTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StandColors.Success,
                            modifier = Modifier.padding(bottom = StandSpacing.sm)
                        )

                        Text(
                            text = if (connectedAppName.isNotEmpty())
                                "$connectedAppName 와 연결되었습니다"
                            else
                                "피트니스 앱이 성공적으로 연결되었습니다",
                            fontSize = StandTypography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = StandSpacing.sm)
                        )

                        Text(
                            text = "🔋 기본 센서가 비활성화되어 배터리가 절약됩니다",
                            fontSize = StandTypography.bodySmall,
                            color = StandColors.Success,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = StandSpacing.md)
                        )

                        // 앱 재시작 안내
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = StandSpacing.md),
                            colors = CardDefaults.cardColors(
                                containerColor = StandColors.Warning.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = "⚠️ 변경사항을 적용하려면 앱을 재시작하세요",
                                fontSize = StandTypography.bodySmall,
                                color = StandColors.Warning,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                // 앱 재시작
                                val activity = context as? android.app.Activity
                                activity?.let {
                                    val intent = it.packageManager.getLaunchIntentForPackage(it.packageName)
                                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    it.startActivity(intent)
                                    it.finish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StandColors.Success
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("앱 재시작하기")
                        }

                        Spacer(modifier = Modifier.height(StandSpacing.sm))

                        // 연결 해제 버튼
                        TextButton(
                            onClick = {
                                preferenceManager.disconnectHealthConnect()
                                hasPermissions = false

                                // 서비스 재시작하여 기본 센서 모드로 전환
                                StepCounterService.stop(context)
                                StepCounterService.start(context)

                                android.widget.Toast.makeText(
                                    context,
                                    "🔌 연결이 해제되었습니다. 기본 센서를 사용합니다.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            Text(
                                "연결 해제",
                                color = Color.Gray,
                                fontSize = StandTypography.bodySmall
                            )
                        }
                    }
                }
                // 연결 필요
                else {
                    // 설치된 앱 목록
                    if (installedApps.isNotEmpty()) {
                        Text(
                            text = "사용 중인 앱",
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )

                        installedApps.forEach { app ->
                            FitnessAppCard(
                                app = app,
                                isInstalled = true,
                                isConnecting = isConnecting,
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
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )

                        notInstalledApps.forEach { app ->
                            FitnessAppCard(
                                app = app,
                                isInstalled = false,
                                isConnecting = false,
                                onConnect = {}
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 에러 메시지
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = StandColors.Error,
                            fontSize = StandTypography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    // 나중에 하기
                    TextButton(onClick = onBack) {
                        Text("나중에 하기 (기본 센서 사용)", color = Color.White.copy(alpha = 0.7f))
                    }

                    Text(
                        text = "* 기본 센서는 덜 정확하며 부정 방지 기능이 제한됩니다",
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FitnessAppCard(
    app: FitnessApp,
    isInstalled: Boolean,
    isConnecting: Boolean = false,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled)
                Color.White.copy(alpha = 0.1f)
            else
                Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 앱 아이콘
                Text(
                    text = app.icon,
                    fontSize = StandTypography.headlineLarge,
                    modifier = Modifier.padding(end = 16.dp)
                )

                // 앱 정보
                Column {
                    Text(
                        text = app.appName,
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isInstalled) "✓ 설치됨" else "설치 필요",
                        fontSize = StandTypography.labelLarge,
                        color = if (isInstalled) StandColors.Success else Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            // 연결 버튼
            if (isInstalled) {
                Button(
                    onClick = onConnect,
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = app.color
                    )
                ) {
                    Text(if (isConnecting) "연결 중..." else "연결하기")
                }
            } else {
                OutlinedButton(
                    onClick = { /* Play Store 이동 로직 추가 가능 */ },
                    enabled = false,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Text("설치 필요")
                }
            }
        }
    }
}
