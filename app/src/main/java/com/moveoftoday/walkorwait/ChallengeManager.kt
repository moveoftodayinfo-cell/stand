package com.moveoftoday.walkorwait

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class ChallengeMode {
    TIME_BASED,         // 기존: 타이머 (독서, 명상, 공부, 플랭크) - 앱에 머물러야 함
    REP_BASED,          // 신규: 횟수 카운터 (스쿼트)
    BACKGROUND_TIMER    // 백그라운드 타이머 (간헐적 단식) - 백그라운드 진행 가능
}

enum class ChallengeType(val displayName: String, val category: String, val title: String, val mode: ChallengeMode = ChallengeMode.TIME_BASED) {
    READING_15("15분 독서", "독서", "책을 좋아하는"),
    READING_30("30분 독서", "독서", "독서하는"),
    READING_60("1시간 독서", "독서", "책벌레"),
    MEDITATION_5("5분 명상", "명상", "명상 입문자"),
    MEDITATION_15("15분 명상", "명상", "명상하는"),
    MEDITATION_30("30분 명상", "명상", "명상 마스터"),
    STUDY_30("30분 공부", "공부", "공부하는"),
    STUDY_60("1시간 공부", "공부", "열공하는"),
    STUDY_120("2시간 공부", "공부", "공부왕"),

    // 운동 - 스쿼트 (횟수 기반)
    SQUAT_10("스쿼트 10회", "운동", "다리 튼튼한", ChallengeMode.REP_BASED),
    SQUAT_20("스쿼트 20회", "운동", "스쿼트 마스터", ChallengeMode.REP_BASED),
    SQUAT_50("스쿼트 50회", "운동", "하체 끝판왕", ChallengeMode.REP_BASED),

    // 운동 - 플랭크 (타이머 기반)
    PLANK_30("30초 플랭크", "운동", "코어 튼튼한"),
    PLANK_60("1분 플랭크", "운동", "플랭크 마스터"),
    PLANK_120("2분 플랭크", "운동", "코어 끝판왕"),

    // 웰니스 - 간헐적 단식 (백그라운드 타이머)
    FASTING_16_8("간헐적 단식 16:8", "웰니스", "건강한", ChallengeMode.BACKGROUND_TIMER),
    FASTING_18_6("간헐적 단식 18:6", "웰니스", "단식 마스터", ChallengeMode.BACKGROUND_TIMER),
    FASTING_20_4("간헐적 단식 20:4", "웰니스", "단식 끝판왕", ChallengeMode.BACKGROUND_TIMER);

    companion object {
        private fun getLang(): String = java.util.Locale.getDefault().language
    }

    /** 다국어 챌린지 이름 */
    fun getLocalizedDisplayName(): String {
        val lang = java.util.Locale.getDefault().language
        return when (this) {
            READING_15 -> when (lang) { "ko" -> "15분 독서"; "ja" -> "15分読書"; "zh" -> "15分钟阅读"; "es" -> "15 min lectura"; else -> "15 min Reading" }
            READING_30 -> when (lang) { "ko" -> "30분 독서"; "ja" -> "30分読書"; "zh" -> "30分钟阅读"; "es" -> "30 min lectura"; else -> "30 min Reading" }
            READING_60 -> when (lang) { "ko" -> "1시간 독서"; "ja" -> "1時間読書"; "zh" -> "1小时阅读"; "es" -> "1h lectura"; else -> "1h Reading" }
            MEDITATION_5 -> when (lang) { "ko" -> "5분 명상"; "ja" -> "5分瞑想"; "zh" -> "5分钟冥想"; "es" -> "5 min meditación"; else -> "5 min Meditation" }
            MEDITATION_15 -> when (lang) { "ko" -> "15분 명상"; "ja" -> "15分瞑想"; "zh" -> "15分钟冥想"; "es" -> "15 min meditación"; else -> "15 min Meditation" }
            MEDITATION_30 -> when (lang) { "ko" -> "30분 명상"; "ja" -> "30分瞑想"; "zh" -> "30分钟冥想"; "es" -> "30 min meditación"; else -> "30 min Meditation" }
            STUDY_30 -> when (lang) { "ko" -> "30분 공부"; "ja" -> "30分勉強"; "zh" -> "30分钟学习"; "es" -> "30 min estudio"; else -> "30 min Study" }
            STUDY_60 -> when (lang) { "ko" -> "1시간 공부"; "ja" -> "1時間勉強"; "zh" -> "1小时学习"; "es" -> "1h estudio"; else -> "1h Study" }
            STUDY_120 -> when (lang) { "ko" -> "2시간 공부"; "ja" -> "2時間勉強"; "zh" -> "2小时学习"; "es" -> "2h estudio"; else -> "2h Study" }
            SQUAT_10 -> when (lang) { "ko" -> "스쿼트 10회"; "ja" -> "スクワット10回"; "zh" -> "深蹲10次"; "es" -> "10 sentadillas"; else -> "10 Squats" }
            SQUAT_20 -> when (lang) { "ko" -> "스쿼트 20회"; "ja" -> "スクワット20回"; "zh" -> "深蹲20次"; "es" -> "20 sentadillas"; else -> "20 Squats" }
            SQUAT_50 -> when (lang) { "ko" -> "스쿼트 50회"; "ja" -> "スクワット50回"; "zh" -> "深蹲50次"; "es" -> "50 sentadillas"; else -> "50 Squats" }
            PLANK_30 -> when (lang) { "ko" -> "30초 플랭크"; "ja" -> "30秒プランク"; "zh" -> "30秒平板支撑"; "es" -> "30s plancha"; else -> "30s Plank" }
            PLANK_60 -> when (lang) { "ko" -> "1분 플랭크"; "ja" -> "1分プランク"; "zh" -> "1分钟平板支撑"; "es" -> "1 min plancha"; else -> "1 min Plank" }
            PLANK_120 -> when (lang) { "ko" -> "2분 플랭크"; "ja" -> "2分プランク"; "zh" -> "2分钟平板支撑"; "es" -> "2 min plancha"; else -> "2 min Plank" }
            FASTING_16_8 -> when (lang) { "ko" -> "간헐적 단식 16:8"; "ja" -> "16:8断食"; "zh" -> "16:8间歇性禁食"; "es" -> "Ayuno 16:8"; else -> "16:8 Fasting" }
            FASTING_18_6 -> when (lang) { "ko" -> "간헐적 단식 18:6"; "ja" -> "18:6断食"; "zh" -> "18:6间歇性禁食"; "es" -> "Ayuno 18:6"; else -> "18:6 Fasting" }
            FASTING_20_4 -> when (lang) { "ko" -> "간헐적 단식 20:4"; "ja" -> "20:4断食"; "zh" -> "20:4间歇性禁食"; "es" -> "Ayuno 20:4"; else -> "20:4 Fasting" }
        }
    }

