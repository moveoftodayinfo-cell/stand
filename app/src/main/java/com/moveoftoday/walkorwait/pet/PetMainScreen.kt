package com.moveoftoday.walkorwait.pet

import java.util.Locale
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.ColorFilter
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.ChallengeManager
import com.moveoftoday.walkorwait.ChallengeType
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.R
import com.moveoftoday.walkorwait.PreferenceManager
import com.moveoftoday.walkorwait.StreakCelebrationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 다국어 문자열 헬퍼 객체
 * 지원 언어: ko(한국어), en(영어 기본), ja(일본어), zh(중국어), es(스페인어)
 */
private object PetMainStrings {
    private fun getLang(): String = Locale.getDefault().language

    // 시간 표시 관련
    fun hoursMinutes(hours: Int, minutes: Int): String = when (getLang()) {
        "ko" -> "${hours}시간 ${minutes}분째"
        "ja" -> "${hours}時間${minutes}分目"
        "zh" -> "${hours}小时${minutes}分钟"
        "es" -> "${hours}h ${minutes}min"
        else -> "${hours}h ${minutes}min"
    }

    fun minutesOnly(minutes: Int): String = when (getLang()) {
        "ko" -> "${minutes}분째"
        "ja" -> "${minutes}分目"
        "zh" -> "${minutes}分钟"
        "es" -> "${minutes} min"
        else -> "${minutes} min"
    }

    fun start(): String = when (getLang()) {
        "ko" -> "시작"
        "ja" -> "開始"
        "zh" -> "开始"
        "es" -> "Inicio"
        else -> "Start"
    }

    // 목표 표시 관련
    fun goalKm(goalKm: Double): String = when (getLang()) {
        "ko" -> "/ %.2f km".format(goalKm)
        "ja" -> "/ %.2f km".format(goalKm)
        "zh" -> "/ %.2f 公里".format(goalKm)
        "es" -> "/ %.2f km".format(goalKm)
        else -> "/ %.2f km".format(goalKm)
    }

    fun goalSteps(steps: Int): String = when (getLang()) {
        "ko" -> "/ %,d 보".format(steps)
        "ja" -> "/ %,d 歩".format(steps)
        "zh" -> "/ %,d 步".format(steps)
        "es" -> "/ %,d pasos".format(steps)
        else -> "/ %,d steps".format(steps)
    }

    // 챌린지 완료
    fun challengeComplete(displayName: String): String = when (getLang()) {
        "ko" -> "${displayName} 완료!"
        "ja" -> "${displayName} 完了!"
        "zh" -> "${displayName} 完成!"
        "es" -> "${displayName} completado!"
        else -> "${displayName} completed!"
    }

    // 챌린지 카테고리 매핑 (내부 비교용)
    fun getCategoryKey(koreanCategory: String): String = when (koreanCategory) {
        "독서" -> "reading"
        "명상" -> "meditation"
        "공부" -> "study"
        else -> koreanCategory
    }

