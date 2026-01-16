package com.moveoftoday.walkorwait

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moveoftoday.walkorwait.ui.theme.StandColors

/**
 * 스탠드 불빛 애니메이션 컬렉션
 * 목표 달성, 진행 중 등 다양한 상태를 시각적으로 표현
 */

/**
 * 💡 불빛 켜지는 애니메이션
 * 목표 달성 시 사용 - 어둠에서 밝게 켜지는 효과
 */
@Composable
fun LightOnAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightOn")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    if (isActive) {
        Box(
            modifier = modifier
                .scale(scale)
                .alpha(alpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.GlowYellow.copy(alpha = 0.8f),
                            StandColors.GlowAmber.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * ✨ 반짝이는 효과
 * 성공 시 짧게 반짝이는 애니메이션
 */
@Composable
fun SparkleAnimation(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isAnimating = true
            kotlinx.coroutines.delay(800)
            isAnimating = false
            onAnimationEnd()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sparkleAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.5f else 0.8f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sparkleScale"
    )

    if (isAnimating) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(alpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.GlowYellow.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * 🔄 펄스 효과
 * 진행 중인 상태를 나타내는 부드러운 펄스
 */
@Composable
fun PulseAnimation(
    isActive: Boolean,
    color: Color = StandColors.Primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (isActive) {
        Box(
            modifier = modifier
                .alpha(alpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * 🚨 긴급 모드 애니메이션
 * 빠르게 깜박이는 경고 효과
 */
@Composable
fun EmergencyAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emergencyAlpha"
    )

    if (isActive) {
        Box(
            modifier = modifier
                .alpha(alpha)
                .background(
                    color = StandColors.Error.copy(alpha = 0.2f)
                )
        )
    }
}

/**
 * 🎯 목표 진행 상태 반짝임
 * 목표에 가까워질수록 빛이 강해지는 효과
 */
@Composable
fun ProgressGlowAnimation(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val glowIntensity = (progress * 0.8f).coerceIn(0.2f, 0.8f)

    val infiniteTransition = rememberInfiniteTransition(label = "progressGlow")

    val alpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.5f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val color = when {
        progress >= 1f -> StandColors.Success
        progress >= 0.7f -> StandColors.GlowAmber
        else -> StandColors.Primary
    }

    Box(
        modifier = modifier
            .alpha(alpha)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

/**
 * 🌟 목표 달성 축하 효과
 * 목표 달성 시 화면 전체에 퍼지는 빛 효과
 */
@Composable
fun GoalAchievedCelebration(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isAnimating = true
            kotlinx.coroutines.delay(2000)
            isAnimating = false
            onAnimationEnd()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 3f else 0f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "celebrationScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 1f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "celebrationAlpha"
    )

    if (isAnimating) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(alpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.GlowYellow.copy(alpha = 0.8f),
                            StandColors.GlowAmber.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
