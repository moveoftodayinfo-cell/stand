package com.moveoftoday.walkorwait.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager
import kotlinx.coroutines.delay

/**
 * Pet-guided Goal Setting Screen matching mockup layout
 */
@Composable
fun PetGoalSettingScreen(
    petType: PetType,
    petName: String,
    currentGoal: Int,
    onConfirm: (Int) -> Unit,
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Goal unit (steps or km)
    val currentUnit = preferenceManager?.getGoalUnit() ?: "steps"
    var selectedUnit by remember { mutableStateOf(currentUnit) }

    // Goal value
    val validGoal = if (currentGoal < 1000) 8000 else currentGoal
    val initialValue = if (selectedUnit == "km") {
        String.format("%.1f", validGoal / 1300.0)
    } else {
        validGoal.toString()
    }
    var inputGoal by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf("") }

    // Pet speech based on personality
    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "매일 걸을 목표를 정해."
        PetPersonality.CUTE -> "목표 정하자! 간바!"
        PetPersonality.TSUNDERE -> "목표... 알아서 정해."
        PetPersonality.DIALECT -> "매일 걸을 목표 정하이소~"
        PetPersonality.TIMID -> "저, 목표 정해주세요..."
        PetPersonality.POSITIVE -> "목표 정하자! 신난다!"
    }

    // Validation
    val isValid = if (selectedUnit == "km") {
        val km = inputGoal.toDoubleOrNull()
        km != null && km >= 0.8 && km <= 40.0
    } else {
        val steps = inputGoal.toIntOrNull()
        steps != null && steps >= 1000 && steps <= 50000
    }

    // Auto-focus
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Pet area with speech
        PetArea(
            petType = petType,
            isWalking = false,
            speechText = speechText,
            happinessLevel = 3,
            modifier = Modifier.height(180.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Goal setting card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MockupColors.CardBackground),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Unit toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Steps button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedUnit == "steps") MockupColors.Border
                                else Color.White
                            )
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .clickable {
                                hapticManager?.lightClick()
                                if (selectedUnit != "steps") {
                                    val kmValue = inputGoal.toDoubleOrNull() ?: 0.0
                                    val stepsValue = (kmValue * 1300).toInt()
                                    inputGoal = stepsValue.toString()
                                    selectedUnit = "steps"
                                    errorMessage = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "걸음 수",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedUnit == "steps") Color.White else MockupColors.TextPrimary
                        )
                    }

                    // Km button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedUnit == "km") MockupColors.Border
                                else Color.White
                            )
                            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
                            .clickable {
                                hapticManager?.lightClick()
                                if (selectedUnit != "km") {
                                    val stepsValue = inputGoal.toIntOrNull() ?: 0
                                    val kmValue = stepsValue / 1300.0
                                    inputGoal = String.format("%.1f", kmValue)
                                    selectedUnit = "km"
                                    errorMessage = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "거리 (km)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedUnit == "km") Color.White else MockupColors.TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Goal input field
                BasicTextField(
                    value = inputGoal,
                    onValueChange = { newValue ->
                        if (selectedUnit == "km") {
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                inputGoal = newValue
                                val goal = newValue.toDoubleOrNull()
                                errorMessage = when {
                                    goal == null && newValue.isNotEmpty() && newValue != "." -> "숫자만 입력하세요"
                                    goal != null && goal < 0.8 -> "최소 0.8km"
                                    goal != null && goal > 40.0 -> "최대 40km"
                                    else -> ""
                                }
                            }
                        } else {
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                inputGoal = newValue
                                val goal = newValue.toIntOrNull()
                                errorMessage = when {
                                    goal == null && newValue.isNotEmpty() -> "숫자만 입력하세요"
                                    goal != null && goal < 1000 -> "최소 1,000보"
                                    goal != null && goal > 50000 -> "최대 50,000보"
                                    else -> ""
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 15.dp)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedUnit == "km") KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isValid) {
                                val goalInSteps = if (selectedUnit == "km") {
                                    (inputGoal.toDoubleOrNull()?.times(1300))?.toInt() ?: 8000
                                } else {
                                    inputGoal.toIntOrNull() ?: 8000
                                }
                                onConfirm(goalInSteps)
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (inputGoal.isEmpty()) {
                                    Text(
                                        text = if (selectedUnit == "km") "6.2" else "8000",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextMuted
                                    )
                                }
                                innerTextField()
                            }
                            Text(
                                text = if (selectedUnit == "km") "km" else "걸음",
                                fontSize = 18.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                    }
                )

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color(0xFFE57373)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info text
                Text(
                    text = if (selectedUnit == "km") "0.8km ~ 40km 가능" else "1,000보 ~ 50,000보 가능",
                    fontSize = 14.sp,
                    color = MockupColors.TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Recommendation card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MockupColors.CardBackground)
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PixelIcon(iconName = "icon_boots", size = 24.dp)
            Column {
                Text(
                    text = "추천: 8,000보 (약 6km)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = "처음이라면 이 목표로 시작해보세요",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Confirm button
        MockupButton(
            text = "확인",
            onClick = {
                focusManager.clearFocus()
                hapticManager?.success()
                preferenceManager?.saveGoalUnit(selectedUnit)
                val goalInSteps = if (selectedUnit == "km") {
                    (inputGoal.toDoubleOrNull()?.times(1300))?.toInt() ?: 8000
                } else {
                    inputGoal.toIntOrNull() ?: 8000
                }
                onConfirm(goalInSteps)
            },
            enabled = isValid
        )
    }
}
