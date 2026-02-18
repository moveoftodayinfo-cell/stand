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

// 다국어 헬퍼
private object StreakStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun achievingPercent(percent: Int): String = when (getLang()) {
        "ko" -> "$percent% 달성 중"
        "ja" -> "$percent% 達成中"
        "zh" -> "$percent% 进行中"
        "es" -> "$percent% progreso"
        else -> "$percent% progress"
    }

    fun stepsUnit(): String = when (getLang()) {
        "ko" -> "보"
        "ja" -> "歩"
        "zh" -> "步"
        else -> "steps"
    }

    fun consecutiveDays(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속 달성"
        "ja" -> "${days}日連続達成"
        "zh" -> "连续${days}天达成"
        "es" -> "${days} días consecutivos"
        else -> "${days}-day streak"
    }

    fun totalDistance(): String = when (getLang()) {
        "ko" -> "총 거리"
        "ja" -> "総距離"
        "zh" -> "总距离"
        "es" -> "Distancia"
        else -> "Distance"
    }

    fun achievementDays(): String = when (getLang()) {
        "ko" -> "달성 일수"
        "ja" -> "達成日数"
        "zh" -> "达成天数"
        "es" -> "Días logrados"
        else -> "Days"
    }

    fun consecutiveStreak(): String = when (getLang()) {
        "ko" -> "연속 달성"
        "ja" -> "連続達成"
        "zh" -> "连续达成"
        "es" -> "Racha"
        else -> "Streak"
    }

    fun daysUnit(days: Int): String = when (getLang()) {
        "ko" -> "${days}일"
        "ja" -> "${days}日"
        "zh" -> "${days}天"
        "es" -> "${days}d"
        else -> "${days}d"
    }

    fun savedToGallery(): String = when (getLang()) {
        "ko" -> "갤러리에 저장됨"
        "ja" -> "ギャラリーに保存"
        "zh" -> "已保存到相册"
        "es" -> "Guardado en galería"
        else -> "Saved to gallery"
    }

    fun saveFailed(msg: String): String = when (getLang()) {
        "ko" -> "저장 실패: $msg"
        "ja" -> "保存失敗: $msg"
        "zh" -> "保存失败: $msg"
        "es" -> "Error al guardar: $msg"
        else -> "Save failed: $msg"
    }

    fun streakDefenseSuccess(): String = when (getLang()) {
        "ko" -> "스트릭 방어 성공!"
        "ja" -> "ストリーク防御成功！"
        "zh" -> "连胜保护成功！"
        "es" -> "¡Racha protegida!"
        else -> "Streak Protected!"
    }

    fun defenseUsedMessage(days: Int): String = when (getLang()) {
        "ko" -> "방어 티켓을 사용해서\n${days}일 스트릭을 이어갑니다!"
        "ja" -> "防御チケットを使用して\n${days}日ストリークを継続！"
        "zh" -> "使用保护券\n保持${days}天连胜！"
        "es" -> "Usaste un ticket de protección\n¡Tu racha de ${days} días continúa!"
        else -> "Used defense ticket\nto keep your ${days}-day streak!"
    }

    fun remainingTickets(count: Int): String = when (getLang()) {
        "ko" -> "남은 방어 티켓: ${count}장"
        "ja" -> "残り防御チケット: ${count}枚"
        "zh" -> "剩余保护券: ${count}张"
        "es" -> "Tickets restantes: $count"
        else -> "Tickets left: $count"
    }

    fun continueBtn(): String = when (getLang()) {
        "ko" -> "계속하기"
        "ja" -> "続ける"
        "zh" -> "继续"
        "es" -> "Continuar"
        else -> "Continue"
    }

    // 성격별 축하 대사 - 첫날
    fun toughFirstDay(): String = when (getLang()) {
        "ko" -> "오늘도 달성했다고?\n됐다. 좋은 시작이야."
        "ja" -> "今日も達成したって?\nよし、いいスタートだ。"
        "zh" -> "今天也达成了?\n好，不错的开始。"
        "es" -> "¿Lograste la meta hoy?\nBien. Buen comienzo."
        else -> "You made it today?\nGood. Nice start."
    }

    fun toughStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "총 ${days}일 달성,\n${km}km 걸었다고?\n됐다. 잘했어."
        "ja" -> "合計${days}日達成、\n${km}km歩いた？\nよし、よくやった。"
        "zh" -> "共${days}天达成，\n走了${km}km?\n好，干得不错。"
        "es" -> "${days} días, ${km}km.\nBien hecho."
        else -> "${days} days, ${km}km.\nGood job."
    }

    fun cuteFirstDay(): String = when (getLang()) {
        "ko" -> "우와~! 오늘도 달성!\n대단해용!"
        "ja" -> "わあ~！今日も達成！\nすごいです~！"
        "zh" -> "哇~！今天也达成了！\n太厉害了~！"
        "es" -> "¡Woow~! ¡Lo lograste!\n¡Increíble~!"
        else -> "Wow~! You did it!\nAmazing~!"
    }

    fun cuteStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "우와~! 총 ${days}일 달성!\n${km}km 걸었다니 대단해용!"
        "ja" -> "わあ~！合計${days}日達成！\n${km}km歩いたなんてすごい~！"
        "zh" -> "哇~！共${days}天达成！\n走了${km}km太厉害了~！"
        "es" -> "¡Wow~! ¡${days} días!\n¡${km}km es increíble~!"
        else -> "Wow~! ${days} days!\n${km}km is amazing~!"
    }

    fun tsundereFirstDay(): String = when (getLang()) {
        "ko" -> "흥, 오늘도 달성?\n뭐... 나쁘지 않네."
        "ja" -> "ふん、今日も達成？\nまあ...悪くないね。"
        "zh" -> "哼，今天也达成了？\n嗯...还不错吧。"
        "es" -> "Hmph, ¿lo lograste?\nBueno... no está mal."
        else -> "Hmph, you made it?\nWell... not bad."
    }

    fun tsundereStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "흥, 총 ${days}일 달성에\n${km}km?\n뭐... 나쁘지 않네."
        "ja" -> "ふん、合計${days}日達成で\n${km}km？\nまあ...悪くないね。"
        "zh" -> "哼，共${days}天达成，\n${km}km？\n嗯...还不错吧。"
        "es" -> "Hmph, ${days} días,\n${km}km?\nBueno... no está mal."
        else -> "Hmph, ${days} days,\n${km}km?\nWell... not bad."
    }

    fun dialectFirstDay(): String = when (getLang()) {
        "ko" -> "오늘도 달성했노~\n좋은 시작이다!"
        "ja" -> "今日も達成やで~\nええスタートや！"
        "zh" -> "今天也达成咯~\n好的开始呀！"
        "es" -> "¡Lo lograste hoy~!\n¡Buen inicio!"
        else -> "You did it today~!\nGreat start!"
    }

    fun dialectStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "총 ${days}일 달성에\n${km}km 걸었노~\nㄹㅇ 대단하다!"
        "ja" -> "合計${days}日達成で\n${km}km歩いたんか~\nマジすごいで！"
        "zh" -> "共${days}天达成，\n走了${km}km呀~\n真的很厉害！"
        "es" -> "¡${days} días y ${km}km~!\n¡De verdad genial!"
        else -> "${days} days, ${km}km~!\nReally awesome!"
    }

    fun timidFirstDay(): String = when (getLang()) {
        "ko" -> "대, 대단해요...!\n오늘도 달성...!"
        "ja" -> "す、すごいです...！\n今日も達成...！"
        "zh" -> "好, 好厉害...！\n今天也达成了...！"
        "es" -> "In-increíble...!\n¡Lo lograste hoy...!"
        else -> "A-amazing...!\nYou made it today...!"
    }

    fun timidStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "대, 대단해요...! 총 ${days}일 달성에\n${km}km 걸었어요...!"
        "ja" -> "す、すごいです...！合計${days}日達成で\n${km}km歩いた...！"
        "zh" -> "好, 好厉害...！共${days}天达成，\n走了${km}km...！"
        "es" -> "In-increíble...! ${days} días,\n${km}km...!"
        else -> "A-amazing...! ${days} days,\n${km}km...!"
    }

    fun positiveFirstDay(): String = when (getLang()) {
        "ko" -> "오늘도 달성!\n좋은 시작! 최고야!"
        "ja" -> "今日も達成！\nいいスタート！最高！"
        "zh" -> "今天也达成了！\n好的开始！最棒！"
        "es" -> "¡Lo lograste!\n¡Buen comienzo! ¡Genial!"
        else -> "You did it!\nGreat start! Awesome!"
    }

    fun positiveStreak(days: Int, km: Float): String = when (getLang()) {
        "ko" -> "총 ${days}일 달성!\n${km}km 걸었어! 최고야!"
        "ja" -> "合計${days}日達成！\n${km}km歩いた！最高！"
        "zh" -> "共${days}天达成！\n走了${km}km！最棒！"
        "es" -> "¡${days} días! ¡${km}km!\n¡Eres genial!"
        else -> "${days} days! ${km}km!\nYou're awesome!"
    }
}

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
    successDays: Int = 0,  // 전체 달성 일수 (연속 아님)
    totalKm: Float = 0f,
    // 첫 주 판단용 파라미터
    isFirstWeek: Boolean = false,  // streak 시작 후 첫 주인지
    streakStartDayOfWeek: Int = 0,  // streak 시작 요일 (0=일, 1=월, ...)
    // 현재 상태 공유용 파라미터
    isQuickShare: Boolean = false,
    currentSpeech: String = "",
    currentSteps: Int = 0,
    goalSteps: Int = 0,
    goalUnit: String = "steps",  // "steps" 또는 "km"
    currentDistance: Double = 0.0,  // km 모드용 현재 거리
    petStateV2: PetState? = null  // V2 펫 상태 (있으면 V2 스프라이트 사용)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kenneyFont = rememberKenneyFont()

    // 장비 상태 (장비 시스템)
    val equipmentState = remember {
        PreferenceManager(context).getEquipmentState()
    }

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
    val petSpeech = remember(isQuickShare, currentSpeech, streakCount, petType, successDays, totalKm) {
        if (isQuickShare && currentSpeech.isNotEmpty()) {
            // 현재 말풍선을 AnnotatedString으로 변환 (볼드 없이 단순하게)
            buildAnnotatedString { append(currentSpeech) }
        } else {
            getStreakCelebrationSpeech(petType.personality, streakCount, successDays, totalKm)
        }
    }

    // Progress 계산 (빠른 공유용) - 음수 방지
    val safeCurrentSteps = currentSteps.coerceAtLeast(0)
    val progressPercent = if (goalSteps > 0) ((safeCurrentSteps.toFloat() / goalSteps) * 100).toInt().coerceIn(0, 100) else 0
    val isKmMode = goalUnit == "km"

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
                            successDays = successDays,
                            totalKm = totalKm,
                            petType = petType,
                            petName = petName,
                            equippedTitle = equippedTitle,
                            petSpeech = petSpeech,
                            today = today,
                            dayNames = dayNames,
                            weeklyAchievements = weeklyAchievements,
                            kenneyFont = kenneyFont,
                            graphicsLayer = fullCardGraphicsLayer,
                            isQuickShare = isQuickShare,
                            currentSteps = currentSteps,
                            goalSteps = goalSteps,
                            progressPercent = progressPercent,
                            isFirstWeek = isFirstWeek,
                            streakStartDayOfWeek = streakStartDayOfWeek,
                            isKmMode = isKmMode,
                            currentDistance = currentDistance,
                            safeCurrentSteps = safeCurrentSteps,
                            petStateV2 = petStateV2,
                            equipmentState = equipmentState
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
                            graphicsLayer = stickerGraphicsLayer,
                            petStateV2 = petStateV2,
                            equipmentState = equipmentState
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
    successDays: Int,
    totalKm: Float,
    petType: PetType,
    petName: String = "",
    equippedTitle: String? = null,
    petSpeech: androidx.compose.ui.text.AnnotatedString,
    today: Int,
    dayNames: List<String>,
    weeklyAchievements: List<Boolean>,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    isQuickShare: Boolean = false,
    currentSteps: Int = 0,
    goalSteps: Int = 0,
    progressPercent: Int = 0,
    isFirstWeek: Boolean = false,
    streakStartDayOfWeek: Int = 0,
    isKmMode: Boolean = false,
    currentDistance: Double = 0.0,
    safeCurrentSteps: Int = 0,
    petStateV2: PetState? = null,
    equipmentState: EquipmentState
) {
    val stripeWidth = 4.dp

    // 인스타 스토리용 9:16 비율
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
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
                    .height(240.dp)
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
                // Speech bubble (하단이 디스플레이 정중앙에 위치)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(120.dp),  // 디스플레이 절반 (240dp / 2)
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SpeechBubbleMultiline(text = petSpeech, fontSize = 12.sp, maxWidth = 220.dp)
                }

                // 스프라이트 + 펫 이름 (절대 위치 - pet name 기준)
                // V2 펫 상태가 있으면 V2 스프라이트 사용, 없으면 V1 폴백
                val fullCardYOffset = petStateV2?.let {
                    it.petType.getDisplayYOffsetDp(it.stage)
                } ?: 0f

                val displayHeight = 240.dp
                val petSize = 128.dp  // 0.8x 축소 (160 → 128)
                val nameY = PetDisplayConstants.calculateNameY(displayHeight)
                val petY = PetDisplayConstants.calculatePetY(displayHeight, petSize, fullCardYOffset)

                // 펫 스프라이트 (절대 위치)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = petY)
                        .size(petSize),
                    contentAlignment = Alignment.Center
                ) {
                    if (petStateV2 != null) {
                        PetSpriteFromStateWithEquipment(
                            petState = petStateV2,
                            equipmentState = equipmentState,
                            isWalking = false,
                            progressPercent = 0,
                            baseSizeDp = 128,  // 0.8x
                            monochrome = true
                        )
                    } else {
                        PetSpriteWithSyncedGlow(
                            petType = petType,
                            isWalking = false,
                            size = 128.dp,  // 0.8x
                            monochrome = true,
                            frameDurationMs = 200
                        )
                    }
                }

                // 칭호 + 펫 이름 (절대 위치 - 기준점)
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
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = nameY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Instruction
            if (isQuickShare) {
                // 빠른 공유: 현재 진행률 표시
                Text(
                    text = StreakStrings.achievingPercent(progressPercent),
                    fontSize = 22.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = buildAnnotatedString {
                        if (isKmMode) {
                            // km 모드: 거리로 표시
                            val goalKm = goalSteps / 1300.0
                            val safeDistance = currentDistance.coerceAtLeast(0.0)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("%.2f".format(safeDistance))
                            }
                            append(" / %.2f km".format(goalKm))
                        } else {
                            // 걸음 모드
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("%,d".format(safeCurrentSteps))
                            }
                            append(" / %,d ${StreakStrings.stepsUnit()}".format(goalSteps))
                        }
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
                    text = StreakStrings.consecutiveDays(streakCount),
                    fontSize = 16.sp,
                    color = MockupColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Week Card
            // 첫 주: 시작 요일부터 7일 표시 (예: TUE부터 시작하면 TUE~MON)
            // 그 이후: 일~토 고정
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MockupColors.CardBackground)
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // 첫 주면 시작 요일부터 순환, 아니면 일~토 고정
                val displayDayNames = if (isFirstWeek) {
                    (0 until 7).map { dayNames[(streakStartDayOfWeek + it) % 7] }
                } else {
                    dayNames
                }

                // 첫 주면 달성 데이터도 시작 요일 기준으로 재배열
                val displayAchievements = if (isFirstWeek) {
                    (0 until 7).map { weeklyAchievements.getOrElse((streakStartDayOfWeek + it) % 7) { false } }
                } else {
                    weeklyAchievements
                }

                // 오늘 요일의 표시 위치
                val todayDisplayIndex = if (isFirstWeek) {
                    (today - streakStartDayOfWeek + 7) % 7
                } else {
                    today
                }

                Column {
                    // Day labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayDayNames.forEachIndexed { index, day ->
                            // 오늘 요일 강조
                            val isToday = index == todayDisplayIndex
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) MockupColors.TextPrimary else MockupColors.TextMuted,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stars (실제 달성 여부로 채움)
                    // 오늘 별 펄스 애니메이션
                    val infiniteTransition = rememberInfiniteTransition(label = "todayStarPulse")
                    val todayStarScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayAchievements.forEachIndexed { index, achieved ->
                            val isToday = index == todayDisplayIndex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .scale(if (isToday) todayStarScale else 1f),
                                contentAlignment = Alignment.Center
                            ) {
                                PixelIcon(
                                    iconName = "icon_star",
                                    size = 24.dp,
                                    // 달성한 날: 진한 회색, 안 한 날: 연한 회색
                                    tint = if (achieved) Color.Black else Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Stats Card (파라미터로 전달받은 실제 데이터 사용)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MockupColors.CardBackground)
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 총 거리
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1fkm", totalKm),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Text(
                            text = StreakStrings.totalDistance(),
                            fontSize = 11.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                    // 전체 달성 일수
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = StreakStrings.daysUnit(successDays),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Text(
                            text = StreakStrings.achievementDays(),
                            fontSize = 11.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                    // 연속 달성
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = StreakStrings.daysUnit(streakCount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Text(
                            text = StreakStrings.consecutiveStreak(),
                            fontSize = 11.sp,
                            color = MockupColors.TextMuted
                        )
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
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    petStateV2: PetState? = null,
    equipmentState: EquipmentState
) {
    // Dialog wrapper (내용에 맞게 크기 조절)
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
                    .height(240.dp)
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
                    // Speech bubble (하단이 디스플레이 정중앙에 위치)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .height(108.dp),  // (240dp - padding 24dp) / 2 = 108dp
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        SpeechBubbleMultiline(text = petSpeech, fontSize = 11.sp, maxWidth = 200.dp)
                    }

                    // 스프라이트 + 펫 이름 + rebon 로고 (절대 위치 - pet name 기준)
                    // V2 펫 상태가 있으면 V2 스프라이트 사용, 없으면 V1 폴백
                    val stickerYOffset = petStateV2?.let {
                        it.petType.getDisplayYOffsetDp(it.stage)
                    } ?: 0f

                    val displayHeight = 240.dp - 24.dp  // padding 제외
                    val petSize = 112.dp  // 0.8x 축소 (140 → 112)
                    val nameY = PetDisplayConstants.calculateNameY(displayHeight)
                    val petY = PetDisplayConstants.calculatePetY(displayHeight, petSize, stickerYOffset)

                    // 펫 스프라이트 (절대 위치)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = petY)
                            .size(petSize),
                        contentAlignment = Alignment.Center
                    ) {
                        if (petStateV2 != null) {
                            PetSpriteFromStateWithEquipment(
                                petState = petStateV2,
                                equipmentState = equipmentState,
                                isWalking = false,
                                progressPercent = 0,
                                baseSizeDp = 112,  // 0.8x
                                monochrome = true
                            )
                        } else {
                            PetSpriteWithSyncedGlow(
                                petType = petType,
                                isWalking = false,
                                size = 112.dp,  // 0.8x
                                monochrome = true,
                                frameDurationMs = 200
                            )
                        }
                    }

                    // 칭호 + 펫 이름 (절대 위치 - 기준점)
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
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = nameY)
                        )
                    }

                    // rebon 로고 (하단 고정)
                    Text(
                        text = "rebon",
                        fontSize = 12.sp,
                        fontFamily = kenneyFont,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 8.dp)  // 더 아래로 (Box 밖으로 나감)
                    )
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
 * 다국어 지원 (ko, ja, zh, es, en)
 */
