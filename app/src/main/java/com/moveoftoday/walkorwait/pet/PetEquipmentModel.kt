package com.moveoftoday.walkorwait.pet

/**
 * 펫 장비 시스템 데이터 모델
 */

/**
 * 장비 슬롯
 */
enum class EquipmentSlot(val displayName: String) {
    HEAD("머리"),
    BACKGROUND("배경"),
    COLOR("색상")
}

/**
 * 장비 아이템
 */
data class EquipmentItem(
    val id: String,                      // "head_crown", "bg_sparkle" 등
    val slot: EquipmentSlot,
    val displayName: String,             // "황금 왕관"
    val description: String,             // "100일 달성 기념"
    val assetPath: String?,              // HEAD/BACKGROUND용 (null이면 COLOR)
    val colorMatrix: FloatArray?,        // COLOR용 (null이면 HEAD/BACKGROUND)
    val unlockCondition: UnlockCondition
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EquipmentItem

        if (id != other.id) return false
        if (slot != other.slot) return false
        if (displayName != other.displayName) return false
        if (description != other.description) return false
        if (assetPath != other.assetPath) return false
        if (colorMatrix != null) {
            if (other.colorMatrix == null) return false
            if (!colorMatrix.contentEquals(other.colorMatrix)) return false
        } else if (other.colorMatrix != null) return false
        if (unlockCondition != other.unlockCondition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + slot.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (assetPath?.hashCode() ?: 0)
        result = 31 * result + (colorMatrix?.contentHashCode() ?: 0)
        result = 31 * result + unlockCondition.hashCode()
        return result
    }
}

/**
 * 해금 조건
 */
sealed class UnlockCondition {
    object Default : UnlockCondition()                      // 기본 제공
    data class Steps(val totalSteps: Long) : UnlockCondition()
    data class Streak(val days: Int) : UnlockCondition()
    data class Level(val level: Int) : UnlockCondition()
    data class Event(val eventId: String) : UnlockCondition()
    data class ChallengeCount(val category: String, val count: Int) : UnlockCondition()  // 카테고리별 챌린지 완료 횟수
}

/**
 * 챌린지 카테고리 상수
 */
object ChallengeCategory {
    const val READING = "독서"
    const val MEDITATION = "명상"
    const val STUDY = "공부"
    const val EXERCISE = "운동"
    const val WELLNESS = "웰니스"
}

/**
 * 장착 상태
 */
data class EquipmentState(
    val headId: String? = null,
    val backgroundId: String? = null,
    val colorId: String? = null
)
