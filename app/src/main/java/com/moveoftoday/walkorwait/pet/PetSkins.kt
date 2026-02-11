package com.moveoftoday.walkorwait.pet

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * 펫 스킨 (색상 변종)
 */
data class PetSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val colorMatrix: FloatArray?,      // ColorMatrix 방식 (틴트)
    val overlayColor: Int?,            // 단색 오버레이 (0xAARRGGBB)
    val blendMode: BlendMode,          // 블렌드 모드
    val unlockCondition: UnlockCondition
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PetSkin

        if (id != other.id) return false
        if (displayName != other.displayName) return false
        if (description != other.description) return false
        if (colorMatrix != null) {
            if (other.colorMatrix == null) return false
            if (!colorMatrix.contentEquals(other.colorMatrix)) return false
        } else if (other.colorMatrix != null) return false
        if (overlayColor != other.overlayColor) return false
        if (blendMode != other.blendMode) return false
        if (unlockCondition != other.unlockCondition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (colorMatrix?.contentHashCode() ?: 0)
        result = 31 * result + (overlayColor ?: 0)
        result = 31 * result + blendMode.hashCode()
        result = 31 * result + unlockCondition.hashCode()
        return result
    }
}

/**
 * 기본 스킨 정의
 *
 * 티어 1: 입문 (첫날 획득 가능) - 5개
 * 티어 2: 초급 (첫 주 획득 가능) - 5개
 * 티어 3: 중급 (2-4주 획득) - 5개
 * 티어 4: 고급 (1-2달 획득) - 4개
 * 티어 5: 최상급 (3달+ 획득) - 4개
 */
object DefaultSkins {

    val ALL_SKINS = listOf(
        // ===== 티어 1: 입문 (첫날 획득 가능) =====

        // 기본 (모노크롬)
        PetSkin(
            id = "default",
            displayName = "기본",
            description = "기본 모노크롬 스타일",
            colorMatrix = null,
            overlayColor = null,
            blendMode = BlendMode.SrcOver,
            unlockCondition = UnlockCondition.Default
        ),

        // 세피아 (500보)
        PetSkin(
            id = "sepia",
            displayName = "세피아",
            description = "총 500보 달성",
            colorMatrix = floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            overlayColor = null,
            blendMode = BlendMode.SrcOver,
            unlockCondition = UnlockCondition.Steps(500)
        ),

        // 블루 틴트 (레벨 1)
        PetSkin(
            id = "blue_tint",
            displayName = "블루 틴트",
            description = "레벨 1 달성",
            colorMatrix = floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            overlayColor = null,
            blendMode = BlendMode.SrcOver,
            unlockCondition = UnlockCondition.Level(1)
        ),

        // 골드 틴트 (1일 연속)
        PetSkin(
            id = "gold_tint",
            displayName = "골드 틴트",
            description = "1일 연속 달성",
            colorMatrix = floatArrayOf(
                1.2f, 0.1f, 0f, 0f, 0f,
                0.1f, 1.1f, 0f, 0f, 0f,
                0f, 0f, 0.6f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            overlayColor = null,
            blendMode = BlendMode.SrcOver,
            unlockCondition = UnlockCondition.Streak(1)
        ),

        // 레드 필터 (명상 1회)
        PetSkin(
            id = "red_filter",
            displayName = "레드 필터",
            description = "명상 1회 완료",
            colorMatrix = null,
            overlayColor = Color.Red.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.MEDITATION, 1)
        ),

        // ===== 티어 2: 초급 (첫 주 획득 가능) =====

        // 블루 필터 (3,000보)
        PetSkin(
            id = "blue_filter",
            displayName = "블루 필터",
            description = "총 3,000보 달성",
            colorMatrix = null,
            overlayColor = Color.Blue.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Steps(3000)
        ),

        // 그린 필터 (독서 3회)
        PetSkin(
            id = "green_filter",
            displayName = "그린 필터",
            description = "독서 3회 완료",
            colorMatrix = null,
            overlayColor = Color.Green.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.READING, 3)
        ),

        // 옐로우 필터 (3일 연속)
        PetSkin(
            id = "yellow_filter",
            displayName = "옐로우 필터",
            description = "3일 연속 달성",
            colorMatrix = null,
            overlayColor = Color.Yellow.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Streak(3)
        ),

        // 오렌지 필터 (레벨 2)
        PetSkin(
            id = "orange_filter",
            displayName = "오렌지 필터",
            description = "레벨 2 달성",
            colorMatrix = null,
            overlayColor = Color(0xFFFF8800).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Level(2)
        ),

