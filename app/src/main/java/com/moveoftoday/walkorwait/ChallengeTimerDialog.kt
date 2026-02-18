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
import java.util.Locale

// 다국어 헬퍼
private object ChallengeDialogStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun earnedTitle(title: String): String = when (getLang()) {
        "ko" -> "획득 칭호: \"$title\""
        "ja" -> "獲得称号: \"$title\""
        "zh" -> "获得称号: \"$title\""
        "es" -> "Título: \"$title\""
        else -> "Earned Title: \"$title\""
    }

    fun checkLater(): String = when (getLang()) {
        "ko" -> "나중에 확인하기"
        "ja" -> "後で確認"
        "zh" -> "稍后查看"
        "es" -> "Ver después"
        else -> "Check Later"
    }

    fun doLater(): String = when (getLang()) {
        "ko" -> "다음에 하기"
        "ja" -> "次にする"
        "zh" -> "下次再做"
        "es" -> "Hacer después"
        else -> "Do Later"
    }

    fun tapToStart(): String = when (getLang()) {
        "ko" -> "터치하여 시작"
        "ja" -> "タップして開始"
        "zh" -> "点击开始"
        "es" -> "Toca para empezar"
        else -> "Tap to Start"
    }

    fun remainingTime(): String = when (getLang()) {
        "ko" -> "남은 시간"
        "ja" -> "残り時間"
        "zh" -> "剩余时间"
        "es" -> "Tiempo restante"
        else -> "Remaining"
    }

    fun remainingFormat(min: Int, sec: Int): String = when (getLang()) {
        "ko" -> String.format("%02d:%02d 남음", min, sec)
        "ja" -> String.format("%02d:%02d 残り", min, sec)
        "zh" -> String.format("剩余 %02d:%02d", min, sec)
        "es" -> String.format("%02d:%02d restante", min, sec)
        else -> String.format("%02d:%02d left", min, sec)
    }

    fun tapToContinue(): String = when (getLang()) {
        "ko" -> "터치하여 계속하기"
        "ja" -> "タップして続行"
        "zh" -> "点击继续"
        "es" -> "Toca para continuar"
        else -> "Tap to Continue"
    }

    fun startFastingHours(hours: Int): String = when (getLang()) {
        "ko" -> "${hours}시간 단식을 시작해보세요"
        "ja" -> "${hours}時間の断食を始めましょう"
        "zh" -> "开始${hours}小时禁食吧"
        "es" -> "Empieza ${hours}h de ayuno"
        else -> "Start ${hours}h fasting"
    }

    fun pressStartWhenReady(): String = when (getLang()) {
        "ko" -> "준비되면 시작 버튼을 눌러주세요"
        "ja" -> "準備ができたら開始ボタンを押してください"
        "zh" -> "准备好后请按开始按钮"
        "es" -> "Presiona inicio cuando estés listo"
        else -> "Press start when ready"
    }

    fun secondsUnit(sec: Int): String = when (getLang()) {
        "ko" -> "${sec}초"
        "ja" -> "${sec}秒"
        "zh" -> "${sec}秒"
        "es" -> "${sec}s"
        else -> "${sec}s"
    }

    fun minutesUnit(min: Int): String = when (getLang()) {
        "ko" -> "${min}분"
        "ja" -> "${min}分"
        "zh" -> "${min}分钟"
        "es" -> "${min} min"
        else -> "${min} min"
    }

    fun focusForTime(timeText: String): String = when (getLang()) {
        "ko" -> "$timeText 동안 집중해보세요"
        "ja" -> "${timeText}間集中してみましょう"
        "zh" -> "集中注意力${timeText}"
        "es" -> "Concéntrate por $timeText"
        else -> "Focus for $timeText"
    }

    fun fasting(): String = when (getLang()) {
        "ko" -> "단식 중입니다"
        "ja" -> "断食中です"
        "zh" -> "禁食中"
        "es" -> "En ayuno"
        else -> "Fasting"
    }

    fun hoursLeft(hours: Int): String = when (getLang()) {
        "ko" -> "${hours}시간 남았어요"
        "ja" -> "あと${hours}時間"
        "zh" -> "还剩${hours}小时"
        "es" -> "${hours}h restantes"
        else -> "${hours}h left"
    }

    fun minutesLeft(min: Int): String = when (getLang()) {
        "ko" -> "${min}분 남았어요"
        "ja" -> "あと${min}分"
        "zh" -> "还剩${min}分钟"
        "es" -> "${min} min restantes"
        else -> "${min} min left"
    }

    fun focusing(): String = when (getLang()) {
        "ko" -> "집중하고 있어요!"
        "ja" -> "集中中！"
        "zh" -> "正在专注！"
        "es" -> "¡Concentrado!"
        else -> "Focusing!"
    }

    fun doingGreatMinutes(min: Int): String = when (getLang()) {
        "ko" -> "${min}분 동안 잘 하고 있어요"
        "ja" -> "${min}分間よく頑張っています"
        "zh" -> "已专注${min}分钟，做得很好"
        "es" -> "¡Llevas ${min} min genial!"
        else -> "Great job for ${min} min!"
    }

    fun leftAndCameBack(): String = when (getLang()) {
        "ko" -> "앱을 나갔다 돌아왔어요"
        "ja" -> "アプリを離れて戻りました"
        "zh" -> "您离开又返回了"
        "es" -> "Saliste y volviste"
        else -> "You left and came back"
    }

    fun oneMoreExitEnds(): String = when (getLang()) {
        "ko" -> "한번 더 나가면 종료돼요"
        "ja" -> "もう一度離れると終了します"
        "zh" -> "再次离开将结束"
        "es" -> "Salir de nuevo terminará"
        else -> "One more exit will end it"
    }

    fun fastingCompleteEat(): String = when (getLang()) {
        "ko" -> "단식 완료! 이제 식사하세요"
        "ja" -> "断食完了！食事してください"
        "zh" -> "禁食完成！现在可以吃饭了"
        "es" -> "¡Ayuno completo! Ya puedes comer"
        else -> "Fasting complete! Time to eat"
    }

    fun greatJobGoalAchieved(): String = when (getLang()) {
        "ko" -> "대단해요! 목표를 달성했어요"
        "ja" -> "素晴らしい！目標達成！"
        "zh" -> "太棒了！目标达成！"
        "es" -> "¡Genial! ¡Meta lograda!"
        else -> "Great job! Goal achieved!"
    }

    fun challengeComplete(): String = when (getLang()) {
        "ko" -> "챌린지 완료!"
        "ja" -> "チャレンジ完了！"
        "zh" -> "挑战完成！"
        "es" -> "¡Desafío completo!"
        else -> "Challenge Complete!"
    }

    fun confirm(): String = when (getLang()) {
        "ko" -> "확인"
        "ja" -> "確認"
        "zh" -> "确认"
        "es" -> "OK"
        else -> "OK"
    }

    fun challengeEnded(): String = when (getLang()) {
        "ko" -> "챌린지 종료"
        "ja" -> "チャレンジ終了"
        "zh" -> "挑战结束"
        "es" -> "Desafío terminado"
        else -> "Challenge Ended"
    }

    fun tryAgainNextTime(): String = when (getLang()) {
        "ko" -> "다음에 다시 도전해봐요!"
        "ja" -> "次回また挑戦しましょう！"
        "zh" -> "下次再挑战吧！"
        "es" -> "¡Inténtalo de nuevo!"
        else -> "Try again next time!"
    }

    fun whenDidYouStartFasting(): String = when (getLang()) {
        "ko" -> "언제 단식을 시작했나요?"
        "ja" -> "いつ断食を始めましたか？"
        "zh" -> "你什么时候开始禁食的？"
        "es" -> "¿Cuándo empezaste el ayuno?"
        else -> "When did you start fasting?"
    }

    fun now(): String = when (getLang()) {
        "ko" -> "지금"
        "ja" -> "今"
        "zh" -> "现在"
        "es" -> "Ahora"
        else -> "Now"
    }

    fun start(): String = when (getLang()) {
        "ko" -> "시작하기"
        "ja" -> "開始"
        "zh" -> "开始"
        "es" -> "Empezar"
        else -> "Start"
    }

    fun startingFastingNow(): String = when (getLang()) {
        "ko" -> "지금부터 단식을 시작합니다"
        "ja" -> "今から断食を開始します"
        "zh" -> "从现在开始禁食"
        "es" -> "Empezando ayuno ahora"
        else -> "Starting fasting now"
    }

    fun beenFastingForHours(hours: Int): String = when (getLang()) {
        "ko" -> "${hours}시간 전부터 단식 중입니다"
        "ja" -> "${hours}時間前から断食中です"
        "zh" -> "已禁食${hours}小时"
        "es" -> "Ayunando desde hace ${hours}h"
        else -> "Fasting for ${hours}h"
    }
}

