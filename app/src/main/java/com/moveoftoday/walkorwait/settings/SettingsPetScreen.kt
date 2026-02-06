package com.moveoftoday.walkorwait.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // 펫 정보
    val petTypeV2 = preferenceManager?.getPetTypeV2()
    val petName = preferenceManager?.getPetNameV2() ?: "친구"
    val petLevel = preferenceManager?.getPetLevelV2() ?: PetLevel()
    val petTotalSteps = preferenceManager?.getPetTotalSteps() ?: 0L

    // 펫 변경 다이얼로그 상태
    var showPetChangeDialog by remember { mutableStateOf(false) }

    // BillingManager
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }

    // 펫 변경 결제 시작
    fun startPetChangePurchase(newPetType: PetTypeV2, newPetName: String) {
        preferenceManager?.savePendingPetChange(newPetType.name, newPetName)
        showPetChangeDialog = false

        val activity = context as? android.app.Activity ?: return

        scope.launch {
            kotlinx.coroutines.delay(100)

            billingManager = BillingManager(
                context = context,
                onPurchaseSuccess = { _ ->
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

                            StepWidgetProvider.updateAllWidgets(context.applicationContext)
                            Toast.makeText(context.applicationContext, "펫이 변경되었습니다!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsPetScreen", "Pet change failed: ${e.message}")
                        }
                    }
                    preferenceManager?.clearPendingPetChange()
                },
                onPurchaseFailure = { error ->
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
                        // 펫 스프라이트
                        if (petTypeV2 != null) {
                            PetSpriteV2WithGlow(
                                petType = petTypeV2,
                                stage = petLevel.stage,
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
