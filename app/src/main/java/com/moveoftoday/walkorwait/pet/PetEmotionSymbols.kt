package com.moveoftoday.walkorwait.pet

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 펫 감정 심볼 시스템 V3
 * - 모노크롬 (검정/회색)
 * - 레트로 ASCII 스타일
 * - 한글 없이 순수 심볼만
 */

// ===== 레트로 심볼 정의 =====
object RetroSymbols {
    // 하트 계열
    const val HEART = "♥"
    const val HEART_EMPTY = "♡"
    const val HEARTS_2 = "♥♥"
    const val HEARTS_3 = "♥♥♥"

    // 별 계열
    const val STAR = "★"
    const val STAR_EMPTY = "☆"
    const val STARS_2 = "★★"
    const val STARS_3 = "★★★"
    const val SPARKLE = "✧"
    const val SPARKLES = "✧✧"

    // 음표 계열
    const val NOTE = "♪"
    const val NOTES = "♪♫"
    const val NOTE_DOUBLE = "♫"

    // 감정 계열
    const val EXCLAIM = "!"
    const val EXCLAIM_2 = "!!"
    const val QUESTION = "?"
    const val QUESTION_2 = "??"
    const val QUESTION_EXCLAIM = "?!"
    const val DOTS = "..."
    const val SWEAT = ";;"
    const val SWEAT_2 = ";;;"
    const val ANGRY = "##"
    const val BLUSH = "//"

    // 수면/휴식
    const val SLEEP = "zzZ"
    const val SLEEP_2 = "ZzZ"

    // 특수
    const val DASH = "~"
    const val SHINE = "**"
    const val SPIRAL = "@"
}

// ===== 감정 타입 =====
enum class EmotionType(
    val symbols: List<String>,
    val animationType: EmotionAnimationType
) {
    // 기쁨 (터치 1회)
    HAPPY(
        symbols = listOf(RetroSymbols.HEART, RetroSymbols.HEART_EMPTY),
        animationType = EmotionAnimationType.FLOAT_UP
    ),

    // 매우 기쁨 (터치 연타)
    VERY_HAPPY(
        symbols = listOf(RetroSymbols.HEARTS_2, RetroSymbols.HEARTS_3),
        animationType = EmotionAnimationType.BURST
    ),

    // 축하 (목표 달성, 레벨업)
    CELEBRATION(
        symbols = listOf(RetroSymbols.STARS_2, RetroSymbols.STARS_3, RetroSymbols.SPARKLES),
        animationType = EmotionAnimationType.SPARKLE
    ),

    // 음악/즐거움 (걷기 시작)
    MUSICAL(
        symbols = listOf(RetroSymbols.NOTE, RetroSymbols.NOTES),
        animationType = EmotionAnimationType.BOUNCE
    ),

    // 졸림
    SLEEPY(
        symbols = listOf(RetroSymbols.SLEEP, RetroSymbols.SLEEP_2),
        animationType = EmotionAnimationType.FLOAT_UP
    ),

    // 놀람
    SURPRISED(
        symbols = listOf(RetroSymbols.EXCLAIM, RetroSymbols.EXCLAIM_2, RetroSymbols.QUESTION_EXCLAIM),
        animationType = EmotionAnimationType.POP
    ),

    // 부끄러움 (츤데레용)
    SHY(
        symbols = listOf(RetroSymbols.BLUSH, RetroSymbols.DOTS),
        animationType = EmotionAnimationType.PULSE
    ),

    // 당황 (덤벙이용)
    CONFUSED(
        symbols = listOf(RetroSymbols.SWEAT, RetroSymbols.SWEAT_2, RetroSymbols.SPIRAL),
        animationType = EmotionAnimationType.WOBBLE
    ),

    // 의문
    QUESTIONING(
        symbols = listOf(RetroSymbols.QUESTION, RetroSymbols.QUESTION_2),
        animationType = EmotionAnimationType.TILT
    ),

    // 슬픔/아쉬움
    SAD(
        symbols = listOf(RetroSymbols.DOTS, RetroSymbols.DASH),
        animationType = EmotionAnimationType.DRIP
    ),

    // 거의 달성
    ALMOST(
        symbols = listOf(RetroSymbols.STAR, RetroSymbols.STARS_2),
        animationType = EmotionAnimationType.SHAKE
    ),

    // 반가움 (방치 후)
    WELCOME(
        symbols = listOf(RetroSymbols.SPARKLE, RetroSymbols.STAR),
        animationType = EmotionAnimationType.SPARKLE
    )
}

// ===== 애니메이션 타입 =====
enum class EmotionAnimationType {
    FLOAT_UP,   // 위로 떠오름
    BURST,      // 터지듯 퍼짐
    SPARKLE,    // 반짝반짝
    BOUNCE,     // 통통 튀기
    POP,        // 팝업
    PULSE,      // 커졌다 작아짐
    WOBBLE,     // 흔들흔들
    TILT,       // 갸웃
    DRIP,       // 떨어짐
    SHAKE       // 좌우 흔들림
}