    /** 다국어 카테고리 */
    fun getLocalizedCategory(): String {
        val lang = java.util.Locale.getDefault().language
        return when (category) {
            "독서" -> when (lang) { "ko" -> "독서"; "ja" -> "読書"; "zh" -> "阅读"; "es" -> "Lectura"; else -> "Reading" }
            "명상" -> when (lang) { "ko" -> "명상"; "ja" -> "瞑想"; "zh" -> "冥想"; "es" -> "Meditación"; else -> "Meditation" }
            "공부" -> when (lang) { "ko" -> "공부"; "ja" -> "勉強"; "zh" -> "学习"; "es" -> "Estudio"; else -> "Study" }
            "운동" -> when (lang) { "ko" -> "운동"; "ja" -> "運動"; "zh" -> "运动"; "es" -> "Ejercicio"; else -> "Exercise" }
            "웰니스" -> when (lang) { "ko" -> "웰니스"; "ja" -> "ウェルネス"; "zh" -> "健康"; "es" -> "Bienestar"; else -> "Wellness" }
            else -> category
        }
    }

    /** 다국어 칭호 */
    fun getLocalizedTitle(): String {
        val lang = java.util.Locale.getDefault().language
        return when (this) {
            READING_15 -> when (lang) { "ko" -> "책을 좋아하는"; "ja" -> "本好きな"; "zh" -> "爱读书的"; "es" -> "Amante de libros"; else -> "Book Lover" }
            READING_30 -> when (lang) { "ko" -> "독서하는"; "ja" -> "読書家"; "zh" -> "读书人"; "es" -> "Lector"; else -> "Reader" }
            READING_60 -> when (lang) { "ko" -> "책벌레"; "ja" -> "本の虫"; "zh" -> "书虫"; "es" -> "Ratón de biblioteca"; else -> "Bookworm" }
            MEDITATION_5 -> when (lang) { "ko" -> "명상 입문자"; "ja" -> "瞑想初心者"; "zh" -> "冥想入门"; "es" -> "Principiante zen"; else -> "Meditation Beginner" }
            MEDITATION_15 -> when (lang) { "ko" -> "명상하는"; "ja" -> "瞑想する"; "zh" -> "冥想者"; "es" -> "Meditador"; else -> "Meditator" }
            MEDITATION_30 -> when (lang) { "ko" -> "명상 마스터"; "ja" -> "瞑想マスター"; "zh" -> "冥想大师"; "es" -> "Maestro zen"; else -> "Meditation Master" }
            STUDY_30 -> when (lang) { "ko" -> "공부하는"; "ja" -> "勉強する"; "zh" -> "学习中的"; "es" -> "Estudioso"; else -> "Studious" }
            STUDY_60 -> when (lang) { "ko" -> "열공하는"; "ja" -> "猛勉強する"; "zh" -> "努力学习的"; "es" -> "Estudioso dedicado"; else -> "Hard Worker" }
            STUDY_120 -> when (lang) { "ko" -> "공부왕"; "ja" -> "勉強王"; "zh" -> "学霸"; "es" -> "Rey del estudio"; else -> "Study King" }
            SQUAT_10 -> when (lang) { "ko" -> "다리 튼튼한"; "ja" -> "足が丈夫な"; "zh" -> "腿部结实的"; "es" -> "Piernas fuertes"; else -> "Strong Legs" }
            SQUAT_20 -> when (lang) { "ko" -> "스쿼트 마스터"; "ja" -> "スクワットマスター"; "zh" -> "深蹲大师"; "es" -> "Maestro sentadillas"; else -> "Squat Master" }
            SQUAT_50 -> when (lang) { "ko" -> "하체 끝판왕"; "ja" -> "下半身の王"; "zh" -> "下肢之王"; "es" -> "Rey de piernas"; else -> "Leg Champion" }
            PLANK_30 -> when (lang) { "ko" -> "코어 튼튼한"; "ja" -> "体幹が強い"; "zh" -> "核心强壮的"; "es" -> "Core fuerte"; else -> "Strong Core" }
            PLANK_60 -> when (lang) { "ko" -> "플랭크 마스터"; "ja" -> "プランクマスター"; "zh" -> "平板支撑大师"; "es" -> "Maestro plancha"; else -> "Plank Master" }
            PLANK_120 -> when (lang) { "ko" -> "코어 끝판왕"; "ja" -> "体幹の王"; "zh" -> "核心之王"; "es" -> "Rey del core"; else -> "Core Champion" }
            FASTING_16_8 -> when (lang) { "ko" -> "건강한"; "ja" -> "健康な"; "zh" -> "健康的"; "es" -> "Saludable"; else -> "Healthy" }
            FASTING_18_6 -> when (lang) { "ko" -> "단식 마스터"; "ja" -> "断食マスター"; "zh" -> "禁食大师"; "es" -> "Maestro ayuno"; else -> "Fasting Master" }
            FASTING_20_4 -> when (lang) { "ko" -> "단식 끝판왕"; "ja" -> "断食の王"; "zh" -> "禁食之王"; "es" -> "Rey del ayuno"; else -> "Fasting Champion" }
        }
    }
}

