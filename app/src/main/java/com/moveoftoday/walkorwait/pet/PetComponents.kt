package com.moveoftoday.walkorwait.pet

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException

// Kenney Pixel Font
@Composable
fun rememberKenneyFont(): FontFamily {
    val context = LocalContext.current
    return remember {
        try {
            val typeface = Typeface.createFromAsset(context.assets, "fonts/Kenney Pixel.ttf")
            FontFamily(typeface)
        } catch (e: Exception) {
            FontFamily.Default
        }
    }
}

// LCD Green color for Game Boy feel
object LCDColors {
    val Background = Color(0xFF9BBC0F) // Classic Game Boy green
    val Dark = Color(0xFF0F380F) // Dark green
    val Light = Color(0xFF8BAC0F) // Light green
    val Lightest = Color(0xFFC4CFA1) // Lightest
}

// Colors matching mockup - 3 Color System (Black/White, Red, Blue)
object MockupColors {
    // Base colors (Black/White)
    val Background = Color.White
    val CardBackground = Color(0xFFF5F5F5)
    val Border = Color(0xFF333333)
    val TextPrimary = Color(0xFF333333)
    val TextSecondary = Color(0xFF555555)
    val TextMuted = Color(0xFF888888)
    val ProgressFill = Color(0xFF333333)

    // Accent colors
    val Blue = Color(0xFF1976D2)          // Selection, check, confirm, progress
    val BlueLight = Color(0xFFE3F2FD)     // Blue background
    val Red = Color(0xFFE53935)           // Warning, error, danger
    val RedLight = Color(0xFFFFEBEE)      // Red background

    // State backgrounds (using Blue for positive states)
    val AchievedBackground = Color(0xFFE3F2FD)  // Blue light
    val AchievedCard = Color(0xFFBBDEFB)        // Blue lighter
    val SadBackground = Color(0xFFE0E0E0)
    val SadCard = Color(0xFFD0D0D0)
}

/**
 * Load icon from assets
 */
@Composable
fun rememberAssetIcon(assetPath: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(assetPath) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: IOException) {
            null
        }
    }
}

/**
 * Pixel icon component with grayscale filter (from assets)
 * tint 파라미터가 있으면 해당 색상으로 tint, 없으면 grayscale
 */
@Composable
fun PixelIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    alpha: Float = 1f,
    tint: Color? = null
) {
    val icon = rememberAssetIcon("icons/$iconName.png")

    icon?.let {
        Image(
            bitmap = it,
            contentDescription = iconName,
            modifier = modifier.size(size),
            colorFilter = if (tint != null) {
                ColorFilter.tint(tint)
            } else {
                ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                )
            },
            alpha = alpha
        )
    }
}

/**
 * Drawable icon component (from res/drawable/IconGodotNode/white)
 * 흰색 아이콘을 어둡게 tint 처리
 */
