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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.IOException

/**
 * 개별 프레임 로더 (폴더 기반)
 */
object FrameLoader {
    private val frameCache = mutableMapOf<String, List<Bitmap>>()

    /**
     * 폴더에서 프레임들 로드
     * @param context Context
     * @param folderPath 폴더 경로 (예: "pets/shiba/baby/idle/")
     * @param monochrome 모노크롬 적용 여부
     * @param fallbackPath 프레임이 없을 때 대체 경로
     */
    fun loadFrames(
        context: Context,
        folderPath: String,
        monochrome: Boolean = true,
        fallbackPath: String? = null
    ): List<Bitmap> {
        val cacheKey = "$folderPath-$monochrome"

        // 캐시에서 먼저 확인
        frameCache[cacheKey]?.let {
            android.util.Log.i("FrameLoader", "✅ Cache hit: $folderPath (${it.size} frames)")
            return it
        }

        android.util.Log.i("FrameLoader", "📂 Loading from: $folderPath")
        val frames = loadFramesFromPath(context, folderPath, monochrome)
        android.util.Log.i("FrameLoader", "   → Found ${frames.size} frames")

        // 프레임이 없으면 폴백 경로 시도
        if (frames.isEmpty() && fallbackPath != null) {
            android.util.Log.i("FrameLoader", "⚠️ No frames, trying fallback: $fallbackPath")
            val fallbackFrames = loadFramesFromPath(context, fallbackPath, monochrome)
            android.util.Log.i("FrameLoader", "   → Fallback found ${fallbackFrames.size} frames")
            if (fallbackFrames.isNotEmpty()) {
                frameCache[cacheKey] = fallbackFrames
                return fallbackFrames
            }
        }

        // 캐시에 저장
        if (frames.isNotEmpty()) {
            frameCache[cacheKey] = frames
        }

        return frames
    }

    private fun loadFramesFromPath(context: Context, folderPath: String, monochrome: Boolean): List<Bitmap> {
        val frames = mutableListOf<Bitmap>()

        try {
            // 폴더 내 파일 목록 가져오기
            val files = context.assets.list(folderPath.trimEnd('/')) ?: emptyArray()

            // frame_000.png, frame_001.png 또는 frame1.png 등 순서로 정렬
            val sortedFiles = files
                .filter { it.endsWith(".png") }
                .sortedBy { it }

            for (fileName in sortedFiles) {
                val fullPath = "${folderPath.trimEnd('/')}/$fileName"
                try {
                    context.assets.open(fullPath).use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            val finalBitmap = if (monochrome) applyGrayscale(bitmap) else bitmap
                            frames.add(finalBitmap)
                        }
                    }
                } catch (e: IOException) {
                    // 파일 열기 실패 - 무시
                }
            }
        } catch (e: IOException) {
            // 폴더 열기 실패 - 무시
        }

        return frames
    }

    /**
     * 그레이스케일 필터 적용
     */
    private fun applyGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayscaleBitmap
    }

    /**
     * 캐시 클리어
     */
    fun clearCache() {
        frameCache.clear()
    }
}

/**
 * V2 펫 스프라이트 애니메이션 (개별 프레임 기반)
 */
@Composable
fun PetSpriteV2(
    petType: PetTypeV2,
    stage: PetGrowthStage,
    animationType: PetAnimationTypeV2,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true
) {
    val context = LocalContext.current

    // 애니메이션 설정 가져오기
    val animConfig = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.animations[animationType] ?: AnimationConfig(4, 200)
    } else {
        petType.getAnimationConfig(animationType)
    }

    // 폴더 경로 생성
    val folderPath = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.getAnimationFolderPath(animationType)
    } else {
        petType.getAnimationFolderPath(stage, animationType)
    }

    // 폴백 경로 (idle로 폴백)
    val fallbackPath = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.getAnimationFolderPath(PetAnimationTypeV2.IDLE)
    } else {
        petType.getAnimationFolderPath(stage, PetAnimationTypeV2.IDLE)
    }

    // 프레임 로드 (없으면 idle로 폴백)
    val frames = remember(folderPath, fallbackPath, monochrome) {
        FrameLoader.loadFrames(context, folderPath, monochrome, fallbackPath)
    }

    // 현재 프레임 인덱스
    var currentFrame by remember { mutableIntStateOf(0) }

    // 프레임 애니메이션
    LaunchedEffect(animationType, frames.size) {
        if (frames.isEmpty()) return@LaunchedEffect
        currentFrame = 0
        while (true) {
            delay(animConfig.frameDurationMs.toLong())
            currentFrame = (currentFrame + 1) % frames.size
        }
    }

    // 프레임 그리기
    if (frames.isNotEmpty() && currentFrame < frames.size) {
        val bitmap = frames[currentFrame]

        Canvas(modifier = modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            // 비율 유지하면서 중앙 정렬
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val dstWidth: Float
            val dstHeight: Float

            if (bitmapRatio >= 1f) {
                dstWidth = canvasWidth
                dstHeight = canvasWidth / bitmapRatio
            } else {
                dstHeight = canvasHeight
                dstWidth = canvasHeight * bitmapRatio
            }

            val dstLeft = (canvasWidth - dstWidth) / 2
            val dstTop = (canvasHeight - dstHeight) / 2

            drawIntoCanvas { canvas ->
                val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = android.graphics.RectF(
                    dstLeft, dstTop,
                    dstLeft + dstWidth, dstTop + dstHeight
                )

                val paint = Paint().apply {
                    isFilterBitmap = false
                    isAntiAlias = false
                }

                canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            }
        }
    }
}

