package com.moveoftoday.walkorwait

import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun RepBasedChallengeDialog(
    progress: ChallengeProgress,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onDebugComplete: () -> Unit = {}
) {
    // 완료 상태 처리
    LaunchedEffect(progress.status) {
        if (progress.status == ChallengeStatus.COMPLETED) {
            onComplete()
        }
    }

    Dialog(
        onDismissRequest = { /* 외부 클릭 무시 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(32.dp, 32.dp, 32.dp, 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val kenneyFont = rememberKenneyFont()

                // 헤더
                Text(
                    text = "rebon challenge",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 챌린지 이름
                Text(
                    text = progress.challenge.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 획득 칭호
                Text(
                    text = "획득 칭호: \"${progress.challenge.type.title}\"",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 상태별 UI
                when (progress.status) {
                    ChallengeStatus.NOT_STARTED -> {
                        // 운동 자세 안내
                        ExerciseInstructions(progress.challenge.type)

                        Spacer(modifier = Modifier.height(24.dp))

                        // 시작 버튼
                        MockupButton(
                            text = "시작하기",
                            onClick = onStart,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ChallengeStatus.RUNNING -> {
                        // 횟수 카운터 원형
                        RepCounterCircle(
                            current = progress.currentReps,
                            target = progress.challenge.targetReps,
                            progress = progress.progressPercent
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 안내 메시지
                        Text(
                            text = "폰을 손에 들고 스쿼트를 하세요",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 다음에 하기 버튼
                Text(
                    text = "다음에 하기",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseInstructions(type: ChallengeType) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "운동 자세 안내",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 스쿼트 자세 안내
        InstructionItem("1.", "폰을 손에 들고 하세요")
        Spacer(modifier = Modifier.height(8.dp))
        InstructionItem("2.", "발을 어깨 너비로 벌리세요")
        Spacer(modifier = Modifier.height(8.dp))
        InstructionItem("3.", "무릎을 90도로 굽혔다 펴세요")
        Spacer(modifier = Modifier.height(8.dp))
        InstructionItem("4.", "화면에서 횟수를 확인하세요", isHighlight = true)
    }
}

@Composable
private fun InstructionItem(icon: String, text: String, isHighlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isHighlight) Color(0xFF666666) else Color.Black
        )
    }
}

@Composable
private fun RepCounterCircle(
    current: Int,
    target: Int,
    progress: Float
) {
    // 애니메이션 효과
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // 진행률 원
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // 배경 원 (회색)
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // 진행률 호 (검정)
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = Color.Black,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth)
            )
        }

        // 횟수 텍스트
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$current",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "/ $target",
                fontSize = 20.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

// MockupButton 재사용 (기존 파일에 있다고 가정)
@Composable
private fun MockupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Color.Black else Color(0xFFCCCCCC))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
