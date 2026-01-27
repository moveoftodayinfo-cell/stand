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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StreakCelebrationDialog(
    streakCount: Int,
    weeklyAchievements: List<Boolean>,
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null,
    petType: PetType = PetType.DOG1,
    petName: String = "",
    equippedTitle: String? = null,  // 칭호 (볼드용)
    dailySteps: Int = 5000,
    totalKm: Float = 0f,
    screenFreeHours: Int = 0,
    // 현재 상태 공유용 파라미터
    isQuickShare: Boolean = false,
    currentSpeech: String = "",
    currentSteps: Int = 0,
    goalSteps: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kenneyFont = rememberKenneyFont()

    // Graphics layers for capture
    val fullCardGraphicsLayer = rememberGraphicsLayer()
    val stickerGraphicsLayer = rememberGraphicsLayer()

    // Pager state for swipe navigation
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Animation
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        if (!isQuickShare) hapticManager?.success()
    }

    // Pet speech: 빠른 공유면 현재 말, 아니면 달성 축하 메시지
    val petSpeech = remember(isQuickShare, currentSpeech, streakCount, petType, dailySteps, totalKm) {
        if (isQuickShare && currentSpeech.isNotEmpty()) {
            // 현재 말풍선을 AnnotatedString으로 변환 (볼드 없이 단순하게)
            buildAnnotatedString { append(currentSpeech) }
        } else {
            getStreakCelebrationSpeech(petType.personality, streakCount, dailySteps, totalKm)
        }
    }

    // Progress 계산 (빠른 공유용)
    val progressPercent = if (goalSteps > 0) ((currentSteps.toFloat() / goalSteps) * 100).toInt().coerceIn(0, 100) else 0

    // Get today's day of week (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 }
    val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Swipeable content area
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
                        // Page 1: Full Card
                        FullCardContent(
                            streakCount = streakCount,
                            screenFreeHours = screenFreeHours,
                            petType = petType,
                            petName = petName,
                            equippedTitle = equippedTitle,
                            petSpeech = petSpeech,
                            today = today,
                            dayNames = dayNames,
                            kenneyFont = kenneyFont,
                            graphicsLayer = fullCardGraphicsLayer,
                            isQuickShare = isQuickShare,
                            currentSteps = currentSteps,
                            goalSteps = goalSteps,
                            progressPercent = progressPercent
                        )
                    }
                    1 -> {
                        // Page 2: Sticker
                        StickerContent(
                            petType = petType,
                            petName = petName,
                            equippedTitle = equippedTitle,
                            petSpeech = petSpeech,
                            kenneyFont = kenneyFont,
                            graphicsLayer = stickerGraphicsLayer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) MockupColors.Border
                                else Color(0xFFCCCCCC)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share button
            Button(
                onClick = {
                    hapticManager?.click()
                    scope.launch {
                        val bitmap = if (pagerState.currentPage == 0) {
                            fullCardGraphicsLayer.toImageBitmap().asAndroidBitmap()
                        } else {
                            stickerGraphicsLayer.toImageBitmap().asAndroidBitmap()
                        }
                        val isSticker = pagerState.currentPage == 1
                        saveAndShareImage(context, bitmap, streakCount, isSticker)

                        // Core 유저 추적: 공유 이벤트 기록
                        (context.applicationContext as? WalkorWaitApp)?.userDataRepository?.trackShareEvent()
                        AnalyticsManager.trackStreakShared(streakCount)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockupColors.Border
                )
            ) {
                Text(
                    text = "Share",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kenneyFont,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close button (text only)
            TextButton(onClick = {
                hapticManager?.click()
                onDismiss()
            }) {
                Text(
                    text = "Close",
                    fontSize = 16.sp,
                    color = MockupColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun FullCardContent(
    streakCount: Int,
    screenFreeHours: Int,
    petType: PetType,
    petName: String = "",
    equippedTitle: String? = null,
    petSpeech: androidx.compose.ui.text.AnnotatedString,
    today: Int,
    dayNames: List<String>,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    isQuickShare: Boolean = false,
    currentSteps: Int = 0,
    goalSteps: Int = 0,
    progressPercent: Int = 0
) {
    val stripeWidth = 4.dp

    // 인스타 피드용 4:5 비율
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(20.dp))
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
            }
            .background(Color.White)
            .border(4.dp, MockupColors.Border, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Title
            Text(
                text = "rebon",
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Display Area with stripes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .drawBehind {
                        val stripeHeightPx = stripeWidth.toPx()
                        val stripeColor = Color(0xFFF0F0F0)
                        var y = 0f
                        while (y < size.height) {
                            drawRect(
                                color = stripeColor,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                                size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                            )
                            y += stripeHeightPx * 2
                        }
                    }
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(16.dp))
            ) {
                // Speech bubble (상단 고정)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    SpeechBubbleMultiline(text = petSpeech, fontSize = 12.sp, maxWidth = 220.dp)
                }

                // 스프라이트 + 펫 이름 (하단 고정)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PetSpriteWithSyncedGlow(
                        petType = petType,
                        isWalking = false,
                        size = 80.dp,
                        monochrome = true,
                        frameDurationMs = 200
                    )

                    // 칭호 + 펫 이름 (스프라이트 아래) - 칭호만 볼드, 이름은 노말
                    if (petName.isNotEmpty() || equippedTitle != null) {
                        Text(
                            text = buildAnnotatedString {
                                if (equippedTitle != null) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("$equippedTitle ")
                                    }
                                }
                                append(petName)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = MockupColors.TextSecondary,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(
                                    color = Color.White,
                                    offset = Offset(0f, 0f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier.offset(y = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Instruction
            if (isQuickShare) {
                // 빠른 공유: 현재 진행률 표시
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("$progressPercent")
                        }
                        append("% 달성 중")
                    },
                    fontSize = 22.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("%,d".format(currentSteps))
                        }
                        append(" / %,d 보".format(goalSteps))
                    },
                    fontSize = 16.sp,
                    color = MockupColors.TextMuted
                )
            } else {
                // 목표 달성: 스트릭 표시
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("$streakCount")
                        }
                        append(" day streak!")
                    },
                    fontSize = 22.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("${screenFreeHours}h")
                        }
                        append(" screen-free")
                    },
                    fontSize = 16.sp,
                    color = MockupColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Week Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MockupColors.CardBackground)
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    // Day labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayNames.forEachIndexed { index, day ->
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index == today) MockupColors.TextPrimary else MockupColors.TextMuted,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stars (fill from Monday to today)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(7) { index ->
                            val isFilled = if (today == 0) {
                                index == 0 // Sunday only
                            } else {
                                index in 1..today // Monday to today
                            }

                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PixelIcon(
                                    iconName = "icon_star",
                                    size = 24.dp,
                                    // 채워진 별: 진한 회색, 빈 별: 연한 회색
                                    tint = if (isFilled) Color(0xFF333333) else Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerContent(
    petType: PetType,
    petName: String = "",
    equippedTitle: String? = null,
    petSpeech: androidx.compose.ui.text.AnnotatedString,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer
) {
    // Dialog wrapper like Full Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(4.dp, MockupColors.Border, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Title
            Text(
                text = "rebon",
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Display Area with checkered background (transparency preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .drawBehind {
                        val squareSize = 16.dp.toPx()
                        val lightColor = Color.White
                        val darkColor = Color(0xFFCCCCCC)

                        var y = 0f
                        var rowIndex = 0
                        while (y < size.height) {
                            var x = 0f
                            var colIndex = if (rowIndex % 2 == 0) 0 else 1
                            while (x < size.width) {
                                drawRect(
                                    color = if (colIndex % 2 == 0) lightColor else darkColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                                )
                                x += squareSize
                                colIndex++
                            }
                            y += squareSize
                            rowIndex++
                        }
                    }
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Sticker content (for capture - transparent background)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            // 명시적으로 투명 배경 그리기
                            drawRect(Color.Transparent)
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                        .padding(12.dp)
                ) {
                    // Speech bubble (상단 고정)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    ) {
                        SpeechBubbleMultiline(text = petSpeech, fontSize = 11.sp, maxWidth = 200.dp)
                    }

                    // 스프라이트 + 펫 이름 + rebon 로고 (하단 고정)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    ) {
                        // Pet with glow
                        PetSpriteWithSyncedGlow(
                            petType = petType,
                            isWalking = false,
                            size = 70.dp,
                            monochrome = true,
                            frameDurationMs = 200
                        )

                        // 칭호 + 펫 이름 (스프라이트 아래) - 칭호만 볼드, 이름은 노말
                        if (petName.isNotEmpty() || equippedTitle != null) {
                            Text(
                                text = buildAnnotatedString {
                                    if (equippedTitle != null) {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("$equippedTitle ")
                                        }
                                    }
                                    append(petName)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = MockupColors.TextSecondary,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = Shadow(
                                        color = Color.White,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier.offset(y = 5.dp)
                            )
                        }

                        // rebon 로고 (반투명)
                        Text(
                            text = "rebon",
                            fontSize = 12.sp,
                            fontFamily = kenneyFont,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Instruction
            Text(
                text = "Sticker",
                fontSize = 22.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = "transparent PNG",
                fontSize = 16.sp,
                color = MockupColors.TextMuted
            )
        }
    }
}

@Composable
private fun SpeechBubbleMultiline(
    text: androidx.compose.ui.text.AnnotatedString,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    maxWidth: androidx.compose.ui.unit.Dp = 280.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 말풍선 본체
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = MockupColors.TextPrimary,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                lineHeight = fontSize * 1.4
            )
        }

        // 말풍선 꼬리 (삼각형)
        Canvas(
            modifier = Modifier
                .size(width = 12.dp, height = 8.dp)
                .offset(y = (-2).dp) // 테두리와 겹치게
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2, size.height)
                close()
            }
            // 흰색 채우기
            drawPath(path, Color.White)
            // 테두리 (왼쪽, 오른쪽 선만)
            drawLine(
                color = MockupColors.Border,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = MockupColors.Border,
                start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Get streak celebration speech based on pet personality
 * 중요한 숫자(걸음수, km, 일수)는 볼드 처리
 */
private fun getStreakCelebrationSpeech(
    personality: PetPersonality,
    streakDays: Int,
    dailySteps: Int,
    totalKm: Float
): androidx.compose.ui.text.AnnotatedString {
    val formattedSteps = "%,d".format(dailySteps)
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)

    return when (personality) {
        PetPersonality.TOUGH -> if (streakDays == 1) {
            buildAnnotatedString {
                append("오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("나 걸었다고?\n됐다. 좋은 시작이야.")
            }
        } else {
            buildAnnotatedString {
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안 매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("씩\n")
                withStyle(boldStyle) { append("${totalKm}km") }
                append("나 걸었다고?\n됐다. 잘했어.")
            }
        }

        PetPersonality.CUTE -> if (streakDays == 1) {
            buildAnnotatedString {
                append("우와~! 오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("나\n걸었다니 대단해용!")
            }
        } else {
            buildAnnotatedString {
                append("우와~! ")
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안\n매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("씩\n")
                withStyle(boldStyle) { append("${totalKm}km") }
                append("나 걸었다니 대단해용!")
            }
        }

        PetPersonality.TSUNDERE -> if (streakDays == 1) {
            buildAnnotatedString {
                append("흥, 오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("?\n뭐... 나쁘지 않네.")
            }
        } else {
            buildAnnotatedString {
                append("흥, ")
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안\n매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("씩 ")
                withStyle(boldStyle) { append("${totalKm}km") }
                append("?\n뭐... 나쁘지 않네.")
            }
        }

        PetPersonality.DIALECT -> if (streakDays == 1) {
            buildAnnotatedString {
                append("오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("나 걸었노~\n좋은 시작이다!")
            }
        } else {
            buildAnnotatedString {
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안 매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("씩\n")
                withStyle(boldStyle) { append("${totalKm}km") }
                append(" 걸었노~\nㄹㅇ 대단하다!")
            }
        }

        PetPersonality.TIMID -> if (streakDays == 1) {
            buildAnnotatedString {
                append("대, 대단해요...!\n오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("나...!")
            }
        } else {
            buildAnnotatedString {
                append("대, 대단해요...! ")
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안\n매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("씩\n")
                withStyle(boldStyle) { append("${totalKm}km") }
                append("나 걸었어요...!")
            }
        }

        PetPersonality.POSITIVE -> if (streakDays == 1) {
            buildAnnotatedString {
                append("오늘 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append(" 완료!\n좋은 시작! 최고야!")
            }
        } else {
            buildAnnotatedString {
                withStyle(boldStyle) { append("${streakDays}일") }
                append(" 동안 매일 ")
                withStyle(boldStyle) { append("${formattedSteps}보") }
                append("!\n총 ")
                withStyle(boldStyle) { append("${totalKm}km") }
                append("! 최고야!")
            }
        }
    }
}

/**
 * Save image to gallery and share
 */
private fun saveAndShareImage(context: Context, bitmap: Bitmap, streakCount: Int, isSticker: Boolean) {
    try {
        val suffix = if (isSticker) "sticker" else "card"
        val filename = "rebon_${streakCount}day_${suffix}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"

        // Sticker의 경우 투명 배경 보존을 위해 ARGB_8888로 변환
        val finalBitmap = if (isSticker && bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/rebon")
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Share the image
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val rebonDir = File(picturesDir, "rebon")
            if (!rebonDir.exists()) rebonDir.mkdirs()

            val file = File(rebonDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png")
            ) { _, uri ->
                uri?.let {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, it)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share"))
                }
            }
        }

        Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
