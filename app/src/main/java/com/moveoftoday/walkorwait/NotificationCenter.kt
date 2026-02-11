package com.moveoftoday.walkorwait

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState

/**
 * 알림 타입
 */
enum class NotificationType {
    WARNING,      // 경고 (접근성, Health Connect 등)
    UPDATE,       // 앱 업데이트
    ANNOUNCEMENT, // 공지사항
    EVENT,        // 이벤트/프로모션
    STATUS        // 상태 정보 (센서, 챌린지 등)
}

/**
 * 알림 데이터
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val subtitle: String,
    val action: NotificationAction? = null,
    val dismissible: Boolean = true  // 경고는 해결 전까지 계속 표시
)

/**
 * 알림 액션
 */
sealed class NotificationAction {
    data class OpenUrl(val url: String) : NotificationAction()
    data class OpenSettings(val settingsAction: String) : NotificationAction()
    object OpenAppSettings : NotificationAction()
    object OpenPlayStore : NotificationAction()
}

/**
 * 알림 아이콘 (유니코드 텍스트 심볼 - 텍스트 스타일 강제)
 */
object NotificationSymbols {
    private const val TEXT_STYLE = "\uFE0E"  // 텍스트 스타일 셀렉터 (모노크롬)
    const val WARNING = "⚠$TEXT_STYLE"       // 경고
    const val UPDATE = "⬆$TEXT_STYLE"        // 업데이트 화살표
    const val ANNOUNCEMENT = "☆$TEXT_STYLE"  // 공지 별
    const val EVENT = "◆$TEXT_STYLE"         // 이벤트 다이아몬드
    const val STATUS = "ℹ$TEXT_STYLE"        // 상태 정보
    const val CLOSE = "✕"                    // 닫기
}

/**
 * 알림 체크 및 생성
 */
@Composable
fun rememberNotifications(
    context: Context = LocalContext.current,
    preferenceManager: PreferenceManager?,
    stepSensorManager: StepSensorManager? = null,
    challengeManager: ChallengeManager? = null
): List<NotificationItem> {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    // Health Connect 매니저
    val healthConnectManager = remember { HealthConnectManager(context) }

    // 백그라운드 챌린지 상태 구독
    val backgroundProgress = challengeManager?.backgroundProgress?.collectAsState()

    // 주기적으로 상태 체크
    LaunchedEffect(Unit, backgroundProgress?.value) {
        while (true) {
            val items = mutableListOf<NotificationItem>()

            // ========== 상태 정보 (STATUS) ==========

            // 1. 백그라운드 챌린지 상태 (간헐적 단식)
            val bgProgress = backgroundProgress?.value
            if (bgProgress != null && bgProgress.status == ChallengeStatus.RUNNING) {
                val elapsedMinutes = bgProgress.elapsedSeconds / 60
                val totalMinutes = (bgProgress.challenge.durationMinutes * 60).toInt() / 60
                val elapsedHours = elapsedMinutes / 60
                val elapsedMins = elapsedMinutes % 60
                val totalHours = totalMinutes / 60

                items.add(
                    NotificationItem(
                        id = "background_challenge",
                        type = NotificationType.STATUS,
                        title = "${bgProgress.challenge.name} 진행 중",
                        subtitle = "${elapsedHours}시간 ${elapsedMins}분 / ${totalHours}시간",
                        action = null,
                        dismissible = false
                    )
                )
            }

            // 2. 현재 센서 상태 (직접 확인)
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val useHealthConnect = preferenceManager?.useHealthConnect() ?: false
            val isHealthConnectAvailable = healthConnectManager.isAvailable()
            val hasHealthConnectPermissions = try {
                if (useHealthConnect && isHealthConnectAvailable) {
                    healthConnectManager.hasAllPermissions()
                } else false
            } catch (e: Exception) { false }

            val (sensorTitle, sensorSubtitle) = when {
                // Health Connect: 설정 + 사용가능 + 권한 있어야 함
                useHealthConnect && isHealthConnectAvailable && hasHealthConnectPermissions ->
                    "Health Connect" to "연결된 앱에서 걸음수 측정"
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null ->
                    "기본 센서" to "기기 내장 센서로 걸음수 측정"
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR) != null ->
                    "스텝 감지기" to "기기 내장 센서로 걸음수 측정"
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER) != null ->
                    "가속도계" to "기기 내장 센서로 걸음수 측정"
                else ->
                    "센서 없음" to "걸음수를 측정할 수 없습니다"
            }

            items.add(
                NotificationItem(
                    id = "sensor_status",
                    type = NotificationType.STATUS,
                    title = sensorTitle,
                    subtitle = sensorSubtitle,
                    action = null,
                    dismissible = false
                )
            )

            // ========== 경고 (WARNING) ==========

            // 3. 접근성 서비스 체크
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val isAccessibilityEnabled = enabledServices?.contains("com.moveoftoday.walkorwait") == true

            if (!isAccessibilityEnabled) {
                items.add(
                    NotificationItem(
                        id = "accessibility_disabled",
                        type = NotificationType.WARNING,
                        title = "접근성 서비스 비활성화",
                        subtitle = "앱 잠금 기능을 사용하려면 활성화하세요",
                        action = NotificationAction.OpenSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                        dismissible = false
                    )
                )
            }

            // 4. Health Connect 권한 체크 (위에서 선언한 변수 재사용)
            if (useHealthConnect && isHealthConnectAvailable) {
                val hasPermissions = try {
                    healthConnectManager.hasAllPermissions()
                } catch (e: Exception) {
                    false
                }

                if (!hasPermissions) {
                    items.add(
                        NotificationItem(
                            id = "health_connect_permission",
                            type = NotificationType.WARNING,
                            title = "Health Connect 권한 필요",
                            subtitle = "걸음수 측정을 위해 권한을 허용하세요",
                            action = NotificationAction.OpenUrl("market://details?id=com.google.android.apps.healthdata"),
                            dismissible = false
                        )
                    )
                }
            }

            // 5. 앱 업데이트 체크 (Firebase Remote Config에서 가져옴)
            // AppUpdateManager에서 처리하므로 여기서는 스킵

            // 6. Firebase 공지사항 체크
            // TODO: Firebase announcements 연동

            notifications = items
            delay(3000)  // 3초마다 체크
        }
    }

    return notifications
}