@Composable
fun DrawableIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = MockupColors.Border
) {
    val context = LocalContext.current
    val icon = remember(iconName) {
        try {
            val inputStream = context.assets.open("IconGodotNode/white/$iconName.png")
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: IOException) {
            // drawable 폴더에서 시도
            try {
                val resId = context.resources.getIdentifier(
                    iconName,
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
                } else null
            } catch (e2: Exception) {
                null
            }
        }
    }

    icon?.let {
        Image(
            bitmap = it,
            contentDescription = iconName,
            modifier = modifier.size(size),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

/**
 * Speech bubble shape with downward arrow
 */
val SpeechBubbleShape: Shape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = 12f
    val arrowWidth = 16f
    val arrowHeight = 8f
    val bodyHeight = height - arrowHeight

    moveTo(cornerRadius, 0f)
    lineTo(width - cornerRadius, 0f)
    quadraticTo(width, 0f, width, cornerRadius)
    lineTo(width, bodyHeight - cornerRadius)
    quadraticTo(width, bodyHeight, width - cornerRadius, bodyHeight)
    lineTo(width / 2 + arrowWidth / 2, bodyHeight)
    lineTo(width / 2, height)
    lineTo(width / 2 - arrowWidth / 2, bodyHeight)
    lineTo(cornerRadius, bodyHeight)
    quadraticTo(0f, bodyHeight, 0f, bodyHeight - cornerRadius)
    lineTo(0f, cornerRadius)
    quadraticTo(0f, 0f, cornerRadius, 0f)
    close()
}

/**
 * Speech bubble matching mockup style
 */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Box(
        modifier = modifier
            .shadow(4.dp, SpeechBubbleShape)
            .clip(SpeechBubbleShape)
            .background(Color.White)
            .border(2.dp, MockupColors.Border, SpeechBubbleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .padding(bottom = 6.dp)
    ) {
        Text(
            text = text,
            color = MockupColors.TextPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pet area matching mockup (180dp height, gray bg, border)
 * Layout: Speech bubble at top -> Pet sprite in center -> Hearts at bottom
 */
@Composable
fun PetArea(
    petType: PetType,
    isWalking: Boolean,
    speechText: String,
    happinessLevel: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MockupColors.CardBackground,
    showHearts: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Speech bubble at top
            if (speechText.isNotEmpty()) {
                SpeechBubble(
                    text = speechText,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Pet sprite in center (scaled 2.5x = 120dp)
            PetSprite(
                petType = petType,
                isWalking = isWalking,
                size = 100.dp,
                monochrome = true
            )

            // Hearts at bottom (only show if showHearts is true and happinessLevel > 0)
            if (showHearts && happinessLevel > 0) {
                Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        val isFilled = index < happinessLevel
                        PixelIcon(
                            iconName = "icon_heart",
                            size = 16.dp,
                            alpha = if (isFilled) 1f else 0.3f
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Stats card matching mockup
 */
@Composable
fun StatsCard(
    stepCount: Int,
    goalSteps: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MockupColors.CardBackground,
    isAchieved: Boolean = false
) {
    val progress = (stepCount.toFloat() / goalSteps).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(3.dp, MockupColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large step count
            Text(
                text = "%,d".format(stepCount),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            // Label with boot icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PixelIcon(iconName = "icon_boots", size = 16.dp)
                if (isAchieved) {
                    PixelIcon(iconName = "icon_star", size = 16.dp)
                    Text(
                        text = "달성!",
                        fontSize = 18.sp,
                        color = MockupColors.TextSecondary
                    )
                } else {
                    Text(
                        text = "걸음",
                        fontSize = 18.sp,
                        color = MockupColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0E0E0))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(MockupColors.ProgressFill)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isAchieved) "달성 완료!" else "목표 %,d".format(goalSteps),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )
                Text(
                    text = "${progressPercent}%",
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )
            }
        }
    }
}

/**
 * Pet selection card matching mockup - square card with centered pet
 */
@Composable
fun PetSelectionCard(
    petType: PetType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f), // Square card
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0E0E0) else MockupColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            color = MockupColors.Border
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PetSprite(
                petType = petType,
                isWalking = false,
                size = 56.dp,
                monochrome = true
            )
        }
    }
}

/**
 * Streak badge matching mockup
 */
@Composable
fun StreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier,
    inactive: Boolean = false
) {
    Row(
        modifier = modifier
            .background(
                if (inactive) Color(0xFF666666) else MockupColors.Border,
                RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PixelIcon(
            iconName = "icon_thunder",
            size = 14.dp,
            alpha = if (inactive) 0.5f else 1f
        )
        Text(
            text = "x$streakCount",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Talk input area matching mockup
 */
@Composable
fun TalkInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    petName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Input field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .border(3.dp, MockupColors.Border, RoundedCornerShape(15.dp))
                .padding(horizontal = 15.dp, vertical = 12.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MockupColors.TextPrimary
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "${petName}에게 말하기...",
                            fontSize = 16.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Send button
        Button(
            onClick = onSend,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "→",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * My message bubble matching mockup
 */
@Composable
fun MyMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground)
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "나" label
        Box(
            modifier = Modifier
                .background(MockupColors.Border, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "나",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Message text
        Text(
            text = "\"$text\"",
            fontSize = 14.sp,
            color = MockupColors.TextSecondary
        )
    }
}

/**
 * Standard button matching mockup
 */
@Composable
fun MockupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MockupColors.Border,
            disabledContainerColor = Color(0xFFCCCCCC)
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Navigation dots for tutorial steps
 * @param currentStep 현재 단계 (0-indexed within dot range)
 * @param totalSteps 전체 닷 개수
 */
@Composable
fun TutorialNavigationDots(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isActive) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MockupColors.Border else Color(0xFFD0D0D0)
                    )
            )
        }
    }
}

/**
 * Common tutorial step layout with fixed elements
 * - Title "Stand" (fixed)
 * - Display area with stripe background (fixed)
 * - Instruction text (fixed)
 * - Middle content (변경 가능)
 * - Navigation dots (optional)
 * - Action button (fixed)
 */
@Composable
fun TutorialStepLayout(
    petType: PetType,
    speechText: String,
    instructionText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean = true,
    showNavigationDots: Boolean = false,
    currentDotStep: Int = 0,
    totalDotSteps: Int = 12,
    isWalking: Boolean = false,
    middleContent: @Composable ColumnScope.() -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - 고정
        Text(
            text = "Stand",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display area - 고정 (스트라이프 배경)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = Color(0xFFF0F0F0)
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = stripeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                        )
                        y += stripeHeightPx * 2
                    }
                }
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SpeechBubble(text = speechText, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = isWalking,
                    size = displayPetSize,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = !isWalking
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction text - 고정
        Text(
            text = instructionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Middle content - 변경 가능
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = middleContent
        )

        // Navigation dots (optional)
        if (showNavigationDots) {
            TutorialNavigationDots(
                currentStep = currentDotStep,
                totalSteps = totalDotSteps
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action button - 고정
        MockupButton(
            text = buttonText,
            onClick = onButtonClick,
            enabled = buttonEnabled
        )
    }
}