enum class ChallengeStatus {
    NOT_STARTED,  // 시작 전
    RUNNING,      // 진행 중
    PAUSED,       // 일시정지 (앱 이탈)
    COMPLETED,    // 성공
    ENDED         // 종료 (2회 이탈)
}

data class Challenge(
    val type: ChallengeType,
    val durationMinutes: Double = 0.0,    // TIME_BASED용 (30초 = 0.5분)
    val targetReps: Int = 0,              // REP_BASED용
    val iconRes: Int
) {
    val name: String get() = type.displayName
    val category: String get() = type.category
    val mode: ChallengeMode get() = type.mode

    val isRepBased: Boolean get() = mode == ChallengeMode.REP_BASED
    val isTimeBased: Boolean get() = mode == ChallengeMode.TIME_BASED
    val isBackgroundTimer: Boolean get() = mode == ChallengeMode.BACKGROUND_TIMER
}

data class ChallengeProgress(
    val challenge: Challenge,
    val startTime: Long = 0L,
    val elapsedSeconds: Int = 0,
    val exitCount: Int = 0,
    val status: ChallengeStatus = ChallengeStatus.NOT_STARTED,

    // 횟수 기반 필드
    val currentReps: Int = 0,
    val lastRepTime: Long = 0L
) {
    val remainingSeconds: Int
        get() = if (challenge.isTimeBased || challenge.isBackgroundTimer) {
            ((challenge.durationMinutes * 60) - elapsedSeconds).toInt()
        } else 0

    val progressPercent: Float
        get() = when {
            challenge.isRepBased -> {
                if (challenge.targetReps > 0) {
                    currentReps.toFloat() / challenge.targetReps
                } else 0f
            }
            challenge.isTimeBased || challenge.isBackgroundTimer -> {
                if (challenge.durationMinutes > 0) {
                    (elapsedSeconds.toFloat() / (challenge.durationMinutes * 60)).toFloat()
                } else 0f
            }
            else -> 0f
        }

    val remainingReps: Int
        get() = if (challenge.isRepBased) {
            (challenge.targetReps - currentReps).coerceAtLeast(0)
        } else 0
}

