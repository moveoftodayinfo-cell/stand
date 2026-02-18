package com.moveoftoday.walkorwait.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.launch
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsSupportStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun submitFeedback(): String = when (getLang()) {
        "ko" -> "불편사항 접수"
        "ja" -> "フィードバック送信"
        "zh" -> "提交反馈"
        "es" -> "Enviar feedback"
        else -> "Submit Feedback"
    }

    fun sendFeedback(): String = when (getLang()) {
        "ko" -> "피드백 보내기"
        "ja" -> "フィードバックを送信"
        "zh" -> "发送反馈"
        "es" -> "Enviar feedback"
        else -> "Send Feedback"
    }

    fun feedbackDescription(): String = when (getLang()) {
        "ko" -> "불편사항이나 개선 의견을 알려주세요"
        "ja" -> "ご不便な点や改善のご意見をお聞かせください"
        "zh" -> "请告诉我们您的问题或改进建议"
        "es" -> "Cuéntanos tus problemas o sugerencias"
        else -> "Let us know your issues or suggestions"
    }

    fun appInfo(): String = when (getLang()) {
        "ko" -> "앱 정보"
        "ja" -> "アプリ情報"
        "zh" -> "应用信息"
        "es" -> "Información de la app"
        else -> "App Info"
    }

    fun appName(): String = when (getLang()) {
        "ko" -> "앱 이름"
        "ja" -> "アプリ名"
        "zh" -> "应用名称"
        "es" -> "Nombre de la app"
        else -> "App Name"
    }

    fun version(): String = when (getLang()) {
        "ko" -> "버전"
        "ja" -> "バージョン"
        "zh" -> "版本"
        "es" -> "Versión"
        else -> "Version"
    }

    fun build(): String = when (getLang()) {
        "ko" -> "빌드"
        "ja" -> "ビルド"
        "zh" -> "构建"
        "es" -> "Build"
        else -> "Build"
    }

    fun links(): String = when (getLang()) {
        "ko" -> "링크"
        "ja" -> "リンク"
        "zh" -> "链接"
        "es" -> "Enlaces"
        else -> "Links"
    }

    fun privacyPolicy(): String = when (getLang()) {
        "ko" -> "개인정보 처리방침"
        "ja" -> "プライバシーポリシー"
        "zh" -> "隐私政策"
        "es" -> "Política de privacidad"
        else -> "Privacy Policy"
    }

    fun termsOfService(): String = when (getLang()) {
        "ko" -> "이용약관"
        "ja" -> "利用規約"
        "zh" -> "服务条款"
        "es" -> "Términos de servicio"
        else -> "Terms of Service"
    }

    fun openSourceLicenses(): String = when (getLang()) {
        "ko" -> "오픈소스 라이선스"
        "ja" -> "オープンソースライセンス"
        "zh" -> "开源许可"
        "es" -> "Licencias de código abierto"
        else -> "Open Source Licenses"
    }

    fun feedbackDialogTitle(): String = when (getLang()) {
        "ko" -> "피드백 보내기"
        "ja" -> "フィードバックを送信"
        "zh" -> "发送反馈"
        "es" -> "Enviar feedback"
        else -> "Send Feedback"
    }

    fun feedbackDialogDescription(): String = when (getLang()) {
        "ko" -> "불편사항이나 개선 의견을 자유롭게 작성해주세요."
        "ja" -> "ご不便な点や改善のご意見を自由にお書きください。"
        "zh" -> "请自由填写您的问题或改进建议。"
        "es" -> "Escribe libremente tus problemas o sugerencias."
        else -> "Please freely write your issues or suggestions."
    }

    fun contentPlaceholder(): String = when (getLang()) {
        "ko" -> "내용을 입력하세요..."
        "ja" -> "内容を入力してください..."
        "zh" -> "请输入内容..."
        "es" -> "Ingresa el contenido..."
        else -> "Enter content..."
    }

    fun cancel(): String = when (getLang()) {
        "ko" -> "취소"
        "ja" -> "キャンセル"
        "zh" -> "取消"
        "es" -> "Cancelar"
        else -> "Cancel"
    }

    fun send(): String = when (getLang()) {
        "ko" -> "보내기"
        "ja" -> "送信"
        "zh" -> "发送"
        "es" -> "Enviar"
        else -> "Send"
    }

    fun userFeedback(): String = when (getLang()) {
        "ko" -> "사용자 피드백"
        "ja" -> "ユーザーフィードバック"
        "zh" -> "用户反馈"
        "es" -> "Feedback de usuario"
        else -> "User Feedback"
    }

    fun feedbackSent(): String = when (getLang()) {
        "ko" -> "피드백이 전송되었습니다!"
        "ja" -> "フィードバックを送信しました！"
        "zh" -> "反馈已发送！"
        "es" -> "¡Feedback enviado!"
        else -> "Feedback sent!"
    }
}

