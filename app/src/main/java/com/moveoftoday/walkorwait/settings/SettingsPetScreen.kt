package com.moveoftoday.walkorwait.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
@OptIn(ExperimentalFoundationApi::class)
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
                    .padding(12.dp)
                    .navigationBarsPadding()
            ) {
                // ========== 현재 펫 ==========
                RetroSectionTitle("현재 펫", kenneyFont)

                // 디버그 모드: 길게 누르면 레벨업
                val isDebug = BuildConfig.DEBUG
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { },
                            onLongClick = {
                                if (isDebug) {
                                    hapticManager.success()
                                    // 레벨업 (경험치 +1000)
                                    val currentLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()
                                    val newLevel = currentLevel.addExp(1000)
                                    preferenceManager?.savePetLevelV2(newLevel)
                                    petLevel = newLevel
                                    android.widget.Toast.makeText(context, "DEBUG: +1000 EXP → Lv.${newLevel.level}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 펫 스프라이트 (스킨 + 오버라이드 반영)
                        petTypeV2?.let { petType ->
                            val effectiveStage = preferenceManager?.getEffectiveDisplayStage() ?: petLevel.stage
                            val equipmentState = preferenceManager?.getEquipmentState() ?: EquipmentState()

                            PetSpriteV2WithEquipment(
                                petType = petType,
                                stage = effectiveStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 100.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 펫 이름 (칭호 포함)
                        val displayName = remember(petName) {
                            val challengeManager = ChallengeManager.getInstance(context)
                            challengeManager.getPetNameWithTitle(petName)
                        }
                        Text(
                            text = displayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // 레벨 & 단계 (실제 성장 단계 표시)
                        val displayStage = preferenceManager?.getEffectiveDisplayStage() ?: petLevel.stage
                        Text(
                            text = "Lv.${petLevel.level} (${displayStage.displayName})",
                            fontSize = 15.sp,
                            color = MockupColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    color = MockupColors.TextPrimary,
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
                                        .background(MockupColors.TextPrimary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 성장 단계 미리보기 ==========
                EvolutionPreviewSection(
                    petTypeV2 = petTypeV2,
                    petLevel = petLevel,
                    kenneyFont = kenneyFont
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 스킨 ==========
                SkinManagementSection(
                    petTypeV2 = petTypeV2,
                    petLevel = petLevel,
                    kenneyFont = kenneyFont,
                    hapticManager = hapticManager
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 통계 ==========
                RetroSectionTitle("펫 통계", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        StatRow("총 걸음수", "${petTotalSteps.formatWithComma()}보", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow("총 경험치", "${petLevel.totalExp} EXP", kenneyFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatRow("현재 성격", petTypeV2?.personality?.description ?: "-", kenneyFont)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ========== 펫 변경 ==========
                RetroSectionTitle("펫 변경", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
                        .clickable {
                            hapticManager.click()
                            showPetChangeDialog = true
                        }
                        .padding(12.dp)
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

                Spacer(modifier = Modifier.height(16.dp))
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
                                            if (isSelected) MockupColors.TextPrimary else MockupColors.Border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            if (isSelected) MockupColors.Border.copy(alpha = 0.15f) else MockupColors.CardBackground,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            hapticManager.click()
                                            selectedPet = pet
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 스프라이트만 2배 크기로 가운데 정렬 (이름 숨김)
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PetSpriteV2WithGlow(
                                            petType = pet,
                                            stage = PetGrowthStage.BABY,
                                            animationType = PetAnimationTypeV2.IDLE,
                                            size = 72.dp,  // 2배 크기 (48 → 72, 박스 내에서 적절한 크기)
                                            monochrome = true,
                                            showGlow = false
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
                            .border(2.dp, MockupColors.TextPrimary, RoundedCornerShape(8.dp))
                            .background(MockupColors.TextPrimary, RoundedCornerShape(8.dp))
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
        // 선택된 카드는 검정 테두리 + 글로우 (CURRENT 스타일)
        isSelected -> StageStyle(
            borderColor = MockupColors.TextPrimary,
            borderWidth = 3.dp,
            backgroundColor = MockupColors.Border.copy(alpha = 0.15f),
            textColor = MockupColors.TextPrimary,
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
            .aspectRatio(0.9f)  // 더 납작한 카드
            .border(style.borderWidth, style.borderColor, RoundedCornerShape(10.dp))
            .background(style.backgroundColor, RoundedCornerShape(10.dp))
            .clickable(
                enabled = status == EvolutionStageStatus.PASSED || status == EvolutionStageStatus.CURRENT,
                onClick = onClick
            )
            .padding(8.dp)
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
                    size = 60.dp,
                    monochrome = true,
                    showGlow = style.showGlow,
                    modifier = Modifier.alpha(style.spriteAlpha)
                )
            }

            // 단계 이름 + 레벨 범위
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stage.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textColor
                )
                Text(
                    text = getLevelRangeText(stage),
                    fontSize = 11.sp,
                    color = style.textColor.copy(alpha = 0.7f)
                )
            }

            // 상태 아이콘
            StatusIcon(status)
        }

        // 선택 표시 (우상단 체크마크)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(MockupColors.TextPrimary, CircleShape)
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

// ========== 펫 스킨 시스템 UI ==========

/**
 * 펫 스킨 관리 섹션
 */
@Composable
private fun SkinManagementSection(
    petTypeV2: PetTypeV2?,
    petLevel: PetLevel,
    kenneyFont: FontFamily,
    hapticManager: HapticManager
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 펼침/접힘 상태
    var isExpanded by remember { mutableStateOf(false) }

    // 현재 스킨
    var currentSkinId by remember { mutableStateOf(prefs.getPetSkin()) }

    // 보유 스킨 목록 (기본 스킨은 항상 포함)
    var ownedSkins by remember {
        // 초기화 시 기본 스킨 보장
        prefs.initializeDefaultSkins()
        mutableStateOf(prefs.getOwnedSkins())
    }

    // 펫 정보
    val petStage = prefs.getEffectiveDisplayStage()

    // 확인 다이얼로그 상태 (보유 스킨 변경)
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingSkin by remember { mutableStateOf<PetSkin?>(null) }

    // 해금 조건 다이얼로그 상태 (잠긴 스킨 클릭)
    var showUnlockInfoDialog by remember { mutableStateOf(false) }
    var lockedSkinInfo by remember { mutableStateOf<PetSkin?>(null) }

    // 스킨 자동 해금 (기본 스킨 포함 + 조건 달성 스킨)
    LaunchedEffect(Unit) {
        // 기본 스킨 초기화 (항상 보유)
        prefs.initializeDefaultSkins()
        // 조건 달성 스킨 해금
        prefs.checkAndUnlockNewSkins()
        // 상태 갱신 (항상)
        ownedSkins = prefs.getOwnedSkins()
    }

    // 스킨 분류
    val ownedSkinsList = DefaultSkins.ALL_SKINS.filter { ownedSkins.contains(it.id) }
    val lockedSkinsList = DefaultSkins.ALL_SKINS.filter { !ownedSkins.contains(it.id) }

    Column {
        // 타이틀
        RetroSectionTitle("펫 스킨", kenneyFont)

        Spacer(modifier = Modifier.height(8.dp))

        // ========== 보유중 섹션 (항상 표시) ==========
        Text(
            text = "보유중 (${ownedSkinsList.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 보유 스킨 그리드 (3열, 고정 높이 계산)
        val ownedRows = (ownedSkinsList.size + 2) / 3
        val ownedGridHeight = (ownedRows * 120).coerceAtLeast(120).dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(ownedGridHeight),
            userScrollEnabled = false
        ) {
            items(ownedSkinsList) { skin ->
                val isSelected = currentSkinId == skin.id

                SkinItemCompact(
                    skin = skin,
                    isOwned = true,
                    isSelected = isSelected,
                    petTypeV2 = petTypeV2,
                    petStage = petStage,
                    onClick = {
                        if (currentSkinId != skin.id) {
                            hapticManager.lightClick()
                            pendingSkin = skin
                            showConfirmDialog = true
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ========== 미보유 섹션 (토글) ==========
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    hapticManager.lightClick()
                    isExpanded = !isExpanded
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "미보유 (${lockedSkinsList.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExpanded) "▼" else "▶",
                fontSize = 12.sp,
                color = MockupColors.TextMuted,
                fontFamily = kenneyFont
            )
        }

        // 미보유 스킨 그리드 (펼쳤을 때만)
        if (isExpanded) {
            val lockedRows = (lockedSkinsList.size + 2) / 3
            val lockedGridHeight = (lockedRows * 120).coerceAtLeast(120).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lockedGridHeight),
                userScrollEnabled = false
            ) {
                items(lockedSkinsList) { skin ->
                    SkinItemCompact(
                        skin = skin,
                        isOwned = false,
                        isSelected = false,
                        petTypeV2 = petTypeV2,
                        petStage = petStage,
                        onClick = {
                            hapticManager.lightClick()
                            lockedSkinInfo = skin
                            showUnlockInfoDialog = true
                        }
                    )
                }
            }
        }
    }

    // 스킨 변경 확인 다이얼로그
    if (showConfirmDialog && pendingSkin != null) {
        val skinToChange = pendingSkin!!
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingSkin = null
            },
            title = {
                Text(
                    text = "스킨 변경",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // 스킨 미리보기
                    if (petTypeV2 != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val equipmentState = EquipmentState(
                                headId = null,
                                backgroundId = null,
                                colorId = skinToChange.id
                            )
                            PetSpriteV2WithEquipment(
                                petType = petTypeV2,
                                stage = petStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 70.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = skinToChange.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 획득 조건 표시 (기본 스킨은 "기본 제공", 나머지는 조건 표시)
                    Text(
                        text = getOwnedSkinConditionText(skinToChange.unlockCondition),
                        fontSize = 13.sp,
                        color = MockupColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "이 스킨으로 변경하시겠습니까?",
                        fontSize = 14.sp,
                        color = MockupColors.TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingSkin?.let { skin ->
                            android.util.Log.d("SkinManagement", "🎨 Saving skin: ${skin.id} (${skin.displayName})")
                            prefs.savePetSkin(skin.id)
                            currentSkinId = skin.id
                            android.util.Log.d("SkinManagement", "✅ Skin saved, currentSkinId now: $currentSkinId")
                            updateAllWidgetsAfterOverride(context)
                            android.util.Log.d("SkinManagement", "🔄 Widgets updated")
                        }
                        showConfirmDialog = false
                        pendingSkin = null
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        pendingSkin = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAAAAAA)
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 해금 조건 안내 다이얼로그 (잠긴 스킨 클릭 시)
    val currentLockedSkinInfo = lockedSkinInfo
    if (showUnlockInfoDialog && currentLockedSkinInfo != null) {
        AlertDialog(
            onDismissRequest = {
                showUnlockInfoDialog = false
                lockedSkinInfo = null
            },
            title = {
                Text(
                    text = currentLockedSkinInfo.displayName,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // 스킨 미리보기
                    if (petTypeV2 != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val equipmentState = EquipmentState(
                                headId = null,
                                backgroundId = null,
                                colorId = currentLockedSkinInfo.id
                            )

                            PetSpriteV2WithEquipment(
                                petType = petTypeV2,
                                stage = petStage,
                                animationType = PetAnimationTypeV2.IDLE,
                                equipmentState = equipmentState,
                                size = 80.dp,
                                monochrome = true,
                                showGlow = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 해금 조건
                    Text(
                        text = "해금 조건",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getUnlockConditionDetailText(currentLockedSkinInfo.unlockCondition, prefs),
                        fontSize = 14.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockInfoDialog = false
                        lockedSkinInfo = null
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}

/**
 * 컴팩트한 스킨 아이템 (펫 관리 화면용) - ChallengeBox 스타일
 */
@Composable
private fun SkinItemCompact(
    skin: PetSkin,
    isOwned: Boolean,
    isSelected: Boolean,
    petTypeV2: PetTypeV2?,
    petStage: PetGrowthStage,
    onClick: () -> Unit
) {
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
        // 펫 미리보기 (중앙 상단)
        if (petTypeV2 != null && isOwned) {
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
        } else if (!isOwned) {
            // 잠금 아이콘
            Text(
                text = "▣",
                fontSize = 40.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-12).dp)
            )
        }

        // 스킨 이름 (하단 고정)
        Text(
            text = skin.displayName,
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

/**
 * 해금 조건을 텍스트로 변환 (간단한 버전)
 */
private fun getUnlockConditionText(condition: UnlockCondition): String {
    return when (condition) {
        is UnlockCondition.Default -> "기본"
        is UnlockCondition.Steps -> "${condition.totalSteps}보"
        is UnlockCondition.Streak -> "${condition.days}일 연속"
        is UnlockCondition.Level -> "Lv.${condition.level}"
        is UnlockCondition.Event -> condition.eventId
        is UnlockCondition.ChallengeCount -> "${condition.category} ${condition.count}회"
    }
}

/**
 * 보유 스킨의 획득 조건 표시 (스킨 변경 다이얼로그용)
 */
private fun getOwnedSkinConditionText(condition: UnlockCondition): String {
    return when (condition) {
        is UnlockCondition.Default -> "기본 제공 스킨"
        is UnlockCondition.Steps -> "총 ${condition.totalSteps}보 달성으로 획득"
        is UnlockCondition.Streak -> "${condition.days}일 연속 달성으로 획득"
        is UnlockCondition.Level -> "레벨 ${condition.level} 달성으로 획득"
        is UnlockCondition.Event -> "이벤트 '${condition.eventId}'로 획득"
        is UnlockCondition.ChallengeCount -> "${condition.category} ${condition.count}회 완료로 획득"
    }
}

/**
 * 해금 조건 상세 텍스트 (현재 진행도 포함)
 */
private fun getUnlockConditionDetailText(condition: UnlockCondition, prefs: PreferenceManager): String {
    return when (condition) {
        is UnlockCondition.Default -> "기본 제공되는 스킨입니다."
        is UnlockCondition.Steps -> {
            val current = prefs.getPetTotalSteps()
            val required = condition.totalSteps
            "총 ${required}보 걸으면 해금됩니다.\n현재: ${current}보 / ${required}보"
        }
        is UnlockCondition.Streak -> {
            val current = prefs.getStreak()
            val required = condition.days
            "${required}일 연속 목표 달성 시 해금됩니다.\n현재: ${current}일 연속"
        }
        is UnlockCondition.Level -> {
            val current = prefs.getPetLevelV2()?.level ?: 1
            val required = condition.level
            "펫 레벨 ${required} 달성 시 해금됩니다.\n현재 레벨: Lv.${current}"
        }
        is UnlockCondition.Event -> "특별 이벤트 기간에 해금됩니다.\n이벤트: ${condition.eventId}"
        is UnlockCondition.ChallengeCount -> {
            val current = prefs.getChallengeCountByCategory(condition.category)
            val required = condition.count
            "${condition.category} 챌린지 ${required}회 완료 시 해금됩니다.\n현재: ${current}회 / ${required}회"
        }
    }
}