// ===== 성격별 터치 반응 =====
object PersonalityTouchReaction {

    fun getSymbol(
        personality: PetPersonalityV2,
        touchCount: Int,  // 연속 터치 횟수
        isRapidTap: Boolean  // 빠른 연타 여부
    ): Pair<String, EmotionAnimationType> {
        return when (personality) {
            PetPersonalityV2.LOYAL -> when {
                isRapidTap -> RetroSymbols.HEARTS_2 to EmotionAnimationType.BURST
                touchCount >= 3 -> RetroSymbols.HEART to EmotionAnimationType.FLOAT_UP
                else -> RetroSymbols.HEART_EMPTY to EmotionAnimationType.FLOAT_UP
            }

            PetPersonalityV2.TSUNDERE -> when {
                isRapidTap -> "${RetroSymbols.BLUSH}${RetroSymbols.HEART}" to EmotionAnimationType.BURST
                touchCount >= 3 -> RetroSymbols.BLUSH to EmotionAnimationType.PULSE
                else -> RetroSymbols.DOTS to EmotionAnimationType.PULSE
            }

            PetPersonalityV2.FOODIE -> when {
                isRapidTap -> RetroSymbols.HEARTS_3 to EmotionAnimationType.BURST
                touchCount >= 3 -> RetroSymbols.HEARTS_2 to EmotionAnimationType.BOUNCE
                else -> RetroSymbols.HEART to EmotionAnimationType.BOUNCE
            }

            PetPersonalityV2.PLAYFUL -> when {
                isRapidTap -> RetroSymbols.NOTES to EmotionAnimationType.BURST
                touchCount >= 3 -> RetroSymbols.NOTE_DOUBLE to EmotionAnimationType.BOUNCE
                else -> RetroSymbols.NOTE to EmotionAnimationType.BOUNCE
            }

            PetPersonalityV2.TIMID -> when {
                isRapidTap -> RetroSymbols.SWEAT to EmotionAnimationType.WOBBLE
                touchCount >= 3 -> RetroSymbols.HEART_EMPTY to EmotionAnimationType.PULSE
                else -> RetroSymbols.DOTS to EmotionAnimationType.PULSE
            }

            PetPersonalityV2.CLUMSY -> when {
                isRapidTap -> RetroSymbols.SPIRAL to EmotionAnimationType.WOBBLE
                touchCount >= 3 -> RetroSymbols.SWEAT to EmotionAnimationType.WOBBLE
                else -> RetroSymbols.EXCLAIM to EmotionAnimationType.POP
            }
        }
    }

    // 방치 후 터치 (30분 이상)
    fun getWelcomeBackSymbol(personality: PetPersonalityV2): Pair<String, EmotionAnimationType> {
        return when (personality) {
            PetPersonalityV2.LOYAL -> RetroSymbols.HEART to EmotionAnimationType.FLOAT_UP
            PetPersonalityV2.TSUNDERE -> "${RetroSymbols.DOTS}${RetroSymbols.HEART}" to EmotionAnimationType.PULSE
            PetPersonalityV2.FOODIE -> RetroSymbols.EXCLAIM_2 to EmotionAnimationType.POP
            PetPersonalityV2.PLAYFUL -> RetroSymbols.NOTES to EmotionAnimationType.BOUNCE
            PetPersonalityV2.TIMID -> RetroSymbols.HEART_EMPTY to EmotionAnimationType.FLOAT_UP
            PetPersonalityV2.CLUMSY -> RetroSymbols.QUESTION_EXCLAIM to EmotionAnimationType.POP
        }
    }
}

// ===== 진행률별 자동 감정 =====
object ProgressEmotions {

    fun getEmotionForProgress(progressPercent: Int): Pair<String, EmotionAnimationType>? {
        return when (progressPercent) {
            in 10..15 -> RetroSymbols.NOTE to EmotionAnimationType.BOUNCE  // 시작
            in 48..52 -> RetroSymbols.HEART to EmotionAnimationType.FLOAT_UP  // 절반
            in 88..92 -> RetroSymbols.HEARTS_2 to EmotionAnimationType.SHAKE  // 거의 다
            100 -> RetroSymbols.HEARTS_3 to EmotionAnimationType.BURST  // 달성
            in 101..150 -> RetroSymbols.NOTES to EmotionAnimationType.BOUNCE  // 초과
            else -> null
        }
    }

    fun getGoalAchieved(): Pair<String, EmotionAnimationType> {
        return RetroSymbols.HEARTS_3 to EmotionAnimationType.BURST
    }

    fun getLevelUp(): Pair<String, EmotionAnimationType> {
        return RetroSymbols.HEARTS_2 to EmotionAnimationType.BURST
    }

