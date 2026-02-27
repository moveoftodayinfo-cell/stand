package com.moveoftoday.walkorwait.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*

/**
 * 펫 스킨 선택 화면
 */
@Composable
fun SettingsSkinScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }

    if (preferenceManager == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("PreferenceManager가 초기화되지 않았습니다", color = Color.Gray)
        }
        return
    }

    // 현재 스킨
    var currentSkinId by remember { mutableStateOf(preferenceManager.getPetSkin()) }

    // 보유 스킨 목록
    var ownedSkins by remember { mutableStateOf(preferenceManager.getOwnedSkins()) }

    // 펫 정보
    val petTypeV2 = preferenceManager.getPetTypeV2()
    val petStage = preferenceManager.getEffectiveDisplayStage()

    // 스킨 자동 해금
    LaunchedEffect(Unit) {
        val newlyUnlocked = preferenceManager.checkAndUnlockNewSkins()
        if (newlyUnlocked.isNotEmpty()) {
            ownedSkins = preferenceManager.getOwnedSkins()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                modifier = Modifier.clickable {
                    hapticManager.lightClick()
                    onBack()
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = SkinScreenStrings.petSkin(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 스크롤 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // 현재 스킨 미리보기
            if (petTypeV2 != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = SkinScreenStrings.currentSkin(),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val currentSkin = DefaultSkins.getById(currentSkinId)
                        val equipmentState = EquipmentState(
                            headId = null,
                            backgroundId = null,
                            colorId = currentSkinId  // colorId를 skinId로 사용
                        )

                        PetSpriteV2WithEquipment(
                            petType = petTypeV2,
                            stage = petStage,
                            animationType = PetAnimationTypeV2.IDLE,
                            equipmentState = equipmentState,
                            size = 96.dp,
                            monochrome = true,
                            showGlow = false
                        )
                    }

                    val currentSkin = DefaultSkins.getById(currentSkinId)
                    Text(
                        text = currentSkin?.getLocalizedDisplayName() ?: SkinScreenStrings.unknown(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEEEEEE))
                    .padding(vertical = 16.dp)
            )

            // 스킨 목록
            Text(
                text = SkinScreenStrings.availableSkins(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 스킨 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                userScrollEnabled = false
            ) {
                items(DefaultSkins.ALL_SKINS) { skin ->
                    val isOwned = ownedSkins.contains(skin.id)
                    val isSelected = currentSkinId == skin.id

                    SkinItem(
                        skin = skin,
                        isOwned = isOwned,
                        isSelected = isSelected,
                        petTypeV2 = petTypeV2,
                        petStage = petStage,
                        onClick = {
                            if (isOwned) {
                                hapticManager.lightClick()
                                preferenceManager.savePetSkin(skin.id)
                                currentSkinId = skin.id
                            } else {
                                hapticManager.warning()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 스킨 아이템
 */
@Composable
private fun SkinItem(
    skin: PetSkin,
    isOwned: Boolean,
    isSelected: Boolean,
    petTypeV2: PetTypeV2?,
    petStage: PetGrowthStage,
    onClick: () -> Unit
) {
    // 챌린지 박스와 동일한 레이아웃 구조
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Black else Color(0xFFDDDDDD),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (isSelected) Color(0xFFF5F5F5) else Color.White,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .alpha(if (isOwned) 1f else 0.5f)
            .padding(8.dp)
    ) {
        // 스킨 미리보기 (중앙 상단) - 보유 여부 상관없이 미리보기 표시
        if (petTypeV2 != null) {
            val equipmentState = EquipmentState(
                headId = null,
                backgroundId = null,
                colorId = skin.id
            )

            PetSpriteV2WithEquipment(
                petType = petTypeV2,
                stage = petStage,
                animationType = PetAnimationTypeV2.IDLE,
                equipmentState = equipmentState,
                size = 72.dp,
                monochrome = true,
                showGlow = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-12).dp)
            )
        }

        // 스킨 이름 (하단 고정)
        Text(
            text = skin.getLocalizedDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = if (!isOwned) (-14).dp else 0.dp)
        )

        // 해금 조건 (최하단)
        if (!isOwned) {
            Text(
                text = getUnlockConditionText(skin.unlockCondition),
                fontSize = 9.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
            )
        }
    }
}

// 다국어 헬퍼 (스킨 화면 UI 텍스트)
private object SkinScreenStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    fun petSkin(): String = when (getLang()) {
        "ko" -> "펫 스킨"; "ja" -> "ペットスキン"; "zh" -> "宠物皮肤"; "es" -> "Skin de mascota"; else -> "Pet Skin"
    }

    fun currentSkin(): String = when (getLang()) {
        "ko" -> "현재 스킨"; "ja" -> "現在のスキン"; "zh" -> "当前皮肤"; "es" -> "Skin actual"; else -> "Current Skin"
    }

    fun unknown(): String = when (getLang()) {
        "ko" -> "알 수 없음"; "ja" -> "不明"; "zh" -> "未知"; "es" -> "Desconocido"; else -> "Unknown"
    }

    fun availableSkins(): String = when (getLang()) {
        "ko" -> "사용 가능한 스킨"; "ja" -> "使用可能なスキン"; "zh" -> "可用皮肤"; "es" -> "Skins disponibles"; else -> "Available Skins"
    }

    fun defaultText(): String = when (getLang()) {
        "ko" -> "기본"; "ja" -> "デフォルト"; "zh" -> "默认"; "es" -> "Defecto"; else -> "Default"
    }

    fun steps(count: Long): String = when (getLang()) {
        "ko" -> "${count}보"; "ja" -> "${count}歩"; "zh" -> "${count}步"; "es" -> "${count} pasos"; else -> "${count} steps"
    }

    fun streak(days: Int): String = when (getLang()) {
        "ko" -> "${days}일 연속"; "ja" -> "${days}日連続"; "zh" -> "连续${days}天"; "es" -> "${days} días seguidos"; else -> "${days} day streak"
    }

    fun level(level: Int): String = when (getLang()) {
        "ko" -> "레벨 ${level}"; "ja" -> "レベル${level}"; "zh" -> "等级${level}"; "es" -> "Nivel ${level}"; else -> "Level ${level}"
    }

    fun event(): String = when (getLang()) {
        "ko" -> "이벤트"; "ja" -> "イベント"; "zh" -> "活动"; "es" -> "Evento"; else -> "Event"
    }

    fun challengeCount(category: String, count: Int): String = when (getLang()) {
        "ko" -> "$category ${count}회"; "ja" -> "$category ${count}回"; "zh" -> "$category ${count}次"; "es" -> "$category ${count}x"; else -> "$category ${count}x"
    }

    /** 카테고리 이름 로컬라이즈 */
    fun localizeCategory(category: String): String {
        val lang = getLang()
        return when (category) {
            "독서" -> when (lang) { "ko" -> "독서"; "ja" -> "読書"; "zh" -> "阅读"; "es" -> "Lectura"; else -> "Reading" }
            "명상" -> when (lang) { "ko" -> "명상"; "ja" -> "瞑想"; "zh" -> "冥想"; "es" -> "Meditación"; else -> "Meditation" }
            "공부" -> when (lang) { "ko" -> "공부"; "ja" -> "勉強"; "zh" -> "学习"; "es" -> "Estudio"; else -> "Study" }
            "운동" -> when (lang) { "ko" -> "운동"; "ja" -> "運動"; "zh" -> "运动"; "es" -> "Ejercicio"; else -> "Exercise" }
            "웰니스" -> when (lang) { "ko" -> "웰니스"; "ja" -> "ウェルネス"; "zh" -> "健康"; "es" -> "Bienestar"; else -> "Wellness" }
            else -> category
        }
    }
}

/**
 * 해금 조건 텍스트
 */
private fun getUnlockConditionText(condition: UnlockCondition): String {
    return when (condition) {
        is UnlockCondition.Default -> SkinScreenStrings.defaultText()
        is UnlockCondition.Steps -> SkinScreenStrings.steps(condition.totalSteps)
        is UnlockCondition.Streak -> SkinScreenStrings.streak(condition.days)
        is UnlockCondition.Level -> SkinScreenStrings.level(condition.level)
        is UnlockCondition.Event -> SkinScreenStrings.event()
        is UnlockCondition.ChallengeCount -> {
            val localizedCategory = SkinScreenStrings.localizeCategory(condition.category)
            SkinScreenStrings.challengeCount(localizedCategory, condition.count)
        }
    }
}