private fun getStreakCelebrationSpeech(
    personality: PetPersonality,
    streakDays: Int,
    successDays: Int,
    totalKm: Float
): androidx.compose.ui.text.AnnotatedString {
    val text = when (personality) {
        PetPersonality.TOUGH -> if (streakDays == 1) {
            StreakStrings.toughFirstDay()
        } else {
            StreakStrings.toughStreak(successDays, totalKm)
        }

        PetPersonality.CUTE -> if (streakDays == 1) {
            StreakStrings.cuteFirstDay()
        } else {
            StreakStrings.cuteStreak(successDays, totalKm)
        }

        PetPersonality.TSUNDERE -> if (streakDays == 1) {
            StreakStrings.tsundereFirstDay()
        } else {
            StreakStrings.tsundereStreak(successDays, totalKm)
        }

        PetPersonality.DIALECT -> if (streakDays == 1) {
            StreakStrings.dialectFirstDay()
        } else {
            StreakStrings.dialectStreak(successDays, totalKm)
        }

        PetPersonality.TIMID -> if (streakDays == 1) {
            StreakStrings.timidFirstDay()
        } else {
            StreakStrings.timidStreak(successDays, totalKm)
        }

        PetPersonality.POSITIVE -> if (streakDays == 1) {
            StreakStrings.positiveFirstDay()
        } else {
            StreakStrings.positiveStreak(successDays, totalKm)
        }
    }
    return buildAnnotatedString { append(text) }
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

        Toast.makeText(context, StreakStrings.savedToGallery(), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, StreakStrings.saveFailed(e.message ?: ""), Toast.LENGTH_SHORT).show()
    }
}