class ChallengeManager private constructor(private val context: Context) {
    private val TAG = "ChallengeManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
    private val prefManager = PreferenceManager(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val hapticManager = HapticManager(context)

    // 운동 센서 매니저
    private var exerciseSensorManager: ExerciseSensorManager? = null

    // 일반 챌린지 (독서, 명상, 공부, 플랭크, 스쿼트)
    private val _currentProgress = MutableStateFlow<ChallengeProgress?>(null)
    val currentProgress: StateFlow<ChallengeProgress?> = _currentProgress.asStateFlow()

    // 백그라운드 챌린지 (간헐적 단식) - 일반 챌린지와 동시 진행 가능
    private val _backgroundProgress = MutableStateFlow<ChallengeProgress?>(null)
    val backgroundProgress: StateFlow<ChallengeProgress?> = _backgroundProgress.asStateFlow()

    // 오늘 완료 횟수 (챌린지 타입별)
    private val _todayCompletionCounts = MutableStateFlow<Map<ChallengeType, Int>>(emptyMap())
    val todayCompletionCounts: StateFlow<Map<ChallengeType, Int>> = _todayCompletionCounts.asStateFlow()

    // 방금 완료된 챌린지 (펫 칭찬 메시지용)
    private val _justCompletedChallenge = MutableStateFlow<Challenge?>(null)
    val justCompletedChallenge: StateFlow<Challenge?> = _justCompletedChallenge.asStateFlow()

    fun clearJustCompletedChallenge() {
        _justCompletedChallenge.value = null
    }

    // 방금 종료된 챌린지 (펫 응원 메시지용)
    private val _justEndedChallenge = MutableStateFlow<Challenge?>(null)
    val justEndedChallenge: StateFlow<Challenge?> = _justEndedChallenge.asStateFlow()

    fun clearJustEndedChallenge() {
        _justEndedChallenge.value = null
    }

    // 획득한 칭호 목록
    private val _unlockedTitles = MutableStateFlow<Set<ChallengeType>>(emptySet())
    val unlockedTitles: StateFlow<Set<ChallengeType>> = _unlockedTitles.asStateFlow()

    // 현재 장착된 칭호
    private val _equippedTitle = MutableStateFlow<ChallengeType?>(null)
    val equippedTitle: StateFlow<ChallengeType?> = _equippedTitle.asStateFlow()

    // 방금 획득한 칭호 (알림용)
    private val _justUnlockedTitle = MutableStateFlow<ChallengeType?>(null)
    val justUnlockedTitle: StateFlow<ChallengeType?> = _justUnlockedTitle.asStateFlow()

    fun clearJustUnlockedTitle() {
        _justUnlockedTitle.value = null
    }

    // 방금 해금된 스킨 (다이얼로그 표시용)
    private val _justUnlockedSkin = MutableStateFlow<com.moveoftoday.walkorwait.pet.PetSkin?>(null)
    val justUnlockedSkin: StateFlow<com.moveoftoday.walkorwait.pet.PetSkin?> = _justUnlockedSkin.asStateFlow()

    fun clearJustUnlockedSkin() {
        _justUnlockedSkin.value = null
    }

    // 모든 챌린지 목록
    val allChallenges: List<Challenge> = listOf(
        // 독서
        Challenge(ChallengeType.READING_15, durationMinutes = 15.0, iconRes = R.drawable.challenge_reading),
        Challenge(ChallengeType.READING_30, durationMinutes = 30.0, iconRes = R.drawable.challenge_reading),
        Challenge(ChallengeType.READING_60, durationMinutes = 60.0, iconRes = R.drawable.challenge_reading),
        // 명상
        Challenge(ChallengeType.MEDITATION_5, durationMinutes = 5.0, iconRes = R.drawable.challenge_meditation),
        Challenge(ChallengeType.MEDITATION_15, durationMinutes = 15.0, iconRes = R.drawable.challenge_meditation),
        Challenge(ChallengeType.MEDITATION_30, durationMinutes = 30.0, iconRes = R.drawable.challenge_meditation),
        // 공부
        Challenge(ChallengeType.STUDY_30, durationMinutes = 30.0, iconRes = R.drawable.challenge_study),
        Challenge(ChallengeType.STUDY_60, durationMinutes = 60.0, iconRes = R.drawable.challenge_study),
        Challenge(ChallengeType.STUDY_120, durationMinutes = 120.0, iconRes = R.drawable.challenge_study),

        // 운동 - 스쿼트 (횟수 기반)
        Challenge(ChallengeType.SQUAT_10, targetReps = 10, iconRes = R.drawable.challenge_squat),
        Challenge(ChallengeType.SQUAT_20, targetReps = 20, iconRes = R.drawable.challenge_squat),
        Challenge(ChallengeType.SQUAT_50, targetReps = 50, iconRes = R.drawable.challenge_squat),

        // 운동 - 플랭크 (타이머 기반)
        Challenge(ChallengeType.PLANK_30, durationMinutes = 0.5, iconRes = R.drawable.challenge_plank),  // 30초
        Challenge(ChallengeType.PLANK_60, durationMinutes = 1.0, iconRes = R.drawable.challenge_plank),  // 1분
        Challenge(ChallengeType.PLANK_120, durationMinutes = 2.0, iconRes = R.drawable.challenge_plank), // 2분

        // 웰니스 - 간헐적 단식 (타이머 기반)
        Challenge(ChallengeType.FASTING_16_8, durationMinutes = 960.0, iconRes = R.drawable.challenge_fasting),  // 16시간 = 960분
        Challenge(ChallengeType.FASTING_18_6, durationMinutes = 1080.0, iconRes = R.drawable.challenge_fasting), // 18시간 = 1080분
        Challenge(ChallengeType.FASTING_20_4, durationMinutes = 1200.0, iconRes = R.drawable.challenge_fasting)  // 20시간 = 1200분
    )

    init {
        loadTodayCompletedChallenges()
        loadUnlockedTitles()
        loadEquippedTitle()

        // 로그인 상태이고 로컬에 오늘의 통계가 없으면 Firebase에서 로드
        if (auth.currentUser != null && _todayCompletionCounts.value.isEmpty()) {
            scope.launch {
                loadTodayStatsFromFirebase()
            }
        }
    }

    fun getChallengesByCategory(category: String?): List<Challenge> {
        // "전체" / "All" 등 로컬라이즈된 전체 카테고리명
        val allCategory = getAllCategoryLabel()
        return if (category == null || category == allCategory || category == "전체" || category == "All") {
            allChallenges
        } else {
            // 로컬라이즈된 카테고리명으로 필터링
            allChallenges.filter {
                it.category == category || it.type.getLocalizedCategory() == category
            }
        }
    }

    /** 전체 카테고리 라벨 */
    fun getAllCategoryLabel(): String {
        return when (java.util.Locale.getDefault().language) {
            "ko" -> "전체"
            "ja" -> "すべて"
            "zh" -> "全部"
            "es" -> "Todo"
            else -> "All"
        }
    }

    /** 카테고리 목록 (다국어) */
    fun getLocalizedCategories(): List<String> {
        val lang = java.util.Locale.getDefault().language
        return listOf(
            getAllCategoryLabel(),
            when (lang) { "ko" -> "독서"; "ja" -> "読書"; "zh" -> "阅读"; "es" -> "Lectura"; else -> "Reading" },
            when (lang) { "ko" -> "명상"; "ja" -> "瞑想"; "zh" -> "冥想"; "es" -> "Meditación"; else -> "Meditation" },
            when (lang) { "ko" -> "공부"; "ja" -> "勉強"; "zh" -> "学习"; "es" -> "Estudio"; else -> "Study" },
            when (lang) { "ko" -> "운동"; "ja" -> "運動"; "zh" -> "运动"; "es" -> "Ejercicio"; else -> "Exercise" },
            when (lang) { "ko" -> "웰니스"; "ja" -> "ウェルネス"; "zh" -> "健康"; "es" -> "Bienestar"; else -> "Wellness" }
        )
    }

    fun searchChallenges(query: String): List<Challenge> {
        if (query.isBlank()) return allChallenges
        return allChallenges.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    // 챌린지 준비 (시작 전 상태)
    fun prepareChallenge(challenge: Challenge) {
        // 이전 챌린지 상태 초기화
        _justCompletedChallenge.value = null

        val newProgress = ChallengeProgress(
            challenge = challenge,
            startTime = 0L,
            elapsedSeconds = 0,
            exitCount = 0,
            status = ChallengeStatus.NOT_STARTED,
            currentReps = 0,  // 명시적 초기화
            lastRepTime = 0L
        )

        // 백그라운드 챌린지는 별도로 관리
        if (challenge.isBackgroundTimer) {
            _backgroundProgress.value = newProgress
            Log.d(TAG, "prepareChallenge (BACKGROUND): ${challenge.name}")
        } else {
            _currentProgress.value = newProgress
            Log.d(TAG, "prepareChallenge (FOREGROUND): ${challenge.name}, isRepBased=${challenge.isRepBased}")
        }
    }

    // 챌린지 시작
    fun startChallenge(challenge: Challenge) {
        _currentProgress.value = ChallengeProgress(
            challenge = challenge,
            startTime = System.currentTimeMillis(),
            elapsedSeconds = 0,
            exitCount = 0,
            status = ChallengeStatus.RUNNING
        )
    }

    // 준비된 챌린지 시작 (NOT_STARTED -> RUNNING)
    fun beginChallenge(startTimeOffsetHours: Int = 0) {
        // 백그라운드와 일반 챌린지 모두 확인
        val backgroundProgress = _backgroundProgress.value
        val foregroundProgress = _currentProgress.value

        val progress = if (backgroundProgress?.status == ChallengeStatus.NOT_STARTED) {
            backgroundProgress
        } else if (foregroundProgress?.status == ChallengeStatus.NOT_STARTED) {
            foregroundProgress
        } else {
            Log.w(TAG, "Cannot begin challenge - no NOT_STARTED challenge found")
            return
        }

        Log.d(TAG, "beginChallenge: ${progress.challenge.name}, isRepBased=${progress.challenge.isRepBased}, isBackground=${progress.challenge.isBackgroundTimer}, offsetHours=$startTimeOffsetHours")

        // Rep 기반 챌린지면 센서 시작 + 걸음수 고정
        if (progress.challenge.isRepBased) {
            startExerciseSensor(progress.challenge)
            // 걸음수 측정 고정 (스쿼트 중 걸음수 증가 방지)
            StepCounterService.getInstance()?.getStepSensorManager()?.freezeStepsForRepChallenge()
        }

        // 시작 시간 계산 (offset 적용)
        val startTime = System.currentTimeMillis() - (startTimeOffsetHours * 3600 * 1000L)
        Log.d(TAG, "Start time: now=${System.currentTimeMillis()}, offset=${startTimeOffsetHours}h, adjusted=$startTime")

        val newProgress = progress.copy(
            startTime = startTime,
            status = ChallengeStatus.RUNNING,
            currentReps = 0  // 확실하게 0으로 초기화
        )

        // 백그라운드/일반 챌린지에 따라 적절한 progress에 저장
        if (progress.challenge.isBackgroundTimer) {
            _backgroundProgress.value = newProgress
            Log.d(TAG, "Background challenge RUNNING")
            // 간헐적 단식 위젯 업데이트
            FastingWidgetProvider.updateAllWidgets(context)
        } else {
            _currentProgress.value = newProgress
            Log.d(TAG, "Foreground challenge RUNNING - currentReps=${newProgress.currentReps}")
        }
    }

    // 운동 센서 시작 (스쿼트)
    private fun startExerciseSensor(challenge: Challenge) {
        val petType = prefManager.getPetTypeV2()

        exerciseSensorManager = ExerciseSensorManager(context).apply {
            // 콜백을 먼저 설정
            onSquatDetected = { reps ->
                Log.d(TAG, "Squat detected callback: $reps/${challenge.targetReps}")
                onRepCompleted(reps)
            }
            // 그 다음 리스닝 시작
            startListening(challenge.targetReps, petType)
        }
        Log.d(TAG, "Exercise sensor started for ${challenge.name}, target: ${challenge.targetReps}")
    }

    // Rep 완료 콜백
    private fun onRepCompleted(reps: Int) {
        val progress = _currentProgress.value ?: return
        if (!progress.challenge.isRepBased) return

        Log.d(TAG, "onRepCompleted: reps=$reps, target=${progress.challenge.targetReps}, current=${progress.currentReps}")

        val newProgress = progress.copy(
            currentReps = reps,
            lastRepTime = System.currentTimeMillis()
        )

        _currentProgress.value = newProgress

        // 목표 달성 체크
        if (reps >= progress.challenge.targetReps) {
            Log.d(TAG, "✅ Challenge COMPLETED: $reps >= ${progress.challenge.targetReps}")
            _currentProgress.value = newProgress.copy(
                status = ChallengeStatus.COMPLETED
            )
            _justCompletedChallenge.value = progress.challenge
            markChallengeCompleted(progress.challenge.type)

            // 센서 중지
            stopExerciseSensor()
        } else {
            Log.d(TAG, "Progress updated: $reps/${progress.challenge.targetReps}")
        }
    }

    // 운동 센서 중지
    private fun stopExerciseSensor() {
        exerciseSensorManager?.stopListening()
        exerciseSensorManager?.cleanup()
        exerciseSensorManager = null
        // 걸음수 측정 재개
        StepCounterService.getInstance()?.getStepSensorManager()?.unfreezeSteps()
        Log.d(TAG, "Exercise sensor stopped")
    }

    // 타이머 업데이트 (1초마다 호출) - TIME_BASED 전용
    fun updateTimer() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.RUNNING) return

        // Rep 기반 챌린지는 타이머 사용 안 함
        if (progress.challenge.isRepBased) {
            Log.d(TAG, "updateTimer: Skipping rep-based challenge")
            return
        }

        // 백그라운드 타이머는 별도 함수 사용
        if (progress.challenge.isBackgroundTimer) {
            updateBackgroundTimer()
            return
        }

        val newElapsed = progress.elapsedSeconds + 1
        val totalSeconds = (progress.challenge.durationMinutes * 60).toInt()

        if (newElapsed >= totalSeconds) {
            // 챌린지 완료
            Log.d(TAG, "updateTimer: Challenge completed via timer ($newElapsed >= $totalSeconds)")
            _currentProgress.value = progress.copy(
                elapsedSeconds = totalSeconds,
                status = ChallengeStatus.COMPLETED
            )
            _justCompletedChallenge.value = progress.challenge
            markChallengeCompleted(progress.challenge.type)
        } else {
            _currentProgress.value = progress.copy(elapsedSeconds = newElapsed)
        }
    }

