package com.moveoftoday.walkorwait.pet

import java.util.Locale

/**
 * Pet Evolution System - Rebon
 * 새로운 펫 성장 시스템
 */

// 다국어 지원
private fun getLang(): String = Locale.getDefault().language

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
        displayName = "새싹",
        levelRange = 1..10,
        sizeMultiplier = 1.0f,
        folderName = "baby"
    ),
    TEEN(
        displayName = "성장",
        levelRange = 11..20,
        sizeMultiplier = 1.2f,
        folderName = "teen"
    ),
    ADULT(
        displayName = "완성",
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

    /**
     * 다국어 성장 단계 이름
     */
    fun getLocalizedName(): String = when (this) {
        EGG -> when (getLang()) {
            "ko" -> "알"
            "ja" -> "たまご"
            "zh" -> "蛋"
            "es" -> "Huevo"
            else -> "Egg"
        }
        BABY -> when (getLang()) {
            "ko" -> "새싹"
            "ja" -> "ベビー"
            "zh" -> "幼崽"
            "es" -> "Bebé"
            else -> "Baby"
        }
        TEEN -> when (getLang()) {
            "ko" -> "성장"
            "ja" -> "成長"
            "zh" -> "成长"
            "es" -> "Joven"
            else -> "Teen"
        }
        ADULT -> when (getLang()) {
            "ko" -> "완성"
            "ja" -> "成体"
            "zh" -> "成年"
            "es" -> "Adulto"
            else -> "Adult"
        }
    }
}

/**
 * 새로운 펫 타입 (6종)
 * displayScales: cat baby 기준으로 모든 펫/단계의 크기 정규화
 * displayYOffsetsDp: cat baby 기준으로 하단 정렬을 위한 Y offset (dp 단위, 음수=위로)
 */
