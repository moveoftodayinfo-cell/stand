package com.moveoftoday.walkorwait.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager
import com.moveoftoday.walkorwait.StreakCelebrationDialog
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * 오늘 자정부터 현재까지의 시간을 계산하여 텍스트로 반환
 */
@Composable
private fun getBlockedTimeText(): String {
    var timeText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMs = now.timeInMillis - midnight.timeInMillis
            val hours = (diffMs / (1000 * 60 * 60)).toInt()
            val minutes = ((diffMs / (1000 * 60)) % 60).toInt()

            timeText = when {
                hours > 0 -> "${hours}시간 ${minutes}분째"
                minutes > 0 -> "${minutes}분째"
                else -> "시작"
            }

            delay(60000) // 1분마다 업데이트
        }
    }

    return timeText
}

/**
 * Main screen with Pet Layout style:
 * 1. Title → "Stand" (32sp, Kenney Font) + 설정/스트릭
 * 2. Display Area → 240dp, 수평 줄무늬, SpeechBubble + PetSpriteWithSyncedGlow
 * 3. Instruction → 걸음수 진행 상태 (22sp)
 * 4. Middle Content → 달성률 프로그레스
 * 5. Chat section → 채팅 입력
 */
@Composable
fun PetMainScreen(
    petType: PetType,
    petName: String,
    happinessLevel: Int,
    stepCount: Int,
    goalSteps: Int,
    streakCount: Int,
    onSettingsClick: () -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    val kenneyFont = rememberKenneyFont()
    val isGoalAchieved = stepCount >= goalSteps
    val progressPercent = ((stepCount.toFloat() / goalSteps) * 100).toInt().coerceIn(0, 100)
    val isWalking = progressPercent > 0 && !isGoalAchieved

    // Determine background color based on state
    val backgroundColor = when {
        isGoalAchieved -> MockupColors.AchievedBackground
        happinessLevel <= 1 -> MockupColors.SadBackground
        else -> MockupColors.Background
    }

    // Pet speech 로직 - 백그라운드 복귀 또는 새 대화 시에만 변경
    var petResponse by remember { mutableStateOf("") }
    var speechRefreshTrigger by remember { mutableStateOf(0) }

    // 현재 상태에 맞는 대사를 가져오는 함수 (달성률 구간별 다양한 대사)
    fun getCurrentSpeech(): String {
        return when {
            isGoalAchieved -> PetDialogues.getGoalAchievedMessage(petType.personality)  // 100%
            progressPercent >= 90 -> PetDialogues.getAlmostThereMessage(petType.personality)  // 90-99%
            progressPercent >= 75 -> PetDialogues.getThreeQuarterMessage(petType.personality)  // 75-89%
            progressPercent >= 50 -> PetDialogues.getHalfwayMessage(petType.personality)  // 50-74%
            progressPercent >= 25 -> PetDialogues.getQuarterMessage(petType.personality)  // 25-49%
            happinessLevel <= 1 -> PetDialogues.getSadMessage(petType.personality)  // 슬픔 상태
            progressPercent >= 10 -> PetDialogues.getStartedMessage(petType.personality)  // 10-24%
            progressPercent > 0 -> PetDialogues.getJustStartedMessage(petType.personality)  // 1-9%
            else -> PetDialogues.getIdleMessage(petType.personality)  // 0%
        }
    }

    // 기본 대사 (백그라운드 복귀 또는 새 대화 시에만 변경)
    var defaultSpeech by remember { mutableStateOf("") }

    // 최초 로드 시 대사 설정
    LaunchedEffect(Unit) {
        if (defaultSpeech.isEmpty()) {
            defaultSpeech = getCurrentSpeech()
        }
    }

    // 대사 새로고침 트리거가 변경되면 대사 업데이트
    LaunchedEffect(speechRefreshTrigger) {
        if (speechRefreshTrigger > 0) {
            defaultSpeech = getCurrentSpeech()
        }
    }

    // 백그라운드에서 돌아왔을 때 대사 새로고침
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                speechRefreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displaySpeech = if (petResponse.isNotEmpty()) petResponse else defaultSpeech

    // Chat state
    var talkInput by remember { mutableStateOf("") }
    var lastUserMessage by remember { mutableStateOf("") }
    var showUserMessage by remember { mutableStateOf(false) }

    // 챌린지 준비중 다이얼로그
    var showComingSoonDialog by remember { mutableStateOf(false) }

    // 공유 다이얼로그 (목표 달성 or 빠른 공유)
    var showShareDialog by remember { mutableStateOf(false) }
    var isQuickShareMode by remember { mutableStateOf(false) }

    // Reset pet response after delay
    LaunchedEffect(petResponse) {
        if (petResponse.isNotEmpty()) {
            delay(5000)
            petResponse = ""
            showUserMessage = false
        }
    }

    val stripeWidth = 4.dp

    // 준비중 다이얼로그
    if (showComingSoonDialog) {
        ComingSoonDialog(
            onDismiss = { showComingSoonDialog = false },
            hapticManager = hapticManager
        )
    }

    // 공유 다이얼로그
    if (showShareDialog) {
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        val dayIndex = if (today == java.util.Calendar.SUNDAY) 6 else today - 2
        val testWeeklyAchievements = List(7) { index -> index <= dayIndex }

        StreakCelebrationDialog(
            streakCount = streakCount.coerceAtLeast(1),
            weeklyAchievements = testWeeklyAchievements,
            onDismiss = {
                showShareDialog = false
                isQuickShareMode = false
            },
            hapticManager = hapticManager,
            petType = petType,
            petName = petName,
            dailySteps = goalSteps,
            totalKm = (goalSteps * 0.0007f) * streakCount.coerceAtLeast(1),
            screenFreeHours = 3 * streakCount.coerceAtLeast(1),
            // 빠른 공유 모드 파라미터
            isQuickShare = isQuickShareMode,
            currentSpeech = displaySpeech,
            currentSteps = stepCount,
            goalSteps = goalSteps
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Title row: Streak | "Stand" | Settings
        Box(modifier = Modifier.fillMaxWidth()) {
            // 좌측: Streak badge
            StreakBadge(
                streakCount = streakCount,
                inactive = streakCount == 0,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // 중앙: Title "Stand"
            Text(
                text = "Stand",
                fontSize = 32.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )

            // 우측: Settings icon
            IconButton(
                onClick = {
                    hapticManager?.click()
                    onSettingsClick()
                },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterEnd)
            ) {
                PixelIcon(
                    iconName = "icon_gear",
                    size = 24.dp,
                    alpha = if (happinessLevel <= 1) 0.7f else 1f
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Display Area - 240dp, 수평 줄무늬, SpeechBubble + PetSpriteWithSyncedGlow (하트 제거)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = if (isGoalAchieved) Color(0xFFFFF9C4) else Color(0xFFF0F0F0)
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
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp))
        ) {
            // 메인 콘텐츠 (중앙)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Speech bubble
                SpeechBubble(text = displaySpeech, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Pet sprite with glow
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = isWalking || isGoalAchieved,
                    size = 140.dp,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = !isWalking && !isGoalAchieved
                )
            }

            // 공유 아이콘 (우측 상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .border(2.dp, MockupColors.Border.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable {
                        hapticManager?.click()
                        isQuickShareMode = true
                        showShareDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                DrawableIcon(
                    iconName = "icon_camera_grid",
                    size = 20.dp,
                    tint = MockupColors.Border
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 채팅 섹션 (Display Area 바로 밑)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My message bubble (if there's a recent message)
            if (showUserMessage && lastUserMessage.isNotEmpty()) {
                MyMessageBubble(text = lastUserMessage)
            }

            // Talk input area
            TalkInputArea(
                value = talkInput,
                onValueChange = { talkInput = it },
                onSend = {
                    if (talkInput.isNotBlank()) {
                        hapticManager?.lightClick()
                        lastUserMessage = talkInput
                        showUserMessage = true
                        petResponse = PetDialogues.getChatResponse(
                            petType.personality,
                            talkInput,
                            petName,
                            happinessLevel >= 3
                        )
                        talkInput = ""
                        // 대화 후 기본 대사도 갱신 (대화 응답이 사라지면 새로운 대사 표시)
                        speechRefreshTrigger++
                    }
                },
                petName = petName
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Instruction - "오늘 목표 달성률" (22sp)
        Text(
            text = "오늘 목표 달성률",
            fontSize = 22.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Middle Content - 달성률 표시
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 달성률 퍼센트 (큰 글씨)
            Text(
                text = "${progressPercent}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGoalAchieved) Color(0xFF4CAF50) else MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 걸음수 정보
            Text(
                text = "%,d / %,d 보".format(stepCount, goalSteps),
                fontSize = 16.sp,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 프로그레스 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0E0E0))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            ) {
                val progress = (stepCount.toFloat() / goalSteps).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            if (isGoalAchieved) Color(0xFF4CAF50) else MockupColors.Border
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 앱 차단 시간 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PixelIcon(iconName = "icon_time", size = 16.dp, tint = MockupColors.TextSecondary)
                Text(
                    text = if (isGoalAchieved) "목표 달성! 앱 사용 가능" else "앱 차단 ${getBlockedTimeText()}",
                    fontSize = 14.sp,
                    color = if (isGoalAchieved) Color(0xFF4CAF50) else MockupColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 🧪 개발용: 목표 달성 테스트 버튼
        if (BuildConfig.DEBUG) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFCDD2))
                    .border(2.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                    .clickable {
                        hapticManager?.click()
                        isQuickShareMode = false  // 달성 모드
                        showShareDialog = true
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧪 목표 달성 테스트",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
            }
        }

        // 5. Action Button - 챌린지 하러가기
        MockupButton(
            text = "챌린지 하러가기",
            onClick = {
                hapticManager?.click()
                showComingSoonDialog = true
            }
        )
    }
}

/**
 * 준비중 다이얼로그 - 깔끔 레트로 스타일
 */
@Composable
fun ComingSoonDialog(
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val kenneyFont = rememberKenneyFont()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘
                PixelIcon(iconName = "icon_gear", size = 48.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // 타이틀
                Text(
                    text = "준비중",
                    fontSize = 24.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 설명
                Text(
                    text = "챌린지 기능을 준비하고 있어요!\n조금만 기다려주세요.",
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 확인 버튼
                MockupButton(
                    text = "확인",
                    onClick = {
                        hapticManager?.click()
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * Simplified main content for integration with existing WalkOrWaitScreen
 * 튜토리얼 스타일 (수평 줄무늬 + 달성률 UI) 적용
 */
@Composable
fun PetMainContent(
    petType: PetType,
    petName: String,
    happinessLevel: Int,
    stepCount: Int,
    goalSteps: Int,
    streakCount: Int,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
) {
    val isGoalAchieved = stepCount >= goalSteps
    val progressPercent = ((stepCount.toFloat() / goalSteps) * 100).toInt().coerceIn(0, 100)
    val isWalking = progressPercent > 0

    val cardBackgroundColor = when {
        isGoalAchieved -> MockupColors.AchievedCard
        happinessLevel <= 1 -> MockupColors.SadCard
        else -> MockupColors.CardBackground
    }

    var petResponse by remember { mutableStateOf("") }
    val defaultSpeech = when {
        isGoalAchieved -> PetDialogues.getGoalAchievedMessage(petType.personality)
        progressPercent >= 90 -> PetDialogues.getAlmostThereMessage(petType.personality)
        progressPercent >= 50 -> PetDialogues.getHalfwayMessage(petType.personality)
        happinessLevel <= 1 -> PetDialogues.getSadMessage(petType.personality)
        progressPercent > 0 -> PetDialogues.getWalkingMessage(petType.personality, progressPercent)
        else -> PetDialogues.getIdleMessage(petType.personality)
    }
    val displaySpeech = if (petResponse.isNotEmpty()) petResponse else defaultSpeech

    var talkInput by remember { mutableStateOf("") }
    var lastUserMessage by remember { mutableStateOf("") }
    var showUserMessage by remember { mutableStateOf(false) }

    val stripeWidth = 4.dp

    // Reset pet response after delay
    LaunchedEffect(petResponse) {
        if (petResponse.isNotEmpty()) {
            delay(5000)
            petResponse = ""
            showUserMessage = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet area - 튜토리얼 스타일 (수평 줄무늬 배경)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = if (isGoalAchieved) Color(0xFFFFF9C4) else Color(0xFFF0F0F0)
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
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Speech bubble at top
                if (displaySpeech.isNotEmpty()) {
                    SpeechBubble(
                        text = displaySpeech,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Pet sprite in center
                PetSprite(
                    petType = petType,
                    isWalking = isWalking || isGoalAchieved,
                    size = 100.dp,
                    monochrome = true
                )

                // Hearts at bottom
                if (happinessLevel > 0) {
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            val isFilled = index < happinessLevel
                            PixelIcon(
                                iconName = "icon_heart",
                                size = 16.dp,
                                alpha = if (isFilled) 1f else 0.3f
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 튜토리얼 스타일 달성률 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(cardBackgroundColor)
                .border(3.dp, MockupColors.Border, RoundedCornerShape(15.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 걸음 수 표시 (튜토리얼 스타일)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "%,d".format(stepCount),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "/ %,d 보".format(goalSteps),
                        fontSize = 18.sp,
                        color = MockupColors.TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 프로그레스 바 (튜토리얼 스타일)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                ) {
                    val progress = (stepCount.toFloat() / goalSteps).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                if (isGoalAchieved) Color(0xFF4CAF50) else MockupColors.Border
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 달성률 퍼센트와 상태
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PixelIcon(iconName = "icon_boots", size = 16.dp)
                        Text(
                            text = if (isGoalAchieved) "목표 달성!" else "목표까지 %,d보".format(goalSteps - stepCount),
                            fontSize = 14.sp,
                            color = if (isGoalAchieved) Color(0xFF4CAF50) else MockupColors.TextSecondary
                        )
                    }
                    Text(
                        text = "${progressPercent}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My message bubble
            if (showUserMessage && lastUserMessage.isNotEmpty()) {
                MyMessageBubble(text = lastUserMessage)
            }

            // Talk input area
            TalkInputArea(
                value = talkInput,
                onValueChange = { talkInput = it },
                onSend = {
                    if (talkInput.isNotBlank()) {
                        hapticManager?.lightClick()
                        lastUserMessage = talkInput
                        showUserMessage = true
                        petResponse = PetDialogues.getChatResponse(
                            petType.personality,
                            talkInput,
                            petName,
                            happinessLevel >= 3
                        )
                        talkInput = ""
                    }
                },
                petName = petName
            )
        }
    }
}