        // 시안 필터 (운동 5회)
        PetSkin(
            id = "cyan_filter",
            displayName = "시안 필터",
            description = "운동 5회 완료",
            colorMatrix = null,
            overlayColor = Color.Cyan.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.EXERCISE, 5)
        ),

        // ===== 티어 3: 중급 (2-4주 획득) =====

        // 네온 (레벨 5)
        PetSkin(
            id = "neon",
            displayName = "네온",
            description = "레벨 5 달성",
            colorMatrix = floatArrayOf(
                1.5f, 0.2f, 0.2f, 0f, 0f,
                0.2f, 1.5f, 0.2f, 0f, 0f,
                0.2f, 0.2f, 1.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            overlayColor = null,
            blendMode = BlendMode.SrcOver,
            unlockCondition = UnlockCondition.Level(5)
        ),

        // 마젠타 필터 (7일 연속)
        PetSkin(
            id = "magenta_filter",
            displayName = "마젠타 필터",
            description = "7일 연속 달성",
            colorMatrix = null,
            overlayColor = Color.Magenta.copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Streak(7)
        ),

        // 핑크 필터 (명상 10회)
        PetSkin(
            id = "pink_filter",
            displayName = "핑크 필터",
            description = "명상 10회 완료",
            colorMatrix = null,
            overlayColor = Color(0xFFFF69B4).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.MEDITATION, 10)
        ),

        // 퍼플 필터 (공부 10회)
        PetSkin(
            id = "purple_filter",
            displayName = "퍼플 필터",
            description = "공부 10회 완료",
            colorMatrix = null,
            overlayColor = Color(0xFF9370DB).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.STUDY, 10)
        ),

        // 라임 필터 (50,000보)
        PetSkin(
            id = "lime_filter",
            displayName = "라임 필터",
            description = "총 50,000보 달성",
            colorMatrix = null,
            overlayColor = Color(0xFF00FF00).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Steps(50000)
        ),

        // ===== 티어 4: 고급 (1-2달 획득) =====

        // 틸 필터 (레벨 10)
        PetSkin(
            id = "teal_filter",
            displayName = "틸 필터",
            description = "레벨 10 달성",
            colorMatrix = null,
            overlayColor = Color(0xFF008080).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Level(10)
        ),

        // 골드 필터 (30일 연속)
        PetSkin(
            id = "gold_filter",
            displayName = "골드 필터",
            description = "30일 연속 달성",
            colorMatrix = null,
            overlayColor = Color(0xFFFFD700).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Streak(30)
        ),

        // 실버 필터 (웰니스 10회 - 간헐적 단식)
        PetSkin(
            id = "silver_filter",
            displayName = "실버 필터",
            description = "웰니스 10회 완료",
            colorMatrix = null,
            overlayColor = Color(0xFFC0C0C0).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.WELLNESS, 10)
        ),

        // 브론즈 필터 (100,000보)
        PetSkin(
            id = "bronze_filter",
            displayName = "브론즈 필터",
            description = "총 100,000보 달성",
            colorMatrix = null,
            overlayColor = Color(0xFFCD7F32).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Steps(100000)
        ),

        // ===== 티어 5: 최상급 (3달+ 획득) =====

        // 다크 레드 필터 (운동 50회)
        PetSkin(
            id = "dark_red_filter",
            displayName = "다크 레드 필터",
            description = "운동 50회 완료",
            colorMatrix = null,
            overlayColor = Color(0xFF8B0000).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.EXERCISE, 50)
        ),

        // 다크 블루 필터 (100일 연속)
        PetSkin(
            id = "dark_blue_filter",
            displayName = "다크 블루 필터",
            description = "100일 연속 달성",
            colorMatrix = null,
            overlayColor = Color(0xFF00008B).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Streak(100)
        ),

        // 다크 그린 필터 (레벨 20)
        PetSkin(
            id = "dark_green_filter",
            displayName = "다크 그린 필터",
            description = "레벨 20 달성",
            colorMatrix = null,
            overlayColor = Color(0xFF006400).copy(alpha = 0.6f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Level(20)
        ),

        // 화이트 필터 (독서 50회)
        PetSkin(
            id = "white_filter",
            displayName = "화이트 필터",
            description = "독서 50회 완료",
            colorMatrix = null,
            overlayColor = Color.White.copy(alpha = 0.4f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.ChallengeCount(ChallengeCategory.READING, 50)
        ),

        // 블랙 필터 (365일 연속)
        PetSkin(
            id = "black_filter",
            displayName = "블랙 필터",
            description = "365일 연속 달성",
            colorMatrix = null,
            overlayColor = Color.Black.copy(alpha = 0.3f).toArgb(),
            blendMode = BlendMode.Modulate,
            unlockCondition = UnlockCondition.Streak(365)
        )
    )

    /**
     * ID로 스킨 찾기
     */
    fun getById(id: String?): PetSkin? = ALL_SKINS.find { it.id == id }

    /**
     * 기본 스킨
     */
    fun getDefault(): PetSkin = ALL_SKINS.first()
}
