package com.moveoftoday.walkorwait.pet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Load sprite sheet from assets folder
 */
private fun loadSpriteSheet(context: Context, assetPath: String): Bitmap? {
    return try {
        context.assets.open(assetPath).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * Apply grayscale filter to bitmap
 */
private fun applyGrayscale(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(grayscaleBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f) // 0 = grayscale
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return grayscaleBitmap
}

/**
 * Pet sprite animation composable - 랜덤 애니메이션 지원
 */
@Composable
fun PetSprite(
    petType: PetType,
    isWalking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true,
    frameDurationMs: Int = 100,
    enableRandomAnimation: Boolean = false, // 랜덤 애니메이션 활성화
    forcedAnimationType: PetAnimationType? = null // 외부에서 강제 지정
) {
    val context = LocalContext.current

    // 랜덤 애니메이션 상태
    var currentAnimationType by remember { mutableStateOf(PetAnimationType.IDLE) }

    // 랜덤 애니메이션 선택 (idle 상태에서만, forcedAnimationType이 없을 때만)
    LaunchedEffect(isWalking, enableRandomAnimation, forcedAnimationType) {
        if (forcedAnimationType != null) {
            currentAnimationType = forcedAnimationType
        } else if (!isWalking && enableRandomAnimation) {
            while (true) {
                delay(3000 + (Math.random() * 4000).toLong()) // 3~7초마다
                val newAnim = petType.getRandomIdleAnimation()
                currentAnimationType = newAnim
                // 특수 애니메이션 후 idle로 복귀
                if (newAnim != PetAnimationType.IDLE) {
                    val animData = petType.animations[newAnim]
                    val animDuration = (animData?.frames ?: 4) * frameDurationMs.toLong()
                    delay(animDuration)
                    currentAnimationType = PetAnimationType.IDLE
                }
            }
        } else if (isWalking) {
            currentAnimationType = PetAnimationType.WALK
        } else {
            currentAnimationType = PetAnimationType.IDLE
        }
    }

    // 현재 애니메이션 데이터
    val animationType = forcedAnimationType ?: (if (isWalking) PetAnimationType.WALK else currentAnimationType)
    val animationData = petType.animations[animationType]
        ?: petType.animations[PetAnimationType.IDLE]!!

    val frameCount = animationData.frames
    val assetPath = animationData.assetPath

    // Load sprite sheet
    val spriteSheet = remember(assetPath, monochrome) {
        loadSpriteSheet(context, assetPath)?.let { bitmap ->
            if (monochrome) applyGrayscale(bitmap) else bitmap
        }
    }

    // Animation state
    var currentFrame by remember { mutableIntStateOf(0) }

    // Frame animation - 애니메이션 타입 변경시 프레임 리셋
    LaunchedEffect(animationType, frameCount) {
        currentFrame = 0
        while (true) {
            delay(frameDurationMs.toLong())
            currentFrame = (currentFrame + 1) % frameCount
        }
    }

    // Draw sprite
    spriteSheet?.let { sheet ->
        // 동적으로 프레임 크기 계산 (스프라이트 시트 너비 / 프레임 수)
        val frameWidth = sheet.width / frameCount
        val frameHeight = sheet.height

        // 비율 유지하면서 크기 조정
        val aspectRatio = frameWidth.toFloat() / frameHeight.toFloat()

        Canvas(modifier = modifier.size(size)) {
            val srcLeft = currentFrame * frameWidth
            val srcTop = 0

            // 비율 유지: 정사각형 캔버스 내에서 중앙 정렬
            val canvasSize = this.size.width.toInt()
            val dstWidth: Int
            val dstHeight: Int
            val dstLeft: Int
            val dstTop: Int

            if (aspectRatio >= 1f) {
                // 가로가 더 긴 경우
                dstWidth = canvasSize
                dstHeight = (canvasSize / aspectRatio).toInt()
                dstLeft = 0
                dstTop = (canvasSize - dstHeight) / 2 // 중앙 정렬
            } else {
                // 세로가 더 긴 경우
                dstHeight = canvasSize
                dstWidth = (canvasSize * aspectRatio).toInt()
                dstLeft = (canvasSize - dstWidth) / 2
                dstTop = 0
            }

            drawIntoCanvas { canvas ->
                val srcRect = android.graphics.Rect(
                    srcLeft, srcTop,
                    srcLeft + frameWidth, srcTop + frameHeight
                )
                val dstRect = android.graphics.Rect(
                    dstLeft, dstTop,
                    dstLeft + dstWidth, dstTop + dstHeight
                )

                val paint = Paint().apply {
                    isFilterBitmap = false // Keep pixel art sharp
                    isAntiAlias = false
                }

                canvas.nativeCanvas.drawBitmap(sheet, srcRect, dstRect, paint)
            }
        }
    }
}

/**
 * Pet sprite with glow - 글로우 레이어와 메인 스프라이트가 동기화된 애니메이션
 */
@Composable
fun PetSpriteWithSyncedGlow(
    petType: PetType,
    isWalking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true,
    frameDurationMs: Int = 100,
    enableRandomAnimation: Boolean = false
) {
    // 공유 애니메이션 상태
    var currentAnimationType by remember { mutableStateOf(PetAnimationType.IDLE) }

    // 랜덤 애니메이션 선택
    LaunchedEffect(isWalking, enableRandomAnimation) {
        if (!isWalking && enableRandomAnimation) {
            while (true) {
                delay(3000 + (Math.random() * 4000).toLong())
                val newAnim = petType.getRandomIdleAnimation()
                currentAnimationType = newAnim
                if (newAnim != PetAnimationType.IDLE) {
                    val animData = petType.animations[newAnim]
                    val animDuration = (animData?.frames ?: 4) * frameDurationMs.toLong()
                    delay(animDuration)
                    currentAnimationType = PetAnimationType.IDLE
                }
            }
        } else if (isWalking) {
            currentAnimationType = PetAnimationType.WALK
        } else {
            currentAnimationType = PetAnimationType.IDLE
        }
    }

    val animationType = currentAnimationType

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow layer
        Box(
            modifier = Modifier
                .offset(y = 4.dp)
                .blur(8.dp)
        ) {
            PetSprite(
                petType = petType,
                isWalking = isWalking,
                size = size,
                monochrome = monochrome,
                frameDurationMs = frameDurationMs,
                forcedAnimationType = animationType
            )
        }
        // Main sprite
        PetSprite(
            petType = petType,
            isWalking = isWalking,
            size = size,
            monochrome = monochrome,
            frameDurationMs = frameDurationMs,
            forcedAnimationType = animationType
        )
    }
}

/**
 * Pet sprite with glow effect
 */
@Composable
fun PetSpriteWithGlow(
    petType: PetType,
    isWalking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true,
    showGlow: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow effect (shadow layer)
        if (showGlow) {
            PetSprite(
                petType = petType,
                isWalking = isWalking,
                size = size + 8.dp,
                monochrome = true,
                modifier = Modifier
            )
        }

        // Main sprite
        PetSprite(
            petType = petType,
            isWalking = isWalking,
            size = size,
            monochrome = monochrome
        )
    }
}

