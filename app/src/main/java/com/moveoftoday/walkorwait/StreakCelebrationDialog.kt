package com.moveoftoday.walkorwait

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StreakCelebrationDialog(
    streakCount: Int,
    weeklyAchievements: List<Boolean>,
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    // 애니메이션
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        hapticManager?.success()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 캡처할 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(24.dp))
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .background(StandColors.DarkBackground)
                    .padding(StandSpacing.xxl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(StandSpacing.lg))

                    // 불꽃 아이콘 (Teal 그라데이션)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        StandColors.Primary,
                                        StandColors.Primary.copy(alpha = 0.7f),
                                        Color(0xFF00897B)
                                    )
                                ),
                                shape = FireShape()
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 내부 밝은 불꽃
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset(y = 10.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            StandColors.GlowYellow,
                                            StandColors.Primary.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = FireShape()
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(StandSpacing.lg))

                    // 숫자 (크게)
                    Text(
                        text = streakCount.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // "연속 달성!" 텍스트
                    Text(
                        text = "일 연속 달성!",
                        fontSize = StandTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.GlowYellow
                    )

                    Spacer(modifier = Modifier.height(StandSpacing.xxl))

                    // 주간 달성 표시
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(StandSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 요일 헤더
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { index, day ->
                                    Text(
                                        text = day,
                                        fontSize = StandTypography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0 || index == 6)
                                            StandColors.GlowYellow.copy(alpha = 0.8f)
                                        else
                                            Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(StandSpacing.md))

                            // 체크마크 아이콘들
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                weeklyAchievements.forEachIndexed { index, achieved ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = if (achieved) StandColors.GlowYellow else Color.White.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (achieved) {
                                            Text(
                                                text = "✓",
                                                fontSize = StandTypography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = StandColors.DarkBackground
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(StandSpacing.lg))

                            // 응원 메시지
                            Text(
                                text = getEncouragementMessage(streakCount),
                                fontSize = StandTypography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(StandSpacing.lg))

                    // Stand 워터마크
                    Text(
                        text = "Stand",
                        fontSize = StandTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(StandSpacing.md))
                }
            }

            Spacer(modifier = Modifier.height(StandSpacing.lg))

            // 버튼들 (캡처 영역 바깥)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 이미지 저장 버튼
                OutlinedButton(
                    onClick = {
                        hapticManager?.click()
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            saveImageToGallery(context, bitmap, streakCount)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "📷 저장",
                        fontSize = StandTypography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 공유 버튼
                OutlinedButton(
                    onClick = {
                        hapticManager?.click()
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            shareImage(context, bitmap, streakCount)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "📤 공유",
                        fontSize = StandTypography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(StandSpacing.md))

            // 계속하기 버튼
            Button(
                onClick = {
                    hapticManager?.click()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StandColors.Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "계속하기",
                    fontSize = StandTypography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 이미지 갤러리에 저장
private fun saveImageToGallery(context: Context, bitmap: Bitmap, streakCount: Int) {
    try {
        val filename = "Stand_${streakCount}일연속_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 이상
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stand")
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
        } else {
            // Android 9 이하
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val standDir = File(picturesDir, "Stand")
            if (!standDir.exists()) standDir.mkdirs()

            val file = File(standDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            // 갤러리에 스캔 요청
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )
        }

        Toast.makeText(context, "📷 이미지가 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// 이미지 공유
private fun shareImage(context: Context, bitmap: Bitmap, streakCount: Int) {
    try {
        // 임시 파일 생성
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val filename = "Stand_${streakCount}일연속.png"
        val file = File(cachePath, filename)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, """
🏃 Stand - ${streakCount}일 연속 달성!

매일 걷기 목표를 달성하고 있어요!
당신도 함께 걸어볼래요?

📱 https://play.google.com/store/apps/details?id=com.moveoftoday.walkorwait
            """.trimIndent())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "달성 기록 공유하기"))
    } catch (e: Exception) {
        Toast.makeText(context, "공유 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// 응원 메시지 생성
private fun getEncouragementMessage(streakCount: Int): String {
    return when {
        streakCount >= 365 -> "대단해요! 1년 넘게 연속 달성 중!"
        streakCount >= 100 -> "놀라워요! 100일 이상 연속 달성!"
        streakCount >= 30 -> "한 달 넘게 꾸준히 걷고 있어요!"
        streakCount >= 7 -> "일주일 연속 달성! 습관이 되어가고 있어요!"
        streakCount >= 3 -> "잘하고 있어요! 계속 힘내세요!"
        else -> "좋은 시작이에요! 내일도 함께해요!"
    }
}

// 불꽃 모양 Shape
private class FireShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            val width = size.width
            val height = size.height

            // 불꽃 모양 경로
            moveTo(width * 0.5f, 0f)

            // 오른쪽 위 곡선
            cubicTo(
                width * 0.7f, height * 0.15f,
                width * 0.85f, height * 0.25f,
                width * 0.9f, height * 0.45f
            )

            // 오른쪽 아래 곡선
            cubicTo(
                width * 0.95f, height * 0.65f,
                width * 0.85f, height * 0.85f,
                width * 0.5f, height
            )

            // 왼쪽 아래 곡선
            cubicTo(
                width * 0.15f, height * 0.85f,
                width * 0.05f, height * 0.65f,
                width * 0.1f, height * 0.45f
            )

            // 왼쪽 위 곡선
            cubicTo(
                width * 0.15f, height * 0.25f,
                width * 0.3f, height * 0.15f,
                width * 0.5f, 0f
            )

            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}