/**
 * 고객 지원 화면 (불편사항, 버전 정보)
 */
@Composable
fun SettingsSupportScreen(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    // 다이얼로그 상태
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "support",
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
                    .navigationBarsPadding()
            ) {
                // ========== 불편사항 접수 ==========
                RetroSectionTitle(SettingsSupportStrings.submitFeedback(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager.click()
                            showFeedbackDialog = true
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
                                text = SettingsSupportStrings.sendFeedback(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = SettingsSupportStrings.feedbackDescription(),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        PixelIcon(
                            iconName = "icon_comment",
                            size = 28.dp,
                            tint = MockupColors.TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 앱 정보 ==========
                RetroSectionTitle(SettingsSupportStrings.appInfo(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        InfoRow(SettingsSupportStrings.appName(), "rebon", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(SettingsSupportStrings.version(), BuildConfig.VERSION_NAME, kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(SettingsSupportStrings.build(), BuildConfig.VERSION_CODE.toString(), kenneyFont)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 링크 ==========
                RetroSectionTitle(SettingsSupportStrings.links(), kenneyFont)

                // 개인정보 처리방침
                LinkCard(
                    title = SettingsSupportStrings.privacyPolicy(),
                    onClick = {
                        hapticManager.click()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stand-64c11.web.app/privacy.html"))
                        context.startActivity(intent)
                    },
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 이용약관
                LinkCard(
                    title = SettingsSupportStrings.termsOfService(),
                    onClick = {
                        hapticManager.click()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stand-64c11.web.app/terms.html"))
                        context.startActivity(intent)
                    },
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 오픈소스 라이선스
                LinkCard(
                    title = SettingsSupportStrings.openSourceLicenses(),
                    onClick = {
                        hapticManager.click()
                        Toast.makeText(context, SettingsSupportStrings.openSourceLicenses(), Toast.LENGTH_SHORT).show()
                    },
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 저작권
                Text(
                    text = "© 2024 moveoftoday. All rights reserved.",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // 피드백 다이얼로그
        if (showFeedbackDialog) {
            FeedbackDialog(
                onDismiss = { showFeedbackDialog = false },
                onSubmit = { feedback ->
                    // 피드백 전송 로직
                    scope.launch {
                        FeedbackManager.submitFeedback(
                            context = context,
                            category = FeedbackManager.Category.OTHER,
                            title = SettingsSupportStrings.userFeedback(),
                            content = feedback
                        )
                    }
                    showFeedbackDialog = false
                    hapticManager.success()
                    Toast.makeText(context, SettingsSupportStrings.feedbackSent(), Toast.LENGTH_SHORT).show()
                },
                hapticManager = hapticManager
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MockupColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            fontFamily = kenneyFont
        )
    }
}

@Composable
private fun LinkCard(
    title: String,
    onClick: () -> Unit,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MockupColors.TextPrimary
            )
            Text(
                text = ">",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.Border,
                fontFamily = kenneyFont
            )
        }
    }
}

/**
 * 피드백 다이얼로그
 */
@Composable
private fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    hapticManager: HapticManager
) {
    val kenneyFont = rememberKenneyFont()
    var feedbackText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.Background, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = SettingsSupportStrings.feedbackDialogTitle(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = SettingsSupportStrings.feedbackDialogDescription(),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text(SettingsSupportStrings.contentPlaceholder()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                            .clickable {
                                hapticManager.click()
                                onDismiss()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsSupportStrings.cancel(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .background(MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .clickable(enabled = feedbackText.isNotBlank()) {
                                hapticManager.success()
                                onSubmit(feedbackText)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SettingsSupportStrings.send(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
