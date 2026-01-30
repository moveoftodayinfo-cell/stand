package com.moveoftoday.walkorwait.pet

/**
 * Pet Evolution System - Rebon
 * 새로운 펫 성장 시스템
 */

/**
 * 펫 애니메이션 타입 (새로운 버전)
 */
enum class PetAnimationTypeV2 {
    IDLE,   // 대기 (느리게)
    WALK,   // 걷기 (느리게)
    RUN,    // 뛰기 (보통)
    BARK,   // 짖기/소리 (느리게)
    SNEAK,  // 살금살금 (느리게)
    WOBBLE, // 알 흔들림 (Egg 전용)
    CRACK,  // 알 금가기 (Egg 전용)
    HATCH   // 부화 (Egg 전용)
}

/**
 * 펫 성장 단계
 */
enum class PetGrowthStage(
    val displayName: String,
    val levelRange: IntRange,
    val sizeMultiplier: Float,
    val folderName: String
) {
    EGG(
        displayName = "알",
        levelRange = 0..0,
        sizeMultiplier = 0.8f,
        folderName = "egg"
    ),
    BABY(
        displayName = "아기",
        levelRange = 1..10,
        sizeMultiplier = 1.0f,
        folderName = "baby"
    ),
    TEEN(
        displayName = "성장기",
        levelRange = 11..20,
        sizeMultiplier = 1.2f,
        folderName = "teen"
    ),
    ADULT(
        displayName = "성체",
        levelRange = 21..Int.MAX_VALUE,
        sizeMultiplier = 1.5f,
        folderName = "adult"
    );

    companion object {
        fun fromLevel(level: Int): PetGrowthStage {
            return when {
                level <= 0 -> EGG
                level <= 10 -> BABY
                level <= 20 -> TEEN
                else -> ADULT
            }
        }
    }
}

/**
 * 새로운 펫 타입 (6종)
 */
enum class PetTypeV2(
    val displayName: String,
    val personality: PetPersonalityV2,
    val folderName: String,
    val defaultAnimationFrames: Map<PetAnimationTypeV2, AnimationConfig>
) {
    SHIBA(
        displayName = "멍이",
        personality = PetPersonalityV2.LOYAL,
        folderName = "shiba",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    ),
    CAT(
        displayName = "냥이",
        personality = PetPersonalityV2.TSUNDERE,
        folderName = "cat",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    ),
    PIG(
        displayName = "꿀꿀이",
        personality = PetPersonalityV2.FOODIE,
        folderName = "pig",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    ),
    RACCOON(
        displayName = "라쿤",
        personality = PetPersonalityV2.PLAYFUL,
        folderName = "raccoon",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    ),
    HAMSTER(
        displayName = "햄찌",
        personality = PetPersonalityV2.TIMID,
        folderName = "hamster",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    ),
    PENGUIN(
        displayName = "펭펭",
        personality = PetPersonalityV2.CLUMSY,
        folderName = "penguin",
        defaultAnimationFrames = mapOf(
            PetAnimationTypeV2.IDLE to AnimationConfig(8, 200),
            PetAnimationTypeV2.WALK to AnimationConfig(4, 200),
            PetAnimationTypeV2.RUN to AnimationConfig(6, 100),
            PetAnimationTypeV2.BARK to AnimationConfig(6, 200),
            PetAnimationTypeV2.SNEAK to AnimationConfig(8, 200)
        )
    );

    /**
     * 애니메이션 폴더 경로 생성
     * 예: pets/shiba/baby/idle/
     */
    fun getAnimationFolderPath(stage: PetGrowthStage, animationType: PetAnimationTypeV2): String {
        return if (stage == PetGrowthStage.EGG) {
            "pets/egg/${animationType.name.lowercase()}/"
        } else {
            "pets/$folderName/${stage.folderName}/${animationType.name.lowercase()}/"
        }
    }

    /**
     * 애니메이션 설정 가져오기
     */
    fun getAnimationConfig(animationType: PetAnimationTypeV2): AnimationConfig {
        return defaultAnimationFrames[animationType] ?: AnimationConfig(4, 200)
    }
}

/**
 * 애니메이션 설정
 */
data class AnimationConfig(
    val frameCount: Int,
    val frameDurationMs: Int
)

/**
 * 새로운 펫 성격 (6종)
 */
enum class PetPersonalityV2(val description: String) {
    LOYAL("충성스러운 상남자"),      // 시바 - 쿨하고 듬직
    TSUNDERE("츤데레"),              // 고양이 - 차갑지만 따뜻
    FOODIE("먹보/낙천가"),           // 돼지 - 행복하고 긍정적
    PLAYFUL("장난꾸러기"),           // 너구리 - 호기심 많고 장난스러움
    TIMID("소심/부지런"),            // 햄스터 - 조심스럽고 열심
    CLUMSY("덤벙/순수")              // 펭귄 - 덤벙대지만 귀여움
}