    fun getEvolution(): Pair<String, EmotionAnimationType> {
        return RetroSymbols.HEARTS_3 to EmotionAnimationType.BURST
    }
}

// ===== 단일 파티클 (연기처럼 위로 피어오름) =====
@Composable
private fun SingleParticle(
    symbol: String,
    startX: Float,  // 시작 X 오프셋 (-1f ~ 1f)
    delayMs: Int,
    durationMs: Int,
    size: Dp,
    driftX: Float,  // 위로 올라가면서 좌우 흔들림 (-1f ~ 1f)
    onFinished: () -> Unit = {}
) {
    var started by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMs, easing = LinearOutSlowInEasing),
        label = "progress",
        finishedListener = { onFinished() }
    )

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        started = true
    }

    if (started) {
        // 위로 올라가면서 점점 사라짐
        val offsetY = (-80f * progress).dp  // 위로 80dp 이동
        val offsetX = (startX * 25f + driftX * 20f * progress).dp  // 좌우 흔들림
        val alpha = 1f - (progress * 0.9f)  // 점점 투명해짐
        val scale = 1f + (progress * 0.3f)  // 살짝 커짐

        Text(
            text = symbol,
            fontSize = size.value.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .scale(scale)
                .alpha(alpha)
        )
    }
}

// ===== 감정 심볼 Composable (파티클 효과) =====
@Composable
fun EmotionSymbolDisplay(
    symbol: String,
    animationType: EmotionAnimationType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    onAnimationEnd: () -> Unit = {}
) {
    // 파티클 개수 (애니메이션 타입에 따라)
    val particleCount = when (animationType) {
        EmotionAnimationType.BURST -> 5
        EmotionAnimationType.SPARKLE -> 4
        EmotionAnimationType.FLOAT_UP -> 3
        else -> 3
    }

    var finishedCount by remember { mutableIntStateOf(0) }

    // 파티클 설정 (랜덤하게 배치)
    val particleConfigs = remember {
        List(particleCount) { index ->
            ParticleConfig(
                startX = when (index) {
                    0 -> 0f
                    1 -> -0.6f
                    2 -> 0.6f
                    3 -> -0.3f
                    else -> 0.3f
                },
                delayMs = index * 80,  // 순차적으로 생성
                durationMs = when (animationType) {
                    EmotionAnimationType.BURST -> 800
                    EmotionAnimationType.SPARKLE -> 1000
                    else -> 1200
                } + (index * 100),
                size = when (index) {
                    0 -> size
                    else -> size * (0.7f + (index % 2) * 0.2f)
                },
                driftX = when (index) {
                    0 -> 0f
                    1 -> -0.5f
                    2 -> 0.5f
                    3 -> 0.3f
                    else -> -0.3f
                }
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        particleConfigs.forEachIndexed { index, config ->
            SingleParticle(
                symbol = symbol,
                startX = config.startX,
                delayMs = config.delayMs,
                durationMs = config.durationMs,
                size = config.size,
                driftX = config.driftX,
                onFinished = {
                    finishedCount++
                    if (finishedCount >= particleCount) {
                        onAnimationEnd()
                    }
                }
            )
        }
    }
}

private data class ParticleConfig(
    val startX: Float,
    val delayMs: Int,
    val durationMs: Int,
    val size: Dp,
    val driftX: Float
)

// ===== 터치 상태 관리 =====
class TouchState {
    var lastTouchTime: Long = 0L
    var touchCount: Int = 0
    var lastInteractionTime: Long = 0L

    fun onTouch(): TouchResult {
        val now = System.currentTimeMillis()
        val timeSinceLastTouch = now - lastTouchTime
        val timeSinceLastInteraction = now - lastInteractionTime

        // 방치 후 복귀 (30분 이상)
        if (timeSinceLastInteraction > 30 * 60 * 1000) {
            lastTouchTime = now
            lastInteractionTime = now
            touchCount = 1
            return TouchResult.WELCOME_BACK
        }

        // 빠른 연타 (500ms 이내)
        val isRapidTap = timeSinceLastTouch < 500

        // 연속 터치 (3초 이내면 카운트 증가)
        if (timeSinceLastTouch < 3000) {
            touchCount++
        } else {
            touchCount = 1
        }

        lastTouchTime = now
        lastInteractionTime = now

        return when {
            isRapidTap && touchCount >= 3 -> TouchResult.RAPID_TAP
            touchCount >= 5 -> TouchResult.COMBO
            touchCount >= 3 -> TouchResult.MULTI
            else -> TouchResult.SINGLE
        }
    }

    enum class TouchResult {
        SINGLE,       // 단일 터치
        MULTI,        // 연속 터치 (3회+)
        RAPID_TAP,    // 빠른 연타
        COMBO,        // 콤보 (5회+)
        WELCOME_BACK  // 방치 후 복귀
    }
}
