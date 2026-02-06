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

/**
 * 알림 타입
 */
enum class NotificationType {
    WARNING,      // 경고 (접근성, Health Connect 등)
    UPDATE,       // 앱 업데이트
    ANNOUNCEMENT, // 공지사항
    EVENT         // 이벤트/프로모션
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
 * 알림 아이콘 (유니코드 텍스트 심볼)
 */
object NotificationSymbols {
    const val WARNING = "△"       // 경고 삼각형
    const val UPDATE = "↑"        // 업데이트 화살표
    const val ANNOUNCEMENT = "☆"  // 공지 별
    const val EVENT = "◈"         // 이벤트 다이아몬드
    const val CLOSE = "×"         // 닫기
}

/**
 * 알림 체크 및 생성
 */
@Composable
fun rememberNotifications(
    context: Context = LocalContext.current,
    preferenceManager: PreferenceManager?
): List<NotificationItem> {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    // Health Connect 매니저
    val healthConnectManager = remember { HealthConnectManager(context) }

    // 주기적으로 상태 체크
    LaunchedEffect(Unit) {
        while (true) {
            val items = mutableListOf<NotificationItem>()

            // 1. 접근성 서비스 체크
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

            // 2. Health Connect 권한 체크
            val useHealthConnect = preferenceManager?.useHealthConnect() ?: false
            val isHealthConnectAvailable = healthConnectManager.isAvailable()

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

            // 3. 앱 업데이트 체크 (Firebase Remote Config에서 가져옴)
            // AppUpdateManager에서 처리하므로 여기서는 스킵

            // 4. Firebase 공지사항 체크
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
    }

    val symbol = when (notification.type) {
        NotificationType.WARNING -> NotificationSymbols.WARNING
        NotificationType.UPDATE -> NotificationSymbols.UPDATE
        NotificationType.ANNOUNCEMENT -> NotificationSymbols.ANNOUNCEMENT
        NotificationType.EVENT -> NotificationSymbols.EVENT
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