/**
 * 펫 레벨/경험치 시스템
 */
data class PetLevel(
    val level: Int = 1,
    val currentExp: Int = 0,
    val totalExp: Int = 0
) {
    val stage: PetGrowthStage get() = PetGrowthStage.fromLevel(level)

    val expToNextLevel: Int get() = calculateExpForLevel(level + 1)

    val expProgress: Float get() {
        val expForCurrent = calculateExpForLevel(level)
        val expForNext = calculateExpForLevel(level + 1)
        val expInCurrentLevel = totalExp - expForCurrent
        val expNeededForLevel = expForNext - expForCurrent
        return (expInCurrentLevel.toFloat() / expNeededForLevel).coerceIn(0f, 1f)
    }

    companion object {
        /**
         * 레벨업에 필요한 총 경험치 계산
         * 레벨 1: 100 exp
         * 레벨 2: 250 exp (150 더 필요)
         * 레벨 3: 450 exp (200 더 필요)
         * ...점점 더 많이 필요
         */
        fun calculateExpForLevel(level: Int): Int {
            if (level <= 1) return 0
            // 레벨 N까지 총 필요 경험치: 50 * N * (N + 1)
            return 50 * level * (level + 1)
        }

        /**
         * 경험치로 레벨 계산
         */
        fun levelFromExp(totalExp: Int): Int {
            var level = 1
            while (calculateExpForLevel(level + 1) <= totalExp) {
                level++
            }
            return level
        }

        /**
         * 걸음수를 경험치로 변환
         * 100걸음 = 1 exp
         */
        fun stepsToExp(steps: Int): Int {
            return steps / 100
        }
    }

    /**
     * 경험치 추가하고 새로운 PetLevel 반환
     */
    fun addExp(exp: Int): PetLevel {
        val newTotalExp = totalExp + exp
        val newLevel = levelFromExp(newTotalExp)
        return PetLevel(
            level = newLevel,
            currentExp = newTotalExp - calculateExpForLevel(newLevel),
            totalExp = newTotalExp
        )
    }

    /**
     * 레벨업 여부 확인
     */
    fun checkLevelUp(newLevel: PetLevel): Boolean {
        return newLevel.level > this.level
    }

    /**
     * 성장 단계 변경 여부 확인
     */
    fun checkStageEvolution(newLevel: PetLevel): Boolean {
        return newLevel.stage != this.stage
    }
}

/**
 * 펫 전체 상태
 */
data class PetState(
    val petType: PetTypeV2,
    val name: String,
    val level: PetLevel = PetLevel(),
    val happiness: Int = 100,  // 0-100
    val lastInteractionTime: Long = System.currentTimeMillis()
) {
    val stage: PetGrowthStage get() = level.stage
    val personality: PetPersonalityV2 get() = petType.personality

    /**
     * 펫 크기 (dp 기준)
     */
    fun getSizeDp(baseSizeDp: Int = 96): Int {
        return (baseSizeDp * stage.sizeMultiplier).toInt()
    }

    /**
     * 현재 상태에 맞는 애니메이션 타입 결정
     */
    fun getCurrentAnimationType(
        isWalking: Boolean,
        progressPercent: Int,
        isNightMode: Boolean = false
    ): PetAnimationTypeV2 {
        return when {
            stage == PetGrowthStage.EGG -> {
                when {
                    progressPercent >= 90 -> PetAnimationTypeV2.CRACK
                    progressPercent >= 50 -> PetAnimationTypeV2.WOBBLE
                    else -> PetAnimationTypeV2.IDLE
                }
            }
            isNightMode -> PetAnimationTypeV2.SNEAK
            progressPercent >= 90 -> PetAnimationTypeV2.RUN
            isWalking -> PetAnimationTypeV2.WALK
            else -> PetAnimationTypeV2.IDLE
        }
    }
}

/**
 * Egg 전용 애니메이션 설정
 */
object EggAnimationConfig {
    // 알은 정지 이미지 위주 (PixelLab 제한)
    // IDLE: 1장 (정지), WOBBLE: 2장 (좌우 흔들림), CRACK: 1장 (금간 상태), HATCH: 3장 (부화 순서)
    val animations = mapOf(
        PetAnimationTypeV2.IDLE to AnimationConfig(1, 200),    // 1프레임, 정지 상태
        PetAnimationTypeV2.WOBBLE to AnimationConfig(2, 300),  // 2프레임, 좌우 흔들림 (느리게)
        PetAnimationTypeV2.CRACK to AnimationConfig(1, 200),   // 1프레임, 금간 상태
        PetAnimationTypeV2.HATCH to AnimationConfig(3, 500)    // 3프레임, 부화 순서 (느리게)
    )

    fun getAnimationFolderPath(animationType: PetAnimationTypeV2): String {
        return "pets/egg/${animationType.name.lowercase()}/"
    }
}
