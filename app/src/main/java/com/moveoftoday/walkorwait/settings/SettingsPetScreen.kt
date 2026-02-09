package com.moveoftoday.walkorwait.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.launch

/**
 * 펫 관리 화면
 */
@Composable
fun SettingsPetScreen(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    // 펫 정보 (상태로 관리 - 변경 시 UI 업데이트됨)
    var petTypeV2 by remember { mutableStateOf(preferenceManager?.getPetTypeV2()) }
    var petName by remember { mutableStateOf(preferenceManager?.getPetNameV2() ?: "친구") }
    var petLevel by remember { mutableStateOf(preferenceManager?.getPetLevelV2() ?: PetLevel()) }
    var petTotalSteps by remember { mutableStateOf(preferenceManager?.getPetTotalSteps() ?: 0L) }

    // 펫 정보 새로고침 트리거
    var refreshTrigger by remember { mutableStateOf(0) }

    // refreshTrigger가 변경되면 펫 정보 다시 로드
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            android.util.Log.d("SettingsPetScreen", "🔄 Refreshing pet info (trigger=$refreshTrigger)")
            petTypeV2 = preferenceManager?.getPetTypeV2()
            petName = preferenceManager?.getPetNameV2() ?: "친구"
            petLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()
            petTotalSteps = preferenceManager?.getPetTotalSteps() ?: 0L
            android.util.Log.d("SettingsPetScreen", "✅ Refreshed: petTypeV2=${petTypeV2?.name}, petName=$petName")
        }
    }

    // 펫 변경 다이얼로그 상태
    var showPetChangeDialog by remember { mutableStateOf(false) }

    // BillingManager
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 펫 변경 결제 시작
    fun startPetChangePurchase(newPetType: PetTypeV2, newPetName: String) {
        android.util.Log.d("SettingsPetScreen", "🛒 Starting purchase for: ${newPetType.name}, name: $newPetName")
        android.util.Log.d("SettingsPetScreen", "📌 Current UI state BEFORE purchase: petTypeV2=${petTypeV2?.name}, petName=$petName")

        preferenceManager?.savePendingPetChange(newPetType.name, newPetName)
        showPetChangeDialog = false

        val activity = context as? android.app.Activity ?: return

        scope.launch {
            kotlinx.coroutines.delay(100)

            billingManager = BillingManager(
                context = context,
                onPurchaseSuccess = { purchase ->
                    // ✅ 펫 변경 purchase인지 확인 (구독 purchase는 무시)
                    if (!purchase.products.contains(BillingManager.PET_CHANGE_PRODUCT_ID)) {
                        android.util.Log.d("SettingsPetScreen", "⚠️ Ignoring non-pet-change purchase: ${purchase.products}")
                        return@BillingManager
                    }

                    android.util.Log.d("SettingsPetScreen", "✅ Pet change purchase confirmed: ${purchase.orderId}")

                    val petTypeName = preferenceManager?.getPendingPetType()
                    val savedPetName = preferenceManager?.getPendingPetName() ?: ""

                    if (petTypeName != null) {
                        try {
                            val petType = try { PetTypeV2.valueOf(petTypeName) } catch (e: Exception) { PetTypeV2.SHIBA }
                            preferenceManager?.savePetTypeV2(petType)
                            preferenceManager?.savePetNameV2(savedPetName)

                            val existingLevel = preferenceManager?.getPetLevelV2()
                            if (existingLevel == null || existingLevel.level == 0) {
                                preferenceManager?.savePetLevelV2(PetLevel(level = 1, currentExp = 0, totalExp = 0))
                            }

                            val app = context.applicationContext as WalkorWaitApp
                            app.userDataRepository.savePetInfo(petTypeName, savedPetName)
                            app.userDataRepository.trackPetChangePurchase(petTypeName, savedPetName)
                            AnalyticsManager.trackPurchaseCompleted("pet_change", 2500.0)

                            android.util.Log.d("SettingsPetScreen", "✅ Payment SUCCESS! Updating UI now...")
                            android.util.Log.d("SettingsPetScreen", "📌 UI state BEFORE update: petTypeV2=${petTypeV2?.name}, petName=$petName")

                            // ✅ UI 즉시 업데이트 (결제 완료 후에만!) - refreshTrigger로 리컴포지션 트리거
                            refreshTrigger++

                            android.util.Log.d("SettingsPetScreen", "📌 Triggered refresh (refreshTrigger=$refreshTrigger)")

                            StepWidgetProvider.updateAllWidgets(context.applicationContext)

                            // Toast는 메인 스레드에서만
                            scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, "펫이 변경되었습니다!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsPetScreen", "Pet change failed: ${e.message}")
                        }
                    }
                    preferenceManager?.clearPendingPetChange()
                },
                onPurchaseFailure = { error ->
                    android.util.Log.e("SettingsPetScreen", "❌ Payment FAILED! UI should NOT change. Error: $error")
                    android.util.Log.d("SettingsPetScreen", "📌 UI state (should be unchanged): petTypeV2=${petTypeV2?.name}, petName=$petName")
                    preferenceManager?.clearPendingPetChange()
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
            billingManager?.startPetChangePurchase(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "pet",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            // 스크롤 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ========== 현재 펫 ==========
                RetroSectionTitle("현재 펫", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                        .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 펫 스프라이트 (오버라이드 반영)
                        petTypeV2?.let { petType ->
                            val effectiveStage = preferenceManager?.getEffectiveDisplayStage() ?: petLevel.stage
                            PetSpriteV2WithGlow(
                                petType = petType,
                                stage = effectiveStage,  // 오버라이드 또는 기본값
                                animationType = PetAnimationTypeV2.IDLE,
                                size = 120.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 펫 이름
                        Text(
                            text = petName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 레벨 & 단계
                        Text(
                            text = "Lv.${petLevel.level} (${petLevel.stage.displayName})",
                            fontSize = 16.sp,
                            color = MockupColors.Blue,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 경험치 바
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "EXP",
                                    fontSize = 12.sp,
                                    color = MockupColors.TextSecondary
                                )
                                Text(
                                    text = "${(petLevel.expProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = MockupColors.Blue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(petLevel.expProgress)
                                        .background(MockupColors.Blue, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 성장 단계 미리보기 ==========
                EvolutionPreviewSection(
                    petTypeV2 = petTypeV2,
                    petLevel = petLevel,
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 펫 통계 ==========
                RetroSectionTitle("펫 통계", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        StatRow("총 걸음수", "${petTotalSteps.formatWithComma()}보", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow("총 경험치", "${petLevel.totalExp} EXP", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow("현재 성격", petTypeV2?.personality?.description ?: "-", kenneyFont)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 펫 변경 ==========
                RetroSectionTitle("펫 변경", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager.click()
                            showPetChangeDialog = true
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "다른 펫으로 변경",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = "레벨은 유지됩니다",
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(6.dp))
                                .background(MockupColors.Background, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "₩1,000",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // 펫 변경 다이얼로그
        if (showPetChangeDialog) {
            PetChangeDialog(
                currentPetType = petTypeV2?.name,
                currentPetName = petName,
                onDismiss = { showPetChangeDialog = false },
                onConfirm = { newType, newName ->
                    startPetChangePurchase(newType, newName)
                },
                hapticManager = hapticManager
            )
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MockupColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            fontFamily = kenneyFont
        )
    }
}

private fun Long.formatWithComma(): String {
    return String.format("%,d", this)
}

/**
 * 펫 변경 다이얼로그
 */
@Composable
private fun PetChangeDialog(
    currentPetType: String?,
    currentPetName: String,
    onDismiss: () -> Unit,
    onConfirm: (PetTypeV2, String) -> Unit,
    hapticManager: HapticManager
) {
    val kenneyFont = rememberKenneyFont()
    var selectedPet by remember { mutableStateOf<PetTypeV2?>(currentPetType?.let {
        try { PetTypeV2.valueOf(it) } catch (e: Exception) { null }
    }) }
    var petName by remember { mutableStateOf(currentPetName) }

    val petTypes = PetTypeV2.entries.toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .border(4.dp, MockupColors.Border, RoundedCornerShape(16.dp))
                .background(MockupColors.Background, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "펫 변경",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    fontFamily = kenneyFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 펫 선택 그리드 (2x3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in petTypes.chunked(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (pet in row) {
                                val isSelected = selectedPet == pet
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .border(
                                            3.dp,
                                            if (isSelected) MockupColors.Blue else MockupColors.Border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            if (isSelected) MockupColors.BlueLight else MockupColors.CardBackground,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            hapticManager.click()
                                            selectedPet = pet
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        PetSpriteV2WithGlow(
                                            petType = pet,
                                            stage = PetGrowthStage.BABY,
                                            animationType = PetAnimationTypeV2.IDLE,
                                            size = 48.dp,
                                            monochrome = true,
                                            showGlow = false
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pet.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 이름 입력
                OutlinedTextField(
                    value = petName,
                    onValueChange = { if (it.length <= 10) petName = it },
                    label = { Text("펫 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                            .clickable {
                                hapticManager.click()
                                onDismiss()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, MockupColors.Blue, RoundedCornerShape(8.dp))
                            .background(MockupColors.Blue, RoundedCornerShape(8.dp))
                            .clickable(enabled = selectedPet != null && petName.isNotBlank()) {
                                hapticManager.success()
                                selectedPet?.let { onConfirm(it, petName) }
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "변경 (₩1,000)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ========== Evolution Preview Section - Helper Functions ==========

/**
 * 성장 단계 도달 상태
 */
private enum class EvolutionStageStatus {
    PASSED,   // 이미 지나간 단계 (현재 레벨 > 단계 최대 레벨)
    CURRENT,  // 현재 단계 (현재 레벨이 단계 범위 내)
    LOCKED    // 아직 도달 안함 (현재 레벨 < 단계 최소 레벨)
}

/**
 * 단계 스타일 데이터
 */
private data class StageStyle(
    val borderColor: Color,
    val borderWidth: Dp,
    val backgroundColor: Color,
    val textColor: Color,
    val spriteAlpha: Float,
    val showGlow: Boolean
)

/**
 * 현재 레벨에서 해당 단계의 상태 계산
 */
private fun getStageStatus(
    stage: PetGrowthStage,
    currentLevel: Int
): EvolutionStageStatus {
    return when {
        currentLevel > stage.levelRange.last -> EvolutionStageStatus.PASSED
        currentLevel in stage.levelRange -> EvolutionStageStatus.CURRENT
        else -> EvolutionStageStatus.LOCKED
    }
}

/**
 * 상태별 스타일 가져오기 (모노크롬)
 * @param isSelected 선택된 카드인지 여부 (오버라이드 선택)
 */
private fun getStageStyle(
    status: EvolutionStageStatus,
    isSelected: Boolean = false
): StageStyle {
    return when {
        // 선택된 카드는 파란 테두리 + 글로우 (CURRENT 스타일 + 파란색)
        isSelected -> StageStyle(
            borderColor = MockupColors.Blue,
            borderWidth = 3.dp,
            backgroundColor = Color(0xFFE3F2FD),  // 연한 파란색
            textColor = MockupColors.Blue,
            spriteAlpha = 1.0f,
            showGlow = true
        )
        status == EvolutionStageStatus.PASSED -> StageStyle(
            borderColor = MockupColors.Border,        // 검정
            borderWidth = 2.dp,
            backgroundColor = MockupColors.CardBackground,  // 연한 회색
            textColor = MockupColors.TextPrimary,
            spriteAlpha = 1.0f,
            showGlow = false
        )
        status == EvolutionStageStatus.CURRENT -> StageStyle(
            borderColor = MockupColors.Border,        // 검정
            borderWidth = 3.dp,                        // 더 두껍게 (현재 강조)
            backgroundColor = MockupColors.CardBackground,
            textColor = MockupColors.TextPrimary,
            spriteAlpha = 1.0f,
            showGlow = true                            // 글로우로 현재 강조
        )
        else -> StageStyle(  // LOCKED
            borderColor = MockupColors.Border.copy(alpha = 0.3f),  // 반투명 검정
            borderWidth = 2.dp,
            backgroundColor = Color(0xFFFAFAFA),       // 아주 연한 회색
            textColor = MockupColors.TextMuted,
            spriteAlpha = 0.4f,
            showGlow = false
        )
    }
}

/**
 * 성장 단계별 레벨 범위 텍스트
 */
private fun getLevelRangeText(stage: PetGrowthStage): String {
    return when (stage) {
        PetGrowthStage.BABY -> "Lv.1-10"
        PetGrowthStage.TEEN -> "Lv.11-20"
        PetGrowthStage.ADULT -> "Lv.21+"
        else -> ""
    }
}

/**
 * 상태 아이콘 표시 (유니코드 텍스트 심볼 + PNG 아이콘)
 */
@Composable
private fun StatusIcon(status: EvolutionStageStatus) {
    // 텍스트 스타일 강제 (이모지 대신 흑백 텍스트로 렌더링)
    val textSelector = "\uFE0E"

    when (status) {
        EvolutionStageStatus.PASSED -> {
            Text(
                text = "✓$textSelector",  // 체크마크 (흑백)
                fontSize = 16.sp,
                color = MockupColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        EvolutionStageStatus.LOCKED -> {
            // IconGodotNode의 icon_lock.png 사용
            DrawableIcon(
                iconName = "icon_lock",
                size = 14.dp,
                tint = MockupColors.TextMuted
            )
        }
        EvolutionStageStatus.CURRENT -> {
            // 현재 단계는 아이콘 없음 (테두리 + 글로우로 강조)
        }
    }
}

/**
 * 개별 성장 단계 카드
 */
@Composable
private fun EvolutionStageCard(
    petType: PetTypeV2?,
    stage: PetGrowthStage,
    status: EvolutionStageStatus,
    isSelected: Boolean = false,  // 선택 상태
    onClick: () -> Unit = {},      // 클릭 핸들러
    modifier: Modifier = Modifier
) {
    val style = getStageStyle(status, isSelected)

    Box(
        modifier = modifier
            .aspectRatio(0.75f)  // 3:4 비율 (세로로 긴 카드)
            .border(style.borderWidth, style.borderColor, RoundedCornerShape(12.dp))
            .background(style.backgroundColor, RoundedCornerShape(12.dp))
            .clickable(
                enabled = status == EvolutionStageStatus.PASSED || status == EvolutionStageStatus.CURRENT,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // 펫 스프라이트
            petType?.let {
                PetSpriteV2WithGlow(
                    petType = it,
                    stage = stage,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = 80.dp,
                    monochrome = true,
                    showGlow = style.showGlow,
                    modifier = Modifier.alpha(style.spriteAlpha)
                )
            }

            // 단계 이름
            Text(
                text = stage.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = style.textColor
            )

            // 레벨 범위
            Text(
                text = getLevelRangeText(stage),
                fontSize = 12.sp,
                color = style.textColor.copy(alpha = 0.7f)
            )

            // 상태 아이콘
            StatusIcon(status)
        }

        // 선택 표시 (우상단 체크마크)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(MockupColors.Blue, CircleShape)
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 성장 단계 미리보기 섹션 (BABY-TEEN-ADULT)
 * 클릭하여 외형 오버라이드 가능
 */
@Composable
private fun EvolutionPreviewSection(
    petTypeV2: PetTypeV2?,
    petLevel: PetLevel,
    kenneyFont: FontFamily
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 현재 오버라이드된 stage 가져오기
    var displayStageOverride by remember {
        mutableStateOf(prefs.getDisplayStageOverride())
    }

    // 확인 다이얼로그 상태
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStage by remember { mutableStateOf<PetGrowthStage?>(null) }
    var isRestoreAction by remember { mutableStateOf(false) }

    Column {
        RetroSectionTitle("성장 단계", kenneyFont)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                PetGrowthStage.BABY,
                PetGrowthStage.TEEN,
                PetGrowthStage.ADULT
            ).forEach { stage ->
                val status = getStageStatus(stage, petLevel.level)
                val isSelected = displayStageOverride == stage

                EvolutionStageCard(
                    petType = petTypeV2,
                    stage = stage,
                    status = status,
                    isSelected = isSelected,
                    onClick = {
                        when (status) {
                            EvolutionStageStatus.PASSED -> {
                                // PASSED 클릭 → 확인 다이얼로그 표시
                                pendingStage = stage
                                isRestoreAction = false
                                showConfirmDialog = true
                            }
                            EvolutionStageStatus.CURRENT -> {
                                // CURRENT 클릭 → 복원 확인 다이얼로그 표시
                                pendingStage = null
                                isRestoreAction = true
                                showConfirmDialog = true
                            }
                            else -> {}  // LOCKED는 아무 동작 없음
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // "기본으로 복원" 버튼 (오버라이드가 설정된 경우만 표시)
        if (displayStageOverride != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    pendingStage = null
                    isRestoreAction = true
                    showConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAAAAAA)
                )
            ) {
                Text(
                    text = "기본으로 복원",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // 확인 다이얼로그
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = if (isRestoreAction) "외형 복원" else "외형 변경",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val message = if (isRestoreAction) {
                    "펫의 외형을 기본으로 복원하시겠습니까?"
                } else {
                    val stageName = pendingStage?.displayName ?: ""
                    "펫의 외형을 ${stageName}으로 변경하시겠습니까?"
                }
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isRestoreAction) {
                            // 기본으로 복원
                            prefs.clearDisplayStageOverride()
                            displayStageOverride = null
                        } else {
                            // 외형 변경
                            pendingStage?.let { stage ->
                                prefs.saveDisplayStageOverride(stage)
                                displayStageOverride = stage
                            }
                        }
                        updateAllWidgetsAfterOverride(context)
                        showConfirmDialog = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAAAAAA)
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 외형 오버라이드 변경 후 모든 위젯 업데이트
 */
private fun updateAllWidgetsAfterOverride(context: Context) {
    StepWidgetProvider.updateAllWidgets(context)
    PetWidget2x2Provider.updateAllWidgets(context)
    WeatherWidgetProvider.updateAllWidgets(context)
    SudokuWidgetProvider.updateAllWidgets(context)
    QuoteWidgetProvider.updateAllWidgets(context)
    TravelPhraseWidgetProvider.updateAllWidgets(context)
    TravelPhraseJapaneseWidgetProvider.updateAllWidgets(context)
    TravelPhraseChineseWidgetProvider.updateAllWidgets(context)
}
