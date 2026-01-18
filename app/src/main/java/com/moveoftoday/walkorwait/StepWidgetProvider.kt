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
import com.moveoftoday.walkorwait.pet.PetType
import java.text.NumberFormat
import java.util.Locale

class StepWidgetProvider : AppWidgetProvider() {

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

            // 비교용 값 (항상 걸음 수 기준)
            val currentProgress = prefs.getCurrentProgress()
            val goal = prefs.getGoal()
            val percent = if (goal > 0) ((currentProgress / goal) * 100).toInt().coerceAtMost(100) else 0

            // 표시용 값 (단위에 맞게 변환)
            val displayValue = prefs.getCurrentProgressForDisplay()
            val displayGoal = prefs.getGoalForDisplay()

            // 연속 일수
            val streakCount = prefs.getStreak()

            // 펫 정보
            val petTypeName = prefs.getPetType()
            val petType = petTypeName?.let {
                try { PetType.valueOf(it) } catch (e: Exception) { null }
            }
            val personality = petType?.personality

            val views = RemoteViews(context.packageName, R.layout.widget_step_counter)

            // 연속 배지
            views.setTextViewText(R.id.widget_streak, "$streakCount 연속")

            // 펫 아이콘 (grayscale)
            if (petType != null) {
                val petBitmap = loadPetFirstFrame(context, petType)
                if (petBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
                }
            }

            // 펫 대사 (말풍선)
            val dialogue = if (personality != null) {
                getDialogueByProgress(personality, percent)
            } else {
                "산책하자!"
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

            // 위젯 전체 클릭 시 앱 열기
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
         * 달성률에 따른 펫 대사 가져오기
         */
        private fun getDialogueByProgress(
            personality: com.moveoftoday.walkorwait.pet.PetPersonality,
            percent: Int
        ): String {
            return when (percent) {
                0 -> PetDialogues.getIdleMessage(personality)
                in 1..9 -> PetDialogues.getJustStartedMessage(personality)
                in 10..24 -> PetDialogues.getStartedMessage(personality)
                in 25..49 -> PetDialogues.getQuarterMessage(personality)
                in 50..74 -> PetDialogues.getHalfwayMessage(personality)
                in 75..89 -> PetDialogues.getThreeQuarterMessage(personality)
                in 90..99 -> PetDialogues.getAlmostThereMessage(personality)
                else -> PetDialogues.getGoalAchievedMessage(personality)
            }
        }

        /**
         * 펫 스프라이트 첫 프레임 추출 (grayscale)
         */
        private fun loadPetFirstFrame(context: Context, petType: PetType): Bitmap? {
            return try {
                val assetPath = petType.idleAssetPath
                context.assets.open(assetPath).use { inputStream ->
                    val spriteSheet = BitmapFactory.decodeStream(inputStream)
                    if (spriteSheet != null) {
                        val frameCount = petType.idleFrames
                        val frameWidth = spriteSheet.width / frameCount
                        val frameHeight = spriteSheet.height
                        val frame = Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, frameHeight)
                        toGrayscale(frame)
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

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, StepWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}