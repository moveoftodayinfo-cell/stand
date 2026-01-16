package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * 사용자 데이터 관리 Repository
 * - 로컬(PreferenceManager)과 원격(Firebase) 데이터 동기화
 * - Single Source of Truth 패턴
 */
class UserDataRepository(
    context: Context,
    private val auth: FirebaseAuth
) {
    private val TAG = "UserDataRepository"
    private val preferenceManager = PreferenceManager(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 사용자 설정 상태
    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings

    // 오늘 걸음 수 상태
    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    init {
        // 로컬 데이터 먼저 로드
        loadLocalData()

        // Firebase 동기화 (백그라운드)
        repositoryScope.launch {
            syncWithFirebase()
        }
    }

    /**
     * 로컬 데이터 로드
     */
    private fun loadLocalData() {
        _userSettings.value = UserSettings(
            goal = preferenceManager.getGoal(),
            deposit = preferenceManager.getDeposit(),
            controlStartDate = preferenceManager.getControlStartDate(),
            controlEndDate = preferenceManager.getControlEndDate(),
            controlDays = preferenceManager.getControlDays(),
            successDays = preferenceManager.getSuccessDays(),
            paidDeposit = preferenceManager.isPaidDeposit()
        )
        _todaySteps.value = preferenceManager.getTodaySteps()
        Log.d(TAG, "📂 Local data loaded")
    }

    /**
     * Firebase와 동기화
     */
    suspend fun syncWithFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "⚠️ No user signed in, skipping Firebase sync")
            return
        }

        try {
            Log.d(TAG, "🔄 Syncing with Firebase...")

            // Firebase에서 데이터 가져오기
            val doc = firestore.collection("users")
                .document(userId)
                .collection("userData")
                .document("settings")
                .get()
                .await()

            if (doc.exists()) {
                // Firebase 데이터가 있으면 로컬과 비교
                val remoteSettings = UserSettings(
                    goal = doc.getLong("goal")?.toInt() ?: 8000,
                    deposit = doc.getLong("deposit")?.toInt() ?: 0,
                    controlStartDate = doc.getString("controlStartDate") ?: "",
                    controlEndDate = doc.getString("controlEndDate") ?: "",
                    controlDays = (doc.get("controlDays") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() }?.toSet() ?: emptySet(),
                    successDays = doc.getLong("successDays")?.toInt() ?: 0,
                    paidDeposit = doc.getBoolean("paidDeposit") ?: false
                )

                val remoteTimestamp = doc.getLong("lastSyncTimestamp") ?: 0L
                val localTimestamp = preferenceManager.getLastSyncTimestamp()

                // Firebase 데이터가 더 최신이면 로컬 업데이트
                if (remoteTimestamp > localTimestamp) {
                    Log.d(TAG, "⬇️ Firebase data is newer, updating local")
                    updateLocalSettings(remoteSettings, remoteTimestamp)
                } else {
                    Log.d(TAG, "⬆️ Local data is newer, updating Firebase")
                    uploadLocalToFirebase()
                }
            } else {
                // Firebase에 데이터 없으면 로컬 데이터 업로드
                Log.d(TAG, "📤 No Firebase data, uploading local")
                uploadLocalToFirebase()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase sync failed: ${e.message}")
        }
    }

    /**
     * 로컬 설정 업데이트
     */
    private fun updateLocalSettings(settings: UserSettings, timestamp: Long) {
        preferenceManager.saveGoal(settings.goal)
        preferenceManager.saveDeposit(settings.deposit)
        preferenceManager.saveControlStartDate(settings.controlStartDate)
        preferenceManager.saveControlEndDate(settings.controlEndDate)
        preferenceManager.saveControlDays(settings.controlDays)
        preferenceManager.saveSuccessDays(settings.successDays)
        preferenceManager.setPaidDeposit(settings.paidDeposit)
        preferenceManager.saveLastSyncTimestamp(timestamp)

        _userSettings.value = settings
        Log.d(TAG, "✅ Local settings updated from Firebase")
    }

    /**
     * 로컬 데이터를 Firebase에 업로드
     */
    private suspend fun uploadLocalToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val settings = _userSettings.value ?: return

        try {
            val timestamp = System.currentTimeMillis()
            val data = hashMapOf(
                "goal" to settings.goal,
                "deposit" to settings.deposit,
                "controlStartDate" to settings.controlStartDate,
                "controlEndDate" to settings.controlEndDate,
                "controlDays" to settings.controlDays.toList(),
                "successDays" to settings.successDays,
                "paidDeposit" to settings.paidDeposit,
                "lastSyncTimestamp" to timestamp
            )

            firestore.collection("users")
                .document(userId)
                .collection("userData")
                .document("settings")
                .set(data, SetOptions.merge())
                .await()

            preferenceManager.saveLastSyncTimestamp(timestamp)
            Log.d(TAG, "✅ Local data uploaded to Firebase")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to upload to Firebase: ${e.message}")
        }
    }

    /**
     * 목표 걸음 수 저장
     */
    fun saveGoal(goal: Int) {
        preferenceManager.saveGoal(goal)
        _userSettings.value = _userSettings.value?.copy(goal = goal)

        // Firebase 동기화
        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 보증금 저장
     */
    fun saveDeposit(deposit: Int) {
        preferenceManager.saveDeposit(deposit)
        _userSettings.value = _userSettings.value?.copy(deposit = deposit)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 제어 기간 저장
     */
    fun saveControlDates(startDate: String, endDate: String) {
        preferenceManager.saveControlStartDate(startDate)
        preferenceManager.saveControlEndDate(endDate)
        _userSettings.value = _userSettings.value?.copy(
            controlStartDate = startDate,
            controlEndDate = endDate
        )

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 제어 요일 저장
     */
    fun saveControlDays(days: Set<Int>) {
        preferenceManager.saveControlDays(days)
        _userSettings.value = _userSettings.value?.copy(controlDays = days)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 성공 일수 저장
     */
    fun saveSuccessDays(days: Int) {
        preferenceManager.saveSuccessDays(days)
        _userSettings.value = _userSettings.value?.copy(successDays = days)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 결제 여부 저장
     */
    fun setPaidDeposit(paid: Boolean) {
        preferenceManager.setPaidDeposit(paid)
        _userSettings.value = _userSettings.value?.copy(paidDeposit = paid)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
    }

    /**
     * 오늘 걸음 수 저장
     */
    fun saveTodaySteps(steps: Int) {
        preferenceManager.saveTodaySteps(steps)
        _todaySteps.value = steps

        // Firebase에도 저장
        repositoryScope.launch {
            saveDailyStepsToFirebase(getCurrentDate(), steps)
        }
    }

    /**
     * 일일 걸음 수 Firebase 저장
     */
    private suspend fun saveDailyStepsToFirebase(dateId: String, steps: Int) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val data = hashMapOf(
                "date" to dateId,
                "steps" to steps,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("dailySteps")
                .document(dateId)
                .set(data, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Daily steps saved: $dateId = $steps")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save daily steps: ${e.message}")
        }
    }

    /**
     * 날짜별 걸음 수 조회
     */
    suspend fun getDailySteps(dateId: String): Int {
        // 먼저 로컬 확인 (오늘 날짜인 경우)
        if (dateId == getCurrentDate()) {
            return _todaySteps.value
        }

        // Firebase에서 조회
        val userId = auth.currentUser?.uid ?: return 0

        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("dailySteps")
                .document(dateId)
                .get()
                .await()

            doc.getLong("steps")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get daily steps: ${e.message}")
            0
        }
    }

    /**
     * Getter 함수들
     */
    fun getGoal(): Int = preferenceManager.getGoal()
    fun getDeposit(): Int = preferenceManager.getDeposit()
    fun getControlStartDate(): String = preferenceManager.getControlStartDate()
    fun getControlEndDate(): String = preferenceManager.getControlEndDate()
    fun getControlDays(): Set<Int> = preferenceManager.getControlDays()
    fun getSuccessDays(): Int = preferenceManager.getSuccessDays()
    fun isPaidDeposit(): Boolean = preferenceManager.isPaidDeposit()
    fun getTodaySteps(): Int = preferenceManager.getTodaySteps()
    fun getYesterdaySteps(): Int = preferenceManager.getYesterdaySteps()
    fun saveYesterdaySteps(steps: Int) = preferenceManager.saveYesterdaySteps(steps)
    fun getLastStepResetDate(): String = preferenceManager.getLastStepResetDate()
    fun saveLastStepResetDate(date: String) = preferenceManager.saveLastStepResetDate(date)

    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}

/**
 * 사용자 설정 데이터 클래스
 */
data class UserSettings(
    val goal: Int,
    val deposit: Int,
    val controlStartDate: String,
    val controlEndDate: String,
    val controlDays: Set<Int>,
    val successDays: Int,
    val paidDeposit: Boolean
)