/**
 * V2 펫 스프라이트 with 글로우 효과
 */
@Composable
fun PetSpriteV2WithGlow(
    petType: PetTypeV2,
    stage: PetGrowthStage,
    animationType: PetAnimationTypeV2,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true,
    showGlow: Boolean = true,
    applyDisplayScale: Boolean = true  // displayScale 적용 여부
) {
    // displayScale 적용: cat baby 기준으로 모든 펫/단계 크기 정규화
    val scaledSize = if (applyDisplayScale) size * petType.getDisplayScale(stage) else size

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow layer
        if (showGlow) {
            Box(
                modifier = Modifier
                    .offset(y = 4.dp)
                    .blur(8.dp)
            ) {
                PetSpriteV2(
                    petType = petType,
                    stage = stage,
                    animationType = animationType,
                    size = scaledSize,
                    monochrome = monochrome
                )
            }
        }

        // Main sprite
        PetSpriteV2(
            petType = petType,
            stage = stage,
            animationType = animationType,
            size = scaledSize,
            monochrome = monochrome
        )
    }
}

/**
 * V2 펫 스프라이트 with 장비 시스템
 */
@Composable
fun PetSpriteV2WithEquipment(
    petType: PetTypeV2,
    stage: PetGrowthStage,
    animationType: PetAnimationTypeV2,
    equipmentState: EquipmentState,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true,
    showGlow: Boolean = true,
    applyDisplayScale: Boolean = true
) {
    val context = LocalContext.current
    val scaledSize = if (applyDisplayScale) size * petType.getDisplayScale(stage) else size

    // 장비 아이템 가져오기 (HEAD 장비 제거됨)
    val bgEquipment = DefaultEquipment.getById(equipmentState.backgroundId)

    // 스킨 시스템: colorId를 skinId로 해석
    val petSkin = DefaultSkins.getById(equipmentState.colorId)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1. BACKGROUND 레이어 (배경 효과)
        bgEquipment?.assetPath?.let { path ->
            val bgBitmap = rememberAssetBitmap(context, path)
            bgBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "background",
                    modifier = Modifier
                        .size(scaledSize * 1.5f)
                        .blur(4.dp),
                    alpha = 0.6f
                )
            }
        }

        // 2. PET SPRITE (Glow + ColorFilter)
        Box(contentAlignment = Alignment.Center) {
            // Glow layer
            if (showGlow) {
                Box(
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .blur(8.dp)
                ) {
                    PetSpriteV2Core(
                        context = context,
                        petType = petType,
                        stage = stage,
                        animationType = animationType,
                        size = scaledSize,
                        monochrome = monochrome,
                        colorMatrix = petSkin?.colorMatrix,
                        overlayColor = petSkin?.overlayColor,
                        blendMode = petSkin?.blendMode ?: BlendMode.SrcOver
                    )
                }
            }

            // Main sprite with color filter (스킨 시스템)
            PetSpriteV2Core(
                context = context,
                petType = petType,
                stage = stage,
                animationType = animationType,
                size = scaledSize,
                monochrome = monochrome,
                colorMatrix = petSkin?.colorMatrix,
                overlayColor = petSkin?.overlayColor,
                blendMode = petSkin?.blendMode ?: BlendMode.SrcOver
            )
        }
    }
}

/**
 * Asset 비트맵 로딩 헬퍼 (캐싱)
 */