/**
 * 알림 센터 패널
 */
@Composable
fun NotificationCenterPanel(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    hapticManager: HapticManager? = null
) {
    val kenneyFont = rememberKenneyFont()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // 알림 패널 (상단 우측)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 16.dp)
                .width(300.dp)
                .clickable(enabled = false) {}  // 패널 클릭 시 닫히지 않도록
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .background(MockupColors.Background, RoundedCornerShape(12.dp))
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MockupColors.CardBackground, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "알림",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )
                Text(
                    text = NotificationSymbols.CLOSE,
                    fontSize = 20.sp,
                    color = MockupColors.TextMuted,
                    modifier = Modifier.clickable {
                        hapticManager?.click()
                        onDismiss()
                    }
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MockupColors.Border)
            )

            // 알림 목록
            if (notifications.isEmpty()) {
                // 알림 없음
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "○",
                            fontSize = 32.sp,
                            color = MockupColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "알림이 없습니다",
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notifications.forEach { notification ->
                        NotificationItemCard(
                            notification = notification,
                            onClick = {
                                hapticManager?.click()
                                onNotificationClick(notification)
                            },
                            kenneyFont = kenneyFont
                        )
                    }
                }
            }
        }
    }
}

/**
 * 개별 알림 카드
 */
@Composable
private fun NotificationItemCard(
    notification: NotificationItem,
    onClick: () -> Unit,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    val (borderColor, bgColor, symbolColor) = when (notification.type) {
        NotificationType.WARNING -> Triple(MockupColors.Red, MockupColors.RedLight, MockupColors.Red)
        NotificationType.UPDATE -> Triple(MockupColors.Blue, MockupColors.BlueLight, MockupColors.Blue)
        NotificationType.ANNOUNCEMENT -> Triple(MockupColors.Purple, Color(0xFFF3E8FF), MockupColors.Purple)
        NotificationType.EVENT -> Triple(MockupColors.Green, MockupColors.GreenLight, MockupColors.Green)
        NotificationType.STATUS -> Triple(MockupColors.Border, MockupColors.CardBackground, MockupColors.TextMuted)
    }

    val symbol = when (notification.type) {
        NotificationType.WARNING -> NotificationSymbols.WARNING
        NotificationType.UPDATE -> NotificationSymbols.UPDATE
        NotificationType.ANNOUNCEMENT -> NotificationSymbols.ANNOUNCEMENT
        NotificationType.EVENT -> NotificationSymbols.EVENT
        NotificationType.STATUS -> NotificationSymbols.STATUS
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // 심볼
            Text(
                text = symbol,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = symbolColor,
                modifier = Modifier.padding(end = 12.dp)
            )

            // 내용
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.subtitle,
                    fontSize = 12.sp,
                    color = MockupColors.TextSecondary
                )
            }

            // 화살표
            Text(
                text = ">",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = borderColor,
                fontFamily = kenneyFont
            )
        }
    }
}

/**
 * 알림 액션 처리
 */
fun handleNotificationAction(context: Context, notification: NotificationItem) {
    when (val action = notification.action) {
        is NotificationAction.OpenUrl -> {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
            context.startActivity(intent)
        }
        is NotificationAction.OpenSettings -> {
            val intent = Intent(action.settingsAction)
            context.startActivity(intent)
        }
        is NotificationAction.OpenAppSettings -> {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
        is NotificationAction.OpenPlayStore -> {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Play Store 앱이 없으면 웹으로
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                }
                context.startActivity(webIntent)
            }
        }
        null -> { /* 액션 없음 */ }
    }
}