enum class PetTypeV2(
    val displayName: String,
    val personality: PetPersonalityV2,
    val folderName: String,
    val defaultAnimationFrames: Map<PetAnimationTypeV2, AnimationConfig>,
    val displayScales: Map<PetGrowthStage, Float>,  // 단계별 스케일 (cat baby 기준)
    val displayYOffsetsDp: Map<PetGrowthStage, Float>   // 단계별 Y offset (dp 단위, 음수=위로)
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 1.0f,
            PetGrowthStage.BABY to 0.85f,
            PetGrowthStage.TEEN to 0.89f,
            PetGrowthStage.ADULT to 0.92f
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -6f,
            PetGrowthStage.TEEN to -12f,
            PetGrowthStage.ADULT to -12f
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 1.0f,
            PetGrowthStage.BABY to 1.0f,
            PetGrowthStage.TEEN to 0.94f,
            PetGrowthStage.ADULT to 0.85f
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -12f,
            PetGrowthStage.TEEN to -12f,
            PetGrowthStage.ADULT to -12f
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 1.0f,
            PetGrowthStage.BABY to 0.72f,
            PetGrowthStage.TEEN to 0.76f,
            PetGrowthStage.ADULT to 0.74f
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -12f,
            PetGrowthStage.TEEN to -12f,
            PetGrowthStage.ADULT to -12f
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 1.0f,
            PetGrowthStage.BABY to 0.76f,
            PetGrowthStage.TEEN to 0.79f,
            PetGrowthStage.ADULT to 0.71f
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -12f,
            PetGrowthStage.TEEN to -12f,
            PetGrowthStage.ADULT to -12f
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 1.0f,
            PetGrowthStage.BABY to 0.72f,
            PetGrowthStage.TEEN to 0.74f,
            PetGrowthStage.ADULT to 0.74f
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -12f,
            PetGrowthStage.TEEN to -12f,
            PetGrowthStage.ADULT to -12f
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
        ),
        displayScales = mapOf(
            PetGrowthStage.EGG to 0.8f,
            PetGrowthStage.BABY to 0.70f,  // 0.87 * 0.8
            PetGrowthStage.TEEN to 0.71f,  // 0.89 * 0.8
            PetGrowthStage.ADULT to 0.64f  // 0.71 * 0.9 (축소)
        ),
        displayYOffsetsDp = mapOf(
            PetGrowthStage.EGG to 0f,
            PetGrowthStage.BABY to -17f,  // 3dp 더 위로 (-14 → -17)
            PetGrowthStage.TEEN to -17f,
            PetGrowthStage.ADULT to -22f  // 5dp 더 위로 (닉네임 가림 방지)
        )
    );

    /** 특정 단계의 displayScale 반환 */
    fun getDisplayScale(stage: PetGrowthStage): Float = displayScales[stage] ?: 1.0f

    /** 특정 단계의 Y offset 반환 (dp 단위, 음수=위로) */
    fun getDisplayYOffsetDp(stage: PetGrowthStage): Float = displayYOffsetsDp[stage] ?: 0f

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

    /**
     * 다국어 펫 이름
     */
    fun getLocalizedDisplayName(): String = when (this) {
        SHIBA -> when (getLang()) {
            "ko" -> "멍이"
            "ja" -> "シバ"
            "zh" -> "柴犬"
            "es" -> "Shiba"
            else -> "Shiba"
        }
        CAT -> when (getLang()) {
            "ko" -> "냥이"
            "ja" -> "ネコ"
            "zh" -> "喵喵"
            "es" -> "Gato"
            else -> "Cat"
        }
        PIG -> when (getLang()) {
            "ko" -> "꿀꿀이"
            "ja" -> "ブタ"
            "zh" -> "小猪"
            "es" -> "Cerdito"
            else -> "Piggy"
        }
        RACCOON -> when (getLang()) {
            "ko" -> "라쿤"
            "ja" -> "ラクーン"
            "zh" -> "浣熊"
            "es" -> "Mapache"
            else -> "Raccoon"
        }
        HAMSTER -> when (getLang()) {
            "ko" -> "햄찌"
            "ja" -> "ハムスター"
            "zh" -> "仓鼠"
            "es" -> "Hámster"
            else -> "Hamster"
        }
        PENGUIN -> when (getLang()) {
            "ko" -> "펭펭"
            "ja" -> "ペンギン"
            "zh" -> "企鹅"
            "es" -> "Pingüino"
            else -> "Penguin"
        }
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
    CLUMSY("덤벙/순수");             // 펭귄 - 덤벙대지만 귀여움

    /**
     * 다국어 성격 설명
     */
    fun getLocalizedDescription(): String = when (this) {
        LOYAL -> when (getLang()) {
            "ko" -> "충성스러운 상남자"
            "ja" -> "忠実なクールガイ"
            "zh" -> "忠诚的酷哥"
            "es" -> "Leal y genial"
            else -> "Loyal & Cool"
        }
        TSUNDERE -> when (getLang()) {
            "ko" -> "츤데레"
            "ja" -> "ツンデレ"
            "zh" -> "傲娇"
            "es" -> "Tsundere"
            else -> "Tsundere"
        }
        FOODIE -> when (getLang()) {
            "ko" -> "먹보/낙천가"
            "ja" -> "食いしん坊/楽天家"
            "zh" -> "吃货/乐天派"
            "es" -> "Glotón/Optimista"
            else -> "Foodie/Optimist"
        }
        PLAYFUL -> when (getLang()) {
            "ko" -> "장난꾸러기"
            "ja" -> "いたずらっ子"
            "zh" -> "淘气鬼"
            "es" -> "Juguetón"
            else -> "Playful"
        }
        TIMID -> when (getLang()) {
            "ko" -> "소심/부지런"
            "ja" -> "臆病/勤勉"
            "zh" -> "胆小/勤快"
            "es" -> "Tímido/Diligente"
            else -> "Timid/Diligent"
        }
        CLUMSY -> when (getLang()) {
            "ko" -> "덤벙/순수"
            "ja" -> "おっちょこちょい/純粋"
            "zh" -> "冒失/纯真"
            "es" -> "Torpe/Puro"
            else -> "Clumsy/Pure"
        }
    }
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
         * 2주 만에 성체(레벨 21) 도달 가능한 곡선
         * 레벨 2: 8 exp (800 걸음, ~10분)
         * 레벨 3: 20 exp (2,000 걸음, ~25분)
         * 레벨 10: 216 exp (21,600 걸음, ~3일)
         * 레벨 21: 920 exp (92,000 걸음, ~13일)
         */
        fun calculateExpForLevel(level: Int): Int {
            if (level <= 1) return 0
            // 레벨 N까지 총 필요 경험치: 2 * N * (N + 1) - 4
            return 2 * level * (level + 1) - 4
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
    val lastInteractionTime: Long = System.currentTimeMillis(),
    val displayStageOverride: PetGrowthStage? = null  // 외형 오버라이드 (null이면 level에 맞는 기본값)
) {
    val stage: PetGrowthStage get() = displayStageOverride ?: level.stage  // 오버라이드 우선
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