    // 접근성: 달성 상태
    fun achieved(): String = when (getLang()) {
        "ko" -> "달성"
        "ja" -> "達成"
        "zh" -> "达成"
        "es" -> "Logrado"
        else -> "Achieved"
    }
}

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
                hours > 0 -> PetMainStrings.hoursMinutes(hours, minutes)
                minutes > 0 -> PetMainStrings.minutesOnly(minutes)
                else -> PetMainStrings.start()
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
    onNotificationClick: () -> Unit = {},  // 알림 센터 클릭
    notificationCount: Int = 0,  // 읽지 않은 알림 개수
    onChallengeClick: () -> Unit = {},
    onQuickStartChallenge: (ChallengeType) -> Unit = {},  // 추천 챌린지 바로 시작
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier,
    isFreeTime: Boolean = false,  // 자유로운 날/시간 (제어 요일 아니거나 제어 시간대 아님)
    petStateV2: PetState? = null,  // V2 펫 상태 (있으면 V2 스프라이트 사용)
    onTestGoalClick: () -> Unit = {},  // 🧪 테스트: 목표 달성 시뮬레이션
    showGoalAchievedDialog: Boolean = false,  // 100% 달성 시 다이얼로그 표시
    onDismissGoalDialog: () -> Unit = {}  // 다이얼로그 닫기 콜백
) {
    val kenneyFont = rememberKenneyFont()
    val isGoalAchieved = stepCount >= goalSteps
    val progressPercent = ((stepCount.toFloat() / goalSteps) * 100).toInt().coerceAtLeast(0)  // 100% 초과 허용
    val isWalking = progressPercent > 0 && !isGoalAchieved

    // 배경색 항상 흰색
    val backgroundColor = MockupColors.Background

    // 펫 터치 감정표현 상태 (V3)
    var showTouchEmotion by remember { mutableStateOf(false) }
    var currentSymbol by remember { mutableStateOf("") }
    var currentAnimationType by remember { mutableStateOf(EmotionAnimationType.FLOAT_UP) }
    var emotionKey by remember { mutableIntStateOf(0) }  // 새 애니메이션 트리거용
    val touchState = remember { TouchState() }

    // 펫 돌아다니기 상태
    var wanderTargetX by remember { mutableFloatStateOf(0f) }
    var isWandering by remember { mutableStateOf(false) }
    var isFacingLeft by remember { mutableStateOf(false) }  // 왼쪽 바라보기 (미러링)
    val wanderX by animateFloatAsState(
        targetValue = wanderTargetX,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
        finishedListener = { isWandering = false },
        label = "wanderX"
    )

    // 펫 돌아다니기 애니메이션 (3~6초마다 랜덤 위치)
    LaunchedEffect(Unit) {
        val random = java.util.Random()
        while (true) {
            delay((3000 + random.nextInt(3000)).toLong())
            val newTarget = -40f + random.nextFloat() * 80f
            isFacingLeft = newTarget < wanderX  // 현재 위치보다 왼쪽으로 가면 미러링
            isWandering = true
            wanderTargetX = newTarget
        }
    }

    // 진행률 변화 감지용
    var lastProgressMilestone by remember { mutableIntStateOf(0) }

    // Pet speech 로직 - 백그라운드 복귀 또는 새 대화 시에만 변경
    var petResponse by remember { mutableStateOf("") }
    var speechRefreshTrigger by remember { mutableStateOf(0) }

    // AI 채팅 일일 제한 관리
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferenceManager = remember { com.moveoftoday.walkorwait.PreferenceManager(context) }

    // 장비 상태 (장비 시스템) - 스킨 변경 감지를 위해 State로 관리
    var currentSkinId by remember { mutableStateOf(preferenceManager.getPetSkin()) }

    // 스킨 변경 감지 (화면 재진입 시)
    LaunchedEffect(Unit) {
        currentSkinId = preferenceManager.getPetSkin()
        android.util.Log.d("PetMainScreen", "🔄 Skin refreshed on screen entry: $currentSkinId")
    }

    val equipmentState = remember(currentSkinId) {
        android.util.Log.d("PetMainScreen", "🎨 Loading equipmentState with skinId: $currentSkinId")
        preferenceManager.getEquipmentState()
    }

    // 목표 단위 (km 또는 steps)
    val goalUnit = preferenceManager.getGoalUnit()
    val isKmMode = goalUnit == "km"

    // 실제 목표 (항상 최신 값 사용)
    val actualGoal = preferenceManager.getGoal()

    // 표시용 값 (단위에 맞게 변환)
    val displayCurrent = if (isKmMode) preferenceManager.getTodayDistance() else stepCount.toDouble()
    val displayGoal = if (isKmMode) actualGoal / 1300.0 else actualGoal.toDouble()
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
    val todayCompletionCounts by challengeManager.todayCompletionCounts.collectAsState()

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

    // 현재 상태에 맞는 대사를 가져오는 함수 (진행률 대사 + 30% AI 명언 + 15% 기능 팁)
    fun getCurrentSpeech(): String {
        val randomValue = (0..99).random()

        // 15% 확률로 기능 팁 표시 (V2 펫일 때만)
        if (randomValue < 15 && petStateV2 != null) {
            return PetDialoguesV2.getFeatureTipMessage(petStateV2.personality)
        }

        // 30% 확률로 AI 명언 표시 (15-44)
        if (randomValue < 45) {
            return PetAIQuoteManager.getQuote(petType.personality)
        }

        // 진행률 기반 대사 (55%)
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
        // V2 시간대별 인사 (첫 실행 시)
        val personality = petStateV2?.personality ?: PetPersonalityV2.LOYAL
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = PetDialoguesV2.getTimeOfDayGreeting(personality, hour)

        // 초기 대사 설정 (시간대별 인사 우선)
        if (defaultSpeech.isEmpty()) {
            defaultSpeech = timeGreeting
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

    // 신규 유저 방어권 지급 다이얼로그
    var showWelcomeDefenseDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!preferenceManager.hasShownWelcomeDefenseDialog() &&
            preferenceManager.getStreakDefenseTickets() > 0) {
            showWelcomeDefenseDialog = true
        }
    }

    // 100% 달성 다이얼로그 표시 (외부에서 제어)
    LaunchedEffect(showGoalAchievedDialog) {
        if (showGoalAchievedDialog) {
            isQuickShareMode = false
            showShareDialog = true
            // 목표 달성 감정표현 (V3)
            currentSymbol = RetroSymbols.HEARTS_3
            currentAnimationType = EmotionAnimationType.BURST
            showTouchEmotion = true
        }
    }

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

    // 신규 유저 방어권 지급 다이얼로그
    if (showWelcomeDefenseDialog) {
        WelcomeDefenseDialog(
            onDismiss = {
                preferenceManager.setShownWelcomeDefenseDialog()
                showWelcomeDefenseDialog = false
            },
            hapticManager = hapticManager
        )
    }

    // 칭호 획득 다이얼로그
    val currentUnlockedTitleType = unlockedTitleType
    if (showTitleUnlockedDialog && currentUnlockedTitleType != null) {
        TitleUnlockedDialog(
            titleType = currentUnlockedTitleType,
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
            streakCount = streakCount,
            weeklyAchievements = realWeeklyAchievements,
            onDismiss = {
                showShareDialog = false
                onDismissGoalDialog()  // 외부 상태도 리셋
                isQuickShareMode = false
            },
            hapticManager = hapticManager,
            petType = petType,
            petName = petStateV2?.name ?: petName,  // V2 이름 우선
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
            goalSteps = actualGoal,  // 항상 최신 목표 사용
            goalUnit = goalUnit,
            currentDistance = preferenceManager.getTodayDistance(),
            petStateV2 = petStateV2  // V2 펫 상태 전달
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = Padding.screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 방어권 티켓 수 (DEBUG 모드에서 수정 가능하도록 State로 관리)
        var defenseTickets by remember { mutableIntStateOf(preferenceManager.getStreakDefenseTickets()) }

        // 1. Title row: Streak | "rebon" | Settings
        Box(modifier = Modifier.fillMaxWidth()) {
            // 좌측: Streak badge + Defense badge
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakBadge(
                    streakCount = streakCount,
                    inactive = streakCount == 0
                )
                DefenseBadge(
                    ticketCount = defenseTickets,
                    isDebug = BuildConfig.DEBUG,
                    onClick = {
                        hapticManager?.click()
                        onSettingsClick()  // 설정 화면으로 이동
                    },
                    onLongClick = {
                        // DEBUG: 길게 누르면 방어권 +1 & 마지막 달성일 2일 전으로 설정
                        if (BuildConfig.DEBUG) {
                            hapticManager?.success()
                            // 방어권 +1
                            preferenceManager.addStreakDefenseTickets(1)
                            defenseTickets = preferenceManager.getStreakDefenseTickets()

                            // 마지막 달성일을 2일 전으로 설정 (하루 건너뛴 상황)
                            val twoDaysAgo = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                .format(java.util.Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000))
                            preferenceManager.setLastAchievedDate(twoDaysAgo)

                            android.util.Log.d("PetMainScreen", "🛡️ DEBUG: 방어권 +1 (현재: $defenseTickets), 마지막 달성일: $twoDaysAgo (테스트용)")
                        }
                    }
                )
            }

            // 중앙: Title "rebon" (DEBUG: 길게 누르면 테스트)
            Text(
                text = "rebon",
                fontSize = 32.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(
                        if (BuildConfig.DEBUG) {
                            @OptIn(ExperimentalFoundationApi::class)
                            Modifier.combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    hapticManager?.click()
                                    onTestGoalClick()
                                }
                            )
                        } else Modifier
                    )
            )

            // 우측: Notification + Settings icons
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 알림 아이콘
                Box {
                    IconButton(
                        onClick = {
                            hapticManager?.click()
                            onNotificationClick()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        DrawableIcon(
                            iconName = "icon_bell",
                            size = 22.dp,
                            tint = MockupColors.TextMuted  // 회색으로 통일
                        )
                    }
                    // 알림 배지 (빨간 점)
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .background(MockupColors.Red, RoundedCornerShape(50))
                        )
                    }
                }

                // 설정 아이콘
                IconButton(
                    onClick = {
                        hapticManager?.click()
                        onSettingsClick()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    PixelIcon(
                        iconName = "icon_gear",
                        size = 24.dp,
                        alpha = if (happinessLevel <= 1) 0.7f else 1f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Display Area - 240dp, 수평 줄무늬, SpeechBubble + PetSpriteWithSyncedGlow (하트 제거)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Size.displayAreaHeight)
                .clip(RoundedCornerShape(Radius.md))
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
                .border(Border.thick, MockupColors.Border, RoundedCornerShape(Radius.md))
        ) {
            // 말풍선 (하단이 디스플레이 정중앙에 위치) - 펫 X축 따라다님
            if (displaySpeech.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(120.dp)  // 디스플레이 절반 (240dp / 2)
                        .offset(x = wanderX.dp),  // 펫 X축 따라다님
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SpeechBubble(text = displaySpeech, fontSize = 14.sp, maxLines = 5)
                }
            }

            // 펫 스프라이트 + 이름 (절대 위치 - pet name 기준)
            // 펫별 Y offset 계산
            val petYOffsetValue = petStateV2?.let { state ->
                state.petType.getDisplayYOffsetDp(state.stage)
            } ?: 0f

            val displayHeight = Size.displayAreaHeight
            val petSize = 120.dp  // Sprite 크기 = Box 크기 (정확한 위치를 위해 동일하게)
            val nameY = PetDisplayConstants.calculateNameY(displayHeight)
            val petY = PetDisplayConstants.calculatePetY(displayHeight, petSize, petYOffsetValue)

            // 펫이 움직이는 중인지 (걷기 + 돌아다니기)
            val isPetMoving = isWalking || isGoalAchieved || isWandering

            // Compose 햅틱 피드백
            val view = androidx.compose.ui.platform.LocalView.current

            // 펫 스프라이트 (절대 위치 + wanderX 수평 이동)
            @OptIn(ExperimentalFoundationApi::class)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = wanderX.dp, y = petY)
                    .size(petSize)
                    .combinedClickable(
                            onClick = {
                                // V3 터치 시스템 - View 햅틱 사용
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                val personality = petStateV2?.personality ?: PetPersonalityV2.LOYAL
                                val touchResult = touchState.onTouch()

                                // 터치 결과에 따른 심볼 결정
                                val (symbol, animType) = when (touchResult) {
                                    TouchState.TouchResult.WELCOME_BACK -> {
                                        PersonalityTouchReaction.getWelcomeBackSymbol(personality)
                                    }
                                    TouchState.TouchResult.RAPID_TAP, TouchState.TouchResult.COMBO -> {
                                        PersonalityTouchReaction.getSymbol(personality, touchState.touchCount, true)
                                    }
                                    else -> {
                                        PersonalityTouchReaction.getSymbol(personality, touchState.touchCount, false)
                                    }
                                }

                                currentSymbol = symbol
                                currentAnimationType = animType
                                emotionKey++  // 새 애니메이션 시작
                                showTouchEmotion = true

                                // 터치 반응 대사도 표시
                                petResponse = PetDialoguesV2.getTouchReactionMessage(personality)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 왼쪽으로 이동 시 미러링
                    val mirrorScale = if (isFacingLeft) -1f else 1f

                    Box(
                        modifier = Modifier.graphicsLayer(scaleX = mirrorScale)
                    ) {
                        if (petStateV2 != null) {
                            // V2 스프라이트 with 장비
                            PetSpriteFromStateWithEquipment(
                                petState = petStateV2,
                                equipmentState = equipmentState,
                                isWalking = isPetMoving,
                                progressPercent = progressPercent,
                                baseSizeDp = 120,
                                monochrome = true
                            )
                        } else {
                            // V1 스프라이트 (폴백)
                            PetSpriteWithSyncedGlow(
                                petType = petType,
                                isWalking = isPetMoving,
                                size = 120.dp,
                                monochrome = true,
                                frameDurationMs = 500,
                                enableRandomAnimation = !isPetMoving
                            )
                        }
                    }

                    // 감정 심볼 오버레이 (펫 머리 위) - V3 모노크롬
                    if (showTouchEmotion && currentSymbol.isNotEmpty()) {
                        key(emotionKey) {  // key로 새 애니메이션 인스턴스 생성
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-30).dp)
                            ) {
                                EmotionSymbolDisplay(
                                    symbol = currentSymbol,
                                    animationType = currentAnimationType,
                                    size = 28.dp,
                                    onAnimationEnd = {
                                        showTouchEmotion = false
                                        currentSymbol = ""
                                    }
                                )
                            }
                        }
                    }
                }

            // 칭호 + 펫 이름 (절대 위치 + wanderX 수평 이동)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = wanderX.dp, y = nameY)
                    .clickable {
                        hapticManager?.click()
                        showTitleSelectionDialog = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentEquippedTitle = equippedTitle
                if (currentEquippedTitle != null) {
                    Text(
                        text = "${currentEquippedTitle.title} ",
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
                    text = petStateV2?.name ?: petName,  // V2 이름 우선
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
                            // 대화 후 기본 대사도 갱신 (대화 응답이 사라지면 새로운 대사 표시)
                            speechRefreshTrigger++
                        }
                    }
                },
                petName = petName
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 달성률 표시 (컴팩트 레이아웃)
        // 통합 레이아웃: 자유시간/제어시간 동일한 구조
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 배지
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isFreeTime) {
                    PixelIcon(iconName = "icon_check", size = 16.dp, tint = MockupColors.TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.free_time),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MockupColors.TextMuted
                    )
                } else {
                    PixelIcon(iconName = "icon_star", size = 16.dp, tint = MockupColors.TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.goal_in_progress),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MockupColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 퍼센트 (대형)
            Text(
                text = "${progressPercent}%",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFreeTime) MockupColors.TextMuted else MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 프로그레스 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                val progress = (stepCount.toFloat() / goalSteps).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            if (isFreeTime) MockupColors.TextMuted else MockupColors.Border,
                            RoundedCornerShape(8.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 걸음수/거리 표시
            val remainingKm = (displayGoal - displayCurrent).coerceAtLeast(0.0)
            val remainingSteps = (goalSteps - stepCount).coerceAtLeast(0)
            Text(
                text = if (isFreeTime) {
                    // 자유시간: "오늘 X보" 형식
                    if (isKmMode) stringResource(R.string.today_km, displayCurrent) else stringResource(R.string.today_steps, stepCount)
                } else if (isGoalAchieved) {
                    // 목표 달성 후에도 걸은 만큼 표시
                    if (isKmMode) stringResource(R.string.km_achieved, displayCurrent) else stringResource(R.string.steps_achieved, stepCount)
                } else if (isKmMode) {
                    stringResource(R.string.km_remaining, remainingKm)
                } else {
                    stringResource(R.string.steps_remaining, remainingSteps)
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 상태 메시지
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isFreeTime) {
                    PixelIcon(iconName = "icon_check", size = 16.dp, tint = MockupColors.TextMuted)
                    Text(
                        text = stringResource(R.string.use_apps_freely),
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )
                } else {
                    PixelIcon(iconName = "icon_time", size = 16.dp, tint = MockupColors.TextSecondary)
                    Text(
                        text = if (isGoalAchieved) stringResource(R.string.goal_achieved_apps_available) else stringResource(R.string.apps_blocked, getBlockedTimeText()),
                        fontSize = 14.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 주간 미니 그래프
        WeeklyMiniGraph(
            todaySteps = stepCount,
            goalSteps = goalSteps,
            preferenceManager = preferenceManager,
            isFreeTime = isFreeTime
        )

        Spacer(modifier = Modifier.weight(1f))

        // 5. Action Button - 챌린지 하러가기 (큰 CTA 버튼)
        MockupButton(
            text = stringResource(R.string.go_to_challenge),
            onClick = {
                hapticManager?.click()
                onChallengeClick()
            },
            height = 56.dp
        )

        Spacer(modifier = Modifier.height(8.dp))
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
                .border(Border.thick, MockupColors.Border, RoundedCornerShape(Radius.md))
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
                    text = stringResource(R.string.preparing_challenge_title),
                    fontSize = 24.sp,
                    fontFamily = kenneyFont,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 설명
                Text(
                    text = stringResource(R.string.preparing_challenge_message),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 확인 버튼
                MockupButton(
                    text = stringResource(R.string.confirm),
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

    // 실제 목표 (항상 최신 값 사용)
    val actualGoal = preferenceManager.getGoal()

    // 표시용 값 (단위에 맞게 변환)
    val displayCurrent = if (isKmMode) preferenceManager.getTodayDistance() else stepCount.toDouble()
    val displayGoal = if (isKmMode) actualGoal / 1300.0 else actualGoal.toDouble()
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
                .border(Border.thick, MockupColors.Border, RoundedCornerShape(Radius.md))
        ) {
            // 펫 + 말풍선 (말풍선이 펫 바로 위, 위로 확장)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speech bubble (directly above pet)
                if (displaySpeech.isNotEmpty()) {
                    SpeechBubble(
                        text = displaySpeech,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Pet sprite
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
                .border(Border.medium, MockupColors.Border, RoundedCornerShape(Radius.sm))
                .padding(Padding.card)
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
                        fontSize = FontSize.display,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKmMode) PetMainStrings.goalKm(displayGoal) else PetMainStrings.goalSteps(goalSteps),
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
                        .height(Size.progressBarHeight)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(Color(0xFFE0E0E0))
                        .border(Border.thin, MockupColors.Border, RoundedCornerShape(Radius.xs))
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
                            text = if (isGoalAchieved) stringResource(R.string.goal_achieved) else if (isKmMode) stringResource(R.string.km_to_goal, displayRemaining) else stringResource(R.string.steps_to_goal, goalSteps - stepCount),
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
                text = stringResource(R.string.new_title_earned),
                fontSize = 20.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = Color.Black
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
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 챌린지 설명
            Text(
                text = PetMainStrings.challengeComplete(titleType.displayName),
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
                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager?.click()
                            onDismiss()
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.later),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // 장착하기 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager?.success()
                            onEquip()
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.equip),
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
                text = stringResource(R.string.select_title),
                fontSize = 20.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 칭호 없음 옵션
            TitleOptionItem(
                title = stringResource(R.string.none),
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
                    text = stringResource(R.string.no_titles_yet),
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
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .clickable {
                        hapticManager?.click()
                        onDismiss()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.close),
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
                color = if (isSelected) Color.Black else Color(0xFFE0E0E0),
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
                color = Color.Black
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.equipped),
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * 최근 챌린지 아이콘 Row
 * - 오늘 완료한 챌린지가 있으면: 완료된 것은 체크 표시, 나머지는 흐리게
 * - 챌린지 0개면: 추천 챌린지 3개 표시 (각 카테고리에서 1개씩)
 */
@Composable
private fun RecentChallengesRow(
    todayCompletionCounts: Map<ChallengeType, Int>,
    onQuickStartChallenge: (ChallengeType) -> Unit
) {
    // 추천 챌린지 (각 카테고리에서 가장 짧은 것)
    val recommendedChallenges = listOf(
        ChallengeType.READING_15,    // 독서 15분
        ChallengeType.MEDITATION_5,  // 명상 5분
        ChallengeType.STUDY_30       // 공부 30분
    )

    val hasCompletedToday = todayCompletionCounts.isNotEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasCompletedToday) {
            // 오늘 완료한 챌린지 표시
            recommendedChallenges.forEach { challengeType ->
                val isCompleted = todayCompletionCounts.containsKey(challengeType) ||
                    todayCompletionCounts.keys.any { it.category == challengeType.category }

                ChallengeIconItem(
                    challengeType = challengeType,
                    isCompleted = isCompleted,
                    onClick = { onQuickStartChallenge(challengeType) }
                )

                Spacer(modifier = Modifier.width(16.dp))
            }
        } else {
            // 추천 챌린지 표시
            Text(
                text = stringResource(R.string.recommended),
                fontSize = 12.sp,
                color = MockupColors.TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))

            recommendedChallenges.forEach { challengeType ->
                ChallengeIconItem(
                    challengeType = challengeType,
                    isCompleted = false,
                    isRecommended = true,
                    onClick = { onQuickStartChallenge(challengeType) }
                )

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
private fun ChallengeIconItem(
    challengeType: ChallengeType,
    isCompleted: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    val iconRes = when (PetMainStrings.getCategoryKey(challengeType.category)) {
        "reading" -> R.drawable.challenge_reading
        "meditation" -> R.drawable.challenge_meditation
        "study" -> R.drawable.challenge_study
        else -> R.drawable.challenge_reading
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) MockupColors.BlueLight else Color.White)
            .border(
                width = if (isCompleted) 2.dp else 1.5.dp,
                color = if (isCompleted) MockupColors.Blue else MockupColors.Border.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = challengeType.displayName,
            modifier = Modifier.size(40.dp),
            alpha = if (isCompleted || isRecommended) 1f else 0.4f
        )

        // 완료 체크 표시
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(20.dp)
                    .background(MockupColors.Blue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 주간 미니 그래프 - 이번 주 걸음 수 추세
 */
@Composable
private fun WeeklyMiniGraph(
    todaySteps: Int,
    goalSteps: Int,
    preferenceManager: com.moveoftoday.walkorwait.PreferenceManager? = null,
    isFreeTime: Boolean = false  // 오늘이 자유시간이면 별 표시
) {
    // 요일 라벨
    val dayLabels = listOf(
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat),
        stringResource(R.string.day_sun)
    )

    // 오늘이 무슨 요일인지 (0=일, 1=월, ..., 6=토)
    val calendar = java.util.Calendar.getInstance()
    val todayDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    // 월요일 기준 인덱스 (0=월, 1=화, ..., 6=일)
    val todayIndex = if (todayDayOfWeek == java.util.Calendar.SUNDAY) 6 else todayDayOfWeek - 2

    // 실제 주간 데이터 가져오기 (날짜별 걸음수, 목표)
    val weeklyPairData = remember(todaySteps) {
        preferenceManager?.getWeeklyData() ?: (0..6).map { Pair(0, goalSteps) }
    }

    // 걸음수 리스트 (그래프 표시용)
    val weeklyData = weeklyPairData.map { it.first }

    val maxSteps = maxOf(weeklyData.maxOrNull() ?: 1, goalSteps)

    // 바 높이 (100% = 80% 높이로 표시, 위에 20% 여유)
    val barHeight = 70.dp
    val maxBarFraction = 0.80f  // 100% 달성 시 80% 높이까지만 (위에 여유)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.5.dp, MockupColors.Border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // 그래프 영역 (점선 + 바)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            // 점선 기준선 그리기 (50%, 100% - maxBarFraction 기준)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        // 50% 점선 (아래에서 50% * 0.8 = 40% 위치 → 위에서 60%)
                        val y50 = size.height * (1f - 0.5f * maxBarFraction)
                        drawLine(
                            color = Color(0xFFDDDDDD),
                            start = Offset(0f, y50),
                            end = Offset(size.width, y50),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                        // 100% 점선 (아래에서 100% * 0.8 = 80% 위치 → 위에서 20%)
                        val y100 = size.height * (1f - 1.0f * maxBarFraction)
                        drawLine(
                            color = Color(0xFFDDDDDD),
                            start = Offset(0f, y100),
                            end = Offset(size.width, y100),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                    }
            )

            // 좌측 Y축 라벨 (점선과 같은 높이에 배치)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(28.dp)
            ) {
                // 100% 라벨 (위에서 20% 위치)
                Text(
                    text = "100%",
                    fontSize = 9.sp,
                    color = MockupColors.TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (barHeight.value * (1f - 1.0f * maxBarFraction) - 5).dp)
                )
                // 50% 라벨 (위에서 60% 위치)
                Text(
                    text = "50%",
                    fontSize = 9.sp,
                    color = MockupColors.TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (barHeight.value * (1f - 0.5f * maxBarFraction) - 5).dp)
                )
            }

            // 바 그래프
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyPairData.forEachIndexed { index, (steps, dayGoal) ->
                    // 목표 대비 퍼센트로 바 높이 계산 (해당 날짜의 목표 사용)
                    val percent = if (dayGoal > 0) (steps.toFloat() / dayGoal) else 0f
                    val targetHeightFraction = (percent * maxBarFraction).coerceIn(0.02f, maxBarFraction)

                    // 애니메이션으로 부드럽게 막대 높이 변경
                    val animatedHeightFraction by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = targetHeightFraction,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 500,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "barHeight"
                    )

                    val isToday = index == todayIndex
                    val isFuture = index > todayIndex
                    val achieved100 = percent >= 1.0f  // 100% 달성
                    // 오늘이 자유시간이면 별 표시 (목표 달성 여부 무관)
                    val showStar = (!isFuture) && (achieved100 || (isToday && isFreeTime))

                    // 바 + 별 (별이 막대 위에 위치)
                    BoxWithConstraints(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val barHeightDp = maxHeight * animatedHeightFraction

                        // 막대 그래프 (애니메이션 적용)
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeightDp)
                                .background(
                                    color = when {
                                        isFuture -> Color(0xFFE0E0E0)
                                        isToday && isFreeTime -> Color(0xFF9E9E9E)  // 자유시간: 회색
                                        achieved100 -> Color(0xFF2D2D2D)  // 100% 달성: 진한 검정
                                        isToday -> Color(0xFF4A4A4A)  // 오늘: 검정
                                        else -> Color(0xFF6A6A6A)  // 과거: 어두운 회색
                                    },
                                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                )
                        )

                        // 별 아이콘 - 막대 바로 위에 표시
                        if (showStar) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_star),
                                contentDescription = PetMainStrings.achieved(),
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = -(barHeightDp + 2.dp)),
                                colorFilter = ColorFilter.tint(Color.Black)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 요일 라벨 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weeklyData.forEachIndexed { index, _ ->
                val isToday = index == todayIndex
                Box(
                    modifier = Modifier.width(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabels[index],
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MockupColors.TextPrimary else MockupColors.TextMuted
                    )
                }
            }
        }
    }
}

/**
 * 신규 유저 방어권 지급 다이얼로그
 */
@Composable
fun WelcomeDefenseDialog(
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 방패 아이콘
            DrawableIcon(
                iconName = "icon_shield",
                size = 48.dp,
                tint = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.defense_ticket_given),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.defense_ticket_message),
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 확인 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .clickable {
                        hapticManager?.click()
                        onDismiss()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
