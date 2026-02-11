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
    private val context: Context,
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
            streakStartDate = preferenceManager.getStreakStartDate(),
            consecutiveDays = preferenceManager.getConsecutiveDays(),
            petHappiness = preferenceManager.getPetHappiness(),
            petTotalSteps = preferenceManager.getPetTotalSteps()
        )
        _todaySteps.value = preferenceManager.getTodaySteps()
        Log.d(TAG, "📂 Local data loaded")
    }

    /**
     * Firebase와 동기화 (통합 구조: users/{userId} 부모 문서만 사용)
     */
    suspend fun syncWithFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "⚠️ No user signed in, skipping Firebase sync")
            return
        }

        try {
            Log.d(TAG, "🔄 Syncing with Firebase (unified structure)...")

            // 부모 문서에서 모든 데이터 가져오기 (10초 타임아웃)
            val userDoc = kotlinx.coroutines.withTimeout(10000) {
                firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
            }

            // 구버전 settings 문서 확인 (마이그레이션 필요 여부)
            val oldSettingsDoc = kotlinx.coroutines.withTimeout(5000) {
                firestore.collection("users")
                    .document(userId)
                    .collection("userData")
                    .document("settings")
                    .get()
                    .await()
            }
            val needsMigration = oldSettingsDoc.exists()
            Log.d(TAG, "🔍 Migration needed: $needsMigration")

            if (userDoc.exists() || needsMigration) {
                // 부모 문서 또는 구버전 settings에서 데이터 읽기
                Log.d(TAG, "📄 User doc exists: ${userDoc.exists()}, old settings exists: $needsMigration")

                // 부모 문서에서 읽기 (우선)
                val docToRead = if (userDoc.exists() && userDoc.getLong("lastSyncTimestamp") != null) userDoc else oldSettingsDoc
                Log.d(TAG, "📄 Reading from: ${if (docToRead == userDoc) "parent doc" else "old settings"}")
                Log.d(TAG, "📄 Doc data: ${docToRead.data}")

                // 데이터 읽기 - 부모 문서와 구버전 settings 병합
                val petName = userDoc.getString("petName")
                    ?: oldSettingsDoc.getString("petName")
                    ?: "멍이"
                val petType = userDoc.getString("petType")?.takeIf { it.isNotBlank() && it != "DOG1" }
                    ?: oldSettingsDoc.getString("petType")?.takeIf { it.isNotBlank() }
                    ?: "DOG1"
                val tutorialCompleted = (userDoc.getBoolean("tutorialCompleted") ?: false) ||
                    (oldSettingsDoc.getBoolean("tutorialCompleted") ?: false)
                val paidDeposit = (userDoc.getBoolean("paidDeposit") ?: false) ||
                    (oldSettingsDoc.getBoolean("paidDeposit") ?: false)
                val lockedApps = ((userDoc.get("lockedApps") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()) +
                    ((oldSettingsDoc.get("lockedApps") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet())

                // 칭호 데이터 (배열로 통합) - 부모 문서, 구버전, 서브컬렉션 순으로 확인
                var unlockedTitles = (userDoc.get("unlockedTitles") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                if (unlockedTitles.isEmpty()) {
                    unlockedTitles = (oldSettingsDoc.get("unlockedTitles") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                }
                if (unlockedTitles.isEmpty()) {
                    unlockedTitles = fetchUnlockedTitlesFromSubcollection(userId)
                }
                val equippedTitle = userDoc.getString("equippedTitle") ?: oldSettingsDoc.getString("equippedTitle")
                val totalChallenges = userDoc.getLong("totalChallengesCompleted")?.toInt()
                    ?: oldSettingsDoc.getLong("totalChallengesCompleted")?.toInt()

                // V2 펫 데이터 읽기
                val petTypeV2Name = userDoc.getString("pet_type_v2") ?: oldSettingsDoc.getString("pet_type_v2")
                val petNameV2 = userDoc.getString("pet_name_v2") ?: oldSettingsDoc.getString("pet_name_v2")
                val petLevelV2 = userDoc.getLong("pet_level_v2")?.toInt() ?: oldSettingsDoc.getLong("pet_level_v2")?.toInt()
                val petLevelExpV2 = userDoc.getLong("pet_level_exp_v2")?.toInt() ?: oldSettingsDoc.getLong("pet_level_exp_v2")?.toInt()
                val petHappinessV2 = userDoc.getLong("pet_happiness_v2")?.toInt() ?: oldSettingsDoc.getLong("pet_happiness_v2")?.toInt()

                Log.d(TAG, "🐾 Pet: $petType, $petName")
                Log.d(TAG, "🐾 Pet V2: $petTypeV2Name, $petNameV2")
                Log.d(TAG, "🔍 tutorialCompleted: $tutorialCompleted, paidDeposit: $paidDeposit, lockedApps: ${lockedApps.size}")
                Log.d(TAG, "🏆 Titles: ${unlockedTitles.size}, equipped: $equippedTitle")

                val remoteSettings = UserSettings(
                    goal = userDoc.getLong("goal")?.toInt()
                        ?: oldSettingsDoc.getLong("goal")?.toInt()
                        ?: 8000,
                    deposit = userDoc.getLong("deposit")?.toInt()
                        ?: oldSettingsDoc.getLong("deposit")?.toInt()
                        ?: 0,
                    controlStartDate = userDoc.getString("controlStartDate")
                        ?: oldSettingsDoc.getString("controlStartDate")
                        ?: "",
                    controlEndDate = userDoc.getString("controlEndDate")
                        ?: oldSettingsDoc.getString("controlEndDate")
                        ?: "",
                    controlDays = (userDoc.get("controlDays") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() }?.toSet()?.ifEmpty { null }
                        ?: (oldSettingsDoc.get("controlDays") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() }?.toSet()?.ifEmpty { null }
                        ?: setOf(1, 2, 3, 4, 5),
                    successDays = userDoc.getLong("successDays")?.toInt()
                        ?: oldSettingsDoc.getLong("successDays")?.toInt()
                        ?: 0,
                    totalDays = userDoc.getLong("totalDays")?.toInt()
                        ?: oldSettingsDoc.getLong("totalDays")?.toInt()
                        ?: 0,
                    paidDeposit = paidDeposit,
                    lockedApps = lockedApps,
                    tutorialCompleted = tutorialCompleted,
                    blockingPeriods = (userDoc.get("blockingPeriods") as? List<*>)?.mapNotNull { it as? String }?.toSet()?.ifEmpty { null }
                        ?: (oldSettingsDoc.get("blockingPeriods") as? List<*>)?.mapNotNull { it as? String }?.toSet()?.ifEmpty { null }
                        ?: setOf("morning", "afternoon", "evening", "night"),
                    petType = petType,
                    petName = petName,
                    // 프로모션 정보
                    usedPromoCode = userDoc.getString("usedPromoCode") ?: oldSettingsDoc.getString("usedPromoCode"),
                    promoCodeType = userDoc.getString("promoCodeType") ?: oldSettingsDoc.getString("promoCodeType"),
                    promoHostId = userDoc.getString("promoHostId") ?: oldSettingsDoc.getString("promoHostId"),
                    promoFreeEndDate = userDoc.getString("promoFreeEndDate") ?: oldSettingsDoc.getString("promoFreeEndDate"),
                    // 연속 달성 및 펫 관련 데이터
                    streak = userDoc.getLong("streak")?.toInt()
                        ?: oldSettingsDoc.getLong("streak")?.toInt()
                        ?: 0,
                    lastAchievedDate = userDoc.getString("lastAchievedDate")
                        ?: oldSettingsDoc.getString("lastAchievedDate")
                        ?: "",
                    streakStartDate = userDoc.getString("streakStartDate")
                        ?: oldSettingsDoc.getString("streakStartDate")
                        ?: "",
                    consecutiveDays = userDoc.getLong("consecutiveDays")?.toInt()
                        ?: oldSettingsDoc.getLong("consecutiveDays")?.toInt()
                        ?: 0,
                    petHappiness = userDoc.getLong("petHappiness")?.toInt()
                        ?: oldSettingsDoc.getLong("petHappiness")?.toInt()
                        ?: 50,
                    petTotalSteps = userDoc.getLong("petTotalSteps")
                        ?: oldSettingsDoc.getLong("petTotalSteps")
                        ?: 0L,
                    // 칭호 데이터 (통합)
                    unlockedTitles = unlockedTitles,
                    equippedTitle = equippedTitle,
                    totalChallengesCompleted = totalChallenges ?: 0
                )

                val remoteTimestamp = userDoc.getLong("lastSyncTimestamp")
                    ?: oldSettingsDoc.getLong("lastSyncTimestamp")
                    ?: 0L
                val localTimestamp = preferenceManager.getLastSyncTimestamp()

                Log.d(TAG, "🔍 Timestamp comparison - remote: $remoteTimestamp, local: $localTimestamp")

                // 로컬이 완전히 빈 데이터(신규 설치)인지 확인
                val localTutorialCompleted = preferenceManager.isTutorialCompleted()
                val localHasData = preferenceManager.getPetType() != null || preferenceManager.getLockedApps().isNotEmpty()

                // 로컬이 빈 데이터이면 Firebase에서 복원
                if (!localTutorialCompleted && !localHasData) {
                    Log.d(TAG, "⬇️ Local is empty, restoring from Firebase")

                    // 유효한 데이터가 있으면 tutorialCompleted = true로 처리
                    var finalSettings = remoteSettings
                    if (remoteSettings.lockedApps.isNotEmpty() || remoteSettings.streak > 0 || remoteSettings.petTotalSteps > 0 || remoteSettings.paidDeposit) {
                        finalSettings = remoteSettings.copy(tutorialCompleted = true)
                    }

                    updateLocalSettings(finalSettings, remoteTimestamp)

                    // V2 펫 데이터 복원
                    restorePetV2Data(petTypeV2Name, petNameV2, petLevelV2, petLevelExpV2, petHappinessV2)

                    // 칭호 데이터 복원
                    if (unlockedTitles.isNotEmpty() || equippedTitle != null || totalChallenges != null) {
                        restoreTitleData(unlockedTitles, equippedTitle, totalChallenges)
                    }
                }
                // Firebase 데이터가 더 최신이면 로컬 업데이트
                else if (remoteTimestamp > localTimestamp) {
                    Log.d(TAG, "⬇️ Firebase data is newer, updating local")
                    updateLocalSettings(remoteSettings, remoteTimestamp)

                    // V2 펫 데이터 복원
                    restorePetV2Data(petTypeV2Name, petNameV2, petLevelV2, petLevelExpV2, petHappinessV2)

                    // 칭호 데이터도 복원
                    if (unlockedTitles.isNotEmpty() || equippedTitle != null || totalChallenges != null) {
                        restoreTitleData(unlockedTitles, equippedTitle, totalChallenges)
                    }
                } else {
                    Log.d(TAG, "⬆️ Local data is newer, uploading to Firebase")

                    // 로컬에는 없지만 Firebase에 칭호 데이터가 있는 경우 복원
                    val localHasTitles = context.getSharedPreferences("challenge_prefs", android.content.Context.MODE_PRIVATE)
                        .getStringSet("unlocked_titles", emptySet())?.isNotEmpty() == true

                    if (!localHasTitles && unlockedTitles.isNotEmpty()) {
                        Log.d(TAG, "⚠️ Local has no titles but Firebase has ${unlockedTitles.size} titles - restoring!")
                        restoreTitleData(unlockedTitles, equippedTitle, totalChallenges)
                    }

                    uploadLocalToFirebase()
                }

                // 마이그레이션: 구버전 데이터를 부모 문서로 이전 후 삭제
                if (needsMigration) {
                    migrateOldSettingsToParent(userId)
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
     * 구버전 settings 데이터를 부모 문서로 마이그레이션
     */
    private suspend fun migrateOldSettingsToParent(userId: String) {
        try {
            Log.d(TAG, "🚚 Starting migration from userData/settings to parent doc...")

            // 1. 현재 부모 문서에 데이터가 제대로 있는지 확인
            val parentDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val hasValidParentData = parentDoc.getLong("lastSyncTimestamp") != null &&
                (parentDoc.getBoolean("tutorialCompleted") == true ||
                 parentDoc.get("lockedApps") != null ||
                 parentDoc.getLong("petTotalSteps") != null)

            if (!hasValidParentData) {
                Log.d(TAG, "⚠️ Parent doc doesn't have valid data yet, skipping delete")
                return
            }

            // 2. 구버전 칭호 서브컬렉션도 마이그레이션
            val titlesSubcollection = firestore.collection("users")
                .document(userId)
                .collection("unlockedTitles")
                .get()
                .await()

            if (!titlesSubcollection.isEmpty) {
                val titles = titlesSubcollection.documents.mapNotNull { it.id }
                Log.d(TAG, "🏆 Migrating ${titles.size} titles from subcollection to array")

                // 부모 문서에 배열로 저장
                firestore.collection("users")
                    .document(userId)
                    .update("unlockedTitles", titles)
                    .await()

                // 서브컬렉션 문서들 삭제
                for (titleDoc in titlesSubcollection.documents) {
                    titleDoc.reference.delete().await()
                }
                Log.d(TAG, "🗑️ Deleted ${titlesSubcollection.documents.size} title subcollection docs")
            }

            // 3. 구버전 settings 문서 삭제
            firestore.collection("users")
                .document(userId)
                .collection("userData")
                .document("settings")
                .delete()
                .await()

            Log.d(TAG, "✅ Migration completed! Old settings document deleted")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Migration failed: ${e.message}")
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
        preferenceManager.setStreakStartDate(settings.streakStartDate)
        preferenceManager.setConsecutiveDays(settings.consecutiveDays)
        preferenceManager.savePetHappiness(settings.petHappiness)
        preferenceManager.savePetTotalSteps(settings.petTotalSteps)
        preferenceManager.saveLastSyncTimestamp(timestamp)

        _userSettings.value = settings
        Log.d(TAG, "✅ Local settings updated from Firebase (lockedApps: ${settings.lockedApps.size}, tutorial: ${settings.tutorialCompleted}, streak: ${settings.streak})")
    }

    /**
     * V2 펫 데이터 복원
     */
    private fun restorePetV2Data(
        petTypeV2Name: String?,
        petNameV2: String?,
        petLevelV2: Int?,
        petLevelExpV2: Int?,
        petHappinessV2: Int?
    ) {
        if (petTypeV2Name != null) {
            try {
                val petTypeV2 = com.moveoftoday.walkorwait.pet.PetTypeV2.valueOf(petTypeV2Name)
                preferenceManager.savePetTypeV2(petTypeV2)
                Log.d(TAG, "🐾 Restored V2 pet type: $petTypeV2Name")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse V2 pet type: $petTypeV2Name")
            }
        }

        if (!petNameV2.isNullOrBlank()) {
            preferenceManager.savePetNameV2(petNameV2)
            Log.d(TAG, "🐾 Restored V2 pet name: $petNameV2")
        }

        if (petLevelV2 != null && petLevelExpV2 != null) {
            val level = com.moveoftoday.walkorwait.pet.PetLevel(
                level = petLevelV2,
                currentExp = petLevelExpV2,
                totalExp = 0  // totalExp는 재계산됨
            )
            preferenceManager.savePetLevelV2(level)
            Log.d(TAG, "🐾 Restored V2 pet level: $petLevelV2 (exp: $petLevelExpV2)")
        }

        if (petHappinessV2 != null) {
            preferenceManager.savePetHappinessV2(petHappinessV2)
            Log.d(TAG, "🐾 Restored V2 pet happiness: $petHappinessV2")
        }
    }

    /**
     * 칭호 데이터 복원 (ChallengeManager용 SharedPreferences에 저장)
     * 복원 후 ChallengeManager 리로드
     */
    private fun restoreTitleData(unlockedTitles: Set<String>, equippedTitle: String?, totalChallengesCompleted: Int? = null) {
        val prefs = context.getSharedPreferences("challenge_prefs", android.content.Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (unlockedTitles.isNotEmpty()) {
            editor.putStringSet("unlocked_titles", unlockedTitles)
            Log.d(TAG, "🏆 Restored unlocked titles: $unlockedTitles")
        }
        if (equippedTitle != null) {
            editor.putString("equipped_title", equippedTitle)
            Log.d(TAG, "🏆 Restored equipped title: $equippedTitle")
        }
        if (totalChallengesCompleted != null && totalChallengesCompleted > 0) {
            editor.putInt("total_challenges_completed", totalChallengesCompleted)
            Log.d(TAG, "🏆 Restored total challenges completed: $totalChallengesCompleted")
        }
        editor.apply()

        // ChallengeManager가 이미 초기화되어 있으면 다시 로드
        try {
            val challengeManager = ChallengeManager.getInstance(context)
            challengeManager.reloadFromPreferences()
            Log.d(TAG, "🔄 ChallengeManager reloaded after title restoration")

            // 오늘의 챌린지 통계도 Firebase에서 로드
            repositoryScope.launch {
                challengeManager.loadTodayStatsFromFirebase()
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to reload ChallengeManager: ${e.message}")
        }
    }

    /**
     * unlockedTitles 서브컬렉션에서 칭호 목록 조회
     * 부모 문서에 칭호 목록이 없을 때 폴백으로 사용
     */
    private suspend fun fetchUnlockedTitlesFromSubcollection(userId: String): Set<String> {
        return try {
            val docs = kotlinx.coroutines.withTimeout(5000) {
                firestore.collection("users")
                    .document(userId)
                    .collection("unlockedTitles")
                    .get()
                    .await()
            }
            val titles = docs.documents.mapNotNull { it.id }.toSet()
            Log.d(TAG, "🏆 Fetched ${titles.size} titles from subcollection: $titles")
            titles
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to fetch titles from subcollection: ${e.message}")
            emptySet()
        }
    }

    /**
     * 로컬 데이터를 Firebase에 업로드 (통합 구조: 부모 문서에만 저장)
     */
    private suspend fun uploadLocalToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val settings = _userSettings.value ?: return

        try {
            // 10초 타임아웃 설정
            kotlinx.coroutines.withTimeout(10000) {
                val timestamp = System.currentTimeMillis()

                // 기존 문서에서 createdAt 확인
                val existingDoc = firestore.collection("users").document(userId).get().await()
                val existingCreatedAt = existingDoc.getLong("createdAt")

                // 로컬 칭호 데이터 가져오기
                val challengePrefs = context.getSharedPreferences("challenge_prefs", android.content.Context.MODE_PRIVATE)
                val localUnlockedTitles = challengePrefs.getStringSet("unlocked_titles", emptySet()) ?: emptySet()
                val localEquippedTitle = challengePrefs.getString("equipped_title", null)
                val localTotalChallenges = challengePrefs.getInt("total_challenges_completed", 0)

                // 부모 문서 (users/{userId})에 모든 데이터 통합 저장
                val data = hashMapOf(
                    // 기본 정보
                    "email" to (auth.currentUser?.email ?: ""),
                    "lastUpdated" to timestamp,
                    "lastActiveAt" to timestamp,
                    "lastSyncTimestamp" to timestamp,
                    // createdAt은 없을 때만 설정 (최초 가입 시간)
                    "createdAt" to (existingCreatedAt ?: timestamp),

                    // 펫 정보 (V1)
                    "petType" to settings.petType,
                    "petName" to settings.petName,
                    "petHappiness" to settings.petHappiness,
                    "petTotalSteps" to settings.petTotalSteps,

                    // 펫 정보 (V2)
                    "pet_type_v2" to preferenceManager.getPetTypeV2()?.name,
                    "pet_name_v2" to preferenceManager.getPetNameV2(),
                    "pet_level_v2" to preferenceManager.getPetLevelV2()?.level,
                    "pet_level_exp_v2" to preferenceManager.getPetLevelV2()?.currentExp,
                    "pet_happiness_v2" to preferenceManager.getPetHappinessV2(),

                    // 진행 상태
                    "tutorialCompleted" to settings.tutorialCompleted,
                    "paidDeposit" to settings.paidDeposit,
                    "streak" to settings.streak,
                    "lastAchievedDate" to settings.lastAchievedDate,
                    "streakStartDate" to settings.streakStartDate,
                    "consecutiveDays" to settings.consecutiveDays,

                    // 설정
                    "goal" to settings.goal,
                    "deposit" to settings.deposit,
                    "controlStartDate" to settings.controlStartDate,
                    "controlEndDate" to settings.controlEndDate,
                    "controlDays" to settings.controlDays.toList(),
                    "successDays" to settings.successDays,
                    "totalDays" to settings.totalDays,
                    "lockedApps" to settings.lockedApps.toList(),
                    "blockingPeriods" to settings.blockingPeriods.toList(),

                    // 프로모션 정보
                    "usedPromoCode" to settings.usedPromoCode,
                    "promoCodeType" to settings.promoCodeType,
                    "promoHostId" to settings.promoHostId,
                    "promoFreeEndDate" to settings.promoFreeEndDate,

                    // 챌린지/칭호 (배열로 통합)
                    "unlockedTitles" to localUnlockedTitles.toList(),
                    "equippedTitle" to localEquippedTitle,
                    "totalChallengesCompleted" to localTotalChallenges
                )

                firestore.collection("users")
                    .document(userId)
                    .set(data, SetOptions.merge())
                    .await()

                preferenceManager.saveLastSyncTimestamp(timestamp)
                Log.d(TAG, "✅ Local data uploaded to Firebase (unified parent doc)")
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "⏰ Firebase upload timed out after 10s")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to upload to Firebase: ${e.message}")
        }
    }

    /**
     * 로컬 데이터를 강제로 Firebase에 업로드 (데이터 충돌 시 로컬 우선)
     */
    fun forceUploadLocalData() {
        Log.d(TAG, "⬆️ Force uploading local data to Firebase...")
        repositoryScope.launch {
            uploadLocalToFirebase()
            _syncCompleted.value = true
            Log.d(TAG, "✅ Force upload completed")
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
     * 펫 교체 결제 추적 (대시보드용) - 부모 문서에 통합 저장
     */
    fun trackPetChangePurchase(petType: String, petName: String) {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""
        val now = System.currentTimeMillis()

        // 부모 문서에 모든 정보 통합 저장
        val userDoc = hashMapOf(
            "email" to userEmail,
            "lastActiveAt" to now,
            "lastUpdated" to now,
            "petType" to petType,
            "petName" to petName,
            "petChangePurchased" to true,
            "lastPetChangeAt" to now,
            "petChangeCount" to com.google.firebase.firestore.FieldValue.increment(1)
        )
        firestore.collection("users")
            .document(userId)
            .set(userDoc, SetOptions.merge())

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
            streakStartDate = preferenceManager.getStreakStartDate(),
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
     * 공유 이벤트 기록 (Core 유저 추적용) - 부모 문서에만 저장
     */
    fun trackShareEvent() {
        val userId = auth.currentUser?.uid ?: return
        val today = getCurrentDate()
        val now = System.currentTimeMillis()

        repositoryScope.launch {
            try {
                // 부모 문서에만 업데이트
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "lastShareAt" to now,
                            "lastShareDate" to today,
                            "lastActiveAt" to now
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
 * 사용자 설정 데이터 클래스 (통합 구조)
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
    val streakStartDate: String = "",  // streak 시작 날짜
    val consecutiveDays: Int = 0,
    val petHappiness: Int = 50,
    val petTotalSteps: Long = 0L,
    // 챌린지/칭호 데이터 (통합)
    val unlockedTitles: Set<String> = emptySet(),
    val equippedTitle: String? = null,
    val totalChallengesCompleted: Int = 0,
    // 이탈 추적용
    val lastActiveAt: Long = System.currentTimeMillis()
)
