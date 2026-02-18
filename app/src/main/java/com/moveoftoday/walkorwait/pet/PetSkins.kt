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
    /** 다국어 스킨 이름 */
    fun getLocalizedDisplayName(): String {
        val lang = java.util.Locale.getDefault().language
        return SkinNameStrings.getName(id, lang) ?: displayName
    }

    /** 다국어 스킨 설명 */
    fun getLocalizedDescription(): String {
        val lang = java.util.Locale.getDefault().language
        return SkinNameStrings.getDescription(id, lang) ?: description
    }

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

/**
 * 스킨 이름 다국어 지원
 */
object SkinNameStrings {
    private val names = mapOf(
        // 기본
        "default" to mapOf("ko" to "기본", "ja" to "デフォルト", "zh" to "默认", "es" to "Defecto", "en" to "Default"),
        // 세피아
        "sepia" to mapOf("ko" to "세피아", "ja" to "セピア", "zh" to "复古", "es" to "Sepia", "en" to "Sepia"),
        // 블루 틴트
        "blue_tint" to mapOf("ko" to "블루 틴트", "ja" to "ブルーティント", "zh" to "蓝色调", "es" to "Tinte azul", "en" to "Blue Tint"),
        // 골드 틴트
        "gold_tint" to mapOf("ko" to "골드 틴트", "ja" to "ゴールドティント", "zh" to "金色调", "es" to "Tinte dorado", "en" to "Gold Tint"),
        // 레드 필터
        "red_filter" to mapOf("ko" to "레드 필터", "ja" to "レッドフィルター", "zh" to "红色滤镜", "es" to "Filtro rojo", "en" to "Red Filter"),
        // 블루 필터
        "blue_filter" to mapOf("ko" to "블루 필터", "ja" to "ブルーフィルター", "zh" to "蓝色滤镜", "es" to "Filtro azul", "en" to "Blue Filter"),
        // 그린 틴트
        "green_tint" to mapOf("ko" to "그린 틴트", "ja" to "グリーンティント", "zh" to "绿色调", "es" to "Tinte verde", "en" to "Green Tint"),
        // 퍼플 틴트
        "purple_tint" to mapOf("ko" to "퍼플 틴트", "ja" to "パープルティント", "zh" to "紫色调", "es" to "Tinte púrpura", "en" to "Purple Tint"),
        // 오렌지 틴트
        "orange_tint" to mapOf("ko" to "오렌지 틴트", "ja" to "オレンジティント", "zh" to "橙色调", "es" to "Tinte naranja", "en" to "Orange Tint"),
        // 핑크 틴트
        "pink_tint" to mapOf("ko" to "핑크 틴트", "ja" to "ピンクティント", "zh" to "粉色调", "es" to "Tinte rosa", "en" to "Pink Tint"),
        // 시안 필터
        "cyan_filter" to mapOf("ko" to "시안 필터", "ja" to "シアンフィルター", "zh" to "青色滤镜", "es" to "Filtro cian", "en" to "Cyan Filter"),
        // 그린 필터
        "green_filter" to mapOf("ko" to "그린 필터", "ja" to "グリーンフィルター", "zh" to "绿色滤镜", "es" to "Filtro verde", "en" to "Green Filter"),
        // 퍼플 필터
        "purple_filter" to mapOf("ko" to "퍼플 필터", "ja" to "パープルフィルター", "zh" to "紫色滤镜", "es" to "Filtro púrpura", "en" to "Purple Filter"),
        // 오렌지 필터
        "orange_filter" to mapOf("ko" to "오렌지 필터", "ja" to "オレンジフィルター", "zh" to "橙色滤镜", "es" to "Filtro naranja", "en" to "Orange Filter"),
        // 핑크 필터
        "pink_filter" to mapOf("ko" to "핑크 필터", "ja" to "ピンクフィルター", "zh" to "粉色滤镜", "es" to "Filtro rosa", "en" to "Pink Filter"),
        // 골드 필터
        "gold_filter" to mapOf("ko" to "골드 필터", "ja" to "ゴールドフィルター", "zh" to "金色滤镜", "es" to "Filtro dorado", "en" to "Gold Filter"),
        // 실버 필터
        "silver_filter" to mapOf("ko" to "실버 필터", "ja" to "シルバーフィルター", "zh" to "银色滤镜", "es" to "Filtro plata", "en" to "Silver Filter"),
        // 브론즈 필터
        "bronze_filter" to mapOf("ko" to "브론즈 필터", "ja" to "ブロンズフィルター", "zh" to "铜色滤镜", "es" to "Filtro bronce", "en" to "Bronze Filter"),
        // 다크 레드 필터
        "dark_red_filter" to mapOf("ko" to "다크 레드 필터", "ja" to "ダークレッドフィルター", "zh" to "深红滤镜", "es" to "Filtro rojo oscuro", "en" to "Dark Red Filter"),
        // 다크 블루 필터
        "dark_blue_filter" to mapOf("ko" to "다크 블루 필터", "ja" to "ダークブルーフィルター", "zh" to "深蓝滤镜", "es" to "Filtro azul oscuro", "en" to "Dark Blue Filter"),
        // 다크 그린 필터
        "dark_green_filter" to mapOf("ko" to "다크 그린 필터", "ja" to "ダークグリーンフィルター", "zh" to "深绿滤镜", "es" to "Filtro verde oscuro", "en" to "Dark Green Filter"),
        // 화이트 필터
        "white_filter" to mapOf("ko" to "화이트 필터", "ja" to "ホワイトフィルター", "zh" to "白色滤镜", "es" to "Filtro blanco", "en" to "White Filter"),
        // 블랙 필터
        "black_filter" to mapOf("ko" to "블랙 필터", "ja" to "ブラックフィルター", "zh" to "黑色滤镜", "es" to "Filtro negro", "en" to "Black Filter")
    )

    fun getName(skinId: String, lang: String): String? {
        val langMap = names[skinId] ?: return null
        return langMap[lang] ?: langMap["en"]
    }

    fun getDescription(skinId: String, lang: String): String? {
        // Descriptions are generated dynamically based on unlock conditions
        // This is a placeholder for potential future use
        return null
    }
}