/**
 * 스트릭 방어 티켓 사용 다이얼로그
 * 스트릭이 끊길 뻔했지만 방어 티켓으로 유지했을 때 표시
 */
@Composable
fun StreakDefenseDialog(
    currentStreak: Int,
    remainingTickets: Int,
    onDismiss: () -> Unit,
    onContinue: () -> Unit  // 축하 다이얼로그로 이어가기
) {
    val kenneyFont = rememberKenneyFont()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MockupColors.Background, RoundedCornerShape(24.dp))
                .border(3.dp, MockupColors.Border, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 방패 아이콘
                Text(
                    text = UnicodeSymbols.SHIELD,
                    fontSize = 64.sp,
                    color = MockupColors.Blue
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 메인 메시지
                Text(
                    text = StreakStrings.streakDefenseSuccess(),
                    fontSize = 24.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 설명
                Text(
                    text = StreakStrings.defenseUsedMessage(currentStreak),
                    fontSize = 16.sp,
                    color = MockupColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 남은 티켓 수
                Box(
                    modifier = Modifier
                        .background(MockupColors.BlueLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = StreakStrings.remainingTickets(remainingTickets),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.Blue
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 확인 버튼
                Button(
                    onClick = {
                        onDismiss()
                        onContinue()  // 축하 다이얼로그로 이어가기
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MockupColors.Blue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = StreakStrings.continueBtn(),
                        fontSize = 16.sp,
                        fontFamily = kenneyFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
