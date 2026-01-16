package com.moveoftoday.walkorwait.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 프리미엄 피트니스 앱 디자인 시스템
 */
object PremiumColors {
    // 그라데이션 색상
    val TealPrimary = Color(0xFF00BFA5)
    val TealDark = Color(0xFF008E76)
    val NavyDark = Color(0xFF0D1B2A)
    val NavyMid = Color(0xFF1B263B)

    // 프로그레스 색상
    val ProgressTrack = Color(0xFF2A2A2A)
    val ProgressTeal = Color(0xFF00D9BB)

    // Glow 색상
    val GlowGold = Color(0xFFFFD700)
    val GlowAmber = Color(0xFFFFC107)

    // 바텀 시트
    val BottomSheetBackground = Color(0xFF0A0A0A)

    // 카드
    val CardBackground = Color.White.copy(alpha = 0.15f)
    val CardBackgroundSelected = Color.White.copy(alpha = 0.25f)

    // 텍스트
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.6f)
    val TextMuted = Color.White.copy(alpha = 0.4f)
}

/**
 * 프리미엄 그라데이션 배경
 */
@Composable
fun PremiumGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        PremiumColors.TealPrimary,
                        PremiumColors.TealDark,
                        PremiumColors.NavyMid,
                        PremiumColors.NavyDark
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        content = content
    )
}

/**
 * 프리미엄 레이아웃 (상단 그라데이션 70% + 하단 바텀시트 30%)
 */
@Composable
fun PremiumLayout(
    currentStep: Int,
    totalSteps: Int,
    stepLabel: String,
    onNextClick: () -> Unit,
    nextButtonEnabled: Boolean = true,
    nextButtonText: String = "다음",
    topContent: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 상단 70% - 그라데이션 배경
        PremiumGradientBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.TopCenter)
        ) {
            topContent()
        }

        // 하단 30% - 바텀 시트
        PremiumBottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .align(Alignment.BottomCenter),
            currentStep = currentStep,
            totalSteps = totalSteps,
            stepLabel = stepLabel,
            onNextClick = onNextClick,
            nextButtonEnabled = nextButtonEnabled,
            nextButtonText = nextButtonText
        )
    }
}

/**
 * 프리미엄 바텀 시트
 */
@Composable
fun PremiumBottomSheet(
    modifier: Modifier = Modifier,
    currentStep: Int,
    totalSteps: Int,
    stepLabel: String,
    onNextClick: () -> Unit,
    nextButtonEnabled: Boolean = true,
    nextButtonText: String = "다음"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(PremiumColors.BottomSheetBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 진행 표시 + 단계 라벨
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측: 프로그레스 인디케이터
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(totalSteps) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index < currentStep) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < currentStep) PremiumColors.TealPrimary
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$currentStep/$totalSteps",
                        color = PremiumColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }

                // 우측: 단계 라벨
                Text(
                    text = stepLabel,
                    color = PremiumColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단: 다음 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nextButtonText,
                    color = if (nextButtonEnabled) PremiumColors.TextPrimary else PremiumColors.TextMuted,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 원형 화살표 버튼
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (nextButtonEnabled) PremiumColors.TealPrimary
                            else PremiumColors.ProgressTrack
                        )
                        .clickable(
                            enabled = nextButtonEnabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNextClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = if (nextButtonEnabled) Color.White else PremiumColors.TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Glow가 있는 원형 프로그레스 바
 */
@Composable
fun CircularProgressWithGlow(
    progress: Float, // 0.0 ~ 1.0
    currentValue: Int,
    targetValue: Int,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    unit: String = "걸음"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // 카운트업 애니메이션
    var displayValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentValue) {
        val startValue = displayValue
        val diff = currentValue - startValue
        val steps = 20
        val stepDelay = 30L

        for (i in 1..steps) {
            displayValue = startValue + (diff * i / steps)
            delay(stepDelay)
        }
        displayValue = currentValue
    }

    // Glow 효과 계산
    val glowConfig = remember(animatedProgress) {
        when {
            animatedProgress < 0.5f -> GlowConfig(0.dp, 0f, false)
            animatedProgress < 0.7f -> GlowConfig(12.dp, 0.2f, false)
            animatedProgress < 0.9f -> GlowConfig(20.dp, 0.45f, false)
            else -> GlowConfig(32.dp, 0.75f, true)
        }
    }

    // Pulse 애니메이션 (90% 이상일 때)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = glowConfig.opacity,
        targetValue = glowConfig.opacity * 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val finalScale = if (glowConfig.animate) pulseScale else 1f
    val finalGlowAlpha = if (glowConfig.animate) pulseAlpha else glowConfig.opacity

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow 레이어 (blur 처리된 arc)
        if (glowConfig.opacity > 0f) {
            Canvas(
                modifier = Modifier
                    .size(size)
                    .scale(finalScale)
                    .blur(glowConfig.blur)
            ) {
                val sweepAngle = animatedProgress * 360f
                val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
                val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

                drawArc(
                    color = PremiumColors.GlowGold.copy(alpha = finalGlowAlpha),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx() * 2, cap = StrokeCap.Round)
                )
            }
        }

        // 메인 프로그레스 바
        Canvas(modifier = Modifier.size(size)) {
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

            // 트랙 (배경)
            drawArc(
                color = PremiumColors.ProgressTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // 프로그레스
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0) {
                drawArc(
                    color = PremiumColors.ProgressTeal,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 중앙 텍스트
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%,d".format(displayValue),
                color = PremiumColors.TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ %,d $unit".format(targetValue),
                color = PremiumColors.TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

private data class GlowConfig(
    val blur: Dp,
    val opacity: Float,
    val animate: Boolean
)

/**
 * 프리미엄 카드 (선택지용)
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PremiumColors.CardBackgroundSelected else PremiumColors.CardBackground,
        animationSpec = tween(durationMillis = 200),
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

/**
 * 프리미엄 섹션 제목
 */
@Composable
fun PremiumSectionTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = PremiumColors.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = PremiumColors.TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

/**
 * 프리미엄 프로그레스 인디케이터 (수평 점)
 */
@Composable
fun PremiumProgressDots(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index < currentStep
            val isCurrent = index == currentStep - 1

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> PremiumColors.TealPrimary
                            isActive -> PremiumColors.TealDark
                            else -> Color.White.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

/**
 * 간소화된 원형 프로그레스 (메인 화면용)
 */
@Composable
fun SimpleCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = PremiumColors.ProgressTrack,
    progressColor: Color = PremiumColors.ProgressTeal
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "simpleProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
        val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

        // 트랙
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // 프로그레스
        if (animatedProgress > 0) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