/**
 * Animated pet sprite for selection screen (always idle, smaller size)
 */
@Composable
fun PetSelectionSprite(
    petType: PetType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "petScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        PetSprite(
            petType = petType,
            isWalking = false,
            size = (size.value * scale).dp,
            monochrome = true,
            frameDurationMs = 200 // Slower animation for selection
        )
    }
}

// ===== Preview =====

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun PreviewAllPets() {
    Column(
        modifier = Modifier.background(Color(0xFFEEEEEE)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Dogs
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PetSprite(petType = PetType.DOG1, isWalking = false, size = 120.dp)
            PetSprite(petType = PetType.DOG2, isWalking = false, size = 120.dp)
        }
        // Row 2: Cats
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PetSprite(petType = PetType.CAT1, isWalking = false, size = 120.dp)
            PetSprite(petType = PetType.CAT2, isWalking = false, size = 120.dp)
        }
        // Row 3: Rat & Bird
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PetSprite(petType = PetType.RAT, isWalking = false, size = 120.dp)
            PetSprite(petType = PetType.BIRD, isWalking = false, size = 120.dp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun PreviewPetSizes() {
    Row(
        modifier = Modifier.background(Color(0xFFEEEEEE)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 강아지 240dp (1.5배)
        PetSprite(petType = PetType.DOG1, isWalking = false, size = 240.dp)
        // 쥐 160dp (기본)
        PetSprite(petType = PetType.RAT, isWalking = false, size = 160.dp)
    }
}