@Composable
private fun rememberAssetBitmap(context: Context, path: String): Bitmap? {
    return remember(path) {
        try {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 펫 스프라이트 코어 (ColorMatrix + OverlayColor 지원)
 */
@Composable
private fun PetSpriteV2Core(
    context: Context,
    petType: PetTypeV2,
    stage: PetGrowthStage,
    animationType: PetAnimationTypeV2,
    size: Dp,
    monochrome: Boolean,
    colorMatrix: FloatArray?,
    overlayColor: Int?,
    blendMode: BlendMode
) {
    // 애니메이션 설정 가져오기
    val animConfig = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.animations[animationType] ?: AnimationConfig(4, 200)
    } else {
        petType.getAnimationConfig(animationType)
    }

    // 폴더 경로 생성
    val folderPath = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.getAnimationFolderPath(animationType)
    } else {
        petType.getAnimationFolderPath(stage, animationType)
    }

    // 폴백 경로 (idle로 폴백)
    val fallbackPath = if (stage == PetGrowthStage.EGG) {
        EggAnimationConfig.getAnimationFolderPath(PetAnimationTypeV2.IDLE)
    } else {
        petType.getAnimationFolderPath(stage, PetAnimationTypeV2.IDLE)
    }

    // 프레임 로드
    val frames = remember(folderPath, fallbackPath, monochrome) {
        FrameLoader.loadFrames(context, folderPath, monochrome, fallbackPath)
    }

    if (frames.isEmpty()) {
        return
    }

    // 애니메이션 상태
    var currentFrame by remember { mutableIntStateOf(0) }

    // 프레임 애니메이션
    LaunchedEffect(animationType, frames.size) {
        currentFrame = 0
        while (true) {
            delay(animConfig.frameDurationMs.toLong())
            currentFrame = (currentFrame + 1) % frames.size
        }
    }

    // 현재 프레임 비트맵
    val bitmap = frames.getOrNull(currentFrame) ?: frames.first()

    // ColorFilter 결정
    val colorFilter = when {
        colorMatrix != null -> {
            // ColorMatrix 방식 (틴트)
            ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(colorMatrix))
        }
        overlayColor != null -> {
            // 단색 오버레이 방식
            ColorFilter.tint(androidx.compose.ui.graphics.Color(overlayColor), blendMode)
        }
        else -> null
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "pet",
        modifier = Modifier.size(size),
        colorFilter = colorFilter
    )
}

/**
 * PetState 기반 자동 애니메이션
 */
@Composable
fun PetSpriteFromState(
    petState: PetState,
    isWalking: Boolean,
    progressPercent: Int,
    modifier: Modifier = Modifier,
    baseSizeDp: Int = 96,
    monochrome: Boolean = true,
    isNightMode: Boolean = false,
    applyDisplayScale: Boolean = true  // displayScale 적용 여부
) {
    val animationType = petState.getCurrentAnimationType(isWalking, progressPercent, isNightMode)
    val sizeDp = petState.getSizeDp(baseSizeDp).dp

    PetSpriteV2WithGlow(
        petType = petState.petType,
        stage = petState.stage,
        animationType = animationType,
        size = sizeDp,
        monochrome = monochrome,
        modifier = modifier,
        applyDisplayScale = applyDisplayScale
    )
}

/**
 * PetState 기반 자동 애니메이션 with 장비 시스템
 */
@Composable
fun PetSpriteFromStateWithEquipment(
    petState: PetState,
    equipmentState: EquipmentState,
    isWalking: Boolean,
    progressPercent: Int,
    modifier: Modifier = Modifier,
    baseSizeDp: Int = 96,
    monochrome: Boolean = true,
    isNightMode: Boolean = false,
    applyDisplayScale: Boolean = true
) {
    val animationType = petState.getCurrentAnimationType(isWalking, progressPercent, isNightMode)
    val sizeDp = petState.getSizeDp(baseSizeDp).dp

    PetSpriteV2WithEquipment(
        petType = petState.petType,
        stage = petState.stage,
        animationType = animationType,
        equipmentState = equipmentState,
        size = sizeDp,
        monochrome = monochrome,
        modifier = modifier,
        applyDisplayScale = applyDisplayScale
    )
}

/**
 * Egg 전용 스프라이트
 */
@Composable
fun EggSprite(
    animationType: PetAnimationTypeV2 = PetAnimationTypeV2.IDLE,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    monochrome: Boolean = true
) {
    val context = LocalContext.current

    val animConfig = EggAnimationConfig.animations[animationType] ?: AnimationConfig(4, 200)
    val folderPath = EggAnimationConfig.getAnimationFolderPath(animationType)
    val fallbackPath = EggAnimationConfig.getAnimationFolderPath(PetAnimationTypeV2.IDLE)

    val frames = remember(folderPath, monochrome) {
        FrameLoader.loadFrames(context, folderPath, monochrome, fallbackPath)
    }

    var currentFrame by remember { mutableIntStateOf(0) }

    LaunchedEffect(animationType, frames.size) {
        if (frames.isEmpty()) return@LaunchedEffect
        currentFrame = 0
        while (true) {
            delay(animConfig.frameDurationMs.toLong())
            currentFrame = (currentFrame + 1) % frames.size
        }
    }

    if (frames.isNotEmpty() && currentFrame < frames.size) {
        val bitmap = frames[currentFrame]

        Canvas(modifier = modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val dstWidth: Float
            val dstHeight: Float

            if (bitmapRatio >= 1f) {
                dstWidth = canvasWidth
                dstHeight = canvasWidth / bitmapRatio
            } else {
                dstHeight = canvasHeight
                dstWidth = canvasHeight * bitmapRatio
            }

            val dstLeft = (canvasWidth - dstWidth) / 2
            val dstTop = (canvasHeight - dstHeight) / 2

            drawIntoCanvas { canvas ->
                val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = android.graphics.RectF(
                    dstLeft, dstTop,
                    dstLeft + dstWidth, dstTop + dstHeight
                )

                val paint = Paint().apply {
                    isFilterBitmap = false
                    isAntiAlias = false
                }

                canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            }
        }
    }
}
