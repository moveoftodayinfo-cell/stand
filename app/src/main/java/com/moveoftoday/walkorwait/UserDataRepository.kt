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
    private val auth: FirebaseAuth,
    autoSync: Boolean = true  // 자동 동기화 여부
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

    // Firebase 동기화 완료 상태
    private val _syncCompleted = MutableStateFlow(false)
    val syncCompleted: StateFlow<Boolean> = _syncCompleted

    init {
        // 로컬 데이터 먼저 로드
        loadLocalData()

        // autoSync가 true면 자동으로 동기화 시작
        if (autoSync) {
            repositoryScope.launch {
                syncWithFirebase()
                _syncCompleted.value = true
                Log.d(TAG, "✅ Firebase sync completed, tutorialCompleted: ${_userSettings.value?.tutorialCompleted}")
            }
        }
    }

    /**
     * 외부에서 동기화 시작 (인증 완료 후 호출)
     */
    fun startSync() {
        // 동기화 시작 전 플래그 리셋 (새 동기화 대기 가능하도록)
        _syncCompleted.value = false
        Log.d(TAG, "🔄 startSync called - syncCompleted reset to false")

        repositoryScope.launch {
            syncWithFirebase()
            _syncCompleted.value = true
            Log.d(TAG, "✅ Firebase sync completed, tutorialCompleted: ${_userSettings.value?.tutorialCompleted}")
        }
    }

    /**
     * 동기화 완료 표시 (인증 실패 등의 경우)
     */
    fun markSyncCompleted() {
        _syncCompleted.value = true
        Log.d(TAG, "⚠️ Sync marked as completed (auth failed or skipped)")
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
            totalDays = preferenceManager.getTotalControlDays(),
            paidDeposit = preferenceManager.isPaidDeposit(),
            // 앱 재설치 시 복원 필요한 데이터
            lockedApps = preferenceManager.getLockedApps(),
            tutorialCompleted = preferenceManager.isTutorialCompleted(),
            blockingPeriods = preferenceManager.getBlockingPeriods(),
            petType = preferenceManager.getPetType() ?: "DOG1",
            petName = preferenceManager.getPetName() ?: "멍이",
            // 프로모션 정보
            usedPromoCode = preferenceManager.getAppliedPromoCode(),
            promoCodeType = preferenceManager.getPromoCodeType(),
            promoHostId = preferenceManager.getPromoHostId(),
            promoFreeEndDate = preferenceManager.getPromoFreeEndDate(),
            // 연속 달성 및 펫 관련 데이터
            streak = preferenceManager.getStreak(),
            lastAchievedDate = preferenceManager.getLastAchievedDate(),
            consecutiveDays = preferenceManager.getConsecutiveDays(),
            petHappiness = preferenceManager.getPetHappiness(),
            petTotalSteps = preferenceManager.getPetTotalSteps()
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

            // Firebase에서 데이터 가져오기 (10초 타임아웃)
            val doc = kotlinx.coroutines.withTimeout(10000) {
                firestore.collection("users")
                    .document(userId)
                    .collection("userData")
                    .document("settings")
                    .get()
                    .await()
            }

            if (doc.exists()) {
                // Firebase 데이터가 있으면 로컬과 비교
                val remoteSettings = UserSettings(
                    goal = doc.getLong("goal")?.toInt() ?: 8000,
                    deposit = doc.getLong("deposit")?.toInt() ?: 0,
                    controlStartDate = doc.getString("controlStartDate") ?: "",
                    controlEndDate = doc.getString("controlEndDate") ?: "",
                    controlDays = (doc.get("controlDays") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() }?.toSet()?.ifEmpty { setOf(1, 2, 3, 4, 5) } ?: setOf(1, 2, 3, 4, 5),
                    successDays = doc.getLong("successDays")?.toInt() ?: 0,
                    totalDays = doc.getLong("totalDays")?.toInt() ?: 0,
                    paidDeposit = doc.getBoolean("paidDeposit") ?: false,
                    // 앱 재설치 시 복원 필요한 데이터
                    lockedApps = (doc.get("lockedApps") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet(),
                    tutorialCompleted = doc.getBoolean("tutorialCompleted") ?: false,
                    blockingPeriods = (doc.get("blockingPeriods") as? List<*>)?.mapNotNull { it as? String }?.toSet()
                        ?: setOf("morning", "afternoon", "evening", "night"),
                    petType = doc.getString("petType") ?: "DOG1",
                    petName = doc.getString("petName") ?: "멍이",
                    // 프로모션 정보
                    usedPromoCode = doc.getString("usedPromoCode"),
                    promoCodeType = doc.getString("promoCodeType"),
                    promoHostId = doc.getString("promoHostId"),
                    promoFreeEndDate = doc.getString("promoFreeEndDate"),
                    // 연속 달성 및 펫 관련 데이터
                    streak = doc.getLong("streak")?.toInt() ?: 0,
                    lastAchievedDate = doc.getString("lastAchievedDate") ?: "",
                    consecutiveDays = doc.getLong("consecutiveDays")?.toInt() ?: 0,
                    petHappiness = doc.getLong("petHappiness")?.toInt() ?: 50,
                    petTotalSteps = doc.getLong("petTotalSteps") ?: 0L
                )

                val remoteTimestamp = doc.getLong("lastSyncTimestamp") ?: 0L
                val localTimestamp = preferenceManager.getLastSyncTimestamp()

                Log.d(TAG, "🔍 Timestamp comparison - remote: $remoteTimestamp, local: $localTimestamp")
                Log.d(TAG, "🔍 Remote data - tutorialCompleted: ${remoteSettings.tutorialCompleted}, petType: ${remoteSettings.petType}")
                Log.d(TAG, "🔍 Local data - tutorialCompleted: ${preferenceManager.isTutorialCompleted()}, petType: ${preferenceManager.getPetType()}")

                // 로컬이 빈 데이터(튜토리얼 미완료)이고 Firebase에 완료된 데이터가 있으면 무조건 복원
                val localTutorialCompleted = preferenceManager.isTutorialCompleted()
                if (!localTutorialCompleted && remoteSettings.tutorialCompleted) {
                    Log.d(TAG, "⬇️ Local is empty but Firebase has completed data - RESTORING")
                    updateLocalSettings(remoteSettings, remoteTimestamp)
                }
                // Firebase 데이터가 더 최신이면 로컬 업데이트
                else if (remoteTimestamp > localTimestamp) {
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

        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "⏰ Firebase sync timed out after 10s")
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
        // 앱 재설치 시 복원 필요한 데이터
        preferenceManager.saveLockedApps(settings.lockedApps)
        preferenceManager.setTutorialCompleted(settings.tutorialCompleted)
        preferenceManager.saveBlockingPeriods(settings.blockingPeriods)
        preferenceManager.savePetType(settings.petType)
        preferenceManager.savePetName(settings.petName)
        // 프로모션 정보 복원
        settings.usedPromoCode?.let { preferenceManager.saveUsedPromoCode(it) }
        settings.promoCodeType?.let { preferenceManager.savePromoCodeType(it) }
        settings.promoHostId?.let { preferenceManager.savePromoHostId(it) }
        settings.promoFreeEndDate?.let { preferenceManager.savePromoFreeEndDate(it) }
        // 연속 달성 및 펫 관련 데이터 복원
        preferenceManager.setStreak(settings.streak)
        preferenceManager.setLastAchievedDate(settings.lastAchievedDate)
        preferenceManager.setConsecutiveDays(settings.consecutiveDays)
        preferenceManager.savePetHappiness(settings.petHappiness)
        preferenceManager.savePetTotalSteps(settings.petTotalSteps)
        preferenceManager.saveLastSyncTimestamp(timestamp)

        _userSettings.value = settings
        Log.d(TAG, "✅ Local settings updated from Firebase (lockedApps: ${settings.lockedApps.size}, tutorial: ${settings.tutorialCompleted}, streak: ${settings.streak})")
    }

    /**
     * 로컬 데이터를 Firebase에 업로드
     */
    private suspend fun uploadLocalToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val settings = _userSettings.value ?: return

        try {
            // 10초 타임아웃 설정
            kotlinx.coroutines.withTimeout(10000) {
                val timestamp = System.currentTimeMillis()
                val data = hashMapOf(
                "goal" to settings.goal,
                "deposit" to settings.deposit,
                "controlStartDate" to settings.controlStartDate,
                "controlEndDate" to settings.controlEndDate,
                "controlDays" to settings.controlDays.toList(),
                "successDays" to settings.successDays,
                "totalDays" to settings.totalDays,
                "paidDeposit" to settings.paidDeposit,
                // 앱 재설치 시 복원 필요한 데이터
                "lockedApps" to settings.lockedApps.toList(),
                "tutorialCompleted" to settings.tutorialCompleted,
                "blockingPeriods" to settings.blockingPeriods.toList(),
                "petType" to settings.petType,
                "petName" to settings.petName,
                // 프로모션 정보
                "usedPromoCode" to settings.usedPromoCode,
                "promoCodeType" to settings.promoCodeType,
                "promoHostId" to settings.promoHostId,
                "promoFreeEndDate" to settings.promoFreeEndDate,
                // 연속 달성 및 펫 관련 데이터
                "streak" to settings.streak,
                "lastAchievedDate" to settings.lastAchievedDate,
                "consecutiveDays" to settings.consecutiveDays,
                "petHappiness" to settings.petHappiness,
                "petTotalSteps" to settings.petTotalSteps,
                "lastActiveAt" to System.currentTimeMillis(),  // 이탈 추적용
                "lastSyncTimestamp" to timestamp
            )

                // 부모 문서 (users/{userId}) 생성 - 대시보드 조회용
                val userDocData = hashMapOf(
                    "email" to (auth.currentUser?.email ?: ""),
                    "lastUpdated" to timestamp,
                    "lastActiveAt" to System.currentTimeMillis(),  // 이탈 추적용
                    "tutorialCompleted" to settings.tutorialCompleted,
                    "paidDeposit" to settings.paidDeposit,
                    "promoCodeType" to settings.promoCodeType
                )
                firestore.collection("users")
                    .document(userId)
                    .set(userDocData, SetOptions.merge())
                    .await()

                // 서브컬렉션 (users/{userId}/userData/settings) 저장
                firestore.collection("users")
                    .document(userId)
                    .collection("userData")
                    .document("settings")
                    .set(data, SetOptions.merge())
                    .await()

                preferenceManager.saveLastSyncTimestamp(timestamp)
                Log.d(TAG, "✅ Local data uploaded to Firebase (parent doc + settings)")
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "⏰ Firebase upload timed out after 10s")
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
     * 펫 교체 결제 추적 (대시보드용)
     */
    fun trackPetChangePurchase(petType: String, petName: String) {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""
        val now = System.currentTimeMillis()

        // 사용자 문서 업데이트
        val userDoc = hashMapOf(
            "email" to userEmail,
            "lastActiveAt" to now,
            "lastUpdated" to now,
            "petChangePurchased" to true,
            "lastPetChangeAt" to now,
            "petChangeCount" to com.google.firebase.firestore.FieldValue.increment(1)
        )
        firestore.collection("users")
            .document(userId)
            .set(userDoc, SetOptions.merge())

        // settings 서브컬렉션 업데이트 (paidDeposit은 건드리지 않음 - 구독 결제와 별개)
        val settingsDoc = hashMapOf(
            "lastActiveAt" to now,
            "petType" to petType,
            "petName" to petName,
            "petChangePurchased" to true,
            "lastPetChangePurchaseAt" to now
        )
        firestore.collection("users")
            .document(userId)
            .collection("userData")
            .document("settings")
            .set(settingsDoc, SetOptions.merge())

        // 펫 교체 이력 저장 (사용자별 서브컬렉션)
        val petChangeHistory = hashMapOf(
            "petType" to petType,
            "petName" to petName,
            "purchasedAt" to now,
            "email" to userEmail
        )
        firestore.collection("users")
            .document(userId)
            .collection("petChanges")
            .add(petChangeHistory)

        // 전체 펫 교체 이력 (대시보드 조회용 - 최상위 컬렉션)
        val globalPetChangeHistory = hashMapOf(
            "userId" to userId,
            "email" to userEmail,
            "petType" to petType,
            "petName" to petName,
            "purchasedAt" to now
        )
        firestore.collection("petChangeHistory")
            .add(globalPetChangeHistory)
            .addOnSuccessListener {
                Log.d(TAG, "Pet change history saved: $userId -> $petType")
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
     * 잠금 앱 목록 저장 (Firebase 동기화 포함)
     */
    fun saveLockedApps(apps: Set<String>) {
        preferenceManager.saveLockedApps(apps)
        _userSettings.value = _userSettings.value?.copy(lockedApps = apps)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
        Log.d(TAG, "🔒 Locked apps saved and synced: ${apps.size} apps")
    }

    /**
     * 튜토리얼 완료 상태 저장 (Firebase 동기화 포함)
     */
    fun setTutorialCompleted(completed: Boolean) {
        preferenceManager.setTutorialCompleted(completed)
        _userSettings.value = _userSettings.value?.copy(tutorialCompleted = completed)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
        Log.d(TAG, "🎓 Tutorial completed saved and synced: $completed")
    }

    /**
     * 차단 시간대 저장 (Firebase 동기화 포함)
     */
    fun saveBlockingPeriods(periods: Set<String>) {
        preferenceManager.saveBlockingPeriods(periods)
        _userSettings.value = _userSettings.value?.copy(blockingPeriods = periods)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
        Log.d(TAG, "⏰ Blocking periods saved and synced: $periods")
    }

    /**
     * 펫 정보 저장 (Firebase 동기화 포함)
     */
    fun savePetInfo(petType: String, petName: String) {
        preferenceManager.savePetType(petType)
        preferenceManager.savePetName(petName)
        _userSettings.value = _userSettings.value?.copy(petType = petType, petName = petName)

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
        Log.d(TAG, "🐾 Pet info saved and synced: $petType, $petName")
    }

    /**
     * 튜토리얼 완료 시 모든 데이터를 한 번에 저장 (Firebase 동기화 포함)
     * - race condition 방지를 위해 단일 업로드
     */
    fun saveTutorialCompletionData(
        lockedApps: Set<String>,
        blockingPeriods: Set<String>,
        controlDays: Set<Int>,
        goal: Int,
        deposit: Int,
        controlStartDate: String,
        controlEndDate: String,
        petType: String,
        petName: String
    ) {
        Log.d(TAG, "📦 Saving tutorial completion data...")

        // 프로모션 코드 사용자인지 확인 (프로모션 사용자는 결제자가 아님)
        val hasPromoCode = !preferenceManager.getPromoCodeType().isNullOrEmpty()
        val isPaidUser = !hasPromoCode  // 프로모션 코드 없으면 결제자

        // 로컬에 모든 데이터 저장
        preferenceManager.setTutorialCompleted(true)
        preferenceManager.setPaidDeposit(isPaidUser)
        preferenceManager.saveLockedApps(lockedApps)
        preferenceManager.saveBlockingPeriods(blockingPeriods)
        preferenceManager.saveControlDays(controlDays)
        preferenceManager.saveGoal(goal)
        preferenceManager.saveDeposit(deposit)
        preferenceManager.saveControlStartDate(controlStartDate)
        preferenceManager.saveControlEndDate(controlEndDate)
        preferenceManager.savePetType(petType)
        preferenceManager.savePetName(petName)

        // _userSettings 한 번에 업데이트
        _userSettings.value = UserSettings(
            goal = goal,
            deposit = deposit,
            controlStartDate = controlStartDate,
            controlEndDate = controlEndDate,
            controlDays = controlDays,
            successDays = preferenceManager.getSuccessDays(),
            paidDeposit = isPaidUser,
            lockedApps = lockedApps,
            tutorialCompleted = true,
            blockingPeriods = blockingPeriods,
            petType = petType,
            petName = petName,
            usedPromoCode = preferenceManager.getAppliedPromoCode(),
            promoCodeType = preferenceManager.getPromoCodeType(),
            promoHostId = preferenceManager.getPromoHostId(),
            promoFreeEndDate = preferenceManager.getPromoFreeEndDate(),
            // 연속 달성 및 펫 관련 데이터
            streak = preferenceManager.getStreak(),
            lastAchievedDate = preferenceManager.getLastAchievedDate(),
            consecutiveDays = preferenceManager.getConsecutiveDays(),
            petHappiness = preferenceManager.getPetHappiness(),
            petTotalSteps = preferenceManager.getPetTotalSteps()
        )

        // 한 번만 Firebase에 업로드
        repositoryScope.launch {
            try {
                uploadLocalToFirebase()
                Log.d(TAG, "✅ Tutorial completion data synced to Firebase successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to sync tutorial completion data: ${e.message}")
            }
        }
    }

    /**
     * 프로모션 정보 저장 (Firebase 동기화 포함)
     */
    fun savePromoInfo(code: String?, type: String?, hostId: String?, endDate: String?) {
        code?.let { preferenceManager.saveUsedPromoCode(it) }
        type?.let { preferenceManager.savePromoCodeType(it) }
        hostId?.let { preferenceManager.savePromoHostId(it) }
        endDate?.let { preferenceManager.savePromoFreeEndDate(it) }
        _userSettings.value = _userSettings.value?.copy(
            usedPromoCode = code,
            promoCodeType = type,
            promoHostId = hostId,
            promoFreeEndDate = endDate
        )

        repositoryScope.launch {
            uploadLocalToFirebase()
        }
        Log.d(TAG, "🎟️ Promo info saved and synced: $type, endDate: $endDate")
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
    fun getLockedApps(): Set<String> = preferenceManager.getLockedApps()
    fun isTutorialCompleted(): Boolean = preferenceManager.isTutorialCompleted()
    fun getBlockingPeriods(): Set<String> = preferenceManager.getBlockingPeriods()

    /**
     * 공유 이벤트 기록 (Core 유저 추적용)
     */
    fun trackShareEvent() {
        val userId = auth.currentUser?.uid ?: return
        val today = getCurrentDate()
        val now = System.currentTimeMillis()

        repositoryScope.launch {
            try {
                // settings에 lastShareAt 업데이트
                firestore.collection("users")
                    .document(userId)
                    .collection("userData")
                    .document("settings")
                    .update(
                        mapOf(
                            "lastShareAt" to now,
                            "lastShareDate" to today
                        )
                    )
                    .await()

                // 사용자 문서에도 업데이트
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "lastShareAt" to now,
                            "lastShareDate" to today
                        )
                    )
                    .await()

                Log.d(TAG, "📤 Share event tracked: $today")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track share event: ${e.message}")
            }
        }
    }

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
    val totalDays: Int = 0,  // 총 제어 일수 (대시보드용)
    val paidDeposit: Boolean,
    // 앱 재설치 시 복원 필요한 데이터
    val lockedApps: Set<String> = emptySet(),
    val tutorialCompleted: Boolean = false,
    val blockingPeriods: Set<String> = setOf("morning", "afternoon", "evening", "night"),
    val petType: String = "DOG1",
    val petName: String = "멍이",
    // 프로모션 정보
    val usedPromoCode: String? = null,
    val promoCodeType: String? = null,
    val promoHostId: String? = null,
    val promoFreeEndDate: String? = null,
    // 연속 달성 및 펫 관련 데이터
    val streak: Int = 0,
    val lastAchievedDate: String = "",
    val consecutiveDays: Int = 0,
    val petHappiness: Int = 50,
    val petTotalSteps: Long = 0L,
    // 이탈 추적용
    val lastActiveAt: Long = System.currentTimeMillis()
)
