package com.moveoftoday.walkorwait

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing

@Composable
fun WidgetRecommendationDialog(
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.DarkBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(StandSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘/이모지
                Text(
                    text = "📱",
                    fontSize = StandTypography.displaySmall
                )

                Spacer(modifier = Modifier.height(StandSpacing.lg))

                // 제목
                Text(
                    text = "위젯 추가하기",
                    fontSize = StandTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(StandSpacing.md))

                // 설명
                Text(
                    text = "홈 화면에 위젯을 추가하면\n앱을 열지 않고도 걸음 수를 확인할 수 있어요!",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = StandTypography.bodyLarge
                )

                Spacer(modifier = Modifier.height(StandSpacing.xl))

                // 위젯 미리보기 (간단한 박스)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = StandColors.Primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "rebon",
                            fontSize = StandTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "0 걸음",
                            fontSize = StandTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "0%",
                            fontSize = StandTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = StandColors.GlowYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(StandSpacing.xxl))

                // 버튼들
                Button(
                    onClick = {
                        hapticManager?.success()
                        // 위젯 추가 요청
                        requestWidgetPin(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StandColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "위젯 추가하기",
                        fontSize = StandTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(StandSpacing.sm))

                TextButton(
                    onClick = {
                        hapticManager?.click()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "나중에 할게요",
                        fontSize = StandTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * 위젯 고정(Pin) 요청
 * Android 8.0 이상에서 지원
 */
private fun requestWidgetPin(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, StepWidgetProvider::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
        } else {
            // 지원하지 않는 경우 위젯 설정 화면으로 이동
            openWidgetSettings(context)
        }
    } else {
        // Android 8.0 미만에서는 위젯 설정 안내
        openWidgetSettings(context)
    }
}

/**
 * 위젯 설정 화면 열기 (지원하지 않는 기기용)
 */
private fun openWidgetSettings(context: android.content.Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        // 실패 시 무시
    }
}
