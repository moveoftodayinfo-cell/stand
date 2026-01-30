package com.moveoftoday.walkorwait.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager

/**
 * V2 Pet Selection Screen
 * 6종의 새로운 펫 중 하나를 선택하는 화면
 */
@Composable
fun PetSelectionScreenV2(
    onPetSelected: (PetTypeV2) -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    var selectedPet by remember { mutableStateOf<PetTypeV2?>(null) }
    val kenneyFont = rememberKenneyFont()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = "rebon",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = kenneyFont,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "함께할 친구를 선택해주세요",
            fontSize = 16.sp,
            color = MockupColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Pet grid (2x3)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(PetTypeV2.entries.toList()) { petType ->
                PetSelectionCardV2(
                    petType = petType,
                    isSelected = selectedPet == petType,
                    onClick = {
                        hapticManager?.click()
                        selectedPet = petType
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm button
        Button(
            onClick = {
                selectedPet?.let {
                    hapticManager?.success()
                    onPetSelected(it)
                }
            },
            enabled = selectedPet != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MockupColors.TextPrimary,
                disabledContainerColor = Color.LightGray
            )
        ) {
            Text(
                text = if (selectedPet != null) "${selectedPet!!.displayName} 선택하기" else "친구를 선택해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Pet selection card for V2
 */
@Composable
fun PetSelectionCardV2(
    petType: PetTypeV2,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MockupColors.TextPrimary else Color.LightGray
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet sprite preview (Baby stage)
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            PetSpriteV2(
                petType = petType,
                stage = PetGrowthStage.BABY,
                animationType = PetAnimationTypeV2.IDLE,
                size = 64.dp,
                monochrome = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pet name
        Text(
            text = petType.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Personality description
        Text(
            text = petType.personality.description,
            fontSize = 12.sp,
            color = MockupColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * V2 Pet Naming Screen
 * 선택한 펫의 이름을 지정하는 화면
 */
@Composable
fun PetNamingScreenV2(
    petType: PetTypeV2,
    onComplete: (String) -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    var petName by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val kenneyFont = rememberKenneyFont()

    // Default names for each pet type
    val defaultName = petType.displayName

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = "rebon",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = kenneyFont,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Egg with ribbon preview
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Show egg (will hatch to this pet type)
            EggSprite(
                animationType = PetAnimationTypeV2.IDLE,
                size = 120.dp,
                monochrome = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info text
        Text(
            text = "이 알에서 ${petType.displayName}(이)가 태어날 거예요!",
            fontSize = 16.sp,
            color = MockupColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Name input
        Text(
            text = "이름을 지어주세요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = petName,
            onValueChange = { if (it.length <= 10) petName = it },
            placeholder = { Text(defaultName, color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (petName.isNotBlank()) {
                        hapticManager?.success()
                        onComplete(petName)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MockupColors.TextPrimary,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${petName.length}/10",
            fontSize = 12.sp,
            color = MockupColors.TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Confirm button
        Button(
            onClick = {
                val finalName = petName.ifBlank { defaultName }
                hapticManager?.success()
                onComplete(finalName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MockupColors.TextPrimary
            )
        ) {
            Text(
                text = "시작하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * V2 Pet Hatching Screen
 * 알이 부화하는 애니메이션 화면
 */
@Composable
fun PetHatchingScreenV2(
    petType: PetTypeV2,
    petName: String,
    onComplete: () -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    var hatchingStage by remember { mutableStateOf(0) }
    val kenneyFont = rememberKenneyFont()

    // Hatching animation sequence
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        hatchingStage = 1  // Wobble
        hapticManager?.click()

        kotlinx.coroutines.delay(1500)
        hatchingStage = 2  // Crack
        hapticManager?.click()

        kotlinx.coroutines.delay(1500)
        hatchingStage = 3  // Hatch
        hapticManager?.success()

        kotlinx.coroutines.delay(2000)
        hatchingStage = 4  // Show baby
        hapticManager?.success()

        kotlinx.coroutines.delay(2000)
        onComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hatching animation area
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (hatchingStage) {
                0, 1 -> EggSprite(
                    animationType = if (hatchingStage == 1) PetAnimationTypeV2.WOBBLE else PetAnimationTypeV2.IDLE,
                    size = 140.dp,
                    monochrome = true
                )
                2 -> EggSprite(
                    animationType = PetAnimationTypeV2.CRACK,
                    size = 140.dp,
                    monochrome = true
                )
                3 -> EggSprite(
                    animationType = PetAnimationTypeV2.HATCH,
                    size = 140.dp,
                    monochrome = true
                )
                4 -> PetSpriteV2WithGlow(
                    petType = petType,
                    stage = PetGrowthStage.BABY,
                    animationType = PetAnimationTypeV2.BARK,
                    size = 140.dp,
                    monochrome = true
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Status text
        Text(
            text = when (hatchingStage) {
                0 -> "알이 움직이기 시작해요..."
                1 -> "뭔가 일어나고 있어요!"
                2 -> "금이 가기 시작했어요!"
                3 -> "곧 만나요!"
                4 -> "${petName}(이)가 태어났어요!"
                else -> ""
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = kenneyFont,
            color = MockupColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        if (hatchingStage == 4) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = PetDialoguesV2.getHatchMessage(petType.personality, petName),
                fontSize = 16.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * V2 Pet Setup Flow
 * 전체 펫 설정 플로우를 관리하는 화면
 */
@Composable
fun PetSetupFlowV2(
    onSetupComplete: () -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    var currentStep by remember { mutableStateOf(0) }
    var selectedPetType by remember { mutableStateOf<PetTypeV2?>(null) }
    var petName by remember { mutableStateOf("") }

    when (currentStep) {
        // Step 0: Pet selection
        0 -> PetSelectionScreenV2(
            onPetSelected = { petType ->
                selectedPetType = petType
                currentStep = 1
            },
            hapticManager = hapticManager,
            modifier = modifier
        )

        // Step 1: Name input
        1 -> PetNamingScreenV2(
            petType = selectedPetType!!,
            onComplete = { name ->
                petName = name

                // Save V2 pet data
                preferenceManager.savePetTypeV2(selectedPetType!!)
                preferenceManager.savePetNameV2(name)
                preferenceManager.savePetLevelV2(PetLevel(level = 0, currentExp = 0, totalExp = 0))  // Start at Egg stage
                preferenceManager.savePetHappinessV2(100)

                currentStep = 2
            },
            hapticManager = hapticManager,
            modifier = modifier
        )

        // Step 2: Hatching animation (optional - can skip to show egg first)
        2 -> {
            // For now, skip hatching animation and start with egg
            // User will see hatching when they reach enough steps
            LaunchedEffect(Unit) {
                onSetupComplete()
            }
        }
    }
}
