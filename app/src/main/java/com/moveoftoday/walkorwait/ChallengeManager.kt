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

enum class ChallengeType(val displayName: String, val category: String, val title: String) {
    READING_15("15분 독서", "독서", "책을 좋아하는"),
    READING_30("30분 독서", "독서", "독서하는"),
    READING_60("1시간 독서", "독서", "책벌레"),
    MEDITATION_5("5분 명상", "명상", "명상 입문자"),
    MEDITATION_15("15분 명상", "명상", "명상하는"),
    MEDITATION_30("30분 명상", "명상", "명상 마스터"),
    STUDY_30("30분 공부", "공부", "공부하는"),
    STUDY_60("1시간 공부", "공부", "열공하는"),
    STUDY_120("2시간 공부", "공부", "공부왕")
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
    val durationMinutes: Int,
    val iconRes: Int
) {
    val name: String get() = type.displayName
    val category: String get() = type.category
}

data class ChallengeProgress(
    val challenge: Challenge,
    val startTime: Long = 0L,
    val elapsedSeconds: Int = 0,
    val exitCount: Int = 0,
    val status: ChallengeStatus = ChallengeStatus.NOT_STARTED
) {
    val remainingSeconds: Int
        get() = (challenge.durationMinutes * 60) - elapsedSeconds

    val progressPercent: Float
        get() = elapsedSeconds.toFloat() / (challenge.durationMinutes * 60)
}

class ChallengeManager private constructor(context: Context) {
    private val TAG = "ChallengeManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentProgress = MutableStateFlow<ChallengeProgress?>(null)
    val currentProgress: StateFlow<ChallengeProgress?> = _currentProgress.asStateFlow()

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

    // 모든 챌린지 목록
    val allChallenges: List<Challenge> = listOf(
        // 독서
        Challenge(ChallengeType.READING_15, 15, R.drawable.challenge_reading),
        Challenge(ChallengeType.READING_30, 30, R.drawable.challenge_reading),
        Challenge(ChallengeType.READING_60, 60, R.drawable.challenge_reading),
        // 명상
        Challenge(ChallengeType.MEDITATION_5, 5, R.drawable.challenge_meditation),
        Challenge(ChallengeType.MEDITATION_15, 15, R.drawable.challenge_meditation),
        Challenge(ChallengeType.MEDITATION_30, 30, R.drawable.challenge_meditation),
        // 공부
        Challenge(ChallengeType.STUDY_30, 30, R.drawable.challenge_study),
        Challenge(ChallengeType.STUDY_60, 60, R.drawable.challenge_study),
        Challenge(ChallengeType.STUDY_120, 120, R.drawable.challenge_study)
    )

    init {
        loadTodayCompletedChallenges()
        loadUnlockedTitles()
        loadEquippedTitle()
    }

    fun getChallengesByCategory(category: String?): List<Challenge> {
        return if (category == null || category == "전체") {
            allChallenges
        } else {
            allChallenges.filter { it.category == category }
        }
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
        _currentProgress.value = ChallengeProgress(
            challenge = challenge,
            startTime = 0L,
            elapsedSeconds = 0,
            exitCount = 0,
            status = ChallengeStatus.NOT_STARTED
        )
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
    fun beginChallenge() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.NOT_STARTED) return

        _currentProgress.value = progress.copy(
            startTime = System.currentTimeMillis(),
            status = ChallengeStatus.RUNNING
        )
    }

    // 타이머 업데이트 (1초마다 호출)
    fun updateTimer() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.RUNNING) return

        val newElapsed = progress.elapsedSeconds + 1
        val totalSeconds = progress.challenge.durationMinutes * 60

        if (newElapsed >= totalSeconds) {
            // 챌린지 완료
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

    // 앱 이탈 시 호출
    fun onAppExit() {
        val progress = _currentProgress.value ?: return
        if (progress.status != ChallengeStatus.RUNNING && progress.status != ChallengeStatus.PAUSED) return

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

        // StateFlow 업데이트
        val currentMap = _todayCompletionCounts.value.toMutableMap()
        currentMap[type] = newCount
        _todayCompletionCounts.value = currentMap

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

        val totalSeconds = progress.challenge.durationMinutes * 60
        _currentProgress.value = progress.copy(
            elapsedSeconds = totalSeconds,
            status = ChallengeStatus.COMPLETED
        )
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

    // Firebase에 칭호 획득 기록 저장
    private suspend fun saveTitleUnlockToFirebase(type: ChallengeType) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val now = System.currentTimeMillis()

            // 칭호 획득 이력 저장
            val titleData = hashMapOf(
                "titleType" to type.name,
                "title" to type.title,
                "challengeName" to type.displayName,
                "unlockedAt" to now
            )
            firestore.collection("users")
                .document(userId)
                .collection("unlockedTitles")
                .document(type.name)
                .set(titleData)
                .await()

            // 사용자 문서에 획득 칭호 목록 업데이트
            val userUpdate = hashMapOf(
                "unlockedTitles" to _unlockedTitles.value.map { it.name },
                "lastTitleUnlockedAt" to now
            )
            firestore.collection("users")
                .document(userId)
                .set(userUpdate, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Title unlock saved to Firebase: ${type.title}")
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
    }

    // 장착된 칭호 로드
    private fun loadEquippedTitle() {
        val equipped = prefs.getString("equipped_title", null)
        _equippedTitle.value = equipped?.let {
            try { ChallengeType.valueOf(it) } catch (e: Exception) { null }
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

    // Firebase에 장착된 칭호 저장
    private suspend fun saveEquippedTitleToFirebase(type: ChallengeType?) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val userUpdate = hashMapOf(
                "equippedTitle" to type?.name,
                "equippedTitleDisplay" to type?.title,
                "lastTitleChangeAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .set(userUpdate, SetOptions.merge())
                .await()

            // settings 서브컬렉션에도 저장 (앱 복원용)
            val settingsUpdate = hashMapOf(
                "equippedTitle" to type?.name
            )
            firestore.collection("users")
                .document(userId)
                .collection("userData")
                .document("settings")
                .set(settingsUpdate, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Equipped title saved to Firebase: ${type?.title ?: "없음"}")
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
