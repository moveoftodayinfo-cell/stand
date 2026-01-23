package com.moveoftoday.walkorwait

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.rememberKenneyFont

/**
 * 서버 제어 공지 다이얼로그
 * 깔끔한 레트로 스타일 (3색 시스템)
 */
@Composable
fun AnnouncementDialog(
    announcement: AnnouncementManager.Announcement,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()

    // 타입별 강조색
    val accentColor = when (announcement.type) {
        "update" -> MockupColors.Blue
        "event" -> MockupColors.Blue
        "notice" -> MockupColors.TextPrimary
        else -> MockupColors.TextPrimary
    }

    Dialog(
        onDismissRequest = {
            if (announcement.dismissible) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = announcement.dismissible,
            dismissOnClickOutside = announcement.dismissible
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MockupColors.Background)
                .border(2.dp, MockupColors.Border, RoundedCornerShape(16.dp))
        ) {
            // 헤더 (rebon 로고)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MockupColors.CardBackground)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "rebon",
                    fontSize = 16.sp,
                    fontFamily = kenneyFont,
                    color = MockupColors.TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MockupColors.Border)
            )

            // 이미지 (있을 경우)
            if (!announcement.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(announcement.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "공지 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )
                // 이미지 아래 구분선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MockupColors.Border)
                )
            }

            // 컨텐츠 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = announcement.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 메시지
                Text(
                    text = announcement.message,
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 메인 버튼
                Button(
                    onClick = {
                        hapticManager?.click()
                        when (announcement.primaryButtonAction) {
                            "url" -> {
                                announcement.primaryButtonUrl?.let { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            }
                            "update" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id=${context.packageName}"
                                ))
                                context.startActivity(intent)
                            }
                        }
                        onPrimaryAction()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = announcement.primaryButtonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 오늘 그만보기 버튼 (dismissible일 때만)
                if (announcement.dismissible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            hapticManager?.click()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "오늘 그만보기",
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}
