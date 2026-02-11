package com.moveoftoday.walkorwait.pet

/**
 * 기본 장비 정의
 */
object DefaultEquipment {

    val ALL_EQUIPMENT = listOf(
        // ===== BACKGROUND 장비 =====

        // BACKGROUND - 반짝이 (레벨 10)
        EquipmentItem(
            id = "bg_sparkle",
            slot = EquipmentSlot.BACKGROUND,
            displayName = "반짝이",
            description = "레벨 10 달성",
            assetPath = "equipment/background/sparkle.png",
            colorMatrix = null,
            unlockCondition = UnlockCondition.Level(10)
        ),

        // BACKGROUND - 오라 (레벨 20)
        EquipmentItem(
            id = "bg_aura",
            slot = EquipmentSlot.BACKGROUND,
            displayName = "황금 오라",
            description = "레벨 20 달성",
            assetPath = "equipment/background/aura.png",
            colorMatrix = null,
            unlockCondition = UnlockCondition.Level(20)
        ),

        // BACKGROUND - 별빛 (50,000보)
        EquipmentItem(
            id = "bg_stars",
            slot = EquipmentSlot.BACKGROUND,
            displayName = "별빛",
            description = "총 50,000보 달성",
            assetPath = "equipment/background/stars.png",
            colorMatrix = null,
            unlockCondition = UnlockCondition.Steps(50000)
        )
    )

    /**
     * ID로 장비 아이템 찾기
     */
    fun getById(id: String?): EquipmentItem? = ALL_EQUIPMENT.find { it.id == id }

    /**
     * 슬롯별 장비 아이템 목록
     */
    fun getBySlot(slot: EquipmentSlot): List<EquipmentItem> = ALL_EQUIPMENT.filter { it.slot == slot }
}