    // 백그라운드 타이머 업데이트 (간헐적 단식 전용) - startTime 기준 계산
    fun updateBackgroundTimer() {
        val progress = _backgroundProgress.value ?: return
        if (!progress.challenge.isBackgroundTimer || progress.status != ChallengeStatus.RUNNING) return

        // startTime으로부터 실제 경과 시간 계산
        val elapsedMillis = System.currentTimeMillis() - progress.startTime
        val elapsedSeconds = (elapsedMillis / 1000).toInt()
        val totalSeconds = (progress.challenge.durationMinutes * 60).toInt()

        Log.d(TAG, "updateBackgroundTimer: elapsed=${elapsedSeconds}s / total=${totalSeconds}s (${progress.challenge.name})")

        if (elapsedSeconds >= totalSeconds) {
            // 자동 완료
            Log.d(TAG, "✅ Background timer completed: ${progress.challenge.name}")
            _backgroundProgress.value = progress.copy(
                elapsedSeconds = totalSeconds,
                status = ChallengeStatus.COMPLETED
            )
            _justCompletedChallenge.value = progress.challenge
            markChallengeCompleted(progress.challenge.type)
            // 간헐적 단식 위젯 업데이트 (완료)
            FastingWidgetProvider.updateAllWidgets(context)
        } else {
            // 경과 시간 업데이트
            _backgroundProgress.value = progress.copy(elapsedSeconds = elapsedSeconds)
            // 간헐적 단식 위젯 업데이트 (진행 중)
            FastingWidgetProvider.updateAllWidgets(context)
        }
    }