@Composable
fun ChallengeTimerDialog(
    progress: ChallengeProgress,
    onStart: (startTimeOffsetHours: Int) -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onCheckLater: () -> Unit = {},
    onComplete: () -> Unit,
    onEnded: () -> Unit,
    onDebugComplete: () -> Unit = {}
) {
    // 시작 시간 offset (간헐적 단식 전용)
    var startTimeOffsetHours by remember { mutableIntStateOf(0) }
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
                    color = Color.Black,
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 챌린지 이름
                Text(
                    text = progress.challenge.type.getLocalizedDisplayName(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 획득 칭호
                Text(
                    text = ChallengeDialogStrings.earnedTitle(progress.challenge.type.getLocalizedTitle()),
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 타이머 원형
                when (progress.status) {
                    ChallengeStatus.NOT_STARTED -> {
                        // 간헐적 단식은 시작 시간 선택 UI
                        val isFasting = progress.challenge.type in listOf(
                            ChallengeType.FASTING_16_8,
                            ChallengeType.FASTING_18_6,
                            ChallengeType.FASTING_20_4
                        )

                        if (isFasting) {
                            FastingStartTimeSelector(
                                selectedOffsetHours = startTimeOffsetHours,
                                onOffsetChanged = { startTimeOffsetHours = it },
                                onClick = { onStart(startTimeOffsetHours) }
                            )
                        } else {
                            StartTimerCircle(onClick = { onStart(0) })
                        }
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

                // 버튼 영역
                val isFasting = progress.challenge.type in listOf(
                    ChallengeType.FASTING_16_8,
                    ChallengeType.FASTING_18_6,
                    ChallengeType.FASTING_20_4
                )

                // 백그라운드 타이머(간헐적 단식) 진행 중이면 "나중에 확인하기" 버튼
                if (isFasting && progress.status == ChallengeStatus.RUNNING) {
                    Text(
                        text = ChallengeDialogStrings.checkLater(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .clickable { onCheckLater() }
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 다음에 하기 버튼
                Text(
                    text = ChallengeDialogStrings.doLater(),
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
                drawPath(path, Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ChallengeDialogStrings.tapToStart(),
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
                color = Color.Black,
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
                    color = Color.Black,
                    fontFamily = kenneyFont
                )
                Text(
                    text = ChallengeDialogStrings.remainingTime(),
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
    val remainingText = ChallengeDialogStrings.remainingFormat(remainingMinutes, remainingSeconds)

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
                color = Color.Black,
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
                    drawPath(path, Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ChallengeDialogStrings.tapToContinue(),
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
    val elapsedHours = elapsedMinutes / 60
    val remainingHours = progress.remainingSeconds / 3600

    // 간헐적 단식 챌린지 체크
    val isFasting = progress.challenge.type in listOf(
        ChallengeType.FASTING_16_8,
        ChallengeType.FASTING_18_6,
        ChallengeType.FASTING_20_4
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (progress.status) {
            ChallengeStatus.NOT_STARTED -> {
                if (isFasting) {
                    val hours = (progress.challenge.durationMinutes / 60).toInt()
                    Text(
                        text = ChallengeDialogStrings.startFastingHours(hours),
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ChallengeDialogStrings.pressStartWhenReady(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val timeText = if (progress.challenge.durationMinutes < 1) {
                        ChallengeDialogStrings.secondsUnit((progress.challenge.durationMinutes * 60).toInt())
                    } else {
                        ChallengeDialogStrings.minutesUnit(progress.challenge.durationMinutes.toInt())
                    }
                    Text(
                        text = ChallengeDialogStrings.focusForTime(timeText),
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ChallengeDialogStrings.pressStartWhenReady(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            ChallengeStatus.RUNNING -> {
                if (isFasting) {
                    Text(
                        text = ChallengeDialogStrings.fasting(),
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    val remainingText = if (remainingHours >= 1) {
                        ChallengeDialogStrings.hoursLeft(remainingHours)
                    } else {
                        val remainingMins = progress.remainingSeconds / 60
                        ChallengeDialogStrings.minutesLeft(remainingMins)
                    }
                    Text(
                        text = remainingText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = ChallengeDialogStrings.focusing(),
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ChallengeDialogStrings.doingGreatMinutes(elapsedMinutes),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            ChallengeStatus.PAUSED -> {
                Text(
                    text = ChallengeDialogStrings.leftAndCameBack(),
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = ChallengeDialogStrings.oneMoreExitEnds(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
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

    // 간헐적 단식 체크
    val isFasting = challenge.type in listOf(
        ChallengeType.FASTING_16_8,
        ChallengeType.FASTING_18_6,
        ChallengeType.FASTING_20_4
    )

    val completeMessage = if (isFasting) {
        ChallengeDialogStrings.fastingCompleteEat()
    } else {
        ChallengeDialogStrings.greatJobGoalAchieved()
    }

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
                text = ChallengeDialogStrings.challengeComplete(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = kenneyFont
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = challenge.type.getLocalizedDisplayName(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = completeMessage,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ChallengeDialogStrings.confirm(),
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
                text = ChallengeDialogStrings.challengeEnded(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = kenneyFont
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = challenge.type.getLocalizedDisplayName(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ChallengeDialogStrings.tryAgainNextTime(),
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ChallengeDialogStrings.confirm(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// 간헐적 단식 시작 시간 선택 UI
@Composable
private fun FastingStartTimeSelector(
    selectedOffsetHours: Int,
    onOffsetChanged: (Int) -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(280.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 설명 텍스트
        Text(
            text = ChallengeDialogStrings.whenDidYouStartFasting(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 시간 선택 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(0, 1, 2, 3, 4).forEach { hours ->
                val isSelected = selectedOffsetHours == hours
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.Black else Color.White)
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable { onOffsetChanged(hours) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hours == 0) ChallengeDialogStrings.now() else "${hours}h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 추가 시간 옵션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(5, 6, 7, 8).forEach { hours ->
                val isSelected = selectedOffsetHours == hours
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.Black else Color.White)
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable { onOffsetChanged(hours) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${hours}h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 시작 버튼
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, Color.Black, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 재생 버튼
                Canvas(modifier = Modifier.size(40.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.9f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                        close()
                    }
                    drawPath(path, Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ChallengeDialogStrings.start(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 선택된 시간 표시
        Text(
            text = if (selectedOffsetHours == 0) {
                ChallengeDialogStrings.startingFastingNow()
            } else {
                ChallengeDialogStrings.beenFastingForHours(selectedOffsetHours)
            },
            fontSize = 13.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}
