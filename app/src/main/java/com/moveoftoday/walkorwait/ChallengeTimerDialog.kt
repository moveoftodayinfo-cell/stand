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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun ChallengeTimerDialog(
    progress: ChallengeProgress,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onEnded: () -> Unit,
    onDebugComplete: () -> Unit = {}
) {
    // 타이머 상태에 따른 처리
    LaunchedEffect(progress.status) {
        when (progress.status) {
            ChallengeStatus.COMPLETED -> onComplete()
            ChallengeStatus.ENDED -> onEnded()
            else -> {}
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
                    color = Color(0xFF333333),
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 챌린지 이름
                Text(
                    text = progress.challenge.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 획득 칭호
                Text(
                    text = "획득 칭호: \"${progress.challenge.type.title}\"",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 타이머 원형
                when (progress.status) {
                    ChallengeStatus.NOT_STARTED -> {
                        StartTimerCircle(onClick = onStart)
                    }
                    ChallengeStatus.RUNNING -> {
                        RunningTimerCircle(progress = progress)
                    }
                    ChallengeStatus.PAUSED -> {
                        PausedTimerCircle(
                            progress = progress,
                            onClick = onResume
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 상태 메시지
                StatusMessage(progress = progress)

                Spacer(modifier = Modifier.height(24.dp))

                // 디버그용: 즉시 완료 버튼
                if (BuildConfig.DEBUG && (progress.status == ChallengeStatus.RUNNING || progress.status == ChallengeStatus.NOT_STARTED)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFCDD2))
                            .border(2.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                            .clickable { onDebugComplete() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🧪 즉시 완료 (DEBUG)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                }

                // 다음에 하기 버튼
                Text(
                    text = "다음에 하기",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun StartTimerCircle(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color(0xFFE0E0E0), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 재생 버튼 (삼각형)
            Canvas(modifier = Modifier.size(48.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.1f)
                    lineTo(size.width * 0.9f, size.height * 0.5f)
                    lineTo(size.width * 0.2f, size.height * 0.9f)
                    close()
                }
                drawPath(path, Color(0xFF333333))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "터치하여 시작",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun RunningTimerCircle(progress: ChallengeProgress) {
    val kenneyFont = rememberKenneyFont()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val progressAngle = progress.progressPercent * 360f
    val remainingMinutes = progress.remainingSeconds / 60
    val remainingSeconds = progress.remainingSeconds % 60
    val timeText = String.format("%02d:%02d", remainingMinutes, remainingSeconds)

    Box(
        modifier = Modifier.size((200 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        // 배경 원
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 2
            )
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = size.minDimension / 2,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 진행률 호
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = progressAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(
                    size.width - 8.dp.toPx(),
                    size.height - 8.dp.toPx()
                )
            )
        }

        // 내부 원
        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeText,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontFamily = kenneyFont
                )
                Text(
                    text = "남은 시간",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun PausedTimerCircle(
    progress: ChallengeProgress,
    onClick: () -> Unit
) {
    val progressAngle = progress.progressPercent * 360f
    val remainingMinutes = progress.remainingSeconds / 60
    val remainingSeconds = progress.remainingSeconds % 60
    val remainingText = String.format("%02d:%02d 남음", remainingMinutes, remainingSeconds)

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 배경 원
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 2
            )
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = size.minDimension / 2,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 진행률 호
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = progressAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(
                    size.width - 8.dp.toPx(),
                    size.height - 8.dp.toPx()
                )
            )
        }

        // 내부 원
        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 재생 버튼
                Canvas(modifier = Modifier.size(40.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.9f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                        close()
                    }
                    drawPath(path, Color(0xFF333333))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "터치하여 계속하기",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = remainingText,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
private fun StatusMessage(progress: ChallengeProgress) {
    val elapsedMinutes = progress.elapsedSeconds / 60

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (progress.status) {
            ChallengeStatus.NOT_STARTED -> {
                Text(
                    text = "${progress.challenge.durationMinutes}분 동안 집중해보세요",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "준비되면 시작 버튼을 눌러주세요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }
            ChallengeStatus.RUNNING -> {
                Text(
                    text = "집중하고 있어요!",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${elapsedMinutes}분 동안 잘 하고 있어요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }
            ChallengeStatus.PAUSED -> {
                Text(
                    text = "앱을 나갔다 돌아왔어요",
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "한번 더 나가면 종료돼요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }
    }
}

// 챌린지 완료 다이얼로그
@Composable
fun ChallengeCompleteDialog(
    challenge: Challenge,
    onDismiss: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .border(3.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "챌린지 완료!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                fontFamily = kenneyFont
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = challenge.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "대단해요! 목표를 달성했어요",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "확인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// 챌린지 종료 다이얼로그
@Composable
fun ChallengeEndedDialog(
    challenge: Challenge,
    onDismiss: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .border(3.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "챌린지 종료",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                fontFamily = kenneyFont
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = challenge.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "다음에 다시 도전해봐요!",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "확인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