    // 앱 이탈 시 호출
    fun onAppExit() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.RUNNING && progress.status != ChallengeStatus.PAUSED) return

        // 백그라운드 타이머(간헐적 단식)는 백그라운드 진행 허용
        if (progress.challenge.isBackgroundTimer) {
            Log.d(TAG, "Background timer challenge - allowing background execution")
            return
        }

        val newExitCount = progress.exitCount + 1

        if (newExitCount >= 2) {
            // 2회 이탈 - 자동 종료
            _currentProgress.value = progress.copy(
                exitCount = newExitCount,
                status = ChallengeStatus.ENDED
            )
            _justEndedChallenge.value = progress.challenge
        } else {
            // 1회 이탈 - 일시정지
            _currentProgress.value = progress.copy(
                exitCount = newExitCount,
                status = ChallengeStatus.PAUSED
            )
        }
    }

    // 챌린지 재개
    fun resumeChallenge() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.PAUSED) return

        _currentProgress.value = progress.copy(status = ChallengeStatus.RUNNING)
    }

    // 챌린지 포기 (다음에 하기)
    fun cancelChallenge() {
        // 백그라운드 챌린지 취소 확인
        val backgroundProgress = _backgroundProgress.value
        if (backgroundProgress != null &&
            (backgroundProgress.status == ChallengeStatus.RUNNING ||
             backgroundProgress.status == ChallengeStatus.NOT_STARTED)) {
            Log.d(TAG, "Cancelling background challenge: ${backgroundProgress.challenge.name}")
            _backgroundProgress.value = null
            // 간헐적 단식 위젯 업데이트 (취소)
            FastingWidgetProvider.updateAllWidgets(context)
            return
        }

        // 일반 챌린지 취소
        stopExerciseSensor()
        _currentProgress.value = null
    }

    // 챌린지 완료 기록 (횟수 증가 + 칭호 획득 + Firebase 저장)
    private fun markChallengeCompleted(type: ChallengeType) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val key = "count_${today}_${type.name}"
        val currentCount = prefs.getInt(key, 0)
        val newCount = currentCount + 1
        prefs.edit().putInt(key, newCount).apply()

        // 🎉 챌린지 완료 햅틱 피드백
        hapticManager.goalAchieved()
        Log.d(TAG, "🎉 Challenge completed: ${type.displayName} - Haptic triggered")

        // StateFlow 업데이트
        val currentMap = _todayCompletionCounts.value.toMutableMap()
        currentMap[type] = newCount
        _todayCompletionCounts.value = currentMap

        // 카테고리별 챌린지 완료 횟수 증가 (스킨 해금용)
        prefManager.incrementChallengeCount(type.category)

        // 🎁 스킨 자동 해금 체크
        val newSkins = prefManager.checkAndUnlockNewSkins()
        if (newSkins.isNotEmpty()) {
            Log.d(TAG, "🎁 New skins unlocked: ${newSkins.map { it.displayName }}")
            // 첫 번째 해금된 스킨 다이얼로그 표시 (여러 개면 하나씩)
            _justUnlockedSkin.value = newSkins.first()
        }

        // 칭호 획득 (처음 완료 시)
        val isFirstCompletion = !_unlockedTitles.value.contains(type)
        unlockTitle(type)

        // Firebase에 챌린지 완료 기록 저장
        scope.launch {
            saveChallengeCompletionToFirebase(type, today, newCount, isFirstCompletion)
        }
    }

    // Firebase에 챌린지 완료 기록 저장
    private suspend fun saveChallengeCompletionToFirebase(
        type: ChallengeType,
        date: String,
        count: Int,
        isFirstCompletion: Boolean
    ) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val now = System.currentTimeMillis()

            // 1. 챌린지 완료 이력 저장 (users/{userId}/challengeHistory)
            val historyData = hashMapOf(
                "challengeType" to type.name,
                "challengeName" to type.displayName,
                "category" to type.category,
                "completedAt" to now,
                "date" to date,
                "dailyCount" to count,
                "earnedTitle" to if (isFirstCompletion) type.title else null
            )
            firestore.collection("users")
                .document(userId)
                .collection("challengeHistory")
                .add(historyData)
                .await()

            // 2. 일일 챌린지 통계 업데이트 (users/{userId}/challengeStats/{date})
            val statsData = hashMapOf(
                "date" to date,
                "lastUpdated" to now,
                "${type.name}_count" to count
            )
            firestore.collection("users")
                .document(userId)
                .collection("challengeStats")
                .document(date)
                .set(statsData, SetOptions.merge())
                .await()

            // 3. 사용자 문서에 총 챌린지 완료 수 업데이트
            val userUpdate = hashMapOf(
                "totalChallengesCompleted" to com.google.firebase.firestore.FieldValue.increment(1),
                "lastChallengeAt" to now,
                "lastChallengeType" to type.name
            )
            firestore.collection("users")
                .document(userId)
                .set(userUpdate, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Challenge completion saved to Firebase: ${type.name}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save challenge to Firebase: ${e.message}")
        }
    }

    private fun loadTodayCompletedChallenges() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        val counts = mutableMapOf<ChallengeType, Int>()
        ChallengeType.entries.forEach { type ->
            val key = "count_${today}_${type.name}"
            val count = prefs.getInt(key, 0)
            if (count > 0) {
                counts[type] = count
            }
        }
        _todayCompletionCounts.value = counts
    }

    fun getCompletionCount(type: ChallengeType): Int {
        return _todayCompletionCounts.value[type] ?: 0
    }

    // 현재 진행 중인 챌린지 정리
    fun clearCurrentProgress() {
        _currentProgress.value = null
    }

    // 디버그용: 챌린지 즉시 완료
    fun debugCompleteChallenge() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.RUNNING && progress.status != ChallengeStatus.NOT_STARTED) return

        if (progress.challenge.isRepBased) {
            // Rep 기반: targetReps로 설정
            _currentProgress.value = progress.copy(
                currentReps = progress.challenge.targetReps,
                status = ChallengeStatus.COMPLETED
            )
            stopExerciseSensor()
        } else {
            // Time 기반: totalSeconds로 설정
            val totalSeconds = (progress.challenge.durationMinutes * 60).toInt()
            _currentProgress.value = progress.copy(
                elapsedSeconds = totalSeconds,
                status = ChallengeStatus.COMPLETED
            )
        }

        _justCompletedChallenge.value = progress.challenge
        markChallengeCompleted(progress.challenge.type)
        Log.d(TAG, "🧪 Debug: Challenge completed instantly")
    }

    // ========== 칭호 관리 ==========

    // 칭호 획득
    private fun unlockTitle(type: ChallengeType) {
        if (_unlockedTitles.value.contains(type)) return // 이미 획득함

        val unlocked = prefs.getStringSet("unlocked_titles", emptySet())?.toMutableSet() ?: mutableSetOf()
        unlocked.add(type.name)
        prefs.edit().putStringSet("unlocked_titles", unlocked).apply()

        _unlockedTitles.value = _unlockedTitles.value + type
        _justUnlockedTitle.value = type

        // Firebase에 칭호 획득 기록 저장
        scope.launch {
            saveTitleUnlockToFirebase(type)
        }
    }

    // Firebase에 칭호 획득 기록 저장 (부모 문서 배열로 통합)
    private suspend fun saveTitleUnlockToFirebase(type: ChallengeType) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val now = System.currentTimeMillis()

            // 부모 문서에 획득 칭호 배열로 저장 (서브컬렉션 대신)
            val userUpdate = hashMapOf(
                "unlockedTitles" to _unlockedTitles.value.map { it.name },
                "lastTitleUnlockedAt" to now,
                "lastActiveAt" to now
            )
            firestore.collection("users")
                .document(userId)
                .set(userUpdate, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Title unlock saved to Firebase (array): ${type.title}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save title unlock to Firebase: ${e.message}")
        }
    }

    // 획득한 칭호 로드
    private fun loadUnlockedTitles() {
        val unlocked = prefs.getStringSet("unlocked_titles", emptySet()) ?: emptySet()
        _unlockedTitles.value = unlocked.mapNotNull {
            try { ChallengeType.valueOf(it) } catch (e: Exception) { null }
        }.toSet()
        Log.d(TAG, "🏆 Loaded unlocked titles: ${_unlockedTitles.value}")
    }

    // 장착된 칭호 로드
    private fun loadEquippedTitle() {
        val equipped = prefs.getString("equipped_title", null)
        _equippedTitle.value = equipped?.let {
            try { ChallengeType.valueOf(it) } catch (e: Exception) { null }
        }
        Log.d(TAG, "🏆 Loaded equipped title: ${_equippedTitle.value}")
    }

    /**
     * SharedPreferences에서 칭호 데이터 다시 로드
     * Firebase에서 복원 완료 후 호출
     */
    fun reloadFromPreferences() {
        Log.d(TAG, "🔄 Reloading challenge data from SharedPreferences...")
        loadUnlockedTitles()
        loadEquippedTitle()
        loadTodayCompletedChallenges()
        Log.d(TAG, "✅ Challenge data reloaded - unlockedTitles: ${_unlockedTitles.value.size}, equipped: ${_equippedTitle.value}")
    }

    /**
     * Firebase에서 오늘의 챌린지 통계 로드
     * 앱 재설치 후 복원 시 호출
     */
    suspend fun loadTodayStatsFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        try {
            val statsDoc = firestore.collection("users")
                .document(userId)
                .collection("challengeStats")
                .document(today)
                .get()
                .await()

            if (statsDoc.exists()) {
                val counts = mutableMapOf<ChallengeType, Int>()
                val editor = prefs.edit()

                ChallengeType.entries.forEach { type ->
                    val countKey = "${type.name}_count"
                    val count = statsDoc.getLong(countKey)?.toInt() ?: 0
                    if (count > 0) {
                        counts[type] = count
                        // SharedPreferences에도 저장 (로컬 캐시)
                        val prefKey = "count_${today}_${type.name}"
                        editor.putInt(prefKey, count)
                    }
                }
                editor.apply()

                _todayCompletionCounts.value = counts
                Log.d(TAG, "📊 Loaded today's stats from Firebase: $counts")
            } else {
                Log.d(TAG, "📊 No stats for today in Firebase")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load today's stats from Firebase: ${e.message}")
        }
    }

    // 칭호 장착
    fun equipTitle(type: ChallengeType?) {
        if (type != null && !_unlockedTitles.value.contains(type)) return // 획득하지 않은 칭호

        if (type == null) {
            prefs.edit().remove("equipped_title").apply()
        } else {
            prefs.edit().putString("equipped_title", type.name).apply()
        }
        _equippedTitle.value = type

        // Firebase에 장착된 칭호 저장
        scope.launch {
            saveEquippedTitleToFirebase(type)
        }
    }

    // Firebase에 장착된 칭호 저장 (부모 문서에만)
    private suspend fun saveEquippedTitleToFirebase(type: ChallengeType?) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val now = System.currentTimeMillis()
            val userUpdate = hashMapOf(
                "equippedTitle" to type?.name,
                "equippedTitleDisplay" to type?.title,
                "lastTitleChangeAt" to now,
                "lastActiveAt" to now
            )
            firestore.collection("users")
                .document(userId)
                .set(userUpdate, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Equipped title saved to Firebase (parent doc): ${type?.title ?: "none"}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save equipped title to Firebase: ${e.message}")
        }
    }

    // 칭호가 적용된 펫 이름 반환
    fun getPetNameWithTitle(petName: String): String {
        val equipped = _equippedTitle.value ?: return petName
        return "${equipped.title} $petName"
    }

    // 칭호 획득 여부
    fun isTitleUnlocked(type: ChallengeType): Boolean {
        return _unlockedTitles.value.contains(type)
    }

    companion object {
        @Volatile
        private var instance: ChallengeManager? = null

        fun getInstance(context: Context): ChallengeManager {
            return instance ?: synchronized(this) {
                instance ?: ChallengeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
