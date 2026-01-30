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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.ChallengeManager
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager
import com.moveoftoday.walkorwait.StreakCelebrationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * 1. Title → "rebon" (32sp, Kenney Font) + 설정/스트릭
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
    onChallengeClick: () -> Unit = {},
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier,
    isFreeTime: Boolean = false,  // 자유로운 날/시간 (제어 요일 아니거나 제어 시간대 아님)
    petStateV2: PetState? = null,  // V2 펫 상태 (있으면 V2 스프라이트 사용)
    onTestGoalClick: () -> Unit = {}  // 🧪 테스트: 목표 달성 시뮬레이션
) {
    val kenneyFont = rememberKenneyFont()
    val isGoalAchieved = stepCount >= goalSteps
    val progressPercent = ((stepCount.toFloat() / goalSteps) * 100).toInt().coerceAtLeast(0)  // 100% 초과 허용
    val isWalking = progressPercent > 0 && !isGoalAchieved

    // 배경색 항상 흰색
    val backgroundColor = MockupColors.Background

    // Pet speech 로직 - 백그라운드 복귀 또는 새 대화 시에만 변경
    var petResponse by remember { mutableStateOf("") }
    var speechRefreshTrigger by remember { mutableStateOf(0) }

    // AI 채팅 일일 제한 관리
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferenceManager = remember { com.moveoftoday.walkorwait.PreferenceManager(context) }

    // 목표 단위 (km 또는 steps)
    val goalUnit = preferenceManager.getGoalUnit()
    val isKmMode = goalUnit == "km"

    // 표시용 값 (단위에 맞게 변환)
    val displayCurrent = if (isKmMode) preferenceManager.getTodayDistance() else stepCount.toDouble()
    val displayGoal = if (isKmMode) goalSteps / 1300.0 else goalSteps.toDouble()
    val displayRemaining = (displayGoal - displayCurrent).coerceAtLeast(0.0)
    val unitText = if (isKmMode) "km" else "보"

    // AI 채팅 매니저 (API 키 없으면 스크립트 전용)
    val aiChatManager = remember {
        PetAIChatManager(
            onAIUsed = { preferenceManager.incrementDailyAIChatCount() }
        )
    }
    val coroutineScope = rememberCoroutineScope()

    // 챌린지 완료 시 펫 칭찬 메시지
    val challengeManager = remember { ChallengeManager.getInstance(context) }
    val justCompletedChallenge by challengeManager.justCompletedChallenge.collectAsState()
    val justEndedChallenge by challengeManager.justEndedChallenge.collectAsState()
    val equippedTitle by challengeManager.equippedTitle.collectAsState()
    val justUnlockedTitle by challengeManager.justUnlockedTitle.collectAsState()

    // 칭호가 적용된 펫 이름
    val displayPetName = challengeManager.getPetNameWithTitle(petName)

    LaunchedEffect(justCompletedChallenge) {
        justCompletedChallenge?.let { challenge ->
            petResponse = PetDialogues.getChallengeCompleteMessage(petType.personality, challenge.name)
            hapticManager?.success()
            challengeManager.clearJustCompletedChallenge()
        }
    }

    // 챌린지 실패 시 펫 응원 메시지
    LaunchedEffect(justEndedChallenge) {
        justEndedChallenge?.let { challenge ->
            petResponse = PetDialogues.getChallengeEndedMessage(petType.personality, challenge.name)
            hapticManager?.lightClick()
            challengeManager.clearJustEndedChallenge()
        }
    }

    // 칭호 획득 알림
    var showTitleUnlockedDialog by remember { mutableStateOf(false) }
    var unlockedTitleType by remember { mutableStateOf<com.moveoftoday.walkorwait.ChallengeType?>(null) }

    // 칭호 선택 다이얼로그
    var showTitleSelectionDialog by remember { mutableStateOf(false) }
    val unlockedTitles by challengeManager.unlockedTitles.collectAsState()

    LaunchedEffect(justUnlockedTitle) {
        justUnlockedTitle?.let { titleType ->
            unlockedTitleType = titleType
            showTitleUnlockedDialog = true
            hapticManager?.success()
            challengeManager.clearJustUnlockedTitle()
        }
    }

    // 현재 상태에 맞는 대사를 가져오는 함수 (진행률 대사 + 30% AI 명언)
    fun getCurrentSpeech(): String {
        // 30% 확률로 AI 명언 표시
        val showAIQuote = (0..9).random() < 3
        if (showAIQuote) {
            return PetAIQuoteManager.getQuote(petType.personality)
        }

        // 진행률 기반 대사
        return when {
            isFreeTime -> PetDialogues.getFreeTimeMessage(petType.personality)
            progressPercent > 100 -> PetDialogues.getOverAchievedMessage(petType.personality, progressPercent)
            isGoalAchieved -> PetDialogues.getGoalAchievedMessage(petType.personality)
            progressPercent >= 90 -> PetDialogues.getAlmostThereMessage(petType.personality)
            progressPercent >= 75 -> PetDialogues.getThreeQuarterMessage(petType.personality)
            progressPercent >= 50 -> PetDialogues.getHalfwayMessage(petType.personality)
            progressPercent >= 25 -> PetDialogues.getQuarterMessage(petType.personality)
            happinessLevel <= 1 -> PetDialogues.getSadMessage(petType.personality)
            progressPercent >= 10 -> PetDialogues.getStartedMessage(petType.personality)
            progressPercent > 0 -> PetDialogues.getJustStartedMessage(petType.personality)
            else -> PetDialogues.getIdleMessage(petType.personality)
        }
    }

    // 기본 대사 (백그라운드 복귀 또는 새 대화 시에만 변경)
    var defaultSpeech by remember { mutableStateOf("") }

    // 앱 시작 시 AI 명언 생성 및 대사 설정
    LaunchedEffect(Unit) {
        // 초기 대사 설정 (기본값)
        if (defaultSpeech.isEmpty()) {
            defaultSpeech = getCurrentSpeech()
        }
        // AI 명언 생성 (완료 후 대사 갱신)
        PetAIQuoteManager.generateQuotes(petType.personality, petName)
        // AI 생성 완료 후 대사 갱신
        defaultSpeech = getCurrentSpeech()
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

    // 마일스톤 체크 (10% 단위 달성 시 펫이 먼저 말함)
    LaunchedEffect(progressPercent) {
        if (!isFreeTime) {
            val newMilestone = preferenceManager.checkNewMilestone(progressPercent)
            if (newMilestone != null) {
                petResponse = PetDialogues.getMilestoneMessage(petType.personality, newMilestone)
                preferenceManager.markMilestoneShown(newMilestone)
                hapticManager?.success()
            }
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

    // Reset pet response after delay (1분 유지)
    LaunchedEffect(petResponse) {
        if (petResponse.isNotEmpty()) {
            delay(60000)
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

    // 칭호 획득 다이얼로그
    if (showTitleUnlockedDialog && unlockedTitleType != null) {
        TitleUnlockedDialog(
            titleType = unlockedTitleType!!,
            petName = petName,
            onEquip = {
                challengeManager.equipTitle(unlockedTitleType)
                showTitleUnlockedDialog = false
            },
            onDismiss = {
                showTitleUnlockedDialog = false
            },
            hapticManager = hapticManager
        )
    }

    // 칭호 선택 다이얼로그
    if (showTitleSelectionDialog) {
        TitleSelectionDialog(
            petName = petName,
            unlockedTitles = unlockedTitles,
            equippedTitle = equippedTitle,
            onSelect = { titleType ->
                challengeManager.equipTitle(titleType)
                showTitleSelectionDialog = false
            },
            onDismiss = {
                showTitleSelectionDialog = false
            },
            hapticManager = hapticManager
        )
    }

    // 공유 다이얼로그
    if (showShareDialog) {
        // 실제 주간 달성 데이터 사용
        val realWeeklyAchievements = preferenceManager.getWeeklyAchievements()

        // 총 걸음수 기반 총거리 (km) - Firebase에서 복원된 petTotalSteps 사용
        val petTotalSteps = preferenceManager.getPetTotalSteps()
        val totalDistanceKm = petTotalSteps * 0.0007f

        StreakCelebrationDialog(
            streakCount = streakCount.coerceAtLeast(1),
            weeklyAchievements = realWeeklyAchievements,
            onDismiss = {
                showShareDialog = false
                isQuickShareMode = false
            },
            hapticManager = hapticManager,
            petType = petType,
            petName = petName,  // 펫 이름만 전달
            equippedTitle = equippedTitle?.title,  // 칭호는 별도로 전달 (볼드용)
            successDays = preferenceManager.getSuccessDays(),
            totalKm = totalDistanceKm,
            // 첫 주 판단용 파라미터
            isFirstWeek = preferenceManager.isFirstWeekOfStreak(),
            streakStartDayOfWeek = preferenceManager.getStreakStartDayOfWeek(),
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

        // 1. Title row: Streak | "rebon" | Settings
        Box(modifier = Modifier.fillMaxWidth()) {
            // 좌측: Streak badge
            StreakBadge(
                streakCount = streakCount,
                inactive = streakCount == 0,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // 중앙: Title "rebon"
            Text(
                text = "rebon",
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
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp))
        ) {
            // 말풍선 (상단 고정)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                SpeechBubble(text = displaySpeech, fontSize = 14.sp)
            }

            // 펫 스프라이트 + 이름 (하단 고정)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 펫 스프라이트 (V2 우선, 없으면 V1)
                if (petStateV2 != null) {
                    // V2 스프라이트
                    PetSpriteFromState(
                        petState = petStateV2,
                        isWalking = isWalking || isGoalAchieved,
                        progressPercent = progressPercent,
                        baseSizeDp = 120,
                        monochrome = true
                    )
                } else {
                    // V1 스프라이트 (폴백)
                    PetSpriteWithSyncedGlow(
                        petType = petType,
                        isWalking = isWalking || isGoalAchieved,
                        size = 120.dp,
                        monochrome = true,
                        frameDurationMs = 500,
                        enableRandomAnimation = !isWalking && !isGoalAchieved
                    )
                }

                // 칭호 + 펫 이름 (스프라이트 아래, 클릭하여 칭호 변경)
                Row(
                    modifier = Modifier
                        .offset(y = 6.dp)
                        .clickable {
                            hapticManager?.click()
                            showTitleSelectionDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (equippedTitle != null) {
                        Text(
                            text = "${equippedTitle!!.title} ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextSecondary,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.White,
                                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                    Text(
                        text = petName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MockupColors.TextSecondary,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.White,
                                offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
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
                        val inputMessage = talkInput
                        val isLimitReached = preferenceManager.isAIChatLimitReached()
                        talkInput = ""

                        // AI 채팅 (비동기)
                        coroutineScope.launch {
                            val result = aiChatManager.getResponse(
                                message = inputMessage,
                                personality = petType.personality,
                                petName = petName,
                                isHappy = happinessLevel >= 3,
                                isAILimitReached = isLimitReached
                            )
                            petResponse = result.getResponse()

                            // 채팅 로그 저장
                            val responseType = when (result) {
                                is PetAIChatManager.ChatResult.AI -> "ai"
                                is PetAIChatManager.ChatResult.Filtered -> "filtered"
                                is PetAIChatManager.ChatResult.LimitReached -> "limit_reached"
                                is PetAIChatManager.ChatResult.Tired -> "tired"
                                is PetAIChatManager.ChatResult.Error -> "error"
                            }
                            com.moveoftoday.walkorwait.ChatLogManager.saveChat(
                                userMessage = inputMessage,
                                petResponse = result.getResponse(),
                                petName = petName,
                                petType = petType.name,
                                responseType = responseType
                            )
                        }
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

        // 4. Middle Content - 달성률 표시 (자유 시간일 때는 다르게)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFreeTime) {
                // 자유 시간 UI - 프로그레스바 없이 간단하게
                Text(
                    text = "자유 시간",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)  // 녹색
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 오늘 걸음수/거리 표시
                Text(
                    text = if (isKmMode) "%.2f km".format(displayCurrent) else "%,d 보".format(stepCount),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 자유롭게 즐기라는 안내
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PixelIcon(iconName = "icon_check", size = 16.dp, tint = Color(0xFF4CAF50))
                    Text(
                        text = "앱 제한 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            } else {
                // 일반 제어 시간 UI
                // 달성률 퍼센트 (큰 글씨)
                Text(
                    text = "${progressPercent}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 걸음수/거리 정보
                Text(
                    text = if (isKmMode) "%.2f / %.2f km".format(displayCurrent, displayGoal) else "%,d / %,d 보".format(stepCount, goalSteps),
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
                            .background(MockupColors.Border)
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
                        color = MockupColors.TextSecondary
                    )
                }
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
                        onTestGoalClick()  // 걸음수 채우기
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
                onChallengeClick()
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
    // 30% 확률로 AI 명언, 나머지는 진행률 대사
    val defaultSpeech = remember(progressPercent, isGoalAchieved, happinessLevel) {
        val showAIQuote = (0..9).random() < 3
        if (showAIQuote) {
            PetAIQuoteManager.getQuote(petType.personality)
        } else {
            when {
                isGoalAchieved -> PetDialogues.getGoalAchievedMessage(petType.personality)
                progressPercent >= 90 -> PetDialogues.getAlmostThereMessage(petType.personality)
                progressPercent >= 50 -> PetDialogues.getHalfwayMessage(petType.personality)
                happinessLevel <= 1 -> PetDialogues.getSadMessage(petType.personality)
                progressPercent > 0 -> PetDialogues.getWalkingMessage(petType.personality, progressPercent)
                else -> PetDialogues.getIdleMessage(petType.personality)
            }
        }
    }
    val displaySpeech = if (petResponse.isNotEmpty()) petResponse else defaultSpeech

    var talkInput by remember { mutableStateOf("") }
    var lastUserMessage by remember { mutableStateOf("") }
    var showUserMessage by remember { mutableStateOf(false) }

    // AI 채팅 일일 제한 관리
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferenceManager = remember { com.moveoftoday.walkorwait.PreferenceManager(context) }

    // 목표 단위 (km 또는 steps)
    val goalUnit = preferenceManager.getGoalUnit()
    val isKmMode = goalUnit == "km"

    // 표시용 값 (단위에 맞게 변환)
    val displayCurrent = if (isKmMode) preferenceManager.getTodayDistance() else stepCount.toDouble()
    val displayGoal = if (isKmMode) goalSteps / 1300.0 else goalSteps.toDouble()
    val displayRemaining = (displayGoal - displayCurrent).coerceAtLeast(0.0)
    val unitText = if (isKmMode) "km" else "보"

    // AI 채팅 매니저 (API 키 없으면 스크립트 전용)
    val aiChatManager = remember {
        PetAIChatManager(
            onAIUsed = { preferenceManager.incrementDailyAIChatCount() }
        )
    }
    val coroutineScope = rememberCoroutineScope()

    val stripeWidth = 4.dp

    // Reset pet response after delay (1분 유지)
    LaunchedEffect(petResponse) {
        if (petResponse.isNotEmpty()) {
            delay(60000)
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
                        text = if (isKmMode) "%.2f".format(displayCurrent) else "%,d".format(stepCount),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKmMode) "/ %.2f km".format(displayGoal) else "/ %,d 보".format(goalSteps),
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
                            .background(MockupColors.Border)
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
                            text = if (isGoalAchieved) "목표 달성!" else if (isKmMode) "목표까지 %.2f km".format(displayRemaining) else "목표까지 %,d보".format(goalSteps - stepCount),
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
                        val inputMessage = talkInput
                        val isLimitReached = preferenceManager.isAIChatLimitReached()
                        talkInput = ""

                        // AI 채팅 (비동기)
                        coroutineScope.launch {
                            val result = aiChatManager.getResponse(
                                message = inputMessage,
                                personality = petType.personality,
                                petName = petName,
                                isHappy = happinessLevel >= 3,
                                isAILimitReached = isLimitReached
                            )
                            petResponse = result.getResponse()

                            // 채팅 로그 저장
                            val responseType = when (result) {
                                is PetAIChatManager.ChatResult.AI -> "ai"
                                is PetAIChatManager.ChatResult.Filtered -> "filtered"
                                is PetAIChatManager.ChatResult.LimitReached -> "limit_reached"
                                is PetAIChatManager.ChatResult.Tired -> "tired"
                                is PetAIChatManager.ChatResult.Error -> "error"
                            }
                            com.moveoftoday.walkorwait.ChatLogManager.saveChat(
                                userMessage = inputMessage,
                                petResponse = result.getResponse(),
                                petName = petName,
                                petType = petType.name,
                                responseType = responseType
                            )
                        }
                    }
                },
                petName = petName
            )
        }
    }
}

/**
 * 칭호 획득 다이얼로그
 */
@Composable
fun TitleUnlockedDialog(
    titleType: com.moveoftoday.walkorwait.ChallengeType,
    petName: String,
    onEquip: () -> Unit,
    onDismiss: () -> Unit,
    hapticManager: com.moveoftoday.walkorwait.HapticManager? = null
) {
    val kenneyFont = rememberKenneyFont()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀
            Text(
                text = "새 칭호 획득!",
                fontSize = 20.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 칭호 미리보기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${titleType.title} $petName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 챌린지 설명
            Text(
                text = "${titleType.displayName} 완료!",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 나중에 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager?.click()
                            onDismiss()
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "나중에",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                // 장착하기 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager?.success()
                            onEquip()
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "장착하기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 칭호 선택 다이얼로그
 */
@Composable
fun TitleSelectionDialog(
    petName: String,
    unlockedTitles: Set<com.moveoftoday.walkorwait.ChallengeType>,
    equippedTitle: com.moveoftoday.walkorwait.ChallengeType?,
    onSelect: (com.moveoftoday.walkorwait.ChallengeType?) -> Unit,
    onDismiss: () -> Unit,
    hapticManager: com.moveoftoday.walkorwait.HapticManager? = null
) {
    val kenneyFont = rememberKenneyFont()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀
            Text(
                text = "칭호 선택",
                fontSize = 20.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 칭호 없음 옵션
            TitleOptionItem(
                title = "없음",
                isSelected = equippedTitle == null,
                onClick = {
                    hapticManager?.click()
                    onSelect(null)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 획득한 칭호들
            if (unlockedTitles.isEmpty()) {
                Text(
                    text = "아직 획득한 칭호가 없어요\n챌린지를 완료하면 칭호를 얻을 수 있어요!",
                    fontSize = 13.sp,
                    color = Color(0xFF999999),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                unlockedTitles.forEach { titleType ->
                    TitleOptionItem(
                        title = titleType.title,
                        isSelected = equippedTitle == titleType,
                        onClick = {
                            hapticManager?.click()
                            onSelect(titleType)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 닫기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                    .clickable {
                        hapticManager?.click()
                        onDismiss()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "닫기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TitleOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF333333) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isSelected) Color(0xFFF5F5F5) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFF333333)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "장착중",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}
