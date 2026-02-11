package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetDialogues
import com.moveoftoday.walkorwait.pet.PetDialoguesV2
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PetTypeV2
import com.moveoftoday.walkorwait.pet.PetGrowthStage
import java.text.NumberFormat
import java.util.Locale

/**
 * 2x2 펫 위젯 - 기획서 디자인 기반
 * - 연속 스트릭 배지
 * - 펫 스프라이트 (모노크롬)
 * - 말풍선
 * - 걸음수/km + 프로그레스 바
 */
class PetWidget2x2Provider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferenceManager(context)
            val goalUnit = prefs.getGoalUnit()
            val isKmMode = goalUnit == "km"

            // 걸음수 데이터
            val currentProgress = prefs.getCurrentProgress()
            val goal = prefs.getGoal()
            val percent = if (goal > 0) ((currentProgress / goal) * 100).toInt().coerceAtMost(100) else 0

            // 표시용 값
            val displayValue = prefs.getCurrentProgressForDisplay()
            val displayGoal = prefs.getGoalForDisplay()

            // 연속 일수
            val streakCount = prefs.getStreak()

            // V2 펫 정보 우선, 없으면 V1
            val petTypeV2 = prefs.getPetTypeV2()
            val petName = prefs.getPetNameV2()?.takeIf { it.isNotBlank() }
                ?: prefs.getPetName() ?: "펫"

            val views = RemoteViews(context.packageName, R.layout.widget_pet_2x2)

            // 연속 배지
            views.setTextViewText(R.id.widget_streak, "$streakCount 연속")

            // 펫 이름
            views.setTextViewText(R.id.widget_pet_name, petName)

            // 진행률에 따른 감정 심볼
            val emotionSymbol = getEmotionByProgress(percent)
            views.setTextViewText(R.id.widget_emotion, emotionSymbol)

            // 펫 아이콘 (V2 우선, 없으면 V1)
            val petBitmap = if (petTypeV2 != null) {
                loadPetV2FirstFrame(context, petTypeV2, prefs)
            } else {
                val petTypeName = prefs.getPetType()
                val petType = petTypeName?.let {
                    try { PetType.valueOf(it) } catch (e: Exception) { null }
                }
                if (petType != null) loadPetFirstFrame(context, petType) else null
            }

            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
            }

            // 펫 대사
            val dialogue = if (petTypeV2 != null) {
                getDialogueByProgressV2(petTypeV2.personality, percent)
            } else {
                val petTypeName = prefs.getPetType()
                val personality = petTypeName?.let {
                    try { PetType.valueOf(it).personality } catch (e: Exception) { null }
                }
                if (personality != null) {
                    getDialogueByProgress(personality, percent)
                } else {
                    "산책하자!"
                }
            }
            views.setTextViewText(R.id.widget_speech, dialogue)

            // 걸음/km 텍스트
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            val stepsText = if (isKmMode) {
                "${String.format("%.2f", displayValue)} / ${String.format("%.2f", displayGoal)} km"
            } else {
                "${numberFormat.format(displayValue.toInt())} / ${numberFormat.format(displayGoal.toInt())} 보"
            }
            views.setTextViewText(R.id.widget_steps_text, stepsText)

            // 퍼센트 텍스트
            views.setTextViewText(R.id.widget_percent, "${percent}%")

            // 프로그래스 바
            views.setProgressBar(R.id.widget_progress_bar, 100, percent, false)

            // 위젯 클릭 시 앱 열기
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * 진행률에 따른 감정 심볼 (ASCII만 사용)
         */
        private fun getEmotionByProgress(percent: Int): String {
            return when (percent) {
                0 -> "zzZ"
                in 1..24 -> "~"
                in 25..49 -> "!"
                in 50..74 -> "!!"
                in 75..89 -> "^o^"
                in 90..99 -> "!!!"
                else -> "***"  // 100%
            }
        }

        /**
         * V1 펫 달성률에 따른 대사
         */
        private fun getDialogueByProgress(
            personality: com.moveoftoday.walkorwait.pet.PetPersonality,
            percent: Int
        ): String {
            return when (percent) {
                0 -> PetDialogues.getIdleMessage(personality)
                in 1..24 -> PetDialogues.getStartedMessage(personality)
                in 25..49 -> PetDialogues.getQuarterMessage(personality)
                in 50..74 -> PetDialogues.getHalfwayMessage(personality)
                in 75..89 -> PetDialogues.getThreeQuarterMessage(personality)
                in 90..99 -> PetDialogues.getAlmostThereMessage(personality)
                else -> PetDialogues.getGoalAchievedMessage(personality)
            }
        }

        /**
         * V2 펫 달성률에 따른 대사
         */
        private fun getDialogueByProgressV2(
            personality: com.moveoftoday.walkorwait.pet.PetPersonalityV2,
            percent: Int
        ): String {
            return when (percent) {
                0 -> PetDialoguesV2.getIdleMessage(personality)
                100 -> PetDialoguesV2.getGoalAchievedMessage(personality)
                else -> PetDialoguesV2.getWalkingMessage(personality, percent)
            }
        }

        /**
         * V2 펫 첫 프레임 로드 (진행률 기반 애니메이션 + 장비)
         */
        private fun loadPetV2FirstFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): Bitmap? {
            val stage = prefs.getEffectiveDisplayStage()  // 오버라이드 반영

            // 스킨 가져오기
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            // 진행률에 따른 애니메이션 타입 결정
            val currentProgress = prefs.getCurrentProgress()
            val goal = prefs.getGoal()
            val percent = if (goal > 0) ((currentProgress / goal) * 100).toInt().coerceAtMost(150) else 0
            val animationType = when (percent) {
                0 -> "idle"
                in 1..49 -> "walk"
                in 50..99 -> "run"
                else -> "bark"  // 100%+
            }

            return try {
                val assetPath = "pets/${petType.folderName}/${stage.folderName}/$animationType/frame_000.png"
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                        var result = toGrayscale(bitmap)
                        // 스킨 적용
                        petSkin?.colorMatrix?.let { matrix ->
                            result = applyColorMatrix(result, matrix)
                        }
                        result
                    }
                }
            } catch (e: Exception) {
                // 애니메이션 폴더가 없으면 idle로 폴백
                try {
                    val fallbackPath = "pets/${petType.folderName}/${stage.folderName}/idle/frame_000.png"
                    context.assets.open(fallbackPath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                            var result = toGrayscale(bitmap)
                            // 스킨 적용
                            petSkin?.colorMatrix?.let { matrix ->
                                result = applyColorMatrix(result, matrix)
                            }
                            result
                        }
                    }
                } catch (e2: Exception) {
                    null
                }
            }
        }

        /**
         * V1 펫 스프라이트 첫 프레임 (grayscale + 스킨)
         */
        private fun loadPetFirstFrame(context: Context, petType: PetType): Bitmap? {
            // 스킨 가져오기
            val prefs = PreferenceManager(context)
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            return try {
                val assetPath = petType.idleAssetPath
                context.assets.open(assetPath).use { inputStream ->
                    val spriteSheet = BitmapFactory.decodeStream(inputStream)
                    if (spriteSheet != null) {
                        val frameCount = petType.idleFrames
                        val frameWidth = spriteSheet.width / frameCount
                        val frameHeight = spriteSheet.height
                        val frame = Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, frameHeight)
                        var result = toGrayscale(frame)
                        // 스킨 적용
                        petSkin?.colorMatrix?.let { matrix ->
                            result = applyColorMatrix(result, matrix)
                        }
                        result
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 비트맵을 grayscale로 변환
         */
        private fun toGrayscale(original: Bitmap): Bitmap {
            val grayscale = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(grayscale)
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return grayscale
        }

        /**
         * ColorMatrix 적용 (장비 시스템)
         */
        private fun applyColorMatrix(original: Bitmap, matrix: FloatArray): Bitmap {
            val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()
            val colorMatrix = ColorMatrix(matrix)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return result
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, PetWidget2x2Provider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
